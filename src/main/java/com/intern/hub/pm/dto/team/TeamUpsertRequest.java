package com.intern.hub.pm.dto.team;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TeamUpsertRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @Min(0) Long budgetToken,
        @NotNull @Min(0) Long rewardToken,
        @NotNull Long assigneeId,
        @NotNull Long projectId,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
