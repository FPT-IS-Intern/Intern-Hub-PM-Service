package com.intern.hub.pm.dto.task;

import lombok.Builder;

@Builder
public record TaskStatisticsResponse(
        long totalTasks,
        long notStartedTasks,
        long inProgressTasks,
        long pendingReviewTasks,
        long completedTasks,
        long overdueTasks,
        long needsRevisionTasks
) {
}
