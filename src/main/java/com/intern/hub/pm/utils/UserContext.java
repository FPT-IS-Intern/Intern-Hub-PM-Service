package com.intern.hub.pm.utils;

import com.intern.hub.pm.exceptions.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class UserContext {

    private UserContext() {}

    public static String requiredEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ForbiddenException("Không thể xác định người dùng hiện tại");
        }
        return authentication.getName();
    }
}
