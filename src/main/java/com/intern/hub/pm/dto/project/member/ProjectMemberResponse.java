package com.intern.hub.pm.dto.project.member;

import com.intern.hub.pm.model.constant.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectMemberResponse {

    @JsonProperty("id")
    @JsonSerialize(using = ToStringSerializer.class)
    Long id;

    @JsonProperty("projectId")
    @JsonSerialize(using = ToStringSerializer.class)
    Long projectId;

    @JsonProperty("userId")
    @JsonSerialize(using = ToStringSerializer.class)
    Long userId;

    String fullName;
    String email;
    Long countProjectTeam;
    String role;
    Status status;
    Long createdAt;
    Long updatedAt;
}
