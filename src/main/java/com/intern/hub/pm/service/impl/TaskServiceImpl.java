package com.intern.hub.pm.service.impl;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.library.common.exception.InternalErrorException;
import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.dto.task.*;
import com.intern.hub.pm.feign.HrmInternalFeignClient;
import com.intern.hub.pm.feign.WalletInternalFeignClient;
import com.intern.hub.pm.feign.model.*;
import com.intern.hub.pm.model.constant.Status;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.document.DocumentScope;
import com.intern.hub.pm.model.document.DocumentType;
import com.intern.hub.pm.model.team.Task;
import com.intern.hub.pm.model.team.Team;
import com.intern.hub.pm.repository.TaskRepository;
import com.intern.hub.pm.repository.TeamMemberRepository;
import com.intern.hub.pm.repository.TeamRepository;
import com.intern.hub.pm.service.DocumentService;
import com.intern.hub.pm.service.TaskService;
import com.intern.hub.pm.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.intern.hub.library.common.exception.BadRequestException;
import com.intern.hub.library.common.exception.ForbiddenException;
import com.intern.hub.library.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final DocumentService documentService;
    private final HrmInternalFeignClient hrmInternalFeignClient;
    private final WalletInternalFeignClient walletInternalFeignClient;

    @Override
    @Transactional
    public TaskResponse createTask(Long projectTeamId, TaskUpsertRequest request, List<MultipartFile> files) {
        Team team = getActiveTeam(projectTeamId);
        Long currentUserId = UserContext.requiredUserId();

        // Kiểm tra ngân sách của Team con (Team -> Task)
        validateTeamBudgetLimit(team, null, request.rewardToken());

        WalletTokenTaskRequest checkTokenRequest = WalletTokenTaskRequest.builder()
                .rt(request.rewardToken())
                .build();
        walletInternalFeignClient.checkAndLockTask(currentUserId, checkTokenRequest);

        Task task = Task.builder()
                .team(team)
                .taskUUID(randomNumberUUI())
                .name(request.name().trim())
                .description(request.description() != null ? request.description().trim() : null)
                .rewardToken(request.rewardToken())
                .creatorId(currentUserId)
                .assigneeId(request.assigneeId())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(StatusWork.NOT_STARTED)
                .build();

        Task savedTask = taskRepository.save(task);
        documentService.replaceDocuments(
                savedTask.getId(),
                DocumentScope.TASK,
                DocumentType.CHARTER,
                currentUserId,
                "pm/tasks/" + savedTask.getId() + "/charter",
                files);
        return toResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedData<TaskResponse> getProjectTeamTasks(
            Long teamId, TaskFilterRequest filter,
            int page, int size) {
        return getTasks(buildTaskSpecification(teamId, null, filter), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedData<TaskResponse> getMyTasks(Long teamId, TaskFilterRequest filter, int page, int size) {
        return getTasks(buildTaskSpecification(teamId, UserContext.requiredUserId(), filter), page, size);
    }

    private Map<Long, String> fetchUserNames(List<Long> userIds) {
        Map<Long, String> userNameMap = new java.util.HashMap<>();
        if (userIds == null || userIds.isEmpty())
            return userNameMap;
        try {
            var response = hrmInternalFeignClient.getUsersByIdsInternal(userIds);
            if (response != null && response.data() != null) {
                response.data().forEach(u -> userNameMap.put(Long.valueOf(u.userId()), u.fullName()));
            }
        } catch (Exception e) {
            // Log error if needed, fallback to IDs managed by getOrDefault
        }
        return userNameMap;
    }

    @Override
    @Transactional(readOnly = true)
    public TaskStatisticsResponse getTaskStatistics(Long teamId) {
        long totalTasks = taskRepository.countByTeamIdAndStatusNot(teamId, StatusWork.CANCELED);
        long notStartedTasks = taskRepository.countByTeamIdAndStatus(teamId, StatusWork.NOT_STARTED);
        long inProgressTasks = taskRepository.countByTeamIdAndStatus(teamId, StatusWork.IN_PROGRESS);
        long pendingReviewTasks = taskRepository.countByTeamIdAndStatus(teamId, StatusWork.PENDING_REVIEW);
        long completedTasks = taskRepository.countByTeamIdAndStatus(teamId, StatusWork.COMPLETED);
        long overdueTasks = taskRepository.countByTeamIdAndStatus(teamId, StatusWork.OVERDUE);
        long needsRevisionTasks = taskRepository.countByTeamIdAndStatus(teamId, StatusWork.NEEDS_REVISION);

        return TaskStatisticsResponse.builder()
                .totalTasks(totalTasks)
                .notStartedTasks(notStartedTasks)
                .inProgressTasks(inProgressTasks)
                .pendingReviewTasks(pendingReviewTasks)
                .completedTasks(completedTasks)
                .overdueTasks(overdueTasks)
                .needsRevisionTasks(needsRevisionTasks)
                .build();
    }

    @Override
    public TaskResponse acceptTask(Long taskId) {
        Task task = getActiveTask(taskId);
        Long currentUserId = UserContext.requiredUserId();

        if (currentUserId.equals(task.getCreatorId())) {
            throw new ForbiddenException("Người giao việc không thể tự nhận task");
        }

        if (task.getAssigneeId() == null) {
            // Kiểm tra user có thuộc team không
            boolean isMember = teamMemberRepository.existsByTeamIdAndUserIdAndStatus(
                    task.getTeam().getId(), currentUserId, Status.ACTIVE);
            if (!isMember) {
                throw new ForbiddenException("Bạn phải là thành viên của Team mới có thể nhận nhiệm vụ này");
            }
            task.setAssigneeId(currentUserId);
        } else if (!currentUserId.equals(task.getAssigneeId())) {
            throw new ForbiddenException("Chỉ người được giao task mới có thể nhận task");
        }

        task.setStatus(StatusWork.IN_PROGRESS);
        Task savedTask = taskRepository.save(task);

        WalletTransactionTaskRequest txRequest = WalletTransactionTaskRequest.builder()
                .taskId(savedTask.getId())
                .taskUUId(savedTask.getTaskUUID())
                .moduleUUId(savedTask.getTeam().getTeamUUID())
                .creatorId(savedTask.getCreatorId())
                .assigneeId(savedTask.getAssigneeId())
                .rt(savedTask.getRewardToken())
                .build();
        walletInternalFeignClient.saveTransactionTask(txRequest);

        return toResponse(savedTask);
    }

    @Override
    public TaskResponse refuseTask(Long taskId) {
        Task task = getActiveTask(taskId);
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(task.getAssigneeId())) {
            throw new ForbiddenException("Chỉ người được giao task mới có thể từ chối task");
        }
        if (task.getStatus() != StatusWork.NOT_STARTED) {
            throw new IllegalArgumentException("Chỉ có thể từ chối task khi ở trạng thái Chưa bắt đầu");
        }
        task.setStatus(StatusWork.REJECTED);
        Task savedTask = taskRepository.save(task);

        return toResponse(savedTask);
    }

    @Override
    public TaskResponse quitTask(Long taskId) {
        Task task = getActiveTask(taskId);
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(task.getAssigneeId())) {
            throw new ForbiddenException("Chỉ người được giao mới có thể dùng chức năng không làm nữa");
        }
        if (task.getStatus() != StatusWork.IN_PROGRESS && task.getStatus() != StatusWork.NEEDS_REVISION) {
            throw new BadRequestException(
                    "Chỉ có thể dùng chức năng không làm nữa khi đang thực hiện hoặc cần sửa lại");
        }
        task.setStatus(StatusWork.QUIT);
        Task savedTask = taskRepository.save(task);
        return toResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId) {
        return toResponse(getActiveTask(taskId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedData<TaskResponse> getPendingTeamTasks(Long teamId, TaskFilterRequest filter, int page, int size) {
        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("team").get("id"), teamId));
            predicates.add(cb.isNull(root.get("assigneeId")));
            predicates.add(cb.notEqual(root.get("status"), StatusWork.CANCELED));

            if (filter != null) {
                if (filter.name() != null && !filter.name().trim().isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.name().toLowerCase().trim() + "%"));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return getTasks(spec, page, size);
    }

    @Override
    @Transactional
    public TaskResponse claimTask(Long taskId) {
        Task task = getActiveTask(taskId);
        Long currentUserId = UserContext.requiredUserId();

        if (task.getAssigneeId() != null) {
            throw new BadRequestException("Nhiệm vụ này đã có người nhận");
        }

        // Kiểm tra user có thuộc team không
        boolean isMember = teamMemberRepository.existsByTeamIdAndUserIdAndStatus(
                task.getTeam().getId(), currentUserId, Status.ACTIVE);
        if (!isMember) {
            throw new ForbiddenException("Bạn phải là thành viên của Team mới có thể nhận nhiệm vụ này");
        }

        task.setAssigneeId(currentUserId);
        task.setStatus(StatusWork.IN_PROGRESS);
        Task savedTask = taskRepository.save(task);

        // Lưu giao dịch vào Wallet
        WalletTransactionTaskRequest txRequest = WalletTransactionTaskRequest.builder()
                .taskId(savedTask.getId())
                .taskUUId(savedTask.getTaskUUID())
                .moduleUUId(savedTask.getTeam().getTeamUUID())
                .creatorId(savedTask.getCreatorId())
                .assigneeId(savedTask.getAssigneeId())
                .rt(savedTask.getRewardToken())
                .build();
        walletInternalFeignClient.saveTransactionTask(txRequest);

        return toResponse(savedTask);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, TaskUpsertRequest request, List<MultipartFile> files) {
        Task task = getActiveTask(taskId);
        assertTaskOwner(task);

        // Kiểm tra ngân sách của Team con khi cập nhật
        validateTeamBudgetLimit(task.getTeam(), task.getId(), request.rewardToken());

        // --- Token Recalculation Unified Flow ---
        WalletEditTaskRequest editTokenRequest = WalletEditTaskRequest.builder()
                .oldRt(task.getRewardToken())
                .newRt(request.rewardToken())
                .build();
        walletInternalFeignClient.editTaskTokens(UserContext.requiredUserId(), editTokenRequest);

        task.setName(request.name().trim());
        task.setDescription(request.description().trim());
        task.setRewardToken(request.rewardToken());
        task.setAssigneeId(request.assigneeId());

        if (request.status() != null) {
            task.setStatus(StatusWork.valueOf(request.status()));
        }

        Task savedTask = taskRepository.save(task);
        documentService.replaceDocuments(
                savedTask.getId(),
                DocumentScope.TASK,
                DocumentType.CHARTER,
                UserContext.requiredUserId(),
                "pm/tasks/" + savedTask.getId() + "/charter",
                files);
        return toResponse(savedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = getActiveTask(taskId);
        assertTaskOwner(task);
        if (task.getStatus() != StatusWork.NOT_STARTED && task.getStatus() != StatusWork.REJECTED
                && task.getStatus() != StatusWork.QUIT) {
            throw new BadRequestException(
                    "Chỉ có thể thu hồi/hủy task khi chưa bắt đầu, bị từ chối hoặc người làm không làm nữa");
        }
        task.setStatus(StatusWork.CANCELED);
        taskRepository.save(task);
        // Gọi sang Wallet để giải phóng token đã khóa cho Task
        WalletEditTaskRequest releaseRequest = WalletEditTaskRequest.builder()
                .oldRt(task.getRewardToken())
                .newRt(BigInteger.ZERO)
                .build();
        walletInternalFeignClient.recalculateTokensOfTask(task.getCreatorId(), releaseRequest);

    }

    @Override
    @Transactional
    public TaskResponse submitTask(Long taskId, String deliverableDescription, String deliverableLink,
            List<MultipartFile> files) {
        Task task = getActiveTask(taskId);
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(task.getAssigneeId())) {
            throw new ForbiddenException("Chỉ người được giao task mới có thể nộp bài");
        }

        documentService.replaceDocuments(
                task.getId(),
                DocumentScope.TASK,
                DocumentType.DELIVERABLE,
                currentUserId,
                "pm/tasks/" + task.getId() + "/submissions",
                files);
        task.setDeliverableDescription(trimToNull(deliverableDescription));
        task.setDeliverableLink(trimToNull(deliverableLink));
        task.setStatus(StatusWork.PENDING_REVIEW);
        return toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse approveTask(Long taskId) {
        Task task = getPendingReviewTask(taskId);
        assertTaskOwner(task);
        task.setStatus(StatusWork.COMPLETED);
        Task savedTask = taskRepository.save(task);

        // Gọi sang Wallet để thực hiện release token (Duyệt cho Task)
        WalletBrowseWorkRequest browseRequest = WalletBrowseWorkRequest.builder()
                .entityId(savedTask.getId())
                .userId(savedTask.getCreatorId())
                .workUUId(savedTask.getTaskUUID())
                .type("task")
                .note(savedTask.getNote())
                .rt(savedTask.getRewardToken())
                .build();
        walletInternalFeignClient.browseWork(browseRequest);

        return toResponse(savedTask);
    }

    @Override
    @Transactional
    public TaskResponse refuseTask(Long taskId, TaskReviewRequest request) {
        Task task = getPendingReviewTask(taskId);
        assertTaskOwner(task);
        task.setStatus(StatusWork.NEEDS_REVISION);
        task.setNote(trimToNull(request.reviewComment()));
        return toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse partialApproveTask(Long taskId, TaskPartialApproveRequest request) {
        Task task = getPendingReviewTask(taskId);
        assertTaskOwner(task);

        validateTeamBudgetLimit(task.getTeam(), task.getId(), request.newRt());

        if (request.newRt().compareTo(task.getRewardToken()) >= 0) {
            throw new BadRequestException("Điểm thưởng mới phải nhỏ hơn điểm thưởng hiện tại");
        }
        // Cập nhật Wallet (Blockchain) cho người tạo task
        WalletEditTaskRequest editTokenRequest = WalletEditTaskRequest.builder()
                .oldRt(task.getRewardToken())
                .newRt(request.newRt())
                .build();
        walletInternalFeignClient.editTaskTokens(task.getCreatorId(), editTokenRequest);

        task.setRewardToken(request.newRt());
        task.setNote(trimToNull(request.reason()));
        task.setStatus(StatusWork.IN_PROGRESS);

        return toResponse(taskRepository.save(task));
    }

    private PaginatedData<TaskResponse> getTasks(Specification<Task> specification, int page, int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page,
                size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt"));
        Page<Task> taskPage = taskRepository.findAll(specification, pageable);
        List<Task> tasks = taskPage.getContent();

        List<Long> userIds = tasks.stream()
                .flatMap(t -> java.util.stream.Stream.of(t.getCreatorId(), t.getAssigneeId()))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> userNameMap = fetchUserNames(userIds);

        List<TaskResponse> items = tasks.stream()
                .map(t -> {
                    String creatorName = userNameMap.getOrDefault(t.getCreatorId(),
                            "User (ID: " + t.getCreatorId() + ")");
                    String assigneeName = t.getAssigneeId() != null
                            ? userNameMap.getOrDefault(t.getAssigneeId(), "User (ID: " + t.getAssigneeId() + ")")
                            : "Chưa có người nhận";
                    return toResponseWithNames(t, creatorName, assigneeName);
                })
                .toList();

        return PaginatedData.<TaskResponse>builder()
                .items(items)
                .totalItems(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .build();
    }

    private Specification<Task> buildTaskSpecification(Long teamId, Long assigneeId, TaskFilterRequest filter) {
        TaskFilterRequest safeFilter = filter != null ? filter : TaskFilterRequest.builder().build();
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (teamId != null) {
                predicates.add(cb.equal(root.get("team").get("id"), teamId));
            }
            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assigneeId"), assigneeId));
            }

            if (safeFilter.status() != null) {
                predicates.add(cb.equal(root.get("status"), safeFilter.status()));
            } else {
                predicates.add(cb.notEqual(root.get("status"), StatusWork.CANCELED));
            }

            if (safeFilter.name() != null && !safeFilter.name().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + safeFilter.name().toLowerCase().trim() + "%"));
            }

            if (safeFilter.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), safeFilter.startDate()));
            }
            if (safeFilter.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), safeFilter.endDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void validateTeamBudgetLimit(Team team, Long currentTaskId, BigInteger newRt) {
        BigInteger currentSumRt = taskRepository.sumRewardTokensByTeamId(team.getId(), currentTaskId);
        if (currentSumRt == null)
            currentSumRt = BigInteger.ZERO;

        BigInteger totalRt = currentSumRt.add(newRt);

        if (totalRt.compareTo(team.getBudgetToken()) > 0) {
            throw new BadRequestException(
                    "Vượt quá ngân sách của team!\n\n" +
                            "- Tổng chi phí hiện tại: " + totalRt + "\n" +
                            "- Ngân sách team: " + team.getBudgetToken() + "\n" +
                            "- Thâm hụt: " + totalRt.subtract(team.getBudgetToken()));
        }
    }

    private Team getActiveTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Team"));
        if (team.getStatus() == StatusWork.CANCELED) {
            throw new NotFoundException("Không tìm thấy Team");
        }
        return team;
    }

    private Task getActiveTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy task"));
        if (task.getStatus() == StatusWork.CANCELED) {
            throw new NotFoundException("Task not found");
        }
        return task;
    }

    private Task getPendingReviewTask(Long taskId) {
        Task task = getActiveTask(taskId);
        if (task.getStatus() != StatusWork.PENDING_REVIEW) {
            throw new BadRequestException("Nhiệm vụ phải đang chờ duyệt");
        }
        return task;
    }

    private void assertTaskOwner(Task task) {
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(task.getCreatorId())) {
            throw new ForbiddenException("Bạn không có quyền ở task này!");
        }
    }

    private TaskResponse toResponse(Task task) {
        String creatorName = "User (ID: " + task.getCreatorId() + ")";
        String assigneeName = task.getAssigneeId() != null
                ? "User (ID: " + task.getAssigneeId() + ")"
                : "Chưa có người nhận";

        try {
            if (task.getCreatorId() != null) {
                var res = hrmInternalFeignClient.getUserByIdInternal(task.getCreatorId());
                if (res != null && res.data() != null)
                    creatorName = res.data().fullName();
            }
            if (task.getAssigneeId() != null) {
                var res = hrmInternalFeignClient.getUserByIdInternal(task.getAssigneeId());
                if (res != null && res.data() != null)
                    assigneeName = res.data().fullName();
            }
        } catch (Exception e) {
            throw new InternalErrorException(e.getMessage());
        }

        return toResponseWithNames(task, creatorName, assigneeName);
    }

    private TaskResponse toResponseWithNames(Task task, String creatorName, String assigneeName) {
        List<DocumentResponse> charterDocuments = documentService.getDocuments(
                task.getId(), DocumentScope.TASK, DocumentType.CHARTER);
        List<DocumentResponse> submissionDocuments = documentService.getDocuments(
                task.getId(), DocumentScope.TASK, DocumentType.DELIVERABLE);

        return new TaskResponse(
                String.valueOf(task.getId()),
                task.getTeam() != null ? String.valueOf(task.getTeam().getId()) : null,
                task.getTaskUUID(),
                task.getName(),
                task.getDescription(),
                task.getNote(),
                task.getStatus(),
                task.getRewardToken(),
                task.getCreatorId() != null ? String.valueOf(task.getCreatorId()) : null,
                task.getAssigneeId() != null ? String.valueOf(task.getAssigneeId()) : null,
                creatorName,
                assigneeName,
                charterDocuments,
                task.getDeliverableDescription(),
                task.getDeliverableLink(),
                task.getStartDate(),
                task.getEndDate(),
                submissionDocuments,
                task.getCreatedAt(),
                task.getUpdatedAt());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String randomNumberUUI() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        String randomStr = String.format("%06d", randomPart).trim();
        return datePart + randomStr;
    }
}
