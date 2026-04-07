package com.intern.hub.pm.dto.task;

import java.math.BigInteger;

public record TaskPartialApproveRequest(
    String reason,
    BigInteger newRt
) {
}
