package com.intern.hub.pm.dtos.response;

import com.intern.hub.pm.enums.StatusWork;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@JsonPropertyOrder({
        "id",
        "nameTask",
        "description",
        "startDate",
        "endDate",
        "status",
        "createdAt",
        "updatedAt"
})
@Data
@Builder
public class TaskResponse {
    private Long id;
    private String nameTask;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private StatusWork status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
