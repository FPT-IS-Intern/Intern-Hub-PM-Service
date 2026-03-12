package com.intern.hub.pm.dto.response;

import com.intern.hub.pm.enums.StatusWork;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkItemResponse {

    private Long id;
    private String wordItemUUID;
    private String memberNumber;
    private String name;
    private String description;
    private StatusWork status;
    private Long budgetPoint;
    private Long rewardPoint;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

