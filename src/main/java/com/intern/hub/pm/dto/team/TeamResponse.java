package com.intern.hub.pm.dto.team;

import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.model.constant.StatusWork;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

public record TeamResponse(
        String id,
        String teamUUID,
        String name,
        String description,
        String note,
        StatusWork status,
        BigInteger budgetToken,
        BigInteger rewardToken,
        String creatorId,
        String assigneeId,
        String creatorName,
        String projectId,
        String deliverableDescription,
        String deliverableLink,
        String completionComment,
        LocalDateTime startDate,
        LocalDateTime endDate,
        List<DocumentResponse> charterDocuments,
        List<DocumentResponse> deliverableDocuments,
        String leadName,
        Integer memberCount,
        Long createdAt,
        Long updatedAt
) {
}
