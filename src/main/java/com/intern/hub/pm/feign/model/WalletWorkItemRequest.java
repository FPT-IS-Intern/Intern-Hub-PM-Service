package com.intern.hub.pm.feign.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletWorkItemRequest {
    private BigInteger oldBt;
    private BigInteger oldRt;
    private BigInteger newBt;
    private BigInteger newRt;
    @Builder.Default
    private boolean project = false;
}
