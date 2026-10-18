package com.smartpark.ingestion.repository;

import com.smartpark.ingestion.model.SensorReading;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorReadingRepository extends MongoRepository<SensorReading, String> {
    List<SensorReading> findByDeviceIdOrderByTimestampDesc(String deviceId, Pageable pageable);
    List<SensorReading> findByDeviceIdAndTimestampBetweenOrderByTimestampAsc(String deviceId, Instant start, Instant end);
    List<SensorReading> findByZoneIdOrderByTimestampDesc(Long zoneId, Pageable pageable);
    long countByDeviceType(String deviceType);
    SensorReading findTop1ByOrderByTimestampDesc();
}
