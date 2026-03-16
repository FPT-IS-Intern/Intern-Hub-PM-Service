package com.intern.hub.pm.service.impl;

import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.dto.task.TaskResponse;
import com.intern.hub.pm.dto.task.TaskReviewRequest;
import com.intern.hub.pm.dto.task.TaskUpsertRequest;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.document.DocumentScope;
import com.intern.hub.pm.model.document.DocumentType;
import com.intern.hub.pm.model.project.Project;
import com.intern.hub.pm.model.team.Task;
import com.intern.hub.pm.repository.ProjectRepository;
import com.intern.hub.pm.repository.TaskRepository;
import com.intern.hub.pm.service.DocumentService;
import com.intern.hub.pm.service.TaskService;
import com.intern.hub.pm.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final DocumentService documentService;

    @Override
    @Transactional
    public TaskResponse createTask(Long projectId, TaskUpsertRequest request, List<MultipartFile> files) {
        Project project = getActiveProject(projectId);
        Long currentUserId = UserContext.requiredUserId();

        Task task = Task.builder()
                .project(project)
                .taskUUID(UUID.randomUUID().toString())
                .name(request.name().trim())
                .description(request.description().trim())
                .note(trimToNull(request.note()))
                .status(request.status())
                .rewardToken(request.rewardToken())
                .creatorId(currentUserId)
                .assigneeId(request.assigneeId())
                .deliverableDescription(trimToNull(request.deliverableDescription()))
                .deliverableLink(trimToNull(request.deliverableLink()))
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
    public List<TaskResponse> getProjectTasks(Long projectId) {
        getActiveProject(projectId);
        return taskRepository.findAllByProjectIdAndStatusNotOrderByCreatedAtDesc(projectId, StatusWork.CANCELED)
                .stream()
                .map(this::toResponse)
                .toList();
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
        task.setNote(trimToNull(request.note()));
        task.setStatus(request.status());
        task.setRewardToken(request.rewardToken());
        task.setAssigneeId(request.assigneeId());
        task.setDeliverableDescription(trimToNull(request.deliverableDescription()));
        task.setDeliverableLink(trimToNull(request.deliverableLink()));

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
        task.setReviewComment(trimToNull(request.reviewComment()));
        task.setRecoveredToken(request.recoveredToken());
        return toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse refuseTask(Long taskId, TaskReviewRequest request) {
        Task task = getPendingReviewTask(taskId);
        assertTaskOwner(task);
        task.setStatus(StatusWork.NEEDS_REVISION);
        task.setReviewComment(trimToNull(request.reviewComment()));
        task.setRecoveredToken(request.recoveredToken());
        return toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks() {
        Long currentUserId = UserContext.requiredUserId();
        return taskRepository.findAllByAssigneeIdAndStatusNotOrderByCreatedAtDesc(currentUserId, StatusWork.CANCELED)
                .stream()
                .map(this::toResponse)
                .toList();
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
                task.getProject() != null ? task.getProject().getId() : null,
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
                task.getReviewComment(),
                task.getRecoveredToken(),
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
}
