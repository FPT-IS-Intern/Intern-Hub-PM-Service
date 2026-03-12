package com.intern.hub.pm.dto.request;

import lombok.Data;

@Data
public class ApproveTaskRequest {
    private Long reclaimedPoint;
    private String note;
}
