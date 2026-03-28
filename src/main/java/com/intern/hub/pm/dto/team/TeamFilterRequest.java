package com.intern.hub.pm.dto.team;

import com.intern.hub.pm.model.constant.StatusWork;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamFilterRequest {
    private String name;
    private StatusWork status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long projectId;
}
