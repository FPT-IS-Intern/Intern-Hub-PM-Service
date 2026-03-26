package com.intern.hub.pm.dto.project.member;

import com.intern.hub.pm.model.constant.Status;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record ProjectMemberResponse(
        @JsonSerialize(using = ToStringSerializer.class) Long id,
        @JsonSerialize(using = ToStringSerializer.class) Long projectId,
        @JsonSerialize(using = ToStringSerializer.class) Long userId,
        String fullName,
        String email,
        Long countProjectTeam,
        String role,
        Status status,
        Long createdAt,
        Long updatedAt
) {
}
