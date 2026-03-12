package com.intern.hub.pm.controller;

import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.pm.dto.request.*;
import com.intern.hub.pm.dto.response.PageResponse;
import com.intern.hub.pm.dto.response.ProjectUserResponse;
import com.intern.hub.pm.dto.response.WorkItemResponse;
import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.service.impl.EntityMemberService;
import com.intern.hub.pm.service.impl.WorkItemService;
import com.intern.hub.pm.utils.UserContext;
import com.intern.hub.starter.security.annotation.Authenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(
        name = "Project Module Controller",
        description = "API liên quan đến module."
)
@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectModuleController {

    EntityMemberService entityMemberService;
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

    @PostMapping(path = "/project/{projectId}/modules", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Tạo project module",
            description = "API dùng để tạo module."
    )
    public ResponseApi<?> createModule(
            @PathVariable Long projectId,
            @RequestPart("project") WorkItemRequest projectJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        Long userId = UserContext.requiredUserId();
        workItemService.createModule(projectId, projectJson, userId);
        if (files != null && !files.isEmpty()) {
            List<MultipartFile> file2 = files;
        }
        return ResponseApi.noContent();
    }

    @GetMapping("/project/{projectId}/modules")
    @Authenticated
    @Operation(
            summary = "Danh sách module",
            description = "API danh sách các module trong một dự án."
    )
    public ResponseApi<PageResponse<WorkItemResponse>> getProjects(
            @PathVariable Long projectId,
            WorkFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        filter.setType(WorkItemType.MODULE);
        filter.setParentId(projectId);
        filter.setStatusNot(String.valueOf(StatusWork.DA_XOA));
        Page<WorkItemResponse> pageResult = workItemService.getAll(filter, page, size);
        return ResponseApi.ok(toPageResponse(pageResult));
    }

    @GetMapping(path = {"/module/{moduleId}/users", "/modules/{moduleId}/users"})
    @Authenticated
    @Operation(
            summary = "Danh sách user của module",
            description = "API dùng để lấy ra danh sách user của một module (để hiển thị trong create task)."
    )
    public ResponseApi<PageResponse<ProjectUserResponse>> getUserOfModule(
            @PathVariable Long moduleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProjectUserResponse> pageResult =
                entityMemberService.projectUserList(moduleId, WorkItemType.MODULE, page, size);

        return ResponseApi.ok(toPageResponse(pageResult));
    }

    //add user vào module
    @PostMapping({"/module/{moduleId}/users", "/modules/{moduleId}/users"})
    @Authenticated
    @Operation(
            summary = "Thêm user vào module",
            description = "API dùng để thêm user vào module."
    )
    public ResponseApi<?> addUserProject(
            @PathVariable Long moduleId,
            @RequestBody List<UserProjectRequest> requests
    ) {
        workItemService.addUserModule(moduleId, requests);
        return ResponseApi.noContent();
    }

    //chinh sua module// sủa lại chỗ save cho 1 hàm thôi
    @PutMapping(path = {"/module/{id}", "/modules/{id}"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Chỉnh sửa module",
            description = "API dùng để thay đổi thông tin module."
    )
    public ResponseApi<?> edit(
            @PathVariable Long id,
            @RequestPart("project") WorkItemRequest projectJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        workItemService.editProject(id, projectJson);
        if (files != null && !files.isEmpty()) {
            List<MultipartFile> file2 = files;
        }
        return ResponseApi.noContent();
    }

    //nộp module
    @PostMapping(path = {"/module/{moduleId}/submit", "/modules/{moduleId}/submit"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Nộp module",
            description = "API dùng để user nộp đáp án module."
    )
    public ResponseApi<?> submitTask(
            @PathVariable Long moduleId,
            @RequestPart("result") SubmitTaskRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        workItemService.submit(moduleId, request, WorkItemType.MODULE);
        return ResponseApi.noContent();
    }

    // từ chối duyệt
    @PostMapping({"/module/{moduleId}/refuse", "/modules/{moduleId}/refuse"})
    @Authenticated
    @Operation(
            summary = "Từ chối duyệt module",
            description = "API dùng để từ chối duyệt một module."
    )
    public ResponseApi<?> refuse(
            @PathVariable Long moduleId,
            @RequestBody NoteRequest request
    ) {
        workItemService.refuse(moduleId, request, WorkItemType.MODULE);
        return ResponseApi.noContent();
    }

    @DeleteMapping(path = {"/module/{id}", "/modules/{id}"})
    @Authenticated
    @Operation(
            summary = "Đóng module",
            description = "API dùng để đóng module(xóa module, update status)."
    )
    public ResponseApi<?> delete(
            @PathVariable Long id
    ) {
        workItemService.deleteWork(id, WorkItemType.MODULE);
        return ResponseApi.noContent();
    }
    // duyệt tk, detail, thêm user, task của user
    // kiểm tra ví, hiển thêm transsaction ở detail
    //xem lại token ở sửa task
    // thêm số thành viên trong 1 dự án/module trong load dự an
}

