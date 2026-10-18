package com.smartpark.core.alert.controller;

import com.smartpark.core.alert.domain.AlertSeverity;
import com.smartpark.core.alert.domain.AlertStatus;
import com.smartpark.core.alert.dto.AlertDto;
import com.smartpark.core.alert.dto.AlertRuleDto;
import com.smartpark.core.alert.dto.CreateAlertRequest;
import com.smartpark.core.alert.dto.ResolveAlertRequest;
import com.smartpark.core.alert.service.AlertService;
import com.smartpark.core.common.dto.ApiResponse;
import com.smartpark.core.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Alerts", description = "Endpoints for environmental alerts, threshold breaches, and alert resolutions")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/api/v1/alerts")
    @Operation(summary = "Query paginated alert events with optional filters (status, severity)")
    public ResponseEntity<ApiResponse<PageResponse<AlertDto>>> getAlerts(
        @RequestParam(required = false) AlertStatus status,
        @RequestParam(required = false) AlertSeverity severity,
        @PageableDefault(size = 20, sort = "triggeredAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(alertService.getAlerts(status, severity, pageable)));
    }

    @GetMapping("/api/v1/alerts/{id}")
    @Operation(summary = "Get detailed alert information")
    public ResponseEntity<ApiResponse<AlertDto>> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(alertService.getAlertById(id)));
    }

    @PostMapping("/api/v1/alerts")
    @Operation(summary = "Publish an alert event (Called by AI engine or rule evaluator)")
    public ResponseEntity<ApiResponse<AlertDto>> createAlert(@Valid @RequestBody CreateAlertRequest request) {
        AlertDto created = alertService.createAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Alert triggered successfully", created));
    }

    @PATCH("/api/v1/alerts/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_CREW')")
    @Operation(summary = "Acknowledge an active alert (Admin or Maintenance Crew)")
    public ResponseEntity<ApiResponse<AlertDto>> acknowledgeAlert(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Alert acknowledged", alertService.acknowledgeAlert(id)));
    }

    @PATCH("/api/v1/alerts/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_CREW')")
    @Operation(summary = "Resolve an alert with resolution notes (Admin or Maintenance Crew)")
    public ResponseEntity<ApiResponse<AlertDto>> resolveAlert(
        @PathVariable Long id,
        @RequestBody(required = false) ResolveAlertRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Alert marked as resolved", alertService.resolveAlert(id, request)));
    }

    @GetMapping("/api/v1/alert-rules")
    @Operation(summary = "List all configured alert threshold rules")
    public ResponseEntity<ApiResponse<List<AlertRuleDto>>> getAllRules() {
        return ResponseEntity.ok(ApiResponse.success(alertService.getAllRules()));
    }
}
