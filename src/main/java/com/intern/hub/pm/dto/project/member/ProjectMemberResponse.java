package com.intern.hub.pm.dto.project.member;

import com.intern.hub.pm.model.constant.Status;

public record ProjectMemberResponse(
        Long id,
        Long projectId,
        Long userId,
        String role,
        Status status,
        Long createdAt,
        Long updatedAt
) {
}
