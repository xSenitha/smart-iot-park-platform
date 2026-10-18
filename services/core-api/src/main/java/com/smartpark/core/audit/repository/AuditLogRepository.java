package com.smartpark.core.audit.repository;

import com.smartpark.core.audit.domain.AuditLog;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);
    List<AuditLog> findTop20ByOrderByCreatedAtDesc();
}
