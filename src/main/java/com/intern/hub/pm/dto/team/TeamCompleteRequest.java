package com.intern.hub.pm.dto.team;

public record TeamCompleteRequest(
        String completionComment,
        String deliverableDescription,
        String deliverableLink
) {
}
