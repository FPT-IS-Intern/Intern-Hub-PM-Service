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
public class WalletTransactionTaskRequest {
    private Long taskUUId;
    private Long moduleUUId;
    private Long creatorId;
    private Long assigneeId;
    private BigInteger rt;
}
