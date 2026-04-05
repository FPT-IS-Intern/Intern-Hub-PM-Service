package com.intern.hub.pm.dto.team;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

public record TeamUpsertRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @Min(0) BigInteger budgetToken,
        @NotNull @Min(0) BigInteger rewardToken,
        @NotNull Long assigneeId,
        @NotNull Long projectId,
        List<TeamMemberCreateRequest> memberList,
        LocalDateTime startDate,
        LocalDateTime endDate) {
}
