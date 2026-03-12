package com.intern.hub.pm.dto.response;

import com.intern.hub.pm.enums.StatusWork;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@JsonPropertyOrder({
        "id",
        "workItemUuid",
        "creator",
        "assignee",
        "name",
        "description",
        "status",
        "result",
        "resultLink",
        "note",
        "startDate",
        "endDate",
        "createdAt",
        "updatedAt"
})
@Getter
@Setter
@Builder
public class WorkItemDetailResponse {

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

