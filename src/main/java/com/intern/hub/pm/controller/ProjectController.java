package com.intern.hub.pm.controller;

import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.pm.dto.request.*;
import com.intern.hub.pm.dto.response.*;
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
        name = "Project Controller",
        description = "API liên quan đến project."
)
@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectController {

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

    @GetMapping("/projects")
    @Authenticated
    @Operation(
            summary = "Danh sách dự án",
            description = "API danh sách dự án."
    )
    public ResponseApi<PageResponse<WorkItemResponse>> getProjects(
            WorkFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        filter.setType(WorkItemType.PROJECT);
        filter.setStatusNot(String.valueOf(StatusWork.DA_XOA));
        Page<WorkItemResponse> pageResult = workItemService.getAll(filter, page, size);
        return ResponseApi.ok(toPageResponse(pageResult));
    }

    @PostMapping(path = {"/project", "/projects"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Tạo dự án",
            description = "API dùng để tạo dự án."
    )
    public ResponseApi<?> create(
            @RequestPart("project") WorkItemRequest projectJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        Long userId = UserContext.requiredUserId();
        workItemService.createProject(projectJson, userId);
        if (files != null && !files.isEmpty()) {
            List<MultipartFile> file2 = files;
        }
        return ResponseApi.noContent();
    }

    // list user trong dự án (để hiển thị trong module )
    @GetMapping(path = {"/project/{projectId}/users", "/projects/{projectId}/users"})
    @Authenticated
    @Operation(
            summary = "Danh sách user của dự án",
            description = "API dùng để lấy ra danh sách user của một dự án (để hiển thị trong module)."
    )
    public ResponseApi<PageResponse<ProjectUserResponse>> getUserOfProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProjectUserResponse> pageResult =
                entityMemberService.projectUserList(projectId, WorkItemType.PROJECT, page, size);
        return ResponseApi.ok(toPageResponse(pageResult));
    }

    //detail, thêm tran với type của work
    @GetMapping({"/project/{id}/detail", "/projects/{id}", "/projects/{id}/detail"})
    @Authenticated
    @Operation(
            summary = "Chi tiết dự án",
            description = "API dùng để xem chi tiết dự án (dự án, module)."
    )
    public ResponseApi<WorkItemDetailResponse> detailProject(@PathVariable Long id) {
        WorkItemDetailResponse response = workItemService.workItemDetailResponse(id);
        return ResponseApi.ok(response);
    }

    //add user vào pro
    @PostMapping({"/project/{projectId}/users", "/projects/{projectId}/users"})
    @Authenticated
    @Operation(
            summary = "Thêm user vào dự án",
            description = "API dùng để thêm user vào dự án."
    )
    public ResponseApi<?> addUserProject(
            @PathVariable Long projectId,
            @RequestBody List<UserProjectRequest> requests
    ) {
        workItemService.addUserProject(projectId, requests);
        return ResponseApi.noContent();
    }

    // xóa user
    @DeleteMapping({"/project/user/{memberId}", "/projects/users/{memberId}"})
    @Authenticated
    @Operation(
            summary = "Xóa user trong dự án",
            description = "API dùng để xóa user trong dự án (project, module)."
    )
    public ResponseApi<?> deleteUserProject(
            @PathVariable Long memberId
    ) {
        entityMemberService.deleteUserOfProject(memberId);
        return ResponseApi.noContent();
    }

    //sửa role user
    @PutMapping({"/project/user/{memberId}", "/projects/users/{memberId}"})
    @Authenticated
    @Operation(
            summary = "Sửa role của user",
            description = "API dùng để sửa role user trong dự án."
    )
    public ResponseApi<?> editRoleUserProject(
            @PathVariable Long memberId,
            @RequestBody EditRoleUserRequest requests
    ) {
        entityMemberService.editRoleUser(memberId, requests);
        return ResponseApi.noContent();
    }

    //chinh sua du an
    @PutMapping(path = {"/project/{id}", "/projects/{id}"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Chỉnh sửa dự án",
            description = "API dùng để thay đổi thông tin dự án."
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

    // delete
    @DeleteMapping(path = {"/project/{id}", "/projects/{id}"})
    @Authenticated
    @Operation(
            summary = "Đóng dự án",
            description = "API dùng để đóng dự án(xóa dự án, update status)."
    )
    public ResponseApi<?> delete(
            @PathVariable Long id
    ) {
        workItemService.deleteWork(id, WorkItemType.PROJECT);
        return ResponseApi.noContent();
    }

    // từ chối duyệt
    @PostMapping({"/project/{projectId}/refuse", "/projects/{projectId}/refuse"})
    @Authenticated
    @Operation(
            summary = "Từ chối duyệt dự án",
            description = "API dùng để từ chối duyệt một dự án."
    )
    public ResponseApi<?> refuse(
            @PathVariable Long projectId,
            @RequestBody NoteRequest request
    ) {
        workItemService.refuse(projectId, request, WorkItemType.PROJECT);
        return ResponseApi.noContent();
    }

}

