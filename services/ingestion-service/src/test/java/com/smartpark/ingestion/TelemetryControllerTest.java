package com.smartpark.ingestion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpark.ingestion.common.dto.TelemetryPayload;
import com.smartpark.ingestion.common.dto.TelemetryStatsDto;
import com.smartpark.ingestion.controller.TelemetryController;
import com.smartpark.ingestion.model.SensorReading;
import com.smartpark.ingestion.service.IngestionService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TelemetryController.class)
@ActiveProfiles("test")
class TelemetryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IngestionService ingestionService;

    @Test
    void testIngestSingle_ValidPayload_Returns201() throws Exception {
        TelemetryPayload payload = new TelemetryPayload(
            "DEV-BIN-0001",
            "BIN",
            1L,
            1L,
            Instant.now(),
            Map.of("fillPercentage", 50.0),
            Map.of()
        );

        SensorReading saved = new SensorReading("DEV-BIN-0001", "BIN", 1L, 1L, Instant.now(), payload.metrics(), payload.metadata());
        saved.setId("mongo-123");

        when(ingestionService.ingestSingle(any(TelemetryPayload.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value("mongo-123"))
            .andExpect(jsonPath("$.data.deviceId").value("DEV-BIN-0001"));
    }

    @Test
    void testGetStats_Returns200() throws Exception {
        TelemetryStatsDto stats = new TelemetryStatsDto(100L, 40L, 40L, 20L, Instant.now());
        when(ingestionService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/telemetry/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalReadingsCount").value(100));
    }

    @Test
    void testGetDeviceHistory_Returns200() throws Exception {
        SensorReading reading = new SensorReading("DEV-BIN-0001", "BIN", 1L, 1L, Instant.now(), Map.of("fillPercentage", 40.0), null);
        when(ingestionService.getRecentHistory(eq("DEV-BIN-0001"), eq(10))).thenReturn(List.of(reading));

        mockMvc.perform(get("/api/v1/telemetry/history/DEV-BIN-0001?limit=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].deviceId").value("DEV-BIN-0001"));
    }
}
