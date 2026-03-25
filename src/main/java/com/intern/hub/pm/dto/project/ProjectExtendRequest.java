package com.intern.hub.pm.dto.project;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ProjectExtendRequest(
        @NotNull LocalDateTime endDate
) {
}
