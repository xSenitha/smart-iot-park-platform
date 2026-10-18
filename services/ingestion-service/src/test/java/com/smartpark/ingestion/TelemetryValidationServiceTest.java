package com.smartpark.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartpark.ingestion.common.dto.TelemetryPayload;
import com.smartpark.ingestion.common.dto.ValidationResult;
import com.smartpark.ingestion.service.TelemetryValidationService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TelemetryValidationServiceTest {

    private TelemetryValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new TelemetryValidationService();
    }

    @Test
    void testValidate_ValidBinPayload() {
        TelemetryPayload payload = new TelemetryPayload(
            "DEV-BIN-0001",
            "BIN",
            1L,
            1L,
            Instant.now(),
            Map.of("fillPercentage", 45.0, "batteryLevel", 90.0),
            Map.of("firmwareVersion", "v1.0")
        );

        ValidationResult result = validationService.validate(payload);

        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void testValidate_InvalidFillPercentage_ReturnsError() {
        TelemetryPayload payload = new TelemetryPayload(
            "DEV-BIN-0001",
            "BIN",
            1L,
            1L,
            Instant.now(),
            Map.of("fillPercentage", 150.0),
            null
        );

        ValidationResult result = validationService.validate(payload);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("fillPercentage must be between 0.0 and 100.0");
    }

    @Test
    void testValidate_InvalidDeviceType_ReturnsError() {
        TelemetryPayload payload = new TelemetryPayload(
            "DEV-UNKNOWN-0001",
            "CAMERA",
            1L,
            1L,
            Instant.now(),
            Map.of("fps", 30),
            null
        );

        ValidationResult result = validationService.validate(payload);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("deviceType must be one of: BIN, SOIL, LIGHT");
    }

    @Test
    void testValidate_FarFutureTimestamp_ReturnsError() {
        TelemetryPayload payload = new TelemetryPayload(
            "DEV-SOIL-0001",
            "SOIL",
            1L,
            1L,
            Instant.now().plus(2, ChronoUnit.HOURS),
            Map.of("soilMoisture", 45.0),
            null
        );

        ValidationResult result = validationService.validate(payload);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).contains("timestamp cannot be more than 10 minutes in the future");
    }
}
