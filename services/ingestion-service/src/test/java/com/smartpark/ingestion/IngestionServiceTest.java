package com.smartpark.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.smartpark.ingestion.common.dto.TelemetryPayload;
import com.smartpark.ingestion.common.dto.TelemetryStatsDto;
import com.smartpark.ingestion.common.dto.ValidationResult;
import com.smartpark.ingestion.common.exception.ValidationException;
import com.smartpark.ingestion.model.SensorReading;
import com.smartpark.ingestion.repository.SensorReadingRepository;
import com.smartpark.ingestion.service.CoreApiSyncService;
import com.smartpark.ingestion.service.IngestionService;
import com.smartpark.ingestion.service.TelemetryValidationService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private SensorReadingRepository readingRepository;

    @Mock
    private TelemetryValidationService validationService;

    @Mock
    private CoreApiSyncService coreApiSyncService;

    private IngestionService ingestionService;

    @BeforeEach
    void setUp() {
        ingestionService = new IngestionService(readingRepository, validationService, coreApiSyncService);
    }

    @Test
    void testIngestSingle_Success() {
        TelemetryPayload payload = new TelemetryPayload(
            "DEV-BIN-0001",
            "BIN",
            1L,
            1L,
            Instant.now(),
            Map.of("fillPercentage", 75.0, "batteryLevel", 88.0),
            Map.of("firmwareVersion", "v2.0")
        );

        when(validationService.validate(payload)).thenReturn(ValidationResult.success());

        SensorReading saved = new SensorReading("DEV-BIN-0001", "BIN", 1L, 1L, Instant.now(), payload.metrics(), payload.metadata());
        saved.setId("mongo-obj-123");
        when(readingRepository.save(any(SensorReading.class))).thenReturn(saved);

        SensorReading result = ingestionService.ingestSingle(payload);

        assertThat(result.getId()).isEqualTo("mongo-obj-123");
        assertThat(result.getDeviceId()).isEqualTo("DEV-BIN-0001");
        verify(coreApiSyncService).syncDeviceHeartbeat(eq("DEV-BIN-0001"), eq(88.0), eq("v2.0"));
    }

    @Test
    void testIngestSingle_Invalid_ThrowsException() {
        TelemetryPayload payload = new TelemetryPayload("DEV-BIN-0001", "BIN", 1L, 1L, Instant.now(), Map.of(), null);
        when(validationService.validate(payload)).thenReturn(ValidationResult.failure(List.of("metrics cannot be empty")));

        assertThatThrownBy(() -> ingestionService.ingestSingle(payload))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Invalid telemetry reading");

        verifyNoInteractions(readingRepository);
    }

    @Test
    void testGetStats() {
        when(readingRepository.count()).thenReturn(150L);
        when(readingRepository.countByDeviceType("BIN")).thenReturn(60L);
        when(readingRepository.countByDeviceType("SOIL")).thenReturn(50L);
        when(readingRepository.countByDeviceType("LIGHT")).thenReturn(40L);
        when(readingRepository.findTop1ByOrderByTimestampDesc()).thenReturn(null);

        TelemetryStatsDto stats = ingestionService.getStats();

        assertThat(stats.totalReadingsCount()).isEqualTo(150L);
        assertThat(stats.binReadingsCount()).isEqualTo(60L);
        assertThat(stats.soilReadingsCount()).isEqualTo(50L);
        assertThat(stats.lightReadingsCount()).isEqualTo(40L);
    }
}
