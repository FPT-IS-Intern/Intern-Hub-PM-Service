package com.intern.hub.pm.dto.request;

import lombok.Data;

@Data
public class PinRequest {
    private String pin;
    private String confirmPin;
}

