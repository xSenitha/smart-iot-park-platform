package com.smartpark.ingestion.controller;

import com.smartpark.ingestion.common.dto.ApiResponse;
import com.smartpark.ingestion.common.dto.TelemetryPayload;
import com.smartpark.ingestion.common.dto.TelemetryStatsDto;
import com.smartpark.ingestion.model.SensorReading;
import com.smartpark.ingestion.service.IngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/telemetry")
@Tag(name = "Telemetry Ingestion", description = "Endpoints for ingesting, querying, and monitoring real-time sensor logs")
public class TelemetryController {

    private final IngestionService ingestionService;

    public TelemetryController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    @Operation(summary = "Ingest a single IoT sensor reading into MongoDB time-series storage")
    public ResponseEntity<ApiResponse<SensorReading>> ingestSingle(@Valid @RequestBody TelemetryPayload payload) {
        SensorReading saved = ingestionService.ingestSingle(payload);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Reading ingested successfully", saved));
    }

    @PostMapping("/batch")
    @Operation(summary = "Ingest a batch of IoT sensor readings into MongoDB")
    public ResponseEntity<ApiResponse<List<SensorReading>>> ingestBatch(@Valid @RequestBody List<TelemetryPayload> payloads) {
        List<SensorReading> saved = ingestionService.ingestBatch(payloads);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(String.format("Batch of %d readings ingested", saved.size()), saved));
    }

    @GetMapping("/history/{deviceId}")
    @Operation(summary = "Query recent historical sensor readings for a specific device")
    public ResponseEntity<ApiResponse<List<SensorReading>>> getDeviceHistory(
        @PathVariable String deviceId,
        @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(ingestionService.getRecentHistory(deviceId, limit)));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get aggregate statistics on total ingested telemetry logs by device category")
    public ResponseEntity<ApiResponse<TelemetryStatsDto>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(ingestionService.getStats()));
    }
}
