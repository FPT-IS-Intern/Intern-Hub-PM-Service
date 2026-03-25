package com.intern.hub.pm.dto.project;

import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.model.constant.StatusWork;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponse(
        Long id,
        String projectUUID,
        String name,
        String description,
        String note,
        StatusWork status,
        Long budgetToken,
        Long rewardToken,
        Long creatorId,
        Long assigneeId,
        String deliverableDescription,
        String deliverableLink,
        String completionComment,
        LocalDateTime startDate,
        LocalDateTime endDate,
        List<DocumentResponse> charterDocuments,
        Long createdAt,
        Long updatedAt
) {
}
