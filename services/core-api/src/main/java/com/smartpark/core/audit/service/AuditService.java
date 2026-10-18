package com.smartpark.core.audit.service;

import com.smartpark.core.audit.domain.AuditLog;
import com.smartpark.core.audit.repository.AuditLogRepository;
import com.smartpark.core.auth.domain.User;
import com.smartpark.core.auth.repository.UserRepository;
import com.smartpark.core.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void logAction(String action, String entityType, Long entityId, String detailsJson) {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username).orElse(null);

        AuditLog log = new AuditLog(user, action, entityType, entityId, null, detailsJson);
        auditLogRepository.save(log);
    }
}
