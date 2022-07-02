package com.safecornerscoffee.microservices.composite.product.service;

import org.junit.jupiter.api.Test;

import static com.safecornerscoffee.microservices.composite.product.service.PersonAssert.assertThat;

class PersonAssertTest {

    @Test
    void whenPersonNameMatches_thenCorrect() {
        Person person = new Person("John Doe", 20);
        assertThat(person).hasFullName("John Doe");
    }

    @Test
    public void whenPersonAgeLessThanEighteen() {
        Person person = new Person("Jane Roe", 17);
        assertThat(person).isNotAdult();
    }

    @Test
    public void whenPersonDoesNotHaveAMatchingNickname_thenIncorrect() {
        Person person = new Person("John Doe", 20);
        person.addNickname("Nick");

        // assertion will fail
        assertThat(person).hasNickName("Nick");
    }
}