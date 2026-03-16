package com.intern.hub.pm.dto.project.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectMemberCreateRequest(
        @NotNull Long userId,
        @NotBlank String role
) {
}
