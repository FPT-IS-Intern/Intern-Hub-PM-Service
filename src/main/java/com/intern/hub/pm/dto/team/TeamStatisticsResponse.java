package com.intern.hub.pm.dto.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatisticsResponse {
    private long totalTeams;
    private long notStartedTeams;
    private long inProgressTeams;
    private long completedTeams;
    private long overdueTeams;
}
