package com.intern.hub.pm.controller;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.library.common.exception.ForbiddenException;
import com.intern.hub.pm.dto.project.ApproveRequest;
import com.intern.hub.pm.dto.team.TeamCompleteRequest;
import com.intern.hub.pm.dto.team.TeamResponse;
import com.intern.hub.pm.dto.team.TeamUpsertRequest;
import com.intern.hub.pm.service.TeamService;
import com.intern.hub.starter.security.annotation.Authenticated;
import com.intern.hub.starter.security.context.AuthContext;
import com.intern.hub.starter.security.context.AuthContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/teams")
@Tag(name = "Dự án team", description = "Các thao tác quản lý dự án của team")
@SecurityRequirement(name = "Bearer")
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    @Operation(summary = "Lấy danh sách dự án team", description = "Danh sách dự án của team.")
    public ResponseApi<PaginatedData<TeamResponse>> getTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseApi.ok(teamService.getTeams(page, size));
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "Chi tiết dự án team", description = "Thông tin chi tiết dự án team")
    public ResponseApi<TeamResponse> getTeam(@PathVariable Long teamId) {
        return ResponseApi.ok(teamService.getTeam(teamId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo dự án team", description = "Tạo dự án team")
    @Authenticated
    public ResponseApi<TeamResponse> createTeam(
            @Valid @RequestPart("request") TeamUpsertRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        AuthContext context = AuthContextHolder.get()
                .orElseThrow(() -> new ForbiddenException("Khong tim thay thong tin xac thuc. Vui long kiem tra lai token hoac cac header (X-UserId, X-Authenticated)."));
        return ResponseApi.ok(teamService.createTeam(context.userId(), request, files));
    }

    @PutMapping(value = "/{teamId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật team", description = "Cập nhật thông tin của dự án team ")
    @Authenticated
    public ResponseApi<TeamResponse> updateTeam(
            @PathVariable Long teamId,
            @Valid @RequestPart("request") TeamUpsertRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseApi.ok(teamService.updateTeam(teamId, request, files));
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "Hủy dự án team", description = "Hủy dự án team.")
    public ResponseApi<?> deleteTeam(@PathVariable Long teamId) {
        teamService.deleteTeam(teamId);
        return ResponseApi.noContent();
    }

    @PostMapping("/{teamId}/complete")
    @Operation(summary = "Nộp đán án dự án team", description = "Nộp đáp án và chuyển trạng thái của dự án sang chờ duyệt.")
    public ResponseApi<TeamResponse> completeTeam(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamCompleteRequest request
    ) {
        return ResponseApi.ok(teamService.completeTeam(teamId, request));
    }

    @PostMapping("/{teamId}/approve")
    @Operation(summary = "Duyệt team", description = "Duyệt dự án team cấp token.")
    public ResponseApi<TeamResponse> approveTeam(
            @PathVariable Long teamId,
            @Valid @RequestBody ApproveRequest request
    ) {
        return ResponseApi.ok(teamService.approveTeam(teamId, request));
    }
}
