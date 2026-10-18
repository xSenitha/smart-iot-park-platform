package com.smartpark.core.maintenance.controller;

import com.smartpark.core.common.dto.ApiResponse;
import com.smartpark.core.common.dto.PageResponse;
import com.smartpark.core.maintenance.domain.TicketPriority;
import com.smartpark.core.maintenance.domain.TicketStatus;
import com.smartpark.core.maintenance.dto.AssignTicketRequest;
import com.smartpark.core.maintenance.dto.CreateTicketRequest;
import com.smartpark.core.maintenance.dto.MaintenanceTicketDto;
import com.smartpark.core.maintenance.dto.UpdateTicketStatusRequest;
import com.smartpark.core.maintenance.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Maintenance", description = "Endpoints for maintenance work orders, technician assignment, and ticket workflows")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    @Operation(summary = "Query paginated maintenance tickets (filter by status, priority)")
    public ResponseEntity<ApiResponse<PageResponse<MaintenanceTicketDto>>> getTickets(
        @RequestParam(required = false) TicketStatus status,
        @RequestParam(required = false) TicketPriority priority,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.getTickets(status, priority, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed ticket information")
    public ResponseEntity<ApiResponse<MaintenanceTicketDto>> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.getTicketById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new maintenance work order (Manual or AI-triggered)")
    public ResponseEntity<ApiResponse<MaintenanceTicketDto>> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        MaintenanceTicketDto created = maintenanceService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Maintenance ticket created successfully", created));
    }

    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_CREW')")
    @Operation(summary = "Progress ticket lifecycle state (Admin or Maintenance Crew)")
    public ResponseEntity<ApiResponse<MaintenanceTicketDto>> updateTicketStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateTicketStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Ticket status updated", maintenanceService.updateTicketStatus(id, request)));
    }

    @RequestMapping(value = "/{id}/assign", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @PreAuthorize("hasAnyRole('ADMIN', 'CITY_PLANNER', 'MAINTENANCE_CREW')")
    @Operation(summary = "Assign a ticket to a technician")
    public ResponseEntity<ApiResponse<MaintenanceTicketDto>> assignTicket(
        @PathVariable Long id,
        @Valid @RequestBody AssignTicketRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Ticket assigned successfully", maintenanceService.assignTicket(id, request)));
    }
}
