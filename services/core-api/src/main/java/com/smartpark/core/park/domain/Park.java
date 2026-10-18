package com.smartpark.core.park.domain;

import com.smartpark.core.zone.domain.Zone;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parks")
public class Park {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(name = "total_area_sqm", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAreaSqm = BigDecimal.ZERO;

    @Column(nullable = false, length = 50)
    private String timezone = "UTC";

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    @OneToMany(mappedBy = "park", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Zone> zones = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Park() {}

    public Park(String name, String location, BigDecimal totalAreaSqm, String timezone, String status) {
        this.name = name;
        this.location = location;
        this.totalAreaSqm = totalAreaSqm;
        this.timezone = timezone != null ? timezone : "UTC";
        this.status = status != null ? status : "ACTIVE";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getTotalAreaSqm() { return totalAreaSqm; }
    public void setTotalAreaSqm(BigDecimal totalAreaSqm) { this.totalAreaSqm = totalAreaSqm; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Zone> getZones() { return zones; }
    public void setZones(List<Zone> zones) { this.zones = zones; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
