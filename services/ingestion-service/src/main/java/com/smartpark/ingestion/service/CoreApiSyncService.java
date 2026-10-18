package com.smartpark.ingestion.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CoreApiSyncService {

    private static final Logger log = LoggerFactory.getLogger(CoreApiSyncService.class);

    private final RestTemplate restTemplate;
    private final String coreApiUrl;
    private final boolean syncEnabled;

    public CoreApiSyncService(
        RestTemplateBuilder builder,
        @Value("${smartpark.core-api.url:http://localhost:8081}") String coreApiUrl,
        @Value("${smartpark.core-api.sync-heartbeat:true}") boolean syncEnabled
    ) {
        this.restTemplate = builder.build();
        this.coreApiUrl = coreApiUrl;
        this.syncEnabled = syncEnabled;
    }

    @Async
    public void syncDeviceHeartbeat(String deviceUid, Object batteryLevel, Object firmwareVersion) {
        if (!syncEnabled) return;

        String url = coreApiUrl + "/api/v1/devices/uid/" + deviceUid + "/heartbeat";
        Map<String, Object> body = Map.of(
            "batteryLevel", batteryLevel != null ? batteryLevel : 100.0,
            "firmwareVersion", firmwareVersion != null ? String.valueOf(firmwareVersion) : "unknown"
        );

        try {
            restTemplate.postForObject(url, body, Map.class);
        } catch (Exception e) {
            log.debug("Core API heartbeat sync skipped for device {}: {}", deviceUid, e.getMessage());
        }
    }
}
