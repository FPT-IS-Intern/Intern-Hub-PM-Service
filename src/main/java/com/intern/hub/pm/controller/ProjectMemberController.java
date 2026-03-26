package com.intern.hub.pm.controller;

import com.intern.hub.pm.dto.project.member.ProjectMemberCreateRequest;
import com.intern.hub.pm.dto.project.member.ProjectMemberResponse;
import com.intern.hub.pm.dto.project.member.ProjectMemberUpdateRequest;
import com.intern.hub.pm.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.library.common.dto.ResponseApi;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/projects")
@Tag(name = "Thành viên dự án", description = "Các thao tác quản lý thành viên trong dự án")
@SecurityRequirement(name = "Bearer")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @PostMapping("/{projectId}/users")
    @Operation(summary = "Thêm thành viên dự án", description = "Thêm mới một thành viên đang hoạt động vào dự án.")
    public ResponseApi<ProjectMemberResponse> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectMemberCreateRequest request) {
        return ResponseApi.ok(projectMemberService.addMember(projectId, request));
    }

    @GetMapping("/{projectId}/users")
    @Operation(summary = "Lấy danh sách thành viên dự án", description = "Trả về danh sách thành viên trong dự án có phân trang.")
    public ResponseApi<PaginatedData<ProjectMemberResponse>> getMembers(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseApi.ok(projectMemberService.getMembers(projectId, page, size));
    }

    @PutMapping("/users/{memberId}")
    @Operation(summary = "Cập nhật thành viên dự án", description = "Cập nhật vai trò của thành viên trong dự án.")
    public ResponseApi<ProjectMemberResponse> updateMember(@PathVariable Long memberId,
                                                           @Valid @RequestBody ProjectMemberUpdateRequest request) {
        return ResponseApi.ok(projectMemberService.updateMember(memberId, request));
    }

    @DeleteMapping("/users/{memberId}")
    @Operation(summary = "Xóa thành viên dự án", description = "Xóa mềm thành viên dự án bằng cách đổi trạng thái sang DELETED.")
    public ResponseApi<?> deleteMember(@PathVariable Long memberId) {
        projectMemberService.deleteMember(memberId);
        return ResponseApi.noContent();
    }
}
