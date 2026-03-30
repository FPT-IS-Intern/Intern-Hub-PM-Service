package com.intern.hub.pm.dto.team;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.intern.hub.pm.model.constant.Status;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeamMemberResponse {

    @JsonProperty("id")
    @JsonSerialize(using = ToStringSerializer.class)
    Long id;

    @JsonProperty("userId")
    @JsonSerialize(using = ToStringSerializer.class)
    Long userId;

    @JsonProperty("teamId")
    @JsonSerialize(using = ToStringSerializer.class)
    Long teamId;

    String fullName;
    String email;
    private String status;
    private String role;
    private Long countTasks;
    private Long createdAt;
    Long updatedAt;
}
