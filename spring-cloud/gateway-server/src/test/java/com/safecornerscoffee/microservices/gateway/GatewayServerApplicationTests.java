package com.safecornerscoffee.microservices.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
class GatewayServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
