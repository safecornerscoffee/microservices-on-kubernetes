package com.safecornerscoffee.microservices.core.product.mapper;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CarDto {

    private String make;
    private int seatCount;
    private String type;

}
