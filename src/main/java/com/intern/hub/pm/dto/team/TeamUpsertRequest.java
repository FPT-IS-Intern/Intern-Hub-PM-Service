package com.intern.hub.pm.dto.team;

import com.intern.hub.pm.dto.project.member.ProjectMemberCreateRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record TeamUpsertRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @Min(0) Long budgetToken,
        @NotNull @Min(0) Long rewardToken,
        @NotNull Long assigneeId,
        @NotNull Long projectId,
        List<ProjectMemberCreateRequest> memberList,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
