package com.smartpark.ingestion.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpark.ingestion.common.dto.TelemetryPayload;
import com.smartpark.ingestion.service.IngestionService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqttTelemetryConsumer implements MqttCallbackExtended {

    private static final Logger log = LoggerFactory.getLogger(MqttTelemetryConsumer.class);

    private final IngestionService ingestionService;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String brokerHost;
    private final int brokerPort;
    private final String clientId;
    private final String topicFilter;

    private MqttClient mqttClient;

    public MqttTelemetryConsumer(
        IngestionService ingestionService,
        ObjectMapper objectMapper,
        @Value("${smartpark.mqtt.enabled:true}") boolean enabled,
        @Value("${smartpark.mqtt.broker-host:localhost}") String brokerHost,
        @Value("${smartpark.mqtt.broker-port:1883}") int brokerPort,
        @Value("${smartpark.mqtt.client-id:smartpark-ingestion-subscriber}") String clientId,
        @Value("${smartpark.mqtt.topic-filter:parks/+/zones/+/devices/+/telemetry}") String topicFilter
    ) {
        this.ingestionService = ingestionService;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.brokerHost = brokerHost;
        this.brokerPort = brokerPort;
        this.clientId = clientId;
        this.topicFilter = topicFilter;
    }

    @PostConstruct
    public void start() {
        if (!enabled) {
            log.info("MQTT Telemetry Subscriber is disabled by configuration.");
            return;
        }

        String brokerUrl = "tcp://" + brokerHost + ":" + brokerPort;
        try {
            mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            mqttClient.setCallback(this);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(60);

            log.info("Connecting to Mosquitto MQTT broker at {}...", brokerUrl);
            mqttClient.connect(options);
        } catch (MqttException e) {
            log.warn("Could not immediately connect to MQTT broker (will auto-reconnect): {}", e.getMessage());
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("Connected to MQTT Broker: {} (reconnect: {})", serverURI, reconnect);
        try {
            mqttClient.subscribe(topicFilter, 1);
            log.info("Subscribed to MQTT telemetry topic: {}", topicFilter);
        } catch (MqttException e) {
            log.error("Failed to subscribe to MQTT topic {}: {}", topicFilter, e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost: {}", cause != null ? cause.getMessage() : "unknown reason");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String json = new String(message.getPayload(), StandardCharsets.UTF_8);
            TelemetryPayload payload = objectMapper.readValue(json, TelemetryPayload.class);
            ingestionService.ingestSingle(payload);
        } catch (Exception e) {
            log.error("Failed to process MQTT message from topic {}: {}", topic, e.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Consumer only, no delivery tokens to process
    }

    @PreDestroy
    public void stop() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
                log.info("MQTT client closed cleanly.");
            } catch (MqttException e) {
                log.error("Error closing MQTT client: {}", e.getMessage());
            }
        }
    }
}
