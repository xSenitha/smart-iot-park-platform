package com.smartpark.core.park.service;

import com.smartpark.core.audit.service.AuditService;
import com.smartpark.core.common.exception.ResourceNotFoundException;
import com.smartpark.core.park.domain.Park;
import com.smartpark.core.park.dto.CreateParkRequest;
import com.smartpark.core.park.dto.ParkDto;
import com.smartpark.core.park.dto.UpdateParkRequest;
import com.smartpark.core.park.repository.ParkRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParkService {

    private final ParkRepository parkRepository;
    private final AuditService auditService;

    public ParkService(ParkRepository parkRepository, AuditService auditService) {
        this.parkRepository = parkRepository;
        this.auditService = auditService;
    }

    public List<ParkDto> getAllParks() {
        return parkRepository.findAll().stream()
            .map(ParkDto::fromEntity)
            .toList();
    }

    public ParkDto getParkById(Long id) {
        Park park = findParkEntity(id);
        return ParkDto.fromEntity(park);
    }

    @Transactional
    public ParkDto createPark(CreateParkRequest request) {
        Park park = new Park(
            request.name(),
            request.location(),
            request.totalAreaSqm(),
            request.timezone(),
            request.status()
        );
        Park saved = parkRepository.save(park);
        auditService.logAction("CREATE", "PARK", saved.getId(), "Created park: " + saved.getName());
        return ParkDto.fromEntity(saved);
    }

    @Transactional
    public ParkDto updatePark(Long id, UpdateParkRequest request) {
        Park park = findParkEntity(id);

        if (request.name() != null) park.setName(request.name());
        if (request.location() != null) park.setLocation(request.location());
        if (request.totalAreaSqm() != null) park.setTotalAreaSqm(request.totalAreaSqm());
        if (request.timezone() != null) park.setTimezone(request.timezone());
        if (request.status() != null) park.setStatus(request.status());

        Park updated = parkRepository.save(park);
        auditService.logAction("UPDATE", "PARK", updated.getId(), "Updated park: " + updated.getName());
        return ParkDto.fromEntity(updated);
    }

    @Transactional
    public void deletePark(Long id) {
        Park park = findParkEntity(id);
        parkRepository.delete(park);
        auditService.logAction("DELETE", "PARK", id, "Deleted park: " + park.getName());
    }

    public Park findParkEntity(Long id) {
        return parkRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Park", "id", id));
    }
}
