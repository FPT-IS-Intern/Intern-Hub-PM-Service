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
@RequestMapping("${api.prefix:/api/v1}")
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

    @PostMapping(path = {"/module/{moduleId}/task", "/modules/{moduleId}/tasks"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Tạo task",
            description = "API dùng để tạo task."
    )
    public ResponseApi<?> createModule(
            @PathVariable Long moduleId,
            @RequestPart("task") TaskRequest taskJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        Long userId = UserContext.requiredUserId();
        workItemService.createTask(moduleId, taskJson, userId);
        if (files != null && !files.isEmpty()) {
            List<MultipartFile> file2 = files;
        }
        return ResponseApi.noContent();
    }

    @GetMapping({"/module/{moduleId}/task", "/modules/{moduleId}/tasks"})
    @Authenticated
    @Operation(
            summary = "Danh sách task",
            description = "API danh sách các task trong một module."
    )
    public ResponseApi<PageResponse<WorkItemResponse>> getTask(
            @PathVariable Long moduleId,
            WorkFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        filter.setType(WorkItemType.TASK);
        filter.setParentId(moduleId);
        filter.setStatusNot(String.valueOf(StatusWork.DA_XOA));
        Page<WorkItemResponse> pageResult = workItemService.getAll(filter, page, size);
        return ResponseApi.ok(toPageResponse(pageResult));
    }

    @GetMapping({"/task/{taskId}", "/tasks/{taskId}"})
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
    @PutMapping(path = {"/task/{id}", "/tasks/{id}"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
        workItemService.editTask(id, taskJson);
        List<MultipartFile> file2 = files;
        return ResponseApi.noContent();
    }

    //nộp task
    @PostMapping(path = {"/task/{taskId}/submit", "/tasks/{taskId}/submit"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
        workItemService.submit(taskId, request, WorkItemType.TASK);
        return ResponseApi.noContent();
    }

    // từ chối duyệt
    @PostMapping({"/task/{taskId}/refuse", "/tasks/{taskId}/refuse"})
    @Authenticated
    @Operation(
            summary = "Từ chối duyệt task",
            description = "API dùng để tạo từ chối duyệt task (đáp án chưa đúng)."
    )
    public ResponseApi<?> refuse(
            @PathVariable Long taskId,
            @RequestBody NoteRequest request
    ) {
        workItemService.refuse(taskId, request, WorkItemType.TASK);
        return ResponseApi.noContent();
    }

    @DeleteMapping(path = {"/task/{id}", "/tasks/{id}"})
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

