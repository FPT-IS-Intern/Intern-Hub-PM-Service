package com.intern.hub.pm.dtos.request;

import lombok.Data;

@Data
public class PinRequest {
    private String pin;
    private String confirmPin;
}
