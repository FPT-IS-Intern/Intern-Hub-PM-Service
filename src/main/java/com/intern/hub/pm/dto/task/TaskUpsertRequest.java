package com.intern.hub.pm.dto.task;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;
import java.time.LocalDateTime;

public record TaskUpsertRequest(
        @NotBlank String name,
        String description,
        @NotNull @Min(0) BigInteger rewardToken,
        @JsonSerialize(using = ToStringSerializer.class) Long assigneeId,
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate,
        String status)
{
}
