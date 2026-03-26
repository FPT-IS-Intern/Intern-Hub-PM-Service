package com.intern.hub.pm.feign.model;

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
public class HrmFilterResponse {
    Integer no;
    Long userId;
    String avatarUrl;
    String fullName;
    String sysStatus;
    String email;
    String role;
    String position;
}
