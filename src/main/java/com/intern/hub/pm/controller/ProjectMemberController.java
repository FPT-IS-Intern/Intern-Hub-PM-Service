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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix:/pm}/projects")
@Tag(name = "Thành viên dự án", description = "Các thao tác quản lý thành viên trong dự án")
@SecurityRequirement(name = "Bearer")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @PostMapping("/{projectId}/users")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Thêm thành viên dự án", description = "Thêm mới một thành viên đang hoạt động vào dự án.")
    public ProjectMemberResponse addMember(@PathVariable Long projectId,
                                           @Valid @RequestBody ProjectMemberCreateRequest request) {
        return projectMemberService.addMember(projectId, request);
    }

    @GetMapping("/{projectId}/users")
    @Operation(summary = "Lấy danh sách thành viên dự án", description = "Trả về danh sách thành viên đang hoạt động trong dự án.")
    public List<ProjectMemberResponse> getMembers(@PathVariable Long projectId) {
        return projectMemberService.getMembers(projectId);
    }

    @PutMapping("/users/{memberId}")
    @Operation(summary = "Cập nhật thành viên dự án", description = "Cập nhật vai trò của thành viên đang hoạt động trong dự án.")
    public ProjectMemberResponse updateMember(@PathVariable Long memberId,
                                              @Valid @RequestBody ProjectMemberUpdateRequest request) {
        return projectMemberService.updateMember(memberId, request);
    }

    @DeleteMapping("/users/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Xóa thành viên dự án", description = "Xóa mềm thành viên dự án bằng cách đổi trạng thái sang DELETED.")
    public void deleteMember(@PathVariable Long memberId) {
        projectMemberService.deleteMember(memberId);
    }
}
