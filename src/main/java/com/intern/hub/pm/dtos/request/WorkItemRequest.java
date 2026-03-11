package com.intern.hub.pm.dtos.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkItemRequest {
    private Long assigneeId;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<UserProjectRequest> userList;
}

