package com.intern.hub.pm.service.impl;

import com.intern.hub.pm.dto.request.EditTaskRequest;
import com.intern.hub.pm.dto.request.NoteRequest;
import com.intern.hub.pm.dto.request.ApproveTaskRequest;
import com.intern.hub.pm.dto.request.CompleteProjectRequest;
import com.intern.hub.pm.dto.request.ExtendProjectRequest;
import com.intern.hub.pm.dto.request.SubmitTaskRequest;
import com.intern.hub.pm.dto.request.TaskRequest;
import com.intern.hub.pm.dto.request.UserProjectRequest;
import com.intern.hub.pm.dto.request.WorkFilterRequest;
import com.intern.hub.pm.dto.request.WorkItemRequest;
import com.intern.hub.pm.dto.response.TaskDetailResponse;
import com.intern.hub.pm.dto.response.WorkItemDetailResponse;
import com.intern.hub.pm.dto.response.WorkItemResponse;
import com.intern.hub.pm.enums.Status;
import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.library.common.exception.ConflictDataException;
import com.intern.hub.library.common.exception.ForbiddenException;
import com.intern.hub.library.common.exception.NotFoundException;
import com.intern.hub.pm.model.Project;
import com.intern.hub.pm.repository.WorkItemRepository;
import com.intern.hub.pm.repository.specification.WorkSpecification;
import com.intern.hub.pm.service.IWorkItemService;
import com.intern.hub.pm.utils.UserContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class WorkItemService implements IWorkItemService {

    private final WorkItemRepository workItemRepository;
    private final HrmUserDirectoryService hrmUserDirectoryService;
    private final EntityMemberService entityMemberService;
    private final DocumentService documentService;

    public Page<Project> getProjects(Pageable pageable) {
        return workItemRepository.findByType(WorkItemType.PROJECT, pageable);
    }

    public Page<Project> getTasks(Long projectId, Pageable pageable) {
        return workItemRepository.findByParentIdAndType(projectId, WorkItemType.TASK, pageable);
    }

    public Page<WorkItemResponse> getAll(WorkFilterRequest filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<Project> spec = WorkSpecification.filter(filter);
        return workItemRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private WorkItemResponse toResponse(Project project) {
        long countMember = entityMemberService.countMemberOfWork(project.getId(), Status.ACTIVE);
        WorkItemResponse res = new WorkItemResponse();
        res.setId(project.getId());
        res.setWordItemUUID(project.getWorkItemUuid());
        res.setMemberNumber(String.valueOf(countMember));
        res.setName(project.getName());
        res.setDescription(project.getDescription());
        res.setStatus(project.getStatus());
        res.setBudgetPoint(project.getBudgetPoint());
        res.setRewardPoint(project.getRewardPoint());
        res.setStartDate(project.getStartDate());
        res.setEndDate(project.getEndDate());
        return res;
    }

    @Override
    @Transactional
    public void createProject(WorkItemRequest request, List<MultipartFile> files, Long userId) {
        var creator = hrmUserDirectoryService.requireById(userId);
        var assignee = hrmUserDirectoryService.requireById(request.getAssigneeId());

        validateDateRange(request.getStartDate(), request.getEndDate());
        validateProjectRequest(request);

        Project project = new Project();
        project.setCreatorId(creator.userId());
        project.setAssigneeId(assignee.userId());
        project.setWorkItemUuid(generateWorkItemUuid());
        project.setParent(null);
        project.setType(WorkItemType.PROJECT);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(StatusWork.NOT_STARTED);
        project.setBudgetPoint(defaultZero(request.getBudgetPoint()));
        project.setRewardPoint(defaultZero(request.getRewardPoint()));
        project.setReclaimedPoint(0L);
        project.setBonusPoint(0L);
        project.setResult("");
        project.setResultLink("");
        project.setNote("");
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(project);
        documentService.saveDocuments(project, "PROJECT", files, userId, "pm/projects/" + project.getId());

        if (request.getUserList() != null && !request.getUserList().isEmpty()) {
            entityMemberService.saveListUserProject(project.getId(), request.getUserList());
        }
    }

    @Override
    @Transactional
    public void createTask(Long projectId, TaskRequest request, List<MultipartFile> files, Long userId) {
        var creator = hrmUserDirectoryService.requireById(userId);
        Project project = workItemRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("project.not.found", "Không tìm thấy dự án có id: " + projectId));
        if (project.getStatus().equals(StatusWork.COMPLETED) || project.getStatus().equals(StatusWork.CANCELED)) {
            throw new NotFoundException("task.create.invalid.state", "Dự án đã hoàn thành hoặc đã hủy nên không thể tạo task");
        }
        if (!project.getAssigneeId().equals(creator.userId()) && !project.getCreatorId().equals(creator.userId())) {
            throw new NotFoundException("task.create.forbidden", "Bạn không có quyền tạo task trong dự án này!");
        }

        var assignee = hrmUserDirectoryService.requireById(request.getAssigneeId());
        validateDateRange(request.getStartDate(), request.getEndDate());
        validateTaskRequest(request);

        Project task = new Project();
        task.setWorkItemUuid(generateWorkItemUuid());
        task.setParent(project);
        task.setType(WorkItemType.TASK);
        task.setCreatorId(creator.userId());
        task.setAssigneeId(assignee.userId());
        task.setName(request.getTaskName());
        task.setDescription(request.getDescription());
        task.setStatus(StatusWork.NOT_STARTED);
        task.setBudgetPoint(defaultZero(request.getBudgetPoint()));
        task.setRewardPoint(defaultZero(request.getRewardPoint()));
        task.setReclaimedPoint(0L);
        task.setBonusPoint(0L);
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());
        task.setResult("");
        task.setResultLink("");
        task.setNote("");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(task);
        documentService.saveDocuments(task, "TASK_GUIDE", files, userId, "pm/tasks/" + task.getId() + "/guide");
    }

    @Override
    public WorkItemDetailResponse workItemDetailResponse(Long id) {
        return toWorkItemDetailResponse(findById(id));
    }

    private WorkItemDetailResponse toWorkItemDetailResponse(Project project) {
        var creator = hrmUserDirectoryService.requireById(project.getCreatorId());
        var assignee = hrmUserDirectoryService.requireById(project.getAssigneeId());
        return WorkItemDetailResponse.builder()
                .id(project.getId())
                .workItemUuid(project.getWorkItemUuid())
                .creator(creator.fullName())
                .assignee(assignee.fullName())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .budgetPoint(project.getBudgetPoint())
                .rewardPoint(project.getRewardPoint())
                .reclaimedPoint(project.getReclaimedPoint())
                .bonusPoint(project.getBonusPoint())
                .result(project.getResult())
                .resultLink(project.getResultLink())
                .note(project.getNote())
                .extensionReason(project.getExtensionReason())
                .documents(documentService.getActiveDocuments(project.getId(), "PROJECT"))
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    @Override
    public void addUserProject(Long projectId, List<UserProjectRequest> requests) {
        entityMemberService.saveListUserProject(projectId, requests);
    }

    @Override
    @Transactional
    public void editProject(Long id, WorkItemRequest request, List<MultipartFile> files) {
        Project project = findById(id);
        Long currentUserId = UserContext.requiredUserId();

        if (!currentUserId.equals(project.getCreatorId())) {
            throw new ForbiddenException("project.update.forbidden", "Bạn không phải là chủ của dự án này!");
        }
        if (project.getStatus() != StatusWork.NOT_STARTED) {
            throw new ConflictDataException("project.update.conflict", "Chỉ được sửa khi dự án chưa bắt đầu");
        }

        var newAssignee = hrmUserDirectoryService.requireById(request.getAssigneeId());
        validateDateRange(request.getStartDate(), request.getEndDate());
        validateProjectRequest(request);

        project.setAssigneeId(newAssignee.userId());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setBudgetPoint(defaultZero(request.getBudgetPoint()));
        project.setRewardPoint(defaultZero(request.getRewardPoint()));
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(project);
        if (files != null) {
            documentService.replaceDocuments(project, "PROJECT", files, currentUserId, "pm/projects/" + project.getId());
        }

        if (request.getUserList() != null) {
            entityMemberService.deleteByProjectId(project.getId());
            entityMemberService.saveListUserProject(project.getId(), request.getUserList());
        }
    }

    @Override
    @Transactional
    public void editTask(Long id, EditTaskRequest request, List<MultipartFile> files) {
        Project project = findById(id);
        Long currentUserId = UserContext.requiredUserId();

        if (!currentUserId.equals(project.getCreatorId())) {
            throw new ForbiddenException("task.update.forbidden", "Bạn không phải là chủ của công việc này!");
        }
        if (project.getStatus() != StatusWork.NOT_STARTED) {
            throw new ConflictDataException("task.update.conflict", "Chỉ được sửa khi công việc chưa bắt đầu");
        }

        var newAssignee = hrmUserDirectoryService.requireById(request.getAssigneeId());
        validateDateRange(request.getStartDate(), request.getEndDate());

        project.setAssigneeId(newAssignee.userId());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setBudgetPoint(defaultZero(request.getBudgetPoint()));
        project.setRewardPoint(defaultZero(request.getRewardPoint()));
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(project);
        if (files != null) {
            documentService.replaceDocuments(project, "TASK_GUIDE", files, currentUserId, "pm/tasks/" + project.getId() + "/guide");
        }
    }

    @Override
    public void deleteWork(Long id, WorkItemType workType) {
        Project project = findById(id);
        Long currentUserId = UserContext.requiredUserId();

        if (!currentUserId.equals(project.getCreatorId())) {
            throw new ForbiddenException("work.delete.forbidden", "Bạn không phải là chủ của " + workType.getLabel() + " này!");
        }
        if (!project.getStatus().equals(StatusWork.NOT_STARTED)) {
            throw new NotFoundException("work.delete.invalid.state", workType.getLabel() + " đang tiến hành không xóa được!");
        }

        project.setStatus(StatusWork.CANCELED);
        project.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(project);
    }

    @Override
    public Project findById(Long id) {
        return workItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("work.not.found", "Không tìm thấy id dự án: " + id));
    }

    @Override
    public Project refuseTask(Long taskId, NoteRequest request) {
        Project work = findById(taskId);
        Long currentUserId = UserContext.requiredUserId();

        if (!currentUserId.equals(work.getCreatorId())) {
            throw new ForbiddenException("task.refuse.forbidden", "Bạn không phải là người giao nhiệm vụ này!");
        }
        if (!work.getStatus().equals(StatusWork.PENDING_REVIEW)) {
            throw new NotFoundException("task.refuse.invalid.state", "Task này chưa nộp nên không thể yêu cầu chỉnh sửa!");
        }

        work.setStatus(StatusWork.NEEDS_REVISION);
        work.setNote(request.getNote());
        work.setUpdatedAt(LocalDateTime.now());
        return workItemRepository.save(work);
    }

    @Override
    public Project approveTask(Long taskId, ApproveTaskRequest request) {
        Project task = findById(taskId);
        Long currentUserId = UserContext.requiredUserId();

        if (task.getType() != WorkItemType.TASK) {
            throw new NotFoundException("task.not.found", "Không tìm thấy task cần duyệt");
        }
        if (!currentUserId.equals(task.getCreatorId())) {
            throw new ForbiddenException("task.approve.forbidden", "Bạn không phải là người giao nhiệm vụ này!");
        }
        if (task.getStatus() != StatusWork.PENDING_REVIEW) {
            throw new ConflictDataException("task.approve.invalid.state", "Chỉ duyệt được task đang ở trạng thái chờ duyệt");
        }

        long reclaimedPoint = defaultZero(request.getReclaimedPoint());
        if (reclaimedPoint > defaultZero(task.getBudgetPoint())) {
            throw new ConflictDataException("task.approve.reclaimed.invalid", "Điểm thu hồi không được lớn hơn ngân sách task");
        }

        task.setReclaimedPoint(reclaimedPoint);
        task.setStatus(StatusWork.COMPLETED);
        task.setNote(request.getNote());
        task.setUpdatedAt(LocalDateTime.now());
        return workItemRepository.save(task);
    }

    @Override
    public TaskDetailResponse taskDetail(Long id) {
        return taskDetailResponse(findById(id));
    }

    private TaskDetailResponse taskDetailResponse(Project project) {
        var creator = hrmUserDirectoryService.requireById(project.getCreatorId());
        var assignee = hrmUserDirectoryService.requireById(project.getAssigneeId());
        return TaskDetailResponse.builder()
                .id(project.getId())
                .workItemUuid(project.getWorkItemUuid())
                .creator(creator.fullName())
                .assignee(assignee.fullName())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .budgetPoint(project.getBudgetPoint())
                .rewardPoint(project.getRewardPoint())
                .reclaimedPoint(project.getReclaimedPoint())
                .result(project.getResult())
                .resultLink(project.getResultLink())
                .note(project.getNote())
                .guideDocuments(documentService.getActiveDocuments(project.getId(), "TASK_GUIDE"))
                .submissionDocuments(documentService.getActiveDocuments(project.getId(), "TASK_SUBMISSION"))
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    @Override
    public void submitTask(Long taskId, SubmitTaskRequest request, List<MultipartFile> files) {
        Project project = findById(taskId);
        Long currentUserId = UserContext.requiredUserId();

        if (!currentUserId.equals(project.getAssigneeId())) {
            throw new NotFoundException("work.submit.forbidden", "Bạn không phải là người thực hiện công việc này!");
        }
        if (project.getType() != WorkItemType.TASK) {
            throw new NotFoundException("task.not.found", "Không tìm thấy task cần nộp");
        }

        project.setResult(request.getResult());
        project.setResultLink(request.getResultLink());
        project.setStatus(StatusWork.PENDING_REVIEW);
        project.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(project);
        documentService.replaceDocuments(
                project,
                "TASK_SUBMISSION",
                files,
                currentUserId,
                "pm/tasks/" + project.getId() + "/submission");
    }

    @Override
    public Project extendProject(Long projectId, ExtendProjectRequest request) {
        Project project = findById(projectId);
        Long currentUserId = UserContext.requiredUserId();

        if (project.getType() != WorkItemType.PROJECT) {
            throw new NotFoundException("project.not.found", "Không tìm thấy dự án cần gia hạn");
        }
        if (!currentUserId.equals(project.getCreatorId()) && !currentUserId.equals(project.getAssigneeId())) {
            throw new ForbiddenException("project.extend.forbidden", "Bạn không có quyền gia hạn dự án này");
        }
        if (request.getNewEndDate() == null) {
            throw new ConflictDataException("project.extend.end-date.required", "Ngày gia hạn mới là bắt buộc");
        }
        if (project.getStatus() == StatusWork.COMPLETED || project.getStatus() == StatusWork.CANCELED) {
            throw new ConflictDataException("project.extend.invalid.state", "Không thể gia hạn dự án đã hoàn thành hoặc đã hủy");
        }
        if (!request.getNewEndDate().isAfter(LocalDateTime.now())) {
            throw new ConflictDataException("project.extend.end-date.invalid", "Ngày gia hạn mới phải lớn hơn hiện tại");
        }
        if (project.getEndDate() != null && !request.getNewEndDate().isAfter(project.getEndDate())) {
            throw new ConflictDataException("project.extend.end-date.not-after", "Ngày gia hạn mới phải lớn hơn ngày kết thúc hiện tại");
        }

        project.setEndDate(request.getNewEndDate());
        project.setExtensionReason(request.getReason());
        project.setStatus(StatusWork.IN_PROGRESS);
        project.setUpdatedAt(LocalDateTime.now());
        return workItemRepository.save(project);
    }

    @Override
    public Project completeProject(Long projectId, CompleteProjectRequest request) {
        Project project = findById(projectId);
        Long currentUserId = UserContext.requiredUserId();

        if (project.getType() != WorkItemType.PROJECT) {
            throw new NotFoundException("project.not.found", "Không tìm thấy dự án cần kết thúc");
        }
        if (!currentUserId.equals(project.getCreatorId()) && !currentUserId.equals(project.getAssigneeId())) {
            throw new ForbiddenException("project.complete.forbidden", "Bạn không có quyền kết thúc dự án này");
        }
        if (project.getStatus() == StatusWork.COMPLETED || project.getStatus() == StatusWork.CANCELED) {
            throw new ConflictDataException("project.complete.invalid.state", "Dự án này không thể kết thúc thêm lần nữa");
        }
        if (workItemRepository.existsByParent_IdAndStatus(projectId, StatusWork.PENDING_REVIEW)) {
            throw new ConflictDataException("project.complete.pending-tasks", "Không thể kết thúc dự án khi còn task chờ duyệt");
        }

        long reclaimedPoint = defaultZero(request.getReclaimedPoint());
        long bonusPoint = defaultZero(request.getBonusPoint());
        if (reclaimedPoint > defaultZero(project.getBudgetPoint())) {
            throw new ConflictDataException("project.complete.reclaimed.invalid", "Điểm thu hồi không được lớn hơn ngân sách dự án");
        }

        project.setReclaimedPoint(reclaimedPoint);
        project.setBonusPoint(bonusPoint);
        project.setNote(request.getNote());
        project.setStatus(StatusWork.COMPLETED);
        project.setUpdatedAt(LocalDateTime.now());
        return workItemRepository.save(project);
    }

    @Override
    public long countTaskByUser(WorkItemType workType, Long projectId, Long userId, StatusWork statusWork) {
        return workItemRepository.countTaskByUser(workType, projectId, userId, statusWork);
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ConflictDataException("work.date.invalid", "Ngày bắt đầu phải trước ngày kết thúc");
        }
    }

    private void validateProjectRequest(WorkItemRequest request) {
        validateCommonWorkRequest(request.getName(), request.getDescription(), request.getStartDate(), request.getEndDate());
        if (request.getAssigneeId() == null) {
            throw new ConflictDataException("project.assignee.required", "PM phụ trách là bắt buộc");
        }
    }

    private void validateTaskRequest(TaskRequest request) {
        validateCommonWorkRequest(request.getTaskName(), request.getDescription(), request.getStartDate(), request.getEndDate());
        if (request.getAssigneeId() == null) {
            throw new ConflictDataException("task.assignee.required", "Người thực hiện task là bắt buộc");
        }
    }

    private void validateCommonWorkRequest(String name, String description, LocalDateTime startDate, LocalDateTime endDate) {
        if (name == null || name.isBlank()) {
            throw new ConflictDataException("work.name.required", "Tên là bắt buộc");
        }
        if (name.length() > 100) {
            throw new ConflictDataException("work.name.size.invalid", "Tên không được vượt quá 100 ký tự");
        }
        if (description == null || description.isBlank()) {
            throw new ConflictDataException("work.description.required", "Mô tả là bắt buộc");
        }
        if (description.length() > 255) {
            throw new ConflictDataException("work.description.size.invalid", "Mô tả không được vượt quá 255 ký tự");
        }
        if (startDate == null || endDate == null) {
            throw new ConflictDataException("work.date.required", "Ngày bắt đầu và ngày kết thúc là bắt buộc");
        }
        if (!startDate.isAfter(LocalDateTime.now())) {
            throw new ConflictDataException("work.start-date.invalid", "Ngày bắt đầu phải lớn hơn thời điểm hiện tại");
        }
    }

    private long defaultZero(Long value) {
        return value == null ? 0L : value;
    }

    private String generateWorkItemUuid() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return datePart + String.format("%06d", randomPart).trim();
    }
}

