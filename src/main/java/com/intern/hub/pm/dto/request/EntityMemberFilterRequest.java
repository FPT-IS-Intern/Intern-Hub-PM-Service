package com.intern.hub.pm.dto.request;

import com.intern.hub.pm.enums.Status;
import com.intern.hub.pm.enums.WorkItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityMemberFilterRequest {
    private WorkItemType entityType;
    private Long entityId;
    private Long userId;
    private String role;
    private Status status;
}

