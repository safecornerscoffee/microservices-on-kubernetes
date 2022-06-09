package com.safecornerscoffee.microservices.core.product.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CarMapperIntegrationTest {

    @Autowired
    CarMapper carMapper;

    @Test
    void shouldMapCarToDto() {
        //given
        Car car = new Car( "Morris", 5, CarType.SEDAN );

        //when
        CarDto carDto = carMapper.carToCarDto( car );

        //then
        assertThat( carDto ).isNotNull();
        assertThat( carDto.getMake() ).isEqualTo( "Morris" );
        assertThat( carDto.getSeatCount() ).isEqualTo( 5 );
        assertThat( carDto.getType() ).isEqualTo( "SEDAN" );
    }
}