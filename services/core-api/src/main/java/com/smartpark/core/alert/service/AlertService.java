package com.smartpark.core.alert.service;

import com.smartpark.core.alert.domain.Alert;
import com.smartpark.core.alert.domain.AlertRule;
import com.smartpark.core.alert.domain.AlertSeverity;
import com.smartpark.core.alert.domain.AlertStatus;
import com.smartpark.core.alert.dto.AlertDto;
import com.smartpark.core.alert.dto.AlertRuleDto;
import com.smartpark.core.alert.dto.CreateAlertRequest;
import com.smartpark.core.alert.dto.ResolveAlertRequest;
import com.smartpark.core.alert.repository.AlertRepository;
import com.smartpark.core.alert.repository.AlertRuleRepository;
import com.smartpark.core.audit.service.AuditService;
import com.smartpark.core.auth.domain.User;
import com.smartpark.core.auth.repository.UserRepository;
import com.smartpark.core.common.dto.PageResponse;
import com.smartpark.core.common.exception.ResourceNotFoundException;
import com.smartpark.core.device.domain.Device;
import com.smartpark.core.device.repository.DeviceRepository;
import com.smartpark.core.event.SseEventBroadcaster;
import com.smartpark.core.security.SecurityUtils;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final SseEventBroadcaster eventBroadcaster;

    public AlertService(
        AlertRepository alertRepository,
        AlertRuleRepository alertRuleRepository,
        DeviceRepository deviceRepository,
        UserRepository userRepository,
        AuditService auditService,
        SseEventBroadcaster eventBroadcaster
    ) {
        this.alertRepository = alertRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.eventBroadcaster = eventBroadcaster;
    }

    public PageResponse<AlertDto> getAlerts(AlertStatus status, AlertSeverity severity, Pageable pageable) {
        Page<Alert> page;
        if (status != null) {
            page = alertRepository.findByStatus(status, pageable);
        } else if (severity != null) {
            page = alertRepository.findBySeverity(severity, pageable);
        } else {
            page = alertRepository.findAll(pageable);
        }
        return PageResponse.from(page.map(AlertDto::fromEntity));
    }

    public AlertDto getAlertById(Long id) {
        Alert alert = findAlertEntity(id);
        return AlertDto.fromEntity(alert);
    }

    @Transactional
    public AlertDto createAlert(CreateAlertRequest request) {
        Device device = deviceRepository.findById(request.deviceId())
            .orElseThrow(() -> new ResourceNotFoundException("Device", "id", request.deviceId()));

        AlertRule rule = null;
        if (request.ruleId() != null) {
            rule = alertRuleRepository.findById(request.ruleId()).orElse(null);
        }

        Alert alert = new Alert(
            device,
            rule,
            request.alertType(),
            request.severity(),
            request.title(),
            request.message()
        );

        Alert saved = alertRepository.save(alert);
        AlertDto dto = AlertDto.fromEntity(saved);

        eventBroadcaster.broadcast("ALERT_TRIGGERED", dto);
        auditService.logAction("CREATE", "ALERT", saved.getId(), "Alert triggered: " + saved.getTitle());
        return dto;
    }

    @Transactional
    public AlertDto acknowledgeAlert(Long id) {
        Alert alert = findAlertEntity(id);
        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        Alert updated = alertRepository.save(alert);

        AlertDto dto = AlertDto.fromEntity(updated);
        eventBroadcaster.broadcast("ALERT_ACKNOWLEDGED", dto);
        auditService.logAction("ACKNOWLEDGE", "ALERT", updated.getId(), "Alert acknowledged: " + updated.getTitle());
        return dto;
    }

    @Transactional
    public AlertDto resolveAlert(Long id, ResolveAlertRequest request) {
        Alert alert = findAlertEntity(id);
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(Instant.now());

        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username).orElse(null);
        alert.setResolvedBy(user);

        if (request != null && request.resolutionNotes() != null) {
            alert.setResolutionNotes(request.resolutionNotes());
        }

        Alert updated = alertRepository.save(alert);
        AlertDto dto = AlertDto.fromEntity(updated);

        eventBroadcaster.broadcast("ALERT_RESOLVED", dto);
        auditService.logAction("RESOLVE", "ALERT", updated.getId(), "Alert resolved: " + updated.getTitle());
        return dto;
    }

    public List<AlertRuleDto> getAllRules() {
        return alertRuleRepository.findAll().stream()
            .map(AlertRuleDto::fromEntity)
            .toList();
    }

    public Alert findAlertEntity(Long id) {
        return alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", id));
    }
}
