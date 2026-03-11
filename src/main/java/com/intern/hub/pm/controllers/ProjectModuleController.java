package com.intern.hub.pm.controllers;

import com.intern.hub.pm.dtos.request.*;
import com.intern.hub.pm.dtos.response.ApiResponseBuilder;
import com.intern.hub.pm.dtos.response.PageResponse;
import com.intern.hub.pm.dtos.response.ProjectUserResponse;
import com.intern.hub.pm.dtos.response.WorkItemResponse;
import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.services.*;
import com.intern.hub.pm.utils.UserContext;
import com.intern.hub.starter.security.annotation.Authenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@SecurityRequirement(name = "bearerAuth")
public class ProjectModuleController {

    private final EntityMemberService entityMemberService;
    private final WorkItemService workItemService;


    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @PostMapping(path ="/project/{projectId}/modules",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Tạo project module",
            description = "API dùng để tạo module."
    )
    public ResponseEntity<?> createModule(
            @PathVariable Long projectId,
            @RequestPart("project") WorkItemRequest projectJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ){
        String username = UserContext.requiredEmail();

        workItemService.createModule(projectId, projectJson, username);
        if (files != null && !files.isEmpty()) {
            List<MultipartFile> file2 = files;
        }
        return ApiResponseBuilder.success("Tạo module thành công", null);
    }

    @GetMapping("/project/{projectId}/modules")
    @Authenticated
    @Operation(
            summary = "Danh sách module",
            description = "API danh sách các module trong một dự án."
    )
    public ResponseEntity<?> getProjects(
            @PathVariable Long projectId,
            WorkFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        filter.setType(WorkItemType.MODULE);
        filter.setParentId(projectId);
        filter.setStatusNot(String.valueOf(StatusWork.DA_XOA));
        Page<WorkItemResponse> pageResult = workItemService.getAll(filter, page, size);
        return ApiResponseBuilder.success(
                "Danh sách project module",
                toPageResponse(pageResult));
    }

    @GetMapping(path = "/module/{moduleId}/users")
    @Authenticated
    @Operation(
            summary = "Danh sách user của module",
            description = "API dùng để lấy ra danh sách user của một module (để hiển thị trong create task)."
    )
    public ResponseEntity<?> getUserOfModule(
            @PathVariable Long moduleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<ProjectUserResponse> pageResult =
                entityMemberService.projectUserList(moduleId, WorkItemType.MODULE, page, size);

        return ApiResponseBuilder.success(
                "Danh sách thành viên trong module",
                toPageResponse(pageResult)
        );
    }

    //add user vào module
    @PostMapping("/module/{moduleId}/users")
    @Authenticated
    @Operation(
            summary = "Thêm user vào module",
            description = "API dùng để thêm user vào module."
    )
    public ResponseEntity<?> addUserProject(
            @PathVariable Long moduleId,
            @RequestBody List<UserProjectRequest> requests
    ) {
        workItemService.addUserModule(moduleId, requests);
        return ApiResponseBuilder.success("Thêm thành viên thành công", null);
    }

    //chinh sua module// sủa lại chỗ save cho 1 hàm thôi
    @PutMapping(path ="/module/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Chỉnh sửa module",
            description = "API dùng để thay đổi thông tin module."
    )
    public ResponseEntity<?> edit(
            @PathVariable Long id,
            @RequestPart("project") WorkItemRequest projectJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ){
        workItemService.editProject(id,projectJson);
        if (files != null && !files.isEmpty()) {
            List<MultipartFile> file2 = files;
        }
        return ApiResponseBuilder.success("Sửa module thành công", null);
    }

    //nộp module
    @PostMapping(path = "/module/{moduleId}/submit",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Authenticated
    @Operation(
            summary = "Nộp module",
            description = "API dùng để user nộp đáp án module."
    )
    public ResponseEntity<?> submitTask(
            @PathVariable Long moduleId,
            @RequestPart("result") SubmitTaskRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ){
        workItemService.submit(moduleId,request,WorkItemType.MODULE);
        return ApiResponseBuilder.success("Nộp module thành công", null);
    }

    // từ chối duyệt
    @PostMapping("/module/{moduleId}/refuse")
    @Authenticated
    @Operation(
            summary = "Từ chối duyệt module",
            description = "API dùng để từ chối duyệt một module."
    )
    public ResponseEntity<?> refuse(
            @PathVariable Long moduleId,
            @RequestBody NoteRequest request
    ){
        workItemService.refuse(moduleId,request,WorkItemType.MODULE);
        return ApiResponseBuilder.success("Từ chối duyệt module thành công", null);
    }

    @DeleteMapping(path ="/module/{id}")
    @Authenticated
    @Operation(
            summary = "Đóng module",
            description = "API dùng để đóng module(xóa module, update status)."
    )
    public ResponseEntity<?> delete(
            @PathVariable Long id
    ){
        workItemService.deleteWork(id, WorkItemType.MODULE);
        return ApiResponseBuilder.success("Xóa module án thành công", null);
    }
    // duyệt tk, detail, thêm user, task của user
    // kiểm tra ví, hiển thêm transsaction ở detail
    //xem lại token ở sửa task
    // thêm số thành viên trong 1 dự án/module trong load dự an
}
