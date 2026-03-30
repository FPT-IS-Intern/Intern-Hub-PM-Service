package com.intern.hub.pm.dto.task;

import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.model.constant.StatusWork;

import java.time.LocalDateTime;
import java.util.List;


public record TaskResponse(
        Long id,
        Long teamId,
        String taskUUID,
        String name,
        String description,
        String note,
        StatusWork status,
        Long rewardToken,
        Long creatorId,
        Long assigneeId,
        String creatorName,
        String assigneeName,
        List<DocumentResponse> charterDocuments,
        String deliverableDescription,
        String deliverableLink,
        LocalDateTime startDate,
        LocalDateTime endDate,
        List<DocumentResponse> submissionDocuments,
        Long createdAt,
        Long updatedAt
) {
}
