package com.smartpark.core.park.controller;

import com.smartpark.core.common.dto.ApiResponse;
import com.smartpark.core.park.dto.CreateParkRequest;
import com.smartpark.core.park.dto.ParkDto;
import com.smartpark.core.park.dto.UpdateParkRequest;
import com.smartpark.core.park.service.ParkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parks")
@Tag(name = "Parks", description = "Endpoints for municipal park management")
public class ParkController {

    private final ParkService parkService;

    public ParkController(ParkService parkService) {
        this.parkService = parkService;
    }

    @GetMapping
    @Operation(summary = "Get list of all parks with zone counts and status")
    public ResponseEntity<ApiResponse<List<ParkDto>>> getAllParks() {
        return ResponseEntity.ok(ApiResponse.success(parkService.getAllParks()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed information for a specific park")
    public ResponseEntity<ApiResponse<ParkDto>> getParkById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(parkService.getParkById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new park (Admin only)")
    public ResponseEntity<ApiResponse<ParkDto>> createPark(@Valid @RequestBody CreateParkRequest request) {
        ParkDto created = parkService.createPark(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Park created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CITY_PLANNER')")
    @Operation(summary = "Update park metadata (Admin or City Planner)")
    public ResponseEntity<ApiResponse<ParkDto>> updatePark(
        @PathVariable Long id,
        @Valid @RequestBody UpdateParkRequest request
    ) {
        ParkDto updated = parkService.updatePark(id, request);
        return ResponseEntity.ok(ApiResponse.success("Park updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a park and cascade child zones/devices (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deletePark(@PathVariable Long id) {
        parkService.deletePark(id);
        return ResponseEntity.ok(ApiResponse.success("Park deleted successfully", null));
    }
}
