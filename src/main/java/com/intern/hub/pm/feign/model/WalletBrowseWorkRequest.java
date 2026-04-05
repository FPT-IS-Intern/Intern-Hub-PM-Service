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
public class WalletBrowseWorkRequest {
    private Long entityId;
    private Long userId;
    private String workUUId;
    private String type;
    private String note;
    private BigInteger bt;
    private BigInteger rt;
}
