package com.intern.hub.pm.dtos.response;

import com.intern.hub.pm.enums.StatusWork;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@JsonPropertyOrder({
        "id",
        "workItemUuid",
        "creator",
        "assignee",
        "name",
        "description",
        "startDate",
        "endDate",
        "status",
        "result",
        "resultLink",
        "note",
        "createdAt",
        "updatedAt"
})
@Data
@Builder
public class TaskDetailResponse {

    private Long id;
    private String workItemUuid;
    private String creator;
    private String assignee;
    private String name;
    private String description;
    private StatusWork status;
    private String result;
    private String resultLink;
    private String note;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
