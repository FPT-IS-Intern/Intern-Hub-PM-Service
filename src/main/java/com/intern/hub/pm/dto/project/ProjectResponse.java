package com.intern.hub.pm.dto.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.model.constant.StatusWork;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponse(
        @JsonSerialize(using = ToStringSerializer.class)
        Long id,
        String projectUUID,
        String name,
        String description,
        String note,
        StatusWork status,
        Long budgetToken,
        Long rewardToken,
        @JsonSerialize(using = ToStringSerializer.class)
        Long creatorId,
        @JsonSerialize(using = ToStringSerializer.class)
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
