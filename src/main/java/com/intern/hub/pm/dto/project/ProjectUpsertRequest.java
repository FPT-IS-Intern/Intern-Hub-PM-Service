package com.intern.hub.pm.dto.project;

import com.intern.hub.pm.model.constant.StatusWork;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectUpsertRequest(
        @NotBlank String name,
        @NotBlank String description,
        String note,
        @NotNull StatusWork status,
        @NotNull @Min(0) Long budgetToken,
        @NotNull @Min(0) Long rewardToken,
        @NotNull Long assigneeId,
        String deliverableDescription,
        String deliverableLink,
        @Min(0) Long endAt
) {
}
