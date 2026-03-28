package com.intern.hub.pm.controller;

import com.intern.hub.pm.dto.team.TeamMemberResponse;
import com.intern.hub.pm.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/teams")
@Tag(name = "Thành viên team", description = "Các thao tác quản lý thành viên trong dự án team")
@SecurityRequirement(name = "Bearer")
public class TeamMemberController {

    private final TeamService teamService;

    @GetMapping("/{teamId}/members")
    @Operation(summary = "Lấy danh sách thành viên trong team")
    public List<TeamMemberResponse> getTeamMembers(@PathVariable Long teamId) {
        return teamService.getTeamMembers(teamId);
    }
}
