package com.intern.hub.pm.dto.request;

import com.intern.hub.pm.enums.WorkItemType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkFilterRequest {

    private String name;
    private String assignee;
    private String status;
    private String statusNot;
    private LocalDate startDate;
    private LocalDate endDate;
    private WorkItemType type; // PROJECT | TASK
    private Long parentId;
}

