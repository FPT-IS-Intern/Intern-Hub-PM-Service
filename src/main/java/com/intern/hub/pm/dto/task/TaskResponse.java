package com.intern.hub.pm.dto.task;

import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.model.constant.StatusWork;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

public record TaskResponse(
        String id,
        String teamId,
        String taskUUID,
        String name,
        String description,
        String note,
        StatusWork status,
        BigInteger rewardToken,
        String creatorId,
        String assigneeId,
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
