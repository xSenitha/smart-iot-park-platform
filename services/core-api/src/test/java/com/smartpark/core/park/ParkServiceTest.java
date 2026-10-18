package com.smartpark.core.park;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.smartpark.core.audit.service.AuditService;
import com.smartpark.core.park.domain.Park;
import com.smartpark.core.park.dto.CreateParkRequest;
import com.smartpark.core.park.dto.ParkDto;
import com.smartpark.core.park.repository.ParkRepository;
import com.smartpark.core.park.service.ParkService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParkServiceTest {

    @Mock
    private ParkRepository parkRepository;

    @Mock
    private AuditService auditService;

    private ParkService parkService;

    @BeforeEach
    void setUp() {
        parkService = new ParkService(parkRepository, auditService);
    }

    @Test
    void testGetAllParks() {
        Park park = new Park("Central Park", "Downtown", new BigDecimal("500000"), "UTC", "ACTIVE");
        park.setId(1L);
        when(parkRepository.findAll()).thenReturn(List.of(park));

        List<ParkDto> list = parkService.getAllParks();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).name()).isEqualTo("Central Park");
    }

    @Test
    void testCreatePark() {
        CreateParkRequest request = new CreateParkRequest("Highland Park", "Uptown", new BigDecimal("350000"), "UTC", "ACTIVE");
        Park saved = new Park("Highland Park", "Uptown", new BigDecimal("350000"), "UTC", "ACTIVE");
        saved.setId(2L);

        when(parkRepository.save(any(Park.class))).thenReturn(saved);

        ParkDto result = parkService.createPark(request);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Highland Park");
        verify(auditService).logAction(eq("CREATE"), eq("PARK"), eq(2L), anyString());
    }
}
