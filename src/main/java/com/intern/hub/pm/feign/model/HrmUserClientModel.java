package com.intern.hub.pm.feign.model;

public record HrmUserClientModel(
        Long userId,
        String email,
        String fullName,
        String avatarUrl,
        String roleId
) {
}
