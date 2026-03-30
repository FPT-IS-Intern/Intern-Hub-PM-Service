package com.intern.hub.pm.dto.team;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.model.constant.StatusWork;

import java.time.LocalDateTime;
import java.util.List;

public record TeamResponse(
        @JsonSerialize(using = ToStringSerializer.class)
        Long id,
        String teamUUID,
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
        @JsonSerialize(using = ToStringSerializer.class)
        Long projectId,
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
