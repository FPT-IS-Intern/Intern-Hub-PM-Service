package com.intern.hub.pm.dto.project;

import jakarta.validation.constraints.Min;

public record ProjectCompleteRequest(
        String completionComment,
        @Min(0) Long recoveredToken,
        @Min(0) Long bonusToken
) {
}
