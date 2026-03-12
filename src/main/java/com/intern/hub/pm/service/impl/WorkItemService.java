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
import com.intern.hub.pm.model.WorkItem;
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

    public Page<WorkItem> getProjects(Pageable pageable) {
        return workItemRepository.findByType(WorkItemType.PROJECT, pageable);
    }

    public Page<WorkItem> getTasks(Long projectId, Pageable pageable) {
        return workItemRepository.findByParentIdAndType(projectId, WorkItemType.TASK, pageable);
    }

    public Page<WorkItemResponse> getAll(WorkFilterRequest filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<WorkItem> spec = WorkSpecification.filter(filter);
        return workItemRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private WorkItemResponse toResponse(WorkItem workItem) {
        long countMember = entityMemberService.countMemberOfWork(workItem.getId(), Status.ACTIVE);
        WorkItemResponse res = new WorkItemResponse();
        res.setId(workItem.getId());
        res.setWordItemUUID(workItem.getWorkItemUuid());
        res.setMemberNumber(String.valueOf(countMember));
        res.setName(workItem.getName());
        res.setDescription(workItem.getDescription());
        res.setStatus(workItem.getStatus());
        res.setBudgetPoint(workItem.getBudgetPoint());
        res.setRewardPoint(workItem.getRewardPoint());
        res.setStartDate(workItem.getStartDate());
        res.setEndDate(workItem.getEndDate());
        return res;
    }

    @Override
    @Transactional
    public void createProject(WorkItemRequest request, List<MultipartFile> files, Long userId) {
        var creator = hrmUserDirectoryService.requireById(userId);
        var assignee = hrmUserDirectoryService.requireById(request.getAssigneeId());

        validateDateRange(request.getStartDate(), request.getEndDate());
        validateProjectRequest(request);

        WorkItem workItem = new WorkItem();
        workItem.setCreatorId(creator.userId());
        workItem.setAssigneeId(assignee.userId());
        workItem.setWorkItemUuid(generateWorkItemUuid());
        workItem.setParent(null);
        workItem.setType(WorkItemType.PROJECT);
        workItem.setName(request.getName());
        workItem.setDescription(request.getDescription());
        workItem.setStatus(StatusWork.CHUA_BAT_DAU);
        workItem.setBudgetPoint(defaultZero(request.getBudgetPoint()));
        workItem.setRewardPoint(defaultZero(request.getRewardPoint()));
        workItem.setReclaimedPoint(0L);
        workItem.setBonusPoint(0L);
        workItem.setResult("");
        workItem.setResultLink("");
        workItem.setNote("");
        workItem.setStartDate(request.getStartDate());
        workItem.setEndDate(request.getEndDate());
        workItem.setCreatedAt(LocalDateTime.now());
        workItem.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(workItem);
        documentService.saveDocuments(workItem, "PROJECT", files, userId, "pm/projects/" + workItem.getId());

        if (request.getUserList() != null && !request.getUserList().isEmpty()) {
            entityMemberService.saveListUserProject(workItem.getId(), request.getUserList());
        }
    }

    @Override
    @Transactional
    public void createTask(Long projectId, TaskRequest request, List<MultipartFile> files, Long userId) {
        var creator = hrmUserDirectoryService.requireById(userId);
        WorkItem project = workItemRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("project.not.found", "Không tìm thấy dự án có id: " + projectId));
        if (project.getStatus().equals(StatusWork.HOAN_THANH) || project.getStatus().equals(StatusWork.DA_HUY)) {
            throw new NotFoundException("task.create.invalid.state", "Dự án đã hoàn thành hoặc đã hủy nên không thể tạo task");
        }
        if (!project.getAssigneeId().equals(creator.userId()) && !project.getCreatorId().equals(creator.userId())) {
            throw new NotFoundException("task.create.forbidden", "Bạn không có quyền tạo task trong dự án này!");
        }

        var assignee = hrmUserDirectoryService.requireById(request.getAssigneeId());
        validateDateRange(request.getStartDate(), request.getEndDate());
        validateTaskRequest(request);

        WorkItem task = new WorkItem();
        task.setWorkItemUuid(generateWorkItemUuid());
        task.setParent(project);
        task.setType(WorkItemType.TASK);
        task.setCreatorId(creator.userId());
        task.setAssigneeId(assignee.userId());
        task.setName(request.getTaskName());
        task.setDescription(request.getDescription());
        task.setStatus(StatusWork.CHUA_BAT_DAU);
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

    private WorkItemDetailResponse toWorkItemDetailResponse(WorkItem workItem) {
        var creator = hrmUserDirectoryService.requireById(workItem.getCreatorId());
        var assignee = hrmUserDirectoryService.requireById(workItem.getAssigneeId());
        return WorkItemDetailResponse.builder()
                .id(workItem.getId())
                .workItemUuid(workItem.getWorkItemUuid())
                .creator(creator.fullName())
                .assignee(assignee.fullName())
                .name(workItem.getName())
                .description(workItem.getDescription())
                .status(workItem.getStatus())
                .budgetPoint(workItem.getBudgetPoint())
                .rewardPoint(workItem.getRewardPoint())
                .reclaimedPoint(workItem.getReclaimedPoint())
                .bonusPoint(workItem.getBonusPoint())
                .result(workItem.getResult())
                .resultLink(workItem.getResultLink())
                .note(workItem.getNote())
                .extensionReason(workItem.getExtensionReason())
                .documents(documentService.getActiveDocuments(workItem.getId(), "PROJECT"))
                .startDate(workItem.getStartDate())
                .endDate(workItem.getEndDate())
                .createdAt(workItem.getCreatedAt())
                .updatedAt(workItem.getUpdatedAt())
                .build();
    }

    @Override
    public void addUserProject(Long projectId, List<UserProjectRequest> requests) {
        entityMemberService.saveListUserProject(projectId, requests);
    }

    @Override
    @Transactional
    public void editProject(Long id, WorkItemRequest request, List<MultipartFile> files) {
        WorkItem workItem = findById(id);
        Long currentUserId = UserContext.requiredUserId();

        if (!currentUserId.equals(workItem.getCreatorId())) {
            throw new ForbiddenException("project.update.forbidden", "Bạn không phải là chủ của dự án này!");
        }
        if (workItem.getStatus() != StatusWork.CHUA_BAT_DAU) {
            throw new ConflictDataException("project.update.conflict", "Chỉ được sửa khi dự án chưa bắt đầu");
        }

        var newAssignee = hrmUserDirectoryService.requireById(request.getAssigneeId());
        validateDateRange(request.getStartDate(), request.getEndDate());
        validateProjectRequest(request);

        workItem.setAssigneeId(newAssignee.userId());
        workItem.setName(request.getName());
        workItem.setDescription(request.getDescription());
        workItem.setBudgetPoint(defaultZero(request.getBudgetPoint()));
        workItem.setRewardPoint(defaultZero(request.getRewardPoint()));
        workItem.setStartDate(request.getStartDate());
        workItem.setEndDate(request.getEndDate());
        workItem.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(workItem);
        if (files != null) {
            documentService.replaceDocuments(workItem, "PROJECT", files, currentUserId, "pm/projects/" + workItem.getId());
        }

        if (request.getUserList() != null) {
            entityMemberService.deleteByProjectId(workItem.getId());
            entityMemberService.saveListUserProject(workItem.getId(), request.getUserList());
        }
    }

    @Override
    @Transactional
    public void editTask(Long id, EditTaskRequest request, List<MultipartFile> files) {
        WorkItem workItem = findById(id);
        Long currentUserId = UserContext.requiredUserId();

        if (!currentUserId.equals(workItem.getCreatorId())) {
            throw new ForbiddenException("task.update.forbidden", "Bạn không phải là chủ của công việc này!");
        }
        if (workItem.getStatus() != StatusWork.CHUA_BAT_DAU) {
            throw new ConflictDataException("task.update.conflict", "Chỉ được sửa khi công việc chưa bắt đầu");
        }

        var newAssignee = hrmUserDirectoryService.requireById(request.getAssigneeId());
        validateDateRange(request.getStartDate(), request.getEndDate());

        workItem.setAssigneeId(newAssignee.userId());
        workItem.setName(request.getName());
        workItem.setDescription(request.getDescription());
        workItem.setBudgetPoint(defaultZero(request.getBudgetPoint()));
        workItem.setRewardPoint(defaultZero(request.getRewardPoint()));
        workItem.setStartDate(request.getStartDate());
        workItem.setEndDate(request.getEndDate());
        workItem.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(workItem);
        if (files != null) {
            documentService.replaceDocuments(workItem, "TASK_GUIDE", files, currentUserId, "pm/tasks/" + workItem.getId() + "/guide");
        }
    }

    @Override
    public void deleteWork(Long id, WorkItemType workType) {
        WorkItem workItem = findById(id);
        Long currentUserId = UserContext.requiredUserId();

        if (!currentUserId.equals(workItem.getCreatorId())) {
            throw new ForbiddenException("work.delete.forbidden", "Bạn không phải là chủ của " + workType.getLabel() + " này!");
        }
        if (!workItem.getStatus().equals(StatusWork.CHUA_BAT_DAU)) {
            throw new NotFoundException("work.delete.invalid.state", workType.getLabel() + " đang tiến hành không xóa được!");
        }

        workItem.setStatus(StatusWork.DA_HUY);
        workItem.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(workItem);
    }

    @Override
    public WorkItem findById(Long id) {
        return workItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("work.not.found", "Không tìm thấy id dự án: " + id));
    }

    @Override
    public WorkItem refuseTask(Long taskId, NoteRequest request) {
        WorkItem work = findById(taskId);
        Long currentUserId = UserContext.requiredUserId();

        if (!currentUserId.equals(work.getCreatorId())) {
            throw new ForbiddenException("task.refuse.forbidden", "Bạn không phải là người giao nhiệm vụ này!");
        }
        if (!work.getStatus().equals(StatusWork.CHO_DUYET)) {
            throw new NotFoundException("task.refuse.invalid.state", "Task này chưa nộp nên không thể yêu cầu chỉnh sửa!");
        }

        work.setStatus(StatusWork.CAN_CHINH_SUA);
        work.setNote(request.getNote());
        work.setUpdatedAt(LocalDateTime.now());
        return workItemRepository.save(work);
    }

    @Override
    public WorkItem approveTask(Long taskId, ApproveTaskRequest request) {
        WorkItem task = findById(taskId);
        Long currentUserId = UserContext.requiredUserId();

        if (task.getType() != WorkItemType.TASK) {
            throw new NotFoundException("task.not.found", "Không tìm thấy task cần duyệt");
        }
        if (!currentUserId.equals(task.getCreatorId())) {
            throw new ForbiddenException("task.approve.forbidden", "Bạn không phải là người giao nhiệm vụ này!");
        }
        if (task.getStatus() != StatusWork.CHO_DUYET) {
            throw new ConflictDataException("task.approve.invalid.state", "Chỉ duyệt được task đang ở trạng thái chờ duyệt");
        }

        long reclaimedPoint = defaultZero(request.getReclaimedPoint());
        if (reclaimedPoint > defaultZero(task.getBudgetPoint())) {
            throw new ConflictDataException("task.approve.reclaimed.invalid", "Điểm thu hồi không được lớn hơn ngân sách task");
        }

        task.setReclaimedPoint(reclaimedPoint);
        task.setStatus(StatusWork.HOAN_THANH);
        task.setNote(request.getNote());
        task.setUpdatedAt(LocalDateTime.now());
        return workItemRepository.save(task);
    }

    @Override
    public TaskDetailResponse taskDetail(Long id) {
        return taskDetailResponse(findById(id));
    }

    private TaskDetailResponse taskDetailResponse(WorkItem workItem) {
        var creator = hrmUserDirectoryService.requireById(workItem.getCreatorId());
        var assignee = hrmUserDirectoryService.requireById(workItem.getAssigneeId());
        return TaskDetailResponse.builder()
                .id(workItem.getId())
                .workItemUuid(workItem.getWorkItemUuid())
                .creator(creator.fullName())
                .assignee(assignee.fullName())
                .name(workItem.getName())
                .description(workItem.getDescription())
                .status(workItem.getStatus())
                .budgetPoint(workItem.getBudgetPoint())
                .rewardPoint(workItem.getRewardPoint())
                .reclaimedPoint(workItem.getReclaimedPoint())
                .result(workItem.getResult())
                .resultLink(workItem.getResultLink())
                .note(workItem.getNote())
                .guideDocuments(documentService.getActiveDocuments(workItem.getId(), "TASK_GUIDE"))
                .submissionDocuments(documentService.getActiveDocuments(workItem.getId(), "TASK_SUBMISSION"))
                .startDate(workItem.getStartDate())
                .endDate(workItem.getEndDate())
                .createdAt(workItem.getCreatedAt())
                .updatedAt(workItem.getUpdatedAt())
                .build();
    }

    @Override
    public void submitTask(Long taskId, SubmitTaskRequest request, List<MultipartFile> files) {
        WorkItem workItem = findById(taskId);
        Long currentUserId = UserContext.requiredUserId();

        if (!currentUserId.equals(workItem.getAssigneeId())) {
            throw new NotFoundException("work.submit.forbidden", "Bạn không phải là người thực hiện công việc này!");
        }
        if (workItem.getType() != WorkItemType.TASK) {
            throw new NotFoundException("task.not.found", "Không tìm thấy task cần nộp");
        }

        workItem.setResult(request.getResult());
        workItem.setResultLink(request.getResultLink());
        workItem.setStatus(StatusWork.CHO_DUYET);
        workItem.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(workItem);
        documentService.replaceDocuments(
                workItem,
                "TASK_SUBMISSION",
                files,
                currentUserId,
                "pm/tasks/" + workItem.getId() + "/submission");
    }

    @Override
    public WorkItem extendProject(Long projectId, ExtendProjectRequest request) {
        WorkItem project = findById(projectId);
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
        if (project.getStatus() == StatusWork.HOAN_THANH || project.getStatus() == StatusWork.DA_HUY) {
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
        project.setStatus(StatusWork.DANG_THUC_HIEN);
        project.setUpdatedAt(LocalDateTime.now());
        return workItemRepository.save(project);
    }

    @Override
    public WorkItem completeProject(Long projectId, CompleteProjectRequest request) {
        WorkItem project = findById(projectId);
        Long currentUserId = UserContext.requiredUserId();

        if (project.getType() != WorkItemType.PROJECT) {
            throw new NotFoundException("project.not.found", "Không tìm thấy dự án cần kết thúc");
        }
        if (!currentUserId.equals(project.getCreatorId()) && !currentUserId.equals(project.getAssigneeId())) {
            throw new ForbiddenException("project.complete.forbidden", "Bạn không có quyền kết thúc dự án này");
        }
        if (project.getStatus() == StatusWork.HOAN_THANH || project.getStatus() == StatusWork.DA_HUY) {
            throw new ConflictDataException("project.complete.invalid.state", "Dự án này không thể kết thúc thêm lần nữa");
        }
        if (workItemRepository.existsByParent_IdAndStatus(projectId, StatusWork.CHO_DUYET)) {
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
        project.setStatus(StatusWork.HOAN_THANH);
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

