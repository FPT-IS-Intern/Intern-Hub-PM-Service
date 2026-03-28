package com.intern.hub.pm.dto.project;

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
public class ProjectFilterRequest {
    String name;
    StatusWork status;
    LocalDateTime startDate;
    LocalDateTime endDate;
}
