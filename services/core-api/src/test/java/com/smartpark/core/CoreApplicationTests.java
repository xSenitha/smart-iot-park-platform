package com.smartpark.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CoreApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring context initializes successfully with H2 database and test security
    }
}
