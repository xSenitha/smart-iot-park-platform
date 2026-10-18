package com.smartpark.core.alert.repository;

import com.smartpark.core.alert.domain.AlertRule;
import com.smartpark.core.device.domain.DeviceType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
    List<AlertRule> findByDeviceTypeAndIsActiveTrue(DeviceType deviceType);
    List<AlertRule> findByIsActiveTrue();
}
