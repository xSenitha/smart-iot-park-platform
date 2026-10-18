package com.smartpark.core.alert.domain;

import com.smartpark.core.device.domain.DeviceType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "alert_rules")
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 30)
    private DeviceType deviceType;

    @Column(name = "metric_name", nullable = false, length = 50)
    private String metricName;

    @Column(nullable = false, length = 20)
    private String operator; // GREATER_THAN, LESS_THAN, EQUALS

    @Column(name = "threshold_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal thresholdValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity = AlertSeverity.WARNING;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AlertRule() {}

    public AlertRule(String name, DeviceType deviceType, String metricName, String operator, BigDecimal thresholdValue, AlertSeverity severity) {
        this.name = name;
        this.deviceType = deviceType;
        this.metricName = metricName;
        this.operator = operator;
        this.thresholdValue = thresholdValue;
        this.severity = severity != null ? severity : AlertSeverity.WARNING;
        this.isActive = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DeviceType getDeviceType() { return deviceType; }
    public void setDeviceType(DeviceType deviceType) { this.deviceType = deviceType; }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public BigDecimal getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(BigDecimal thresholdValue) { this.thresholdValue = thresholdValue; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
