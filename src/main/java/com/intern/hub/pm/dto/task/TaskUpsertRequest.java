package com.intern.hub.pm.dto.task;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskUpsertRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @Min(0) Long rewardToken,
        @NotNull Long assigneeId
) {
}
