package com.intern.hub.pm.controller;

import com.intern.hub.library.common.exception.ForbiddenException;
import com.intern.hub.pm.dto.project.ProjectResponse;
import com.intern.hub.pm.dto.project.ProjectCompleteRequest;
import com.intern.hub.pm.dto.project.ProjectUpsertRequest;
import com.intern.hub.pm.service.ProjectService;
import com.intern.hub.starter.security.annotation.Authenticated;
import com.intern.hub.starter.security.context.AuthContext;
import com.intern.hub.starter.security.context.AuthContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.library.common.dto.ResponseApi;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/projects")
@Tag(name = "Dự án", description = "Các thao tác quản lý dự án")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Lấy danh sách dự án", description = "Trả về danh sách dự án có phân trang.")
    public ResponseApi<PaginatedData<ProjectResponse>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseApi.ok(projectService.getProjects(page, size));
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Lấy chi tiết dự án", description = "Trả về thông tin chi tiết của dự án theo id.")
    public ResponseApi<ProjectResponse> getProject(@PathVariable Long projectId) {
        return ResponseApi.ok(projectService.getProject(projectId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo dự án", description = "Tạo mới dự án và có thể đính kèm tài liệu charter.")
    @Authenticated
    public ResponseApi<ProjectResponse> createProject(
            @Valid @RequestPart("request") ProjectUpsertRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        AuthContext context = AuthContextHolder.get()
                .orElseThrow(() -> new ForbiddenException("Không tìm thấy thông tin xác thực. Vui lòng kiểm tra lại token hoặc các header (X-UserId, X-Authenticated)."));
        Long userId = context.userId();
//        Long userId = 159220116939083776L;
        return ResponseApi.ok(projectService.createProject(userId, request, files));
    }

    @PutMapping(value = "/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật dự án", description = "Cập nhật thông tin dự án và thay thế bộ tài liệu charter nếu có file mới.")
    @Authenticated
    public ResponseApi<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestPart("request") ProjectUpsertRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files)
    {
        return ResponseApi.ok(projectService.updateProject(projectId, request, files));
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Hủy dự án", description = "Hủy mềm dự án bằng cách chuyển trạng thái sang CANCELED.")
    public ResponseApi<?> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseApi.noContent();
    }

//    @PostMapping("/{projectId}/extend")
//    @Operation(summary = "Gia hạn dự án", description = "Cập nhật thời gian kết thúc và lưu lý do gia hạn dự án.")
//    public ProjectResponse extendProject(@PathVariable Long projectId,
//                                         @Valid @RequestBody ProjectExtendRequest request) {
//        return projectService.extendProject(projectId, request);
//    }

    @PostMapping("/{projectId}/complete")
    @Operation(summary = "Hoàn thành dự án", description = "Đánh dấu dự án hoàn thành khi không còn task nào đang chờ duyệt.")
    public ResponseApi<ProjectResponse> completeProject(@PathVariable Long projectId,
                                                        @Valid @RequestBody ProjectCompleteRequest request) {
        return ResponseApi.ok(projectService.completeProject(projectId, request));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        AuthContext context = AuthContextHolder.get().orElseThrow();
        Long userId = context.userId();
        return ResponseEntity.ok(userId);
    }
}
