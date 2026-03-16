package com.intern.hub.pm.dto.document;

public record DocumentResponse(
        Long id,
        String fileName,
        String fileUrl
) {
}
