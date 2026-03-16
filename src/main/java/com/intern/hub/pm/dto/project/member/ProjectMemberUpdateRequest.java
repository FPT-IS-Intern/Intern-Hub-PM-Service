package com.intern.hub.pm.dto.project.member;

import jakarta.validation.constraints.NotBlank;

public record ProjectMemberUpdateRequest(
        @NotBlank String role
) {
}
