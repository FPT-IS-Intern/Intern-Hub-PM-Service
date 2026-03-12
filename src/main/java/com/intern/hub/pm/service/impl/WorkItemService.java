package com.intern.hub.pm.service.impl;

import com.intern.hub.pm.dtos.request.EditTaskRequest;
import com.intern.hub.pm.dtos.request.NoteRequest;
import com.intern.hub.pm.dtos.request.SubmitTaskRequest;
import com.intern.hub.pm.dtos.request.TaskRequest;
import com.intern.hub.pm.dtos.request.UserProjectRequest;
import com.intern.hub.pm.dtos.request.WorkFilterRequest;
import com.intern.hub.pm.dtos.request.WorkItemRequest;
import com.intern.hub.pm.dtos.response.TaskDetailResponse;
import com.intern.hub.pm.dtos.response.WorkItemDetailResponse;
import com.intern.hub.pm.dtos.response.WorkItemResponse;
import com.intern.hub.pm.enums.Status;
import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.exceptions.ConflictException;
import com.intern.hub.pm.exceptions.ForbiddenException;
import com.intern.hub.pm.exceptions.NotFoundException;
import com.intern.hub.pm.model.User;
import com.intern.hub.pm.model.WorkItem;
import com.intern.hub.pm.repository.WorkItemRepository;
import com.intern.hub.pm.repository.specification.WorkSpecification;
import com.intern.hub.pm.service.IWorkItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class WorkItemService implements IWorkItemService {

    private final WorkItemRepository workItemRepository;
    private final UserService userService;
    private final EntityMemberService entityMemberService;

    public Page<WorkItem> getProjects(Pageable pageable) {
        return workItemRepository.findByType(WorkItemType.PROJECT, pageable);
    }

    public Page<WorkItem> getModules(Long projectId, Pageable pageable) {
        return workItemRepository.findByParentIdAndType(projectId, WorkItemType.MODULE, pageable);
    }

    public Page<WorkItem> getTasks(Long moduleId, Pageable pageable) {
        return workItemRepository.findByParentIdAndType(moduleId, WorkItemType.TASK, pageable);
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
        res.setStartDate(workItem.getStartDate());
        res.setEndDate(workItem.getEndDate());
        return res;
    }

    @Override
    @Transactional
    public void createProject(WorkItemRequest request, String emailUser) {
        User creator = userService.findByEmail(emailUser);
        User assignee = userService.findById(request.getAssigneeId());

        validateDateRange(request.getStartDate(), request.getEndDate());

        WorkItem workItem = new WorkItem();
        workItem.setCreatorId(creator.getId());
        workItem.setAssigneeId(assignee.getId());
        workItem.setWorkItemUuid(generateWorkItemUuid());
        workItem.setParent(null);
        workItem.setType(WorkItemType.PROJECT);
        workItem.setName(request.getName());
        workItem.setDescription(request.getDescription());
        workItem.setStatus(StatusWork.CHUA_BAT_DAU);
        workItem.setResult("");
        workItem.setResultLink("");
        workItem.setNote("");
        workItem.setStartDate(request.getStartDate());
        workItem.setEndDate(request.getEndDate());
        workItem.setCreatedAt(LocalDateTime.now());
        workItem.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(workItem);

        if (request.getUserList() != null && !request.getUserList().isEmpty()) {
            entityMemberService.saveListUserProject(workItem.getId(), request.getUserList());
        }
    }

    @Override
    @Transactional
    public void createModule(Long projectId, WorkItemRequest request, String emailUser) {
        User creator = userService.findByEmail(emailUser);
        User assignee = userService.findById(request.getAssigneeId());
        WorkItem project = findById(projectId);

        if (!project.getAssigneeId().equals(creator.getId())) {
            throw new NotFoundException("Bạn không phải là chủ dự án này nên không có quyền tạo module!");
        }
        if (project.getStatus().equals(StatusWork.CHO_DUYET) || project.getStatus().equals(StatusWork.DA_DUYET)) {
            throw new NotFoundException("Module đang chờ duyệt hoặc đã kết thúc nên không thể tạo module");
        }

        validateDateRange(request.getStartDate(), request.getEndDate());

        WorkItem workItem = new WorkItem();
        workItem.setCreatorId(creator.getId());
        workItem.setAssigneeId(assignee.getId());
        workItem.setWorkItemUuid(generateWorkItemUuid());
        workItem.setParent(project);
        workItem.setType(WorkItemType.MODULE);
        workItem.setName(request.getName());
        workItem.setDescription(request.getDescription());
        workItem.setStatus(StatusWork.CHUA_BAT_DAU);
        workItem.setResult("");
        workItem.setResultLink("");
        workItem.setNote("");
        workItem.setStartDate(request.getStartDate());
        workItem.setEndDate(request.getEndDate());
        workItem.setCreatedAt(LocalDateTime.now());
        workItem.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(workItem);

        if (request.getUserList() != null && !request.getUserList().isEmpty()) {
            entityMemberService.saveListUserModule(workItem.getId(), request.getUserList());
        }
    }

    @Override
    public void createTask(Long moduleId, TaskRequest request, String emailUser) {
        User creator = userService.findByEmail(emailUser);
        WorkItem module = workItemRepository.findById(moduleId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy module có id: " + moduleId));
        if (module.getStatus().equals(StatusWork.CHO_DUYET) || module.getStatus().equals(StatusWork.DA_DUYET)) {
            throw new NotFoundException("Module đang chờ duyệt hoặc đã kết thúc nên không thể tạo task");
        }
        if (!module.getAssigneeId().equals(creator.getId())) {
            throw new NotFoundException("Bạn không phải là chủ của module này nên không có quyền tạo task!");
        }

        User assignee = userService.findById(request.getAssigneeId());
        validateDateRange(request.getStartDate(), request.getEndDate());

        WorkItem task = new WorkItem();
        task.setWorkItemUuid(generateWorkItemUuid());
        task.setParent(module);
        task.setType(WorkItemType.TASK);
        task.setCreatorId(creator.getId());
        task.setAssigneeId(assignee.getId());
        task.setName(request.getTaskName());
        task.setDescription(request.getDescription());
        task.setStatus(StatusWork.CHUA_BAT_DAU);
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());
        task.setResult("");
        task.setResultLink("");
        task.setNote("");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(task);
    }

    @Override
    public WorkItemDetailResponse workItemDetailResponse(Long id) {
        return toWorkItemDetailResponse(findById(id));
    }

    private WorkItemDetailResponse toWorkItemDetailResponse(WorkItem workItem) {
        User creator = userService.findById(workItem.getCreatorId());
        User assignee = userService.findById(workItem.getAssigneeId());
        return WorkItemDetailResponse.builder()
                .id(workItem.getId())
                .workItemUuid(workItem.getWorkItemUuid())
                .creator(creator.getFullName())
                .assignee(assignee.getFullName())
                .name(workItem.getName())
                .description(workItem.getDescription())
                .status(workItem.getStatus())
                .result(workItem.getResult())
                .resultLink(workItem.getResultLink())
                .note(workItem.getNote())
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
    public void addUserModule(Long moduleId, List<UserProjectRequest> requests) {
        entityMemberService.saveListUserModule(moduleId, requests);
    }

    @Override
    @Transactional
    public void editProject(Long id, WorkItemRequest request) {
        WorkItem workItem = findById(id);
        User user = getCurrentUser();

        if (!user.getId().equals(workItem.getCreatorId())) {
            throw new ForbiddenException("Bạn không phải là chủ của dự án này!");
        }
        if (workItem.getStatus() != StatusWork.CHUA_BAT_DAU) {
            throw new ConflictException("Chỉ được sửa khi dự án chưa bắt đầu");
        }

        User newAssignee = userService.findById(request.getAssigneeId());
        validateDateRange(request.getStartDate(), request.getEndDate());

        workItem.setAssigneeId(newAssignee.getId());
        workItem.setName(request.getName());
        workItem.setDescription(request.getDescription());
        workItem.setStartDate(request.getStartDate());
        workItem.setEndDate(request.getEndDate());
        workItem.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(workItem);

        if (request.getUserList() != null) {
            entityMemberService.deleteByProjectId(workItem.getId());
            entityMemberService.saveListUserProject(workItem.getId(), request.getUserList());
        }
    }

    @Override
    @Transactional
    public void editTask(Long id, EditTaskRequest request) {
        WorkItem workItem = findById(id);
        User user = getCurrentUser();

        if (!user.getId().equals(workItem.getCreatorId())) {
            throw new ForbiddenException("Bạn không phải là chủ của công việc này!");
        }
        if (workItem.getStatus() != StatusWork.CHUA_BAT_DAU) {
            throw new ConflictException("Chỉ được sửa khi công việc chưa bắt đầu");
        }

        User newAssignee = userService.findById(request.getAssigneeId());
        validateDateRange(request.getStartDate(), request.getEndDate());

        workItem.setAssigneeId(newAssignee.getId());
        workItem.setName(request.getName());
        workItem.setDescription(request.getDescription());
        workItem.setStartDate(request.getStartDate());
        workItem.setEndDate(request.getEndDate());
        workItem.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(workItem);
    }

    @Override
    public void deleteWork(Long id, WorkItemType workType) {
        WorkItem workItem = findById(id);
        User user = getCurrentUser();

        if (!user.getId().equals(workItem.getCreatorId())) {
            throw new ForbiddenException("Bạn không phải là chủ của " + workType.getLabel() + " này!");
        }
        if (!workItem.getStatus().equals(StatusWork.CHUA_BAT_DAU)) {
            throw new NotFoundException(workType.getLabel() + " đang tiến hành không xóa được!");
        }

        workItem.setStatus(StatusWork.DA_XOA);
        workItem.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(workItem);
    }

    @Override
    public WorkItem findById(Long id) {
        return workItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy id dự án: " + id));
    }

    @Override
    public WorkItem refuse(Long workId, NoteRequest request, WorkItemType workItemType) {
        WorkItem work = findById(workId);
        User user = getCurrentUser();

        if (!user.getId().equals(work.getCreatorId())) {
            throw new ForbiddenException("Bạn không phải là chủ của " + workItemType.getLabel().toLowerCase() + " này!");
        }
        if (!work.getStatus().equals(StatusWork.CHO_DUYET)) {
            throw new NotFoundException(workItemType.getLabel() + " này chưa nộp nên không từ chối được!");
        }

        work.setStatus(StatusWork.TU_CHOI);
        work.setNote(request.getNote());
        work.setUpdatedAt(LocalDateTime.now());
        return workItemRepository.save(work);
    }

    @Override
    public TaskDetailResponse taskDetail(Long id) {
        return taskDetailResponse(findById(id));
    }

    private TaskDetailResponse taskDetailResponse(WorkItem workItem) {
        User creator = userService.findById(workItem.getCreatorId());
        User assignee = userService.findById(workItem.getAssigneeId());
        return TaskDetailResponse.builder()
                .id(workItem.getId())
                .workItemUuid(workItem.getWorkItemUuid())
                .creator(creator.getFullName())
                .assignee(assignee.getFullName())
                .name(workItem.getName())
                .description(workItem.getDescription())
                .status(workItem.getStatus())
                .result(workItem.getResult())
                .resultLink(workItem.getResultLink())
                .note(workItem.getNote())
                .startDate(workItem.getStartDate())
                .endDate(workItem.getEndDate())
                .createdAt(workItem.getCreatedAt())
                .updatedAt(workItem.getUpdatedAt())
                .build();
    }

    @Override
    public void submit(Long taskId, SubmitTaskRequest request, WorkItemType workItemType) {
        WorkItem workItem = findById(taskId);
        User user = getCurrentUser();

        if (!user.getId().equals(workItem.getAssigneeId())) {
            throw new NotFoundException("Bạn không phải là người thực hiện công việc này!");
        }

        WorkItemType childType = getChildType(workItemType);
        if (childType != null) {
            boolean exists = workItemRepository.existsByParent_IdAndTypeAndStatusNotIn(
                    taskId,
                    childType,
                    List.of(StatusWork.DA_DUYET, StatusWork.DA_XOA)
            );

            if (exists) {
                throw new NotFoundException("Có " + childType.name().toLowerCase() + " chưa hoàn thành nên không thể nộp!");
            }
        }

        workItem.setResult(request.getResult());
        workItem.setResultLink(request.getResultLink());
        workItem.setStatus(StatusWork.CHO_DUYET);
        workItem.setUpdatedAt(LocalDateTime.now());
        workItemRepository.save(workItem);
    }

    private WorkItemType getChildType(WorkItemType type) {
        return switch (type) {
            case PROJECT -> WorkItemType.MODULE;
            case MODULE -> WorkItemType.TASK;
            default -> null;
        };
    }

    @Override
    public long countTaskByUser(WorkItemType workType, Long projectId, Long userId, StatusWork statusWork) {
        return workItemRepository.countTaskByUser(workType, projectId, userId, statusWork);
    }

    private User getCurrentUser() {
        String email = UserContext.requiredEmail();
        return userService.findByEmail(email);
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ConflictException("Ngày bắt đầu phải trước ngày kết thúc");
        }
    }

    private String generateWorkItemUuid() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return datePart + String.format("%06d", randomPart).trim();
    }
}
