package com.safecornerscoffee.microservices.composite.product.service;

public class Assertions {
    public static PersonAssert assertThat(Person actual) {
        return new PersonAssert(actual);
    }
}
