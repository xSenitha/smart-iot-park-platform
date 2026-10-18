package com.smartpark.core.park.repository;

import com.smartpark.core.park.domain.Park;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkRepository extends JpaRepository<Park, Long> {
    List<Park> findByStatus(String status);
}
