package com.intern.hub.pm.dto.project;

import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.model.constant.StatusWork;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponse(
        String id,
        String projectUUID,
        String name,
        String description,
        String note,
        StatusWork status,
        BigInteger budgetToken,
        BigInteger rewardToken,
        String creatorId,
        String assigneeId,
        String creatorName,
        String assigneeName,
        String deliverableDescription,
        String deliverableLink,
        String completionComment,
        Long memberCount,
        LocalDateTime startDate,
        LocalDateTime endDate,
        List<DocumentResponse> charterDocuments,
        List<DocumentResponse> deliverableDocuments,
        Long createdAt,
        Long updatedAt
) {
}
