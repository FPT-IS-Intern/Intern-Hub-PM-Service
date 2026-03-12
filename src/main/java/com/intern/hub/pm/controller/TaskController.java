package com.intern.hub.pm.controller;

import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.pm.dto.request.*;
import com.intern.hub.pm.dto.response.*;
import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.service.impl.WorkItemService;
import com.intern.hub.pm.utils.UserContext;
import com.intern.hub.starter.security.annotation.Authenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(
        name = "Task Controller",
        description = "API liên quan đến task."
)
@RestController
@RequestMapping("${api.prefix:/pm}")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskController {

    WorkItemService workItemService;

    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @PostMapping(path = "/projects/{projectId}/tasks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Tạo task",
            description = "API dùng để tạo task."
    )
    public ResponseApi<?> createTask(
            @PathVariable Long projectId,
            @RequestPart("task") TaskRequest taskJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        Long userId = UserContext.requiredUserId();
        workItemService.createTask(projectId, taskJson, files, userId);
        return ResponseApi.noContent();
    }

    @GetMapping("/projects/{projectId}/tasks")
    @Authenticated
    @Operation(
            summary = "Danh sách task",
            description = "API danh sách các task trong một dự án."
    )
    public ResponseApi<PageResponse<WorkItemResponse>> getTask(
            @PathVariable Long projectId,
            WorkFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        filter.setType(WorkItemType.TASK);
        filter.setParentId(projectId);
        filter.setStatusNot(String.valueOf(StatusWork.DA_HUY));
        Page<WorkItemResponse> pageResult = workItemService.getAll(filter, page, size);
        return ResponseApi.ok(toPageResponse(pageResult));
    }

    @GetMapping("/tasks/{taskId}")
    @Authenticated
    @Operation(
            summary = "Chi tiết task",
            description = "API chi tiết một task."
    )
    public ResponseApi<TaskDetailResponse> getTask(@PathVariable Long taskId) {
        TaskDetailResponse response = workItemService.taskDetail(taskId);
        return ResponseApi.ok(response);
    }

    //edit
    @PutMapping(path = "/tasks/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Sửa task",
            description = "API dùng để sửa nội dung của task."
    )
    public ResponseApi<?> edit(
            @PathVariable Long id,
            @RequestPart("task") EditTaskRequest taskJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        workItemService.editTask(id, taskJson, files);
        return ResponseApi.noContent();
    }

    //nộp task
    @PostMapping(path = "/tasks/{taskId}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Nộp task",
            description = "API dùng để user nộp đáp án task."
    )
    public ResponseApi<?> submitTask(
            @PathVariable Long taskId,
            @RequestPart("result") SubmitTaskRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        workItemService.submitTask(taskId, request, files);
        return ResponseApi.noContent();
    }

    @GetMapping("/my-tasks")
    @Authenticated
    @Operation(
            summary = "Nhiệm vụ của tôi",
            description = "API dùng để lấy danh sách task được giao cho user hiện tại."
    )
    public ResponseApi<PageResponse<WorkItemResponse>> getMyTasks(
            WorkFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        filter.setType(WorkItemType.TASK);
        filter.setAssignee(String.valueOf(UserContext.requiredUserId()));
        filter.setStatusNot(String.valueOf(StatusWork.DA_HUY));
        Page<WorkItemResponse> pageResult = workItemService.getAll(filter, page, size);
        return ResponseApi.ok(toPageResponse(pageResult));
    }

    // từ chối duyệt
    @PostMapping("/tasks/{taskId}/refuse")
    @Authenticated
    @Operation(
            summary = "Từ chối duyệt task",
            description = "API dùng để tạo từ chối duyệt task (đáp án chưa đúng)."
    )
    public ResponseApi<?> refuse(
            @PathVariable Long taskId,
            @RequestBody NoteRequest request
    ) {
        workItemService.refuseTask(taskId, request);
        return ResponseApi.noContent();
    }

    @PostMapping("/tasks/{taskId}/approve")
    @Authenticated
    @Operation(
            summary = "Duyệt task",
            description = "API dùng để duyệt task đã nộp và chuyển sang hoàn thành."
    )
    public ResponseApi<?> approve(
            @PathVariable Long taskId,
            @RequestBody ApproveTaskRequest request
    ) {
        workItemService.approveTask(taskId, request);
        return ResponseApi.noContent();
    }

    @DeleteMapping(path = "/tasks/{id}")
    @Authenticated
    @Operation(
            summary = "Đóng task",
            description = "API dùng để đóng task(xóa task, update status)."
    )
    public ResponseApi<?> delete(
            @PathVariable Long id
    ) {
        workItemService.deleteWork(id, WorkItemType.TASK);
        return ResponseApi.noContent();
    }

//
    // api task của user với id
    // duyệt task, duyệt 2 câấp (khi nhận task rồi thì k đc hủy)
    // xem chi tiết 1 task UI
    // xét ai có quền tạo task
    //
}

