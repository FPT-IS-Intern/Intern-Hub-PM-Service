package com.intern.hub.pm.controller;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.pm.dto.team.TeamMemberCreateRequest;
import com.intern.hub.pm.dto.team.TeamMemberResponse;
import com.intern.hub.pm.service.TeamMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/teams")
@Tag(name = "Thành viên team", description = "Các thao tác quản lý thành viên trong Team")
@SecurityRequirement(name = "Bearer")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @PostMapping("/{teamId}/users")
    @Operation(summary = "Thêm thành viên vào team")
    public ResponseApi<List<TeamMemberResponse>> addMembers(
            @PathVariable Long teamId,
            @Valid @RequestBody List<TeamMemberCreateRequest> requests) {
        return ResponseApi.ok(teamMemberService.addMembers(teamId, requests));
    }

    @GetMapping("/{teamId}/users")
    @Operation(summary = "Lấy danh sách và tìm kiếm thành viên trong team")
    public ResponseApi<PaginatedData<TeamMemberResponse>> getMembers(
            @PathVariable Long teamId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedData<TeamMemberResponse> response = teamMemberService.getMembers(teamId, keyword, page, size);
        if (response != null && response.getItems() != null) {
            response.getItems().forEach(m -> System.out
                    .println("  - Member: " + m.getFullName() + ", userId: " + m.getUserId() + ", id: " + m.getId()));
        }
        return ResponseApi.ok(response);
    }

    @DeleteMapping("/users/{memberId}")
    @Operation(summary = "Xóa thành viên khỏi team")
    public ResponseApi<?> deleteMember(@PathVariable Long memberId) {
        teamMemberService.deleteMember(memberId);
        return ResponseApi.noContent();
    }
}
