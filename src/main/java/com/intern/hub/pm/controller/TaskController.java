package com.intern.hub.pm.controller;

import com.intern.hub.pm.dtos.request.*;
import com.intern.hub.pm.dtos.response.*;
import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.services.WorkItemService;
import com.intern.hub.pm.utils.UserContext;
import com.intern.hub.starter.security.annotation.Authenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @PostMapping(path = "/module/{moduleId}/task", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Tạo task",
            description = "API dùng để tạo task."
    )
    public ResponseEntity<?> createModule(
            @PathVariable Long moduleId,
            @RequestPart("task") TaskRequest taskJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        String username = UserContext.requiredEmail();
        workItemService.createTask(moduleId, taskJson, username);
        if (files != null && !files.isEmpty()) {
            List<MultipartFile> file2 = files;
        }
        return ApiResponseBuilder.success("Tạo task thành công", null);
    }

    @GetMapping("/module/{moduleId}/task")
    @Authenticated
    @Operation(
            summary = "Danh sách task",
            description = "API danh sách các task trong một module."
    )
    public ResponseEntity<?> getTask(
            @PathVariable Long moduleId,
            WorkFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        filter.setType(WorkItemType.TASK);
        filter.setParentId(moduleId);
        filter.setStatusNot(String.valueOf(StatusWork.DA_XOA));
        Page<WorkItemResponse> pageResult = workItemService.getAll(filter, page, size);
        return ApiResponseBuilder.success(
                "Danh sách task",
                toPageResponse(pageResult));
    }

    @GetMapping("/task/{taskId}")
    @Authenticated
    @Operation(
            summary = "Chi tiết task",
            description = "API chi tiết một task."
    )
    public ResponseEntity<?> getTask(@PathVariable Long taskId) {
        try {
            TaskDetailResponse response = workItemService.taskDetail(taskId);
            return ApiResponseBuilder.success("Chi tiết task", response);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError(e.getMessage());
        }
    }

    //edit
    @PutMapping(path = "/task/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Sửa task",
            description = "API dùng để sửa nội dung của task."
    )
    public ResponseEntity<?> edit(
            @PathVariable Long id,
            @RequestPart("task") EditTaskRequest taskJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        workItemService.editTask(id, taskJson);
        List<MultipartFile> file2 = files;
        return ApiResponseBuilder.success("Sửa task thành công", null);
    }

    //nộp task
    @PostMapping(path = "/task/{taskId}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Nộp task",
            description = "API dùng để user nộp đáp án task."
    )
    public ResponseEntity<?> submitTask(
            @PathVariable Long taskId,
            @RequestPart("result") SubmitTaskRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        workItemService.submit(taskId, request, WorkItemType.TASK);
        return ApiResponseBuilder.success("Nộp task thành công", null);
    }

    // từ chối duyệt
    @PostMapping("/task/{taskId}/refuse")
    @Authenticated
    @Operation(
            summary = "Từ chối duyệt task",
            description = "API dùng để tạo từ chối duyệt task (đáp án chưa đúng)."
    )
    public ResponseEntity<?> refuse(
            @PathVariable Long taskId,
            @RequestBody NoteRequest request
    ) {
        workItemService.refuse(taskId, request, WorkItemType.TASK);
        return ApiResponseBuilder.success("Từ chối duyệt task thành công", null);
    }

    @DeleteMapping(path = "/task/{id}")
    @Authenticated
    @Operation(
            summary = "Đóng task",
            description = "API dùng để đóng task(xóa task, update status)."
    )
    public ResponseEntity<?> delete(
            @PathVariable Long id
    ) {
        workItemService.deleteWork(id, WorkItemType.TASK);
        return ApiResponseBuilder.success("Xóa task thành công", null);
    }

//
    // api task của user với id
    // duyệt task, duyệt 2 câấp (khi nhận task rồi thì k đc hủy)
    // xem chi tiết 1 task UI
    // xét ai có quền tạo task
    //
}
