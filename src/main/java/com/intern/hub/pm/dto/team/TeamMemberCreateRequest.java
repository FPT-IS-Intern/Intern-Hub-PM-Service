package com.intern.hub.pm.dto.team;

import jakarta.validation.constraints.NotNull;

public record TeamMemberCreateRequest(
        @NotNull Long userId,
        String role
) {
}
