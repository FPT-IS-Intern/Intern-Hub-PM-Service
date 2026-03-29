package com.intern.hub.pm.dto.task;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskUpsertRequest(
        @NotBlank String name,
        String description,
        @NotNull @Min(0) Long rewardToken,
        @NotNull Long assigneeId,
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate
) {
}
