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
public class WalletTransactionModuleRequest {
    private Long moduleId;
    private String moduleUUId;
    private String projectUUId;
    private Long creatorId;
    private Long assigneeId;
    private BigInteger bt;
    private BigInteger rt;
}
