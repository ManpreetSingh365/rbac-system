package com.fleetmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = TestConfig.class
)
@TestPropertySource(properties = {
    "app.jwt.secret=e8c7eada52157a9d24ffb47a220eea40",
    "app.jwt.expiration=3600000",
    "app.data-init.enabled=false"
})
class RbacSystemApplicationTests {

    @Test
    void contextLoads() {
        // This test will verify that the Spring context loads successfully
        // without any database or security dependencies
    }

}
