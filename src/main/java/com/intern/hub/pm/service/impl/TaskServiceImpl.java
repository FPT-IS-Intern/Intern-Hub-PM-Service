package com.intern.hub.pm.service.impl;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.dto.task.TaskResponse;
import com.intern.hub.pm.dto.task.TaskReviewRequest;
import com.intern.hub.pm.dto.task.TaskUpsertRequest;
import com.intern.hub.pm.dto.task.TaskStatisticsResponse;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.document.DocumentScope;
import com.intern.hub.pm.model.document.DocumentType;
import com.intern.hub.pm.model.project.Project;
import com.intern.hub.pm.model.team.Task;
import com.intern.hub.pm.model.team.Team;
import com.intern.hub.pm.repository.ProjectRepository;
import com.intern.hub.pm.repository.TaskRepository;
import com.intern.hub.pm.repository.TeamRepository;
import com.intern.hub.pm.service.DocumentService;
import com.intern.hub.pm.service.TaskService;
import com.intern.hub.pm.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final DocumentService documentService;

    @Override
    @Transactional
    public TaskResponse createTask(Long projectTeamId, TaskUpsertRequest request, List<MultipartFile> files) {
        Team team = getActiveTeam(projectTeamId);
        Long currentUserId = UserContext.requiredUserId();

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
                files
        );
        return toResponse(savedTask);
    }


    @Override
    @Transactional(readOnly = true)
    public PaginatedData<TaskResponse> getProjectTeamTasks(
            Long teamId, String name, StatusWork status,
            LocalDateTime startDate, LocalDateTime endDate,
            int page, int size) {
        
        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Lọc theo teamId (giả định relationship team.id)
            predicates.add(cb.equal(root.get("team").get("id"), teamId));
            
            // Loại bỏ status CANCELED mặc định, hoặc lọc theo status cụ thể
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            } else {
                predicates.add(cb.notEqual(root.get("status"), StatusWork.CANCELED));
            }
            
            // Lọc theo tên (không phân biệt hoa thường)
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase().trim() + "%"));
            }
            
            // Lọc theo khoảng thời gian
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), endDate));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        List<TaskResponse> items = taskPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PaginatedData.<TaskResponse>builder()
                .items(items)
                .totalItems(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .build();
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
    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId) {
        return toResponse(getActiveTask(taskId));
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, TaskUpsertRequest request, List<MultipartFile> files) {
        Task task = getActiveTask(taskId);
        assertTaskOwner(task);

        task.setName(request.name().trim());
        task.setDescription(request.description().trim());
        task.setRewardToken(request.rewardToken());
        task.setAssigneeId(request.assigneeId());

        Task savedTask = taskRepository.save(task);
        documentService.replaceDocuments(
                savedTask.getId(),
                DocumentScope.TASK,
                DocumentType.CHARTER,
                UserContext.requiredUserId(),
                "pm/tasks/" + savedTask.getId() + "/charter",
                files
        );
        return toResponse(savedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = getActiveTask(taskId);
        assertTaskOwner(task);
        task.setStatus(StatusWork.CANCELED);
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public TaskResponse submitTask(Long taskId, String deliverableLink, List<MultipartFile> files) {
        Task task = getActiveTask(taskId);
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(task.getAssigneeId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the assigned user can submit this task");
        }

        documentService.replaceDocuments(
                task.getId(),
                DocumentScope.TASK,
                DocumentType.DELIVERABLE,
                currentUserId,
                "pm/tasks/" + task.getId() + "/submissions",
                files
        );
        task.setDeliverableLink(trimToNull(deliverableLink));
        task.setStatus(StatusWork.PENDING_REVIEW);
        return toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse approveTask(Long taskId, TaskReviewRequest request) {
        Task task = getPendingReviewTask(taskId);
        assertTaskOwner(task);
        task.setStatus(StatusWork.COMPLETED);
        return toResponse(taskRepository.save(task));
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
    @Transactional(readOnly = true)
    public PaginatedData<TaskResponse> getMyTasks(int page, int size) {
        Long currentUserId = UserContext.requiredUserId();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        org.springframework.data.domain.Page<Task> taskPage = taskRepository.findAllByAssigneeIdAndStatusNot(currentUserId, StatusWork.CANCELED, pageable);

        List<TaskResponse> items = taskPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return com.intern.hub.library.common.dto.PaginatedData.<TaskResponse>builder()
                .items(items)
                .totalItems(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .build();
    }

    private Team getActiveTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy dự án team"));
        if (team.getStatus() == StatusWork.CANCELED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy dự án team");
        }
        return team;
    }

    private Project getActiveProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (project.getStatus() == StatusWork.CANCELED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        return project;
    }

    private Task getActiveTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        if (task.getStatus() == StatusWork.CANCELED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        return task;
    }

    private Task getPendingReviewTask(Long taskId) {
        Task task = getActiveTask(taskId);
        if (task.getStatus() != StatusWork.PENDING_REVIEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task must be pending review");
        }
        return task;
    }

    private void assertTaskOwner(Task task) {
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(task.getCreatorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the task creator can modify this task");
        }
    }

    private TaskResponse toResponse(Task task) {
        List<DocumentResponse> charterDocuments = documentService.getDocuments(
                task.getId(), DocumentScope.TASK, DocumentType.CHARTER);
        List<DocumentResponse> submissionDocuments = documentService.getDocuments(
                task.getId(), DocumentScope.TASK, DocumentType.DELIVERABLE);

        return new TaskResponse(
                task.getId(),
                task.getTeam() != null ? task.getTeam().getId() : null,
                task.getTaskUUID(),
                task.getName(),
                task.getDescription(),
                task.getNote(),
                task.getStatus(),
                task.getRewardToken(),
                task.getCreatorId(),
                task.getAssigneeId(),
                charterDocuments,
                task.getDeliverableDescription(),
                task.getDeliverableLink(),
                submissionDocuments,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String randomNumberUUI(){
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        String randomStr = String.format("%06d", randomPart).trim();
        return datePart + randomStr;
    }
}
