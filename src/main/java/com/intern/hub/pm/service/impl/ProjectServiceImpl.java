package com.intern.hub.pm.service.impl;

import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.dto.project.ProjectCompleteRequest;
import com.intern.hub.pm.dto.project.ProjectExtendRequest;
import com.intern.hub.pm.dto.project.ProjectResponse;
import com.intern.hub.pm.dto.project.ProjectUpsertRequest;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.document.DocumentScope;
import com.intern.hub.pm.model.document.DocumentType;
import com.intern.hub.pm.model.project.Project;
import com.intern.hub.pm.repository.ProjectRepository;
import com.intern.hub.pm.repository.TaskRepository;
import com.intern.hub.pm.service.DocumentService;
import com.intern.hub.pm.service.ProjectService;
import com.intern.hub.pm.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private static final Sort PROJECT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final DocumentService documentService;

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects() {
        return projectRepository.findAllByStatusNot(StatusWork.CANCELED, PROJECT_SORT)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long projectId) {
        return toResponse(getActiveProject(projectId));
    }

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectUpsertRequest request, List<MultipartFile> files) {
        Long currentUserId = UserContext.requiredUserId();

        Project project = Project.builder()
                .projectUUID(UUID.randomUUID().toString())
                .name(request.name().trim())
                .description(request.description().trim())
                .note(trimToNull(request.note()))
                .status(request.status())
                .budgetToken(request.budgetToken())
                .rewardToken(request.rewardToken())
                .creatorId(currentUserId)
                .assigneeId(request.assigneeId())
                .deliverableDescription(trimToNull(request.deliverableDescription()))
                .deliverableLink(trimToNull(request.deliverableLink()))
                .endAt(request.endAt())
                .build();

        Project savedProject = projectRepository.save(project);
        documentService.replaceDocuments(
                savedProject.getId(),
                DocumentScope.PROJECT,
                DocumentType.CHARTER,
                currentUserId,
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

        project.setName(request.name().trim());
        project.setDescription(request.description().trim());
        project.setNote(trimToNull(request.note()));
        project.setStatus(request.status());
        project.setBudgetToken(request.budgetToken());
        project.setRewardToken(request.rewardToken());
        project.setAssigneeId(request.assigneeId());
        project.setDeliverableDescription(trimToNull(request.deliverableDescription()));
        project.setDeliverableLink(trimToNull(request.deliverableLink()));
        project.setEndAt(request.endAt());

        Project savedProject = projectRepository.save(project);
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
        project.setStatus(StatusWork.CANCELED);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public ProjectResponse extendProject(Long projectId, ProjectExtendRequest request) {
        Project project = getActiveProject(projectId);
        assertProjectOwner(project);
        project.setEndAt(request.endAt());
        project.setExtensionReason(trimToNull(request.reason()));
        return toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    public ProjectResponse completeProject(Long projectId, ProjectCompleteRequest request) {
        Project project = getActiveProject(projectId);
        assertProjectOwner(project);
        boolean hasPendingReview = !taskRepository.findAllByProjectIdAndStatusNotOrderByCreatedAtDesc(projectId, StatusWork.CANCELED)
                .stream()
                .filter(task -> task.getStatus() == StatusWork.PENDING_REVIEW)
                .toList()
                .isEmpty();
        if (hasPendingReview) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project still has tasks pending review");
        }

        project.setStatus(StatusWork.COMPLETED);
        project.setCompletionComment(trimToNull(request.completionComment()));
        project.setRecoveredToken(request.recoveredToken());
        project.setBonusToken(request.bonusToken());
        return toResponse(projectRepository.save(project));
    }

    private Project getActiveProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (project.getStatus() == StatusWork.CANCELED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        return project;
    }

    private void assertProjectOwner(Project project) {
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(project.getCreatorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project owner can modify this project");
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
                project.getEndAt(),
                project.getExtensionReason(),
                project.getCompletionComment(),
                project.getRecoveredToken(),
                project.getBonusToken(),
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
}
