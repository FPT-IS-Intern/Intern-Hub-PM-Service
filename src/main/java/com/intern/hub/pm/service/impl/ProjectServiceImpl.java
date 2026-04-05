package com.intern.hub.pm.service.impl;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.library.common.exception.ConflictDataException;
import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.dto.project.ProjectCompleteRequest;
import com.intern.hub.pm.dto.project.ProjectExtendRequest;
import com.intern.hub.pm.dto.project.ProjectResponse;
import com.intern.hub.pm.dto.project.ProjectUpsertRequest;
import com.intern.hub.pm.dto.project.ProjectFilterRequest;
import com.intern.hub.pm.dto.project.ProjectStatisticsResponse;
import com.intern.hub.pm.dto.project.ApproveRequest;
import com.intern.hub.pm.repository.TeamRepository;
import com.intern.hub.pm.repository.specification.ProjectSpecification;
import com.intern.hub.pm.model.constant.Status;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.document.DocumentScope;
import com.intern.hub.pm.model.document.DocumentType;
import com.intern.hub.pm.model.project.Project;
import com.intern.hub.pm.model.project.ProjectMember;
import com.intern.hub.pm.repository.ProjectMemberRepository;
import com.intern.hub.pm.repository.ProjectRepository;
import com.intern.hub.pm.service.DocumentService;
import com.intern.hub.pm.service.ProjectService;
import com.intern.hub.pm.feign.HrmInternalFeignClient;
import com.intern.hub.pm.feign.WalletInternalFeignClient;
import com.intern.hub.pm.feign.model.*;
import com.intern.hub.pm.utils.UserContext;
import lombok.RequiredArgsConstructor;
import com.intern.hub.library.common.exception.ForbiddenException;
import com.intern.hub.library.common.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private static final Sort PROJECT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TeamRepository teamRepository;
    private final DocumentService documentService;
    private final HrmInternalFeignClient hrmInternalFeignClient;
    private final WalletInternalFeignClient walletInternalFeignClient;

    @Override
    @Transactional(readOnly = true)
    public PaginatedData<ProjectResponse> getProjects(int page, int size) {
        return getProjects(new ProjectFilterRequest(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedData<ProjectResponse> getProjects(ProjectFilterRequest filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, PROJECT_SORT);
        Specification<Project> spec = ProjectSpecification.filter(filter);
        Page<Project> projectPage = projectRepository.findAll(spec, pageable);
        List<Project> projects = projectPage.getContent();

        List<Long> userIds = projects.stream()
                .flatMap(p -> java.util.stream.Stream.of(p.getCreatorId(), p.getAssigneeId()))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        java.util.Map<Long, String> userNameMap = fetchUserNames(userIds);

        List<Long> projectIds = projects.stream().map(Project::getId).toList();
        java.util.Map<Long, Long> memberCountMap = projectMemberRepository
                .countMembersByProjectIds(projectIds, Status.ACTIVE)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]));

        List<ProjectResponse> items = projects.stream()
                .map(p -> toResponseWithNames(
                        p,
                        userNameMap.getOrDefault(p.getCreatorId(), "User (ID: " + p.getCreatorId() + ")"),
                        userNameMap.getOrDefault(p.getAssigneeId(), "User (ID: " + p.getAssigneeId() + ")"),
                        memberCountMap.getOrDefault(p.getId(), 0L)))
                .toList();

        return PaginatedData.<ProjectResponse>builder()
                .items(items)
                .totalItems(projectPage.getTotalElements())
                .totalPages(projectPage.getTotalPages())
                .build();
    }

    private java.util.Map<Long, String> fetchUserNames(List<Long> userIds) {
        java.util.Map<Long, String> userNameMap = new java.util.HashMap<>();
        if (userIds == null || userIds.isEmpty())
            return userNameMap;
        try {
            var response = hrmInternalFeignClient.getUsersByIdsInternal(userIds);
            if (response != null && response.data() != null) {
                response.data().forEach(u -> userNameMap.put(Long.valueOf(u.userId()), u.fullName()));
            }
        } catch (Exception e) {
            // Log error if needed
        }
        return userNameMap;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectStatisticsResponse getProjectStatistics() {
        return ProjectStatisticsResponse.builder()
                .totalProjects(projectRepository.countByStatusNot(StatusWork.CANCELED))
                .notStartedProjects(projectRepository.countByStatus(StatusWork.NOT_STARTED))
                .inProgressProjects(projectRepository.countByStatus(StatusWork.IN_PROGRESS))
                .completedProjects(projectRepository.countByStatus(StatusWork.COMPLETED))
                .overdueProjects(projectRepository.countByStatus(StatusWork.OVERDUE))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long projectId) {
        Project project = getActiveProject(projectId);
        long memberCount = projectMemberRepository.countByProjectIdAndStatus(projectId, Status.ACTIVE);
        String creatorName = fetchUserName(project.getCreatorId());
        String assigneeName = fetchUserName(project.getAssigneeId());
        return toResponseWithNames(project, creatorName, assigneeName, memberCount);
    }

    private String fetchUserName(Long userId) {
        if (userId == null)
            return null;
        try {
            var res = hrmInternalFeignClient.getUserByIdInternal(userId);
            if (res != null && res.data() != null)
                return res.data().fullName();
        } catch (Exception e) {
            // ignore
        }
        return "User (ID: " + userId + ")";
    }

    @Override
    @Transactional
    public ProjectResponse createProject(Long userId, ProjectUpsertRequest request, List<MultipartFile> files) {
        Project project = Project.builder()
                .projectUUID(randomNumberUUI())
                .name(request.name().trim())
                .description(request.description().trim())
                .status(StatusWork.NOT_STARTED)
                .budgetToken(request.budgetToken())
                .rewardToken(request.rewardToken())
                .creatorId(userId)
                .assigneeId(request.assigneeId())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        Project savedProject = projectRepository.save(project);

        if (request.memberList() != null && !request.memberList().isEmpty()) {
            List<ProjectMember> members = request.memberList().stream()
                    .map(m -> ProjectMember.builder()
                            .userId(m.userId())
                            .role(m.role())
                            .project(savedProject)
                            .status(Status.ACTIVE)
                            .build())
                    .toList();
            projectMemberRepository.saveAll(members);
        }

        documentService.replaceDocuments(
                savedProject.getId(),
                DocumentScope.PROJECT,
                DocumentType.CHARTER,
                userId,
                "pm/projects/" + savedProject.getId() + "/charter",
                files);
        return toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectUpsertRequest request, List<MultipartFile> files) {
        Project project = getActiveProject(projectId);
        assertProjectOwner(project);

        if (project.getStatus() != StatusWork.NOT_STARTED) {
            throw new ConflictDataException("Chỉ được sửa khi dự án chưa bắt đầu");
        }

        if (request.startDate().isAfter(request.endDate())) {
            throw new ConflictDataException("Ngày bắt đầu phải trước ngày kết thúc");
        }

        project.setName(request.name().trim());
        project.setDescription(request.description().trim());
        project.setBudgetToken(request.budgetToken());
        project.setRewardToken(request.rewardToken());
        project.setAssigneeId(request.assigneeId());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());

        Project savedProject = projectRepository.save(project);

        if (request.memberList() != null) {
            List<ProjectMember> existingMembers = projectMemberRepository
                    .findAllByProjectIdAndStatusOrderByCreatedAtAsc(projectId, Status.ACTIVE);
            projectMemberRepository.deleteAll(existingMembers);

            List<ProjectMember> newMembers = request.memberList().stream()
                    .map(m -> ProjectMember.builder()
                            .userId(m.userId())
                            .role(m.role())
                            .project(savedProject)
                            .status(Status.ACTIVE)
                            .build())
                    .toList();
            projectMemberRepository.saveAll(newMembers);
        }

        documentService.replaceDocuments(
                savedProject.getId(),
                DocumentScope.PROJECT,
                DocumentType.CHARTER,
                UserContext.requiredUserId(),
                "pm/projects/" + savedProject.getId() + "/charter",
                files);
        return toResponse(savedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId) {
        Project project = getActiveProject(projectId);
        assertProjectOwner(project);
        if (project.getStatus() != StatusWork.NOT_STARTED && project.getStatus() != StatusWork.REJECTED) {
            throw new ConflictDataException("Chỉ có thể thu hồi/hủy dự án khi chưa bắt đầu hoặc bị từ chối");
        }
        project.setStatus(StatusWork.CANCELED);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public ProjectResponse extendProject(Long projectId, ProjectExtendRequest request) {
        Project project = getActiveProject(projectId);
        assertProjectOwner(project);
        project.setEndDate(request.endDate());
        return toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    public ProjectResponse completeProject(Long projectId, ProjectCompleteRequest request, List<MultipartFile> files) {
        Project project = getActiveProject(projectId);
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(project.getAssigneeId())) {
            throw new ForbiddenException("Chỉ người được giao mới có thể nộp đáp án dự án này!");
        }

        long incompleteTeamCount = teamRepository.countByProjectIdAndStatusNotIn(
                projectId,
                List.of(StatusWork.COMPLETED, StatusWork.CANCELED, StatusWork.REJECTED));
        if (incompleteTeamCount > 0) {
            throw new ConflictDataException("Vẫn còn team trong dự án chưa hoàn thành");
        }

        project.setStatus(StatusWork.PENDING_REVIEW);
        project.setCompletionComment(trimToNull(request.completionComment()));
        project.setDeliverableDescription(trimToNull(request.deliverableDescription()));
        project.setDeliverableLink(trimToNull(request.deliverableLink()));
        Project savedProject = projectRepository.save(project);

        documentService.replaceDocuments(
                savedProject.getId(),
                DocumentScope.PROJECT,
                DocumentType.DELIVERABLE,
                UserContext.requiredUserId(),
                "pm/projects/" + savedProject.getId() + "/submission",
                files);

        return toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse approveProject(Long projectId, ApproveRequest request) {
        Project project = getActiveProject(projectId);
        assertProjectOwner(project);

        if (project.getStatus() != StatusWork.PENDING_REVIEW) {
            throw new ConflictDataException("Không phải trạng thái chờ duyệt, không duyệt được");
        }

        project.setStatus(StatusWork.COMPLETED);
        project.setNote(trimToNull(request.note()));
        Project savedProject = projectRepository.save(project);

        // Gọi sang Wallet để thực hiện release token (Duyệt)
        WalletBrowseWorkRequest browseRequest = WalletBrowseWorkRequest.builder()
                .entityId(savedProject.getId())
                .workUUId(Long.parseLong(savedProject.getProjectUUID()))
                .type("project")
                .note(savedProject.getNote())
                .build();
        walletInternalFeignClient.browseWork(browseRequest);

        return toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse refuseProject(Long projectId, ApproveRequest request) {
        Project project = getActiveProject(projectId);
        assertProjectOwner(project);

        if (project.getStatus() != StatusWork.PENDING_REVIEW) {
            throw new ConflictDataException("Không phải trạng thái chờ duyệt, không yêu cầu sửa được");
        }

        project.setStatus(StatusWork.NEEDS_REVISION);
        project.setNote(trimToNull(request.note()));
        return toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    public ProjectResponse acceptProject(Long projectId) {
        Project project = getActiveProject(projectId);
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(project.getAssigneeId())) {
            throw new ForbiddenException("Chỉ người được giao mới có thể nhận dự án");
        }
        project.setStatus(StatusWork.IN_PROGRESS);
        Project savedProject = projectRepository.save(project);

        // Gọi sang Wallet để lưu transaction lên Blockchain
        WalletTransactionProjectRequest txRequest = WalletTransactionProjectRequest.builder()
                .projectId(savedProject.getId())
                .projectUUId(Long.parseLong(savedProject.getProjectUUID()))
                .creatorId(savedProject.getCreatorId())
                .assigneeId(savedProject.getAssigneeId())
                .bt(savedProject.getBudgetToken())
                .rt(savedProject.getRewardToken())
                .build();
        walletInternalFeignClient.saveTransactionProject(txRequest);

        return toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse rejectProject(Long projectId) {
        Project project = getActiveProject(projectId);
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(project.getAssigneeId())) {
            throw new ForbiddenException("Chỉ người được giao mới có thể từ chối dự án");
        }
        if (project.getStatus() != StatusWork.NOT_STARTED) {
            throw new ConflictDataException("Chỉ có thể từ chối dự án khi ở trạng thái Chưa bắt đầu");
        }
        project.setStatus(StatusWork.REJECTED);
        return toResponse(projectRepository.save(project));
    }

    private Project getActiveProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dự án"));
        if (project.getStatus() == StatusWork.CANCELED) {
            throw new NotFoundException("Không tìm thấy dự án");
        }
        return project;
    }

    private void assertProjectOwner(Project project) {
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(project.getCreatorId())) {
            throw new ForbiddenException("Bạn không phải chủ dự án này!");
        }
    }

    private ProjectResponse toResponse(Project project) {
        long memberCount = projectMemberRepository.countByProjectIdAndStatus(project.getId(), Status.ACTIVE);
        String creatorName = fetchUserName(project.getCreatorId());
        String assigneeName = fetchUserName(project.getAssigneeId());

        return toResponseWithNames(project, creatorName, assigneeName, memberCount);
    }

    private ProjectResponse toResponseWithNames(Project project, String creatorName, String assigneeName,
            Long memberCount) {
        List<DocumentResponse> charterDocuments = documentService.getDocuments(
                project.getId(), DocumentScope.PROJECT, DocumentType.CHARTER);

        return new ProjectResponse(
                String.valueOf(project.getId()),
                project.getProjectUUID(),
                project.getName(),
                project.getDescription(),
                project.getNote(),
                project.getStatus(),
                project.getBudgetToken(),
                project.getRewardToken(),
                project.getCreatorId() != null ? String.valueOf(project.getCreatorId()) : null,
                project.getAssigneeId() != null ? String.valueOf(project.getAssigneeId()) : null,
                creatorName,
                assigneeName,
                project.getDeliverableDescription(),
                project.getDeliverableLink(),
                project.getCompletionComment(),
                memberCount,
                project.getStartDate(),
                project.getEndDate(),
                charterDocuments,
                project.getCreatedAt(),
                project.getUpdatedAt());
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
