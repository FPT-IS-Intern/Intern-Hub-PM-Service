package com.intern.hub.pm.utils;

import com.intern.hub.starter.security.context.AuthContext;
import com.intern.hub.starter.security.context.AuthContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

public final class UserContext {

    private UserContext() {
    }

    public static AuthContext current() {
        try {
            return AuthContextHolder.get().orElse(AuthContext.UNAUTHENTICATED_CONTEXT);
        } catch (IllegalStateException ex) {
            return AuthContext.UNAUTHENTICATED_CONTEXT;
        }
    }

    public static boolean isAuthenticated() {
        return current().authenticated();
    }

    public static boolean isInternal() {
        return current().internal();
    }

    public static Optional<Long> userId() {
        return Optional.ofNullable(current().userId());
    }

    public static Long requiredUserId() {
        return userId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));
    }

    public static Set<String> authorities() {
        return current().permissions();
    }
}
