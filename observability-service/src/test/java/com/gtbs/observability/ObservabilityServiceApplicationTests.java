package com.gtbs.observability;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {"booking-lifecycle", "route-policy-updated"}
)
class ObservabilityServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
