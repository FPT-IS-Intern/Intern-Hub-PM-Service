package com.intern.hub.pm.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatisticsResponse {
    private long totalProjects;
    private long notStartedProjects;
    private long inProgressProjects;
    private long completedProjects;
    private long overdueProjects;
}
