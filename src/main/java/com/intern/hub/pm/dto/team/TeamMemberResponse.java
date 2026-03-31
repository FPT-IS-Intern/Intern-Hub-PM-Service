package com.intern.hub.pm.dto.team;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    String id;

    @JsonProperty("userId")
    String userId;

    @JsonProperty("teamId")
    String teamId;

    String fullName;
    String email;
    private String status;
    private String role;
    private Long countTasks;
    private Long createdAt;
    Long updatedAt;
}
