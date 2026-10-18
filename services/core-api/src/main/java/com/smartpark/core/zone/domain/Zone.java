package com.smartpark.core.zone.domain;

import com.smartpark.core.device.domain.Device;
import com.smartpark.core.park.domain.Park;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "zones")
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id", nullable = false)
    private Park park;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "zone_type", nullable = false, length = 50)
    private String zoneType;

    @Column(name = "polygon_geojson", columnDefinition = "TEXT")
    private String polygonGeojson;

    @Column(name = "target_moisture_level", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetMoistureLevel = new BigDecimal("45.00");

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Device> devices = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Zone() {}

    public Zone(Park park, String name, String zoneType, String polygonGeojson, BigDecimal targetMoistureLevel) {
        this.park = park;
        this.name = name;
        this.zoneType = zoneType;
        this.polygonGeojson = polygonGeojson;
        this.targetMoistureLevel = targetMoistureLevel != null ? targetMoistureLevel : new BigDecimal("45.00");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Park getPark() { return park; }
    public void setPark(Park park) { this.park = park; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getZoneType() { return zoneType; }
    public void setZoneType(String zoneType) { this.zoneType = zoneType; }

    public String getPolygonGeojson() { return polygonGeojson; }
    public void setPolygonGeojson(String polygonGeojson) { this.polygonGeojson = polygonGeojson; }

    public BigDecimal getTargetMoistureLevel() { return targetMoistureLevel; }
    public void setTargetMoistureLevel(BigDecimal targetMoistureLevel) { this.targetMoistureLevel = targetMoistureLevel; }

    public List<Device> getDevices() { return devices; }
    public void setDevices(List<Device> devices) { this.devices = devices; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
