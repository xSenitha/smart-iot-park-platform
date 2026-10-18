package com.smartpark.ingestion.service;

import com.smartpark.ingestion.common.dto.TelemetryPayload;
import com.smartpark.ingestion.common.dto.TelemetryStatsDto;
import com.smartpark.ingestion.common.dto.ValidationResult;
import com.smartpark.ingestion.common.exception.ValidationException;
import com.smartpark.ingestion.model.SensorReading;
import com.smartpark.ingestion.repository.SensorReadingRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final SensorReadingRepository readingRepository;
    private final TelemetryValidationService validationService;
    private final CoreApiSyncService coreApiSyncService;

    public IngestionService(
        SensorReadingRepository readingRepository,
        TelemetryValidationService validationService,
        CoreApiSyncService coreApiSyncService
    ) {
        this.readingRepository = readingRepository;
        this.validationService = validationService;
        this.coreApiSyncService = coreApiSyncService;
    }

    public SensorReading ingestSingle(TelemetryPayload payload) {
        ValidationResult validation = validationService.validate(payload);
        if (!validation.isValid()) {
            throw new ValidationException("Invalid telemetry reading", validation.errors());
        }

        SensorReading reading = new SensorReading(
            payload.deviceId(),
            payload.deviceType().toUpperCase(),
            payload.zoneId(),
            payload.parkId(),
            payload.timestamp() != null ? payload.timestamp() : Instant.now(),
            payload.metrics(),
            payload.metadata()
        );

        SensorReading saved = readingRepository.save(reading);

        // Asynchronously update Core API device heartbeat & battery status
        Object battery = payload.metrics().get("batteryLevel");
        Object fw = payload.metadata() != null ? payload.metadata().get("firmwareVersion") : null;
        coreApiSyncService.syncDeviceHeartbeat(payload.deviceId(), battery, fw);

        log.debug("Ingested telemetry for device {} (type: {})", saved.getDeviceId(), saved.getDeviceType());
        return saved;
    }

    public List<SensorReading> ingestBatch(List<TelemetryPayload> payloads) {
        List<SensorReading> results = new ArrayList<>();
        for (TelemetryPayload p : payloads) {
            results.add(ingestSingle(p));
        }
        return results;
    }

    public List<SensorReading> getRecentHistory(String deviceId, int limit) {
        int safeLimit = Math.max(1, Math.min(500, limit));
        return readingRepository.findByDeviceIdOrderByTimestampDesc(deviceId, PageRequest.of(0, safeLimit));
    }

    public TelemetryStatsDto getStats() {
        long total = readingRepository.count();
        long bins = readingRepository.countByDeviceType("BIN");
        long soil = readingRepository.countByDeviceType("SOIL");
        long lights = readingRepository.countByDeviceType("LIGHT");

        SensorReading latest = readingRepository.findTop1ByOrderByTimestampDesc();
        Instant latestTime = latest != null ? latest.getTimestamp() : null;

        return new TelemetryStatsDto(total, bins, soil, lights, latestTime);
    }
}
