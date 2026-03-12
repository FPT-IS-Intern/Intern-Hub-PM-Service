package com.intern.hub.pm.dto.request;

import lombok.Data;

@Data
public class CompleteProjectRequest {
    private Long reclaimedPoint;
    private Long bonusPoint;
    private String note;
}
