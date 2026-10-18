package com.smartpark.core.maintenance.repository;

import com.smartpark.core.maintenance.domain.MaintenanceTicket;
import com.smartpark.core.maintenance.domain.TicketPriority;
import com.smartpark.core.maintenance.domain.TicketStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicket, Long> {
    Page<MaintenanceTicket> findByStatus(TicketStatus status, Pageable pageable);
    Page<MaintenanceTicket> findByPriority(TicketPriority priority, Pageable pageable);
    List<MaintenanceTicket> findByAssignedToId(Long assignedToId);
    List<MaintenanceTicket> findByParkId(Long parkId);
    List<MaintenanceTicket> findByZoneId(Long zoneId);
    long countByStatus(TicketStatus status);
}
