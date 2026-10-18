package com.smartpark.core.device.dto;

import java.math.BigDecimal;

public record HeartbeatRequest(
    BigDecimal batteryLevel,
    String firmwareVersion
) {}
