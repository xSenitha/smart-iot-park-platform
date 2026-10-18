package com.smartpark.core.alert.repository;

import com.smartpark.core.alert.domain.Alert;
import com.smartpark.core.alert.domain.AlertSeverity;
import com.smartpark.core.alert.domain.AlertStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    Page<Alert> findByStatus(AlertStatus status, Pageable pageable);
    Page<Alert> findBySeverity(AlertSeverity severity, Pageable pageable);
    List<Alert> findByDeviceId(Long deviceId);

    @Query("SELECT a FROM Alert a WHERE a.device.zone.park.id = :parkId ORDER BY a.triggeredAt DESC")
    List<Alert> findRecentByParkId(@Param("parkId") Long parkId, Pageable pageable);

    long countByStatus(AlertStatus status);
}
