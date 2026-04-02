package com.intern.hub.pm.controller;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.library.common.exception.ForbiddenException;
import com.intern.hub.pm.dto.project.ApproveRequest;
import com.intern.hub.pm.dto.task.TaskResponse;
import com.intern.hub.pm.dto.team.TeamCompleteRequest;
import com.intern.hub.pm.dto.team.TeamResponse;
import com.intern.hub.pm.dto.team.TeamUpsertRequest;
import com.intern.hub.pm.dto.team.TeamFilterRequest;
import com.intern.hub.pm.dto.team.TeamStatisticsResponse;
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
@Tag(name = "Team", description = "Các thao tác quản lý dự án của team")
@SecurityRequirement(name = "Bearer")
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    @Operation(summary = "Lấy danh sách Team", description = "Danh sách dự án của team có phân trang và lọc.")
    public ResponseApi<PaginatedData<TeamResponse>> getTeams(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) com.intern.hub.pm.model.constant.StatusWork status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        TeamFilterRequest filter = TeamFilterRequest.builder()
                .projectId(projectId)
                .name(name)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        return ResponseApi.ok(teamService.getTeams(filter, page, size));
    }

    @GetMapping("/my-teams")
    @Operation(summary = "Lấy danh sách team của tôi", description = "Trả về danh sách team mà user đang đăng nhập là thành viên hoặc leader")
    public ResponseApi<PaginatedData<TeamResponse>> getMyTeams(
            @RequestParam Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseApi.ok(teamService.getMyTeams(projectId, page, size));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Lấy thống kê team", description = "Trả về số lượng team theo từng trạng thái.")
    public ResponseApi<TeamStatisticsResponse> getTeamStatistics(@RequestParam(required = false) Long projectId) {
        return ResponseApi.ok(teamService.getTeamStatistics(projectId));
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "Chi tiết Team", description = "Thông tin chi tiết Team")
    public ResponseApi<TeamResponse> getTeam(@PathVariable Long teamId) {
        return ResponseApi.ok(teamService.getTeam(teamId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo Team", description = "Tạo Team")
    @Authenticated
    public ResponseApi<TeamResponse> createTeam(
            @Valid @RequestPart("request") TeamUpsertRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        AuthContext context = AuthContextHolder.get()
                .orElseThrow(() -> new ForbiddenException(
                        "Không tìm thấy thông tin xác thực)."));
        return ResponseApi.ok(teamService.createTeam(context.userId(), request, files));
    }

    @PutMapping(value = "/{teamId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật team", description = "Cập nhật thông tin của Team ")
    @Authenticated
    public ResponseApi<TeamResponse> updateTeam(
            @PathVariable Long teamId,
            @Valid @RequestPart("request") TeamUpsertRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseApi.ok(teamService.updateTeam(teamId, request, files));
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "Hủy Team", description = "Hủy Team.")
    public ResponseApi<?> deleteTeam(@PathVariable Long teamId) {
        teamService.deleteTeam(teamId);
        return ResponseApi.noContent();
    }

    @PostMapping(value = "/{teamId}/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Nộp đán án Team", description = "Nộp đáp án và chuyển trạng thái của dự án sang chờ duyệt.")
    public ResponseApi<TeamResponse> completeTeam(
            @PathVariable Long teamId,
            @Valid @RequestPart("request") TeamCompleteRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseApi.ok(teamService.completeTeam(teamId, request, files));
    }

    @PostMapping("/{teamId}/approve")
    @Operation(summary = "Duyệt team", description = "Duyệt Team cấp token.")
    public ResponseApi<TeamResponse> approveTeam(
            @PathVariable Long teamId,
            @Valid @RequestBody ApproveRequest request) {
        return ResponseApi.ok(teamService.approveTeam(teamId, request));
    }

    @PostMapping("/{teamId}/accept")
    @Operation(summary = "Nhận Team", description = "Nhận Team để làm (cấp ngân sách).")
    public ResponseApi<TeamResponse> acceptTeam(
            @PathVariable Long teamId) {
        return ResponseApi.ok(teamService.acceptTeam(teamId));
    }

    @PostMapping("/{teamId}/reject")
    @Operation(summary = "Từ chối nhận team", description = "User từ chối nhận team được giao (trạng thái sẽ chuyển sang REJECTED).")
    public ResponseApi<TeamResponse> refuseToAccept(
            @PathVariable Long teamId) {
        return ResponseApi.ok(teamService.refuseTask(teamId));
    }

    @PostMapping("/{teamId}/refuse")
    @Operation(summary = "Yêu cầu làm lại team", description = "Từ chối và yêu cầu chỉnh sửa dự án của team khi đang ở trạng thái chờ duyệt.")
    public ResponseApi<TeamResponse> refuseTeam(
            @PathVariable Long teamId,
            @Valid @RequestBody ApproveRequest request) {
        return ResponseApi.ok(teamService.refuseTeam(teamId, request));
    }
}
