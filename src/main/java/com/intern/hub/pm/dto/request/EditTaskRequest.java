package com.intern.hub.pm.dto.request;

import com.intern.hub.pm.enums.StatusWork;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EditTaskRequest {
    private Long assigneeId;
    private String name;
    private String description;
    private StatusWork status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

