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
public class WalletTokenRequest {
    private BigInteger rt;
    private BigInteger bt;
    private boolean isProject;
}
