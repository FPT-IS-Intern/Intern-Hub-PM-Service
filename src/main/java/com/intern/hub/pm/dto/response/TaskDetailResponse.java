package com.intern.hub.pm.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.intern.hub.pm.enums.StatusWork;
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
        "budgetPoint",
        "rewardPoint",
        "reclaimedPoint",
        "result",
        "resultLink",
        "note",
        "guideDocuments",
        "submissionDocuments",
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
    private Long budgetPoint;
    private Long rewardPoint;
    private Long reclaimedPoint;
    private String result;
    private String resultLink;
    private String note;
    private java.util.List<DocumentResponse> guideDocuments;
    private java.util.List<DocumentResponse> submissionDocuments;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

