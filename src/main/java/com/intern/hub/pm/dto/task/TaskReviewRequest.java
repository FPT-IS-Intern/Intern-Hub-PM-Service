package com.intern.hub.pm.dto.task;

import jakarta.validation.constraints.Min;

public record TaskReviewRequest(
        String reviewComment,
        @Min(0) Long recoveredToken
) {
}
