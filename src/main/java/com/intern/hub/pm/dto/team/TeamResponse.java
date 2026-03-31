package com.intern.hub.pm.dto.team;

import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.model.constant.StatusWork;

import java.time.LocalDateTime;
import java.util.List;

public record TeamResponse(
        String id,
        String teamUUID,
        String name,
        String description,
        String note,
        StatusWork status,
        Long budgetToken,
        Long rewardToken,
        String creatorId,
        String assigneeId,
        String projectId,
        String deliverableDescription,
        String deliverableLink,
        String completionComment,
        LocalDateTime startDate,
        LocalDateTime endDate,
        List<DocumentResponse> charterDocuments,
        String leadName,
        Integer memberCount,
        Long createdAt,
        Long updatedAt
) {
}
