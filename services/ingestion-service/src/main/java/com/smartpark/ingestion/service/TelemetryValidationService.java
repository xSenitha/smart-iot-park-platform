package com.smartpark.ingestion.service;

import com.smartpark.ingestion.common.dto.TelemetryPayload;
import com.smartpark.ingestion.common.dto.ValidationResult;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TelemetryValidationService {

    public ValidationResult validate(TelemetryPayload payload) {
        List<String> errors = new ArrayList<>();

        if (payload.deviceId() == null || payload.deviceId().isBlank()) {
            errors.add("deviceId cannot be blank");
        }

        if (payload.deviceType() == null || payload.deviceType().isBlank()) {
            errors.add("deviceType cannot be blank");
        } else {
            String type = payload.deviceType().toUpperCase();
            if (!type.equals("BIN") && !type.equals("SOIL") && !type.equals("LIGHT")) {
                errors.add("deviceType must be one of: BIN, SOIL, LIGHT");
            }
        }

        if (payload.zoneId() == null || payload.zoneId() <= 0) {
            errors.add("zoneId must be a positive integer");
        }

        if (payload.parkId() == null || payload.parkId() <= 0) {
            errors.add("parkId must be a positive integer");
        }

        if (payload.timestamp() != null) {
            Instant maxFuture = Instant.now().plus(10, ChronoUnit.MINUTES);
            if (payload.timestamp().isAfter(maxFuture)) {
                errors.add("timestamp cannot be more than 10 minutes in the future");
            }
        }

        Map<String, Object> metrics = payload.metrics();
        if (metrics == null || metrics.isEmpty()) {
            errors.add("metrics payload cannot be empty");
        } else {
            validateMetricsByType(payload.deviceType(), metrics, errors);
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    private void validateMetricsByType(String deviceType, Map<String, Object> metrics, List<String> errors) {
        if (deviceType == null) return;
        String type = deviceType.toUpperCase();

        if (metrics.containsKey("batteryLevel")) {
            double battery = toDouble(metrics.get("batteryLevel"));
            if (battery < 0.0 || battery > 100.0) {
                errors.add("batteryLevel must be between 0.0 and 100.0");
            }
        }

        switch (type) {
            case "BIN" -> {
                if (metrics.containsKey("fillPercentage")) {
                    double fill = toDouble(metrics.get("fillPercentage"));
                    if (fill < 0.0 || fill > 100.0) {
                        errors.add("fillPercentage must be between 0.0 and 100.0");
                    }
                }
            }
            case "SOIL" -> {
                if (metrics.containsKey("soilMoisture")) {
                    double moisture = toDouble(metrics.get("soilMoisture"));
                    if (moisture < 0.0 || moisture > 100.0) {
                        errors.add("soilMoisture must be between 0.0 and 100.0");
                    }
                }
            }
            case "LIGHT" -> {
                if (metrics.containsKey("lightLux")) {
                    double lux = toDouble(metrics.get("lightLux"));
                    if (lux < 0.0) {
                        errors.add("lightLux must be non-negative");
                    }
                }
            }
        }
    }

    private double toDouble(Object obj) {
        if (obj instanceof Number num) {
            return num.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(obj));
        } catch (Exception e) {
            return -1.0;
        }
    }
}
