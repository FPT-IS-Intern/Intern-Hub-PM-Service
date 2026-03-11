package com.intern.hub.pm.dtos.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoteRequest {
    private String note;
}
