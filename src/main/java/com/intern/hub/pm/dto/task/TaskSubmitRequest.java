package com.intern.hub.pm.dto.task;

public record TaskSubmitRequest(
        String deliverableDescription,
        String deliverableLink
) {
}
