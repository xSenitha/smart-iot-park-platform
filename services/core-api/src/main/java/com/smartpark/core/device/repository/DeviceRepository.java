package com.smartpark.core.device.repository;

import com.smartpark.core.device.domain.Device;
import com.smartpark.core.device.domain.DeviceStatus;
import com.smartpark.core.device.domain.DeviceType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceUid(String deviceUid);
    List<Device> findByZoneId(Long zoneId);
    List<Device> findByType(DeviceType type);
    List<Device> findByStatus(DeviceStatus status);

    @Query("SELECT d FROM Device d WHERE d.zone.park.id = :parkId")
    List<Device> findByParkId(@Param("parkId") Long parkId);

    long countByZoneId(Long zoneId);
    long countByZoneIdAndStatus(Long zoneId, DeviceStatus status);
}
