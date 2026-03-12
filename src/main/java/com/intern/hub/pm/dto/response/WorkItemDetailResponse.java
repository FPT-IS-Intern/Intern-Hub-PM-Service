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
        "budgetPoint",
        "rewardPoint",
        "reclaimedPoint",
        "bonusPoint",
        "result",
        "resultLink",
        "note",
        "extensionReason",
        "documents",
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
    private Long budgetPoint;
    private Long rewardPoint;
    private Long reclaimedPoint;
    private Long bonusPoint;
    private String result;
    private String resultLink;
    private String note;
    private String extensionReason;
    private java.util.List<DocumentResponse> documents;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

