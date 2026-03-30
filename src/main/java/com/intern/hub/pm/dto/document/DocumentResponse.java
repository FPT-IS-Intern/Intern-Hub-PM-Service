package com.intern.hub.pm.dto.document;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record DocumentResponse(
        @JsonSerialize(using = ToStringSerializer.class)
        Long id,
        String fileName,
        String fileUrl,
        Long createdAt
) {
}
