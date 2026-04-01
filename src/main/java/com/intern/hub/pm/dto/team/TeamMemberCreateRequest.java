package com.intern.hub.pm.dto.team;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;

public record TeamMemberCreateRequest(
        @NotNull
        @JsonSerialize(using = ToStringSerializer.class)
        Long userId
) {
}
