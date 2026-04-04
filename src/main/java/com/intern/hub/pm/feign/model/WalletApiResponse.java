package com.intern.hub.pm.feign.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletApiResponse<T> {
    private int status;
    private String message;
    private T data;
}
