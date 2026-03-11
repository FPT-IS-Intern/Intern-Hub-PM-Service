package com.intern.hub.pm.dtos.response;

import com.intern.hub.pm.enums.StatusWork;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {
    private Long id;
    private String nameProject;
    private String description;
    private StatusWork status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
