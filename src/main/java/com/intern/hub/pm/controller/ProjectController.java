package com.intern.hub.pm.controller;

import com.intern.hub.pm.dto.project.ProjectResponse;
import com.intern.hub.pm.dto.project.ProjectCompleteRequest;
import com.intern.hub.pm.dto.project.ProjectExtendRequest;
import com.intern.hub.pm.dto.project.ProjectUpsertRequest;
import com.intern.hub.pm.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix:/pm}/projects")
@Tag(name = "Dự án", description = "Các thao tác quản lý dự án")
@SecurityRequirement(name = "Bearer")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Lấy danh sách dự án", description = "Trả về danh sách dự án đang hoạt động.")
    public List<ProjectResponse> getProjects() {
        return projectService.getProjects();
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Lấy chi tiết dự án", description = "Trả về thông tin chi tiết của dự án theo id.")
    public ProjectResponse getProject(@PathVariable Long projectId) {
        return projectService.getProject(projectId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo dự án", description = "Tạo mới dự án và có thể đính kèm tài liệu charter.")
    public ProjectResponse createProject(@Valid @RequestPart("request") ProjectUpsertRequest request,
                                         @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return projectService.createProject(request, files);
    }

    @PutMapping(value = "/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật dự án", description = "Cập nhật thông tin dự án và thay thế bộ tài liệu charter nếu có file mới.")
    public ProjectResponse updateProject(@PathVariable Long projectId,
                                         @Valid @RequestPart("request") ProjectUpsertRequest request,
                                         @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return projectService.updateProject(projectId, request, files);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Hủy dự án", description = "Hủy mềm dự án bằng cách chuyển trạng thái sang CANCELED.")
    public void deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
    }

    @PostMapping("/{projectId}/extend")
    @Operation(summary = "Gia hạn dự án", description = "Cập nhật thời gian kết thúc và lưu lý do gia hạn dự án.")
    public ProjectResponse extendProject(@PathVariable Long projectId,
                                         @Valid @RequestBody ProjectExtendRequest request) {
        return projectService.extendProject(projectId, request);
    }

    @PostMapping("/{projectId}/complete")
    @Operation(summary = "Hoàn thành dự án", description = "Đánh dấu dự án hoàn thành khi không còn task nào đang chờ duyệt.")
    public ProjectResponse completeProject(@PathVariable Long projectId,
                                           @Valid @RequestBody ProjectCompleteRequest request) {
        return projectService.completeProject(projectId, request);
    }
}
