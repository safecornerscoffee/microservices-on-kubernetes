package com.safecornerscoffee.microservices.composite.product.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MyApiInfoTest {

    @Autowired
    MyApiInfo swaggerProperties;

    @Test
    void propertiesLoads() {

        System.out.println(swaggerProperties.getTitle());
        assertThat(swaggerProperties.getTitle()).isNotNull();
        System.out.println(swaggerProperties.getContact().getEmail());
    }

}