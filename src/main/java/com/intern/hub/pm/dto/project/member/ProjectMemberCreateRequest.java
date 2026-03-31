package com.intern.hub.pm.dto.project.member;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectMemberCreateRequest(
        @NotNull
        @JsonSerialize(using = ToStringSerializer.class)
        Long userId,
        @NotBlank String role
) {
}
