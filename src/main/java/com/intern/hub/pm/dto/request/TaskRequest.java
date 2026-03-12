package com.intern.hub.pm.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskRequest {

    private Long assigneeId;
    private String taskName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

