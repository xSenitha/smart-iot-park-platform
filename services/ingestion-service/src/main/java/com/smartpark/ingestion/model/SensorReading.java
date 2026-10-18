package com.smartpark.ingestion.model;

import java.time.Instant;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sensor_readings")
@CompoundIndex(name = "device_timestamp_idx", def = "{'deviceId': 1, 'timestamp': -1}")
@CompoundIndex(name = "zone_timestamp_idx", def = "{'zoneId': 1, 'timestamp': -1}")
public class SensorReading {

    @Id
    private String id;

    @Indexed
    private String deviceId;

    private String deviceType;

    @Indexed
    private Long zoneId;

    private Long parkId;

    @Indexed
    private Instant timestamp;

    private Map<String, Object> metrics;
    private Map<String, Object> metadata;

    public SensorReading() {}

    public SensorReading(String deviceId, String deviceType, Long zoneId, Long parkId, Instant timestamp,
                         Map<String, Object> metrics, Map<String, Object> metadata) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.zoneId = zoneId;
        this.parkId = parkId;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.metrics = metrics;
        this.metadata = metadata;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }

    public Long getParkId() { return parkId; }
    public void setParkId(Long parkId) { this.parkId = parkId; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Map<String, Object> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
