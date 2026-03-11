package com.intern.hub.pm.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProjectRequest {
    private Long id; // userId
    private String role;
}
