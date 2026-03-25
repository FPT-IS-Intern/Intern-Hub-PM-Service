package com.intern.hub.pm.dto.task;

import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.model.constant.StatusWork;

import java.util.List;

public record TaskResponse(
        Long id,
        Long projectId,
        String taskUUID,
        String name,
        String description,
        String note,
        StatusWork status,
        Long rewardToken,
        Long creatorId,
        Long assigneeId,
        List<DocumentResponse> charterDocuments,
        String deliverableDescription,
        String deliverableLink,
        List<DocumentResponse> submissionDocuments,
        Long createdAt,
        Long updatedAt
) {
}
