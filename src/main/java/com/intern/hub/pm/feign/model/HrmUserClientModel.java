package com.intern.hub.pm.feign.model;

import lombok.Data;
import lombok.Getter;

public record HrmUserClientModel(
        Long userId,
        String email,
        String fullName,
        String avatarUrl,
        String roleId
) {
}
