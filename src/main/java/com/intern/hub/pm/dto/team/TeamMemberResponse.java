package com.intern.hub.pm.dto.team;

import com.intern.hub.pm.model.constant.Status;

public record TeamMemberResponse(
        Long id,
        Long userId,
        String fullName,
        String email,
        Status status
) {
}
