package com.intern.hub.pm.dto.project;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectExtendRequest(
        @NotNull @Min(0) Long endAt,
        @NotBlank String reason
) {
}
