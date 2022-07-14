package com.safecornerscoffee.microservices.composite.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safecornerscoffee.microservices.api.composite.product.ProductAggregate;
import com.safecornerscoffee.microservices.api.core.product.Product;
import com.safecornerscoffee.microservices.api.event.Event;
import org.junit.jupiter.api.Test;

import static com.safecornerscoffee.microservices.api.event.Event.Type.CREATE;

public class EventTest {

    @Test
    void jsonEvent() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        ProductAggregate composite = new ProductAggregate(1, "name", 1, null, null, null);

        Event<Integer, Product> event = new Event<>(CREATE, composite.getProductId(), new Product(composite.getProductId(), composite.getName(), composite.getWeight(), null));

        String eventString = objectMapper.writeValueAsString(event);

        System.out.println(eventString);
    }
}
