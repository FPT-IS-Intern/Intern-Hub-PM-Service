package com.intern.hub.pm.dto.task;

import jakarta.validation.constraints.Min;

public record TaskReviewRequest(
        String reviewComment
) {
}
