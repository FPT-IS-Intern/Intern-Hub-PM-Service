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
import com.intern.hub.pm.repository.specification.ProjectSpecification;
import com.intern.hub.pm.model.constant.Status;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.document.DocumentScope;
import com.intern.hub.pm.model.document.DocumentType;
import com.intern.hub.pm.model.project.Project;
import com.intern.hub.pm.model.project.ProjectMember;
import com.intern.hub.pm.repository.ProjectMemberRepository;
import com.intern.hub.pm.repository.ProjectRepository;
import com.intern.hub.pm.repository.TaskRepository;
import com.intern.hub.pm.service.DocumentService;
import com.intern.hub.pm.service.ProjectService;
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
    private final TaskRepository taskRepository;
    private final DocumentService documentService;

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

        List<ProjectResponse> items = projectPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PaginatedData.<ProjectResponse>builder()
                .items(items)
                .totalItems(projectPage.getTotalElements())
                .totalPages(projectPage.getTotalPages())
                .build();
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
        return toResponse(getActiveProject(projectId));
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
                files
        );
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
            // Simple replace strategy for members
            List<ProjectMember> existingMembers = projectMemberRepository.findAllByProjectIdAndStatusOrderByCreatedAtAsc(projectId, Status.ACTIVE);
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
                files
        );
        return toResponse(savedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId) {
        Project project = getActiveProject(projectId);
        assertProjectOwner(project);
        if (project.getStatus() != StatusWork.NOT_STARTED) {
            throw new ConflictDataException("Chỉ đóng được dự án khi, dự án chưa bắt đầu");
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
    public ProjectResponse completeProject(Long projectId, ProjectCompleteRequest request) {
//        Project project = getActiveProject(projectId);
//        assertProjectOwner(project);
//        boolean hasPendingReview = !taskRepository.findAllByProjectIdAndStatusNotOrderByCreatedAtDesc(projectId, StatusWork.CANCELED)
//                .stream()
//                .filter(task -> task.getStatus() == StatusWork.PENDING_REVIEW)
//                .toList()
//                .isEmpty();
//        if (hasPendingReview) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project still has tasks pending review");
//        }
//
//        project.setStatus(StatusWork.COMPLETED);
//        project.setCompletionComment(trimToNull(request.completionComment()));
//        return toResponse(projectRepository.save(project));
        return null;
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
        List<DocumentResponse> charterDocuments = documentService.getDocuments(
                project.getId(), DocumentScope.PROJECT, DocumentType.CHARTER);

        return new ProjectResponse(
                project.getId(),
                project.getProjectUUID(),
                project.getName(),
                project.getDescription(),
                project.getNote(),
                project.getStatus(),
                project.getBudgetToken(),
                project.getRewardToken(),
                project.getCreatorId(),
                project.getAssigneeId(),
                project.getDeliverableDescription(),
                project.getDeliverableLink(),
                project.getCompletionComment(),
                project.getStartDate(),
                project.getEndDate(),
                charterDocuments,
                project.getCreatedAt(),
                project.getUpdatedAt()
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
