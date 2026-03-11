package com.intern.hub.pm.dtos.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@JsonPropertyOrder({
        "id",
        "idUser",
        "name",
        "role",
        "createdAt"
})
@Data
@Builder
public class ProjectUserResponse {
    private Long id;
    private Long idUser;
    private String name;
    private String role;
    private long tasksCount;
    private LocalDateTime createdAt;
}
