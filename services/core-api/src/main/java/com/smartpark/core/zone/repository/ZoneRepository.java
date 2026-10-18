package com.smartpark.core.zone.repository;

import com.smartpark.core.zone.domain.Zone;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    List<Zone> findByParkId(Long parkId);
}
