package com.intern.hub.pm.controller;

import com.intern.hub.pm.dtos.request.*;
import com.intern.hub.pm.dtos.response.*;
import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.exceptions.NotFoundException;
import com.intern.hub.pm.service.impl.EntityMemberService;
import com.intern.hub.pm.service.impl.WorkItemService;
import com.intern.hub.starter.security.annotation.Authenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> getProjects(
            WorkFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        filter.setType(WorkItemType.PROJECT);
        filter.setStatusNot(String.valueOf(StatusWork.DA_XOA));
        Page<WorkItemResponse> pageResult = workItemService.getAll(filter, page, size);
        return ApiResponseBuilder.success(
                "Danh sách project",
                toPageResponse(pageResult));
    }

    @PostMapping(path = "/project", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Tạo dự án",
            description = "API dùng để tạo dự án."
    )
    public ResponseEntity<?> create(
            @RequestPart("project") WorkItemRequest projectJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        String username = UserContext.requiredEmail();

        workItemService.createProject(projectJson, username);
        if (files != null && !files.isEmpty()) {
            List<MultipartFile> file2 = files;
        }
        return ApiResponseBuilder.success("Tạo dự án thành công", null);
    }

    // list user trong dự án (để hiển thị trong module )
    @GetMapping(path = "/project/{projectId}/users")
    @Authenticated
    @Operation(
            summary = "Danh sách user của dự án",
            description = "API dùng để lấy ra danh sách user của một dự án (để hiển thị trong module)."
    )
    public ResponseEntity<?> getUserOfProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProjectUserResponse> pageResult =
                entityMemberService.projectUserList(projectId, WorkItemType.PROJECT, page, size);
        return ApiResponseBuilder.success(
                "Danh sách thành viên trong dự án",
                toPageResponse(pageResult)
        );
    }

    //detail, thêm tran với type của work
    @GetMapping("/project/{id}/detail")
    @Authenticated
    @Operation(
            summary = "Chi tiết dự án",
            description = "API dùng để xem chi tiết dự án (dự án, module)."
    )
    public ResponseEntity<?> detailProject(@PathVariable Long id) {
        WorkItemDetailResponse response = workItemService.workItemDetailResponse(id);
        return ApiResponseBuilder.success("Chi tiết dự án", response);
    }

    //add user vào pro
    @PostMapping("/project/{projectId}/users")
    @Authenticated
    @Operation(
            summary = "Thêm user vào dự án",
            description = "API dùng để thêm user vào dự án."
    )
    public ResponseEntity<?> addUserProject(
            @PathVariable Long projectId,
            @RequestBody List<UserProjectRequest> requests
    ) {
        workItemService.addUserProject(projectId, requests);
        return ApiResponseBuilder.success("Thêm thành viên thành công", null);
    }

    // xóa user
    @DeleteMapping("/project/user/{memberId}")
    @Authenticated
    @Operation(
            summary = "Xóa user trong dự án",
            description = "API dùng để xóa user trong dự án (project, module)."
    )
    public ResponseEntity<?> deleteUserProject(
            @PathVariable Long memberId
    ) throws NotFoundException {
        entityMemberService.deleteUserOfProject(memberId);
        return ApiResponseBuilder.success("Xóa thành viên thành công", null);
    }

    //sửa role user
    @PutMapping("/project/user/{memberId}")
    @Authenticated
    @Operation(
            summary = "Sửa role của user",
            description = "API dùng để sửa role user trong dự án."
    )
    public ResponseEntity<?> editRoleUserProject(
            @PathVariable Long memberId,
            @RequestBody EditRoleUserRequest requests
    ) {
        entityMemberService.editRoleUser(memberId, requests);
        return ApiResponseBuilder.success("Sửa role user thành công", null);
    }

    //chinh sua du an
    @PutMapping(path = "/project/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Chỉnh sửa dự án",
            description = "API dùng để thay đổi thông tin dự án."
    )
    public ResponseEntity<?> edit(
            @PathVariable Long id,
            @RequestPart("project") WorkItemRequest projectJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        workItemService.editProject(id, projectJson);
        if (files != null && !files.isEmpty()) {
            List<MultipartFile> file2 = files;
        }
        return ApiResponseBuilder.success("Sửa dự án thành công", null);
    }

    // delete
    @DeleteMapping(path = "/project/{id}")
    @Authenticated
    @Operation(
            summary = "Đóng dự án",
            description = "API dùng để đóng dự án(xóa dự án, update status)."
    )
    public ResponseEntity<?> delete(
            @PathVariable Long id
    ) {
        workItemService.deleteWork(id, WorkItemType.PROJECT);
        return ApiResponseBuilder.success("Xóa dự án thành công", null);
    }

    // từ chối duyệt
    @PostMapping("/project/{projectId}/refuse")
    @Authenticated
    @Operation(
            summary = "Từ chối duyệt dự án",
            description = "API dùng để từ chối duyệt một dự án."
    )
    public ResponseEntity<?> refuse(
            @PathVariable Long projectId,
            @RequestBody NoteRequest request
    ) {
        workItemService.refuse(projectId, request, WorkItemType.PROJECT);
        return ApiResponseBuilder.success("Từ chối duyệt dự án thành công", null);
    }

}
