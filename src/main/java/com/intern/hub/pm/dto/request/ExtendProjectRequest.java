package com.intern.hub.pm.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExtendProjectRequest {
    private LocalDateTime newEndDate;
    private String reason;
}
