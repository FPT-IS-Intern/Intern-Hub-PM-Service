package com.intern.hub.pm.dto.task;

import com.intern.hub.pm.model.constant.StatusWork;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TaskFilterRequest(
        String name,
        StatusWork status,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
