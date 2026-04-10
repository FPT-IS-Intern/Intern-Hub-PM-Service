package com.intern.hub.pm.feign.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WalletStatusResponse {
    private boolean hasWallet;
    private boolean hasPin;
    private String address;
    private String walletStatus;
}
