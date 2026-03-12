package com.intern.hub.pm.dto.response;

import lombok.Builder;

@Builder
public record DocumentResponse(
        Long id,
        String fileName,
        String fileUrl
) {
}
