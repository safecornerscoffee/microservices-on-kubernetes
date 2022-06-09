package com.safecornerscoffee.microservices.core.product.mapper;

import com.safecornerscoffee.microservices.api.core.product.Product;
import com.safecornerscoffee.microservices.core.product.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapperTests() {

        assertThat(mapper).isNotNull();

        Product api = new Product(1, "name", 1, "sa");

        ProductEntity entity = mapper.apiToEntity(api);

        assertThat(entity.getProductId()).isEqualTo(api.getProductId());
        assertThat(entity.getName()).isEqualTo(api.getName());
        assertThat(entity.getWeight()).isEqualTo(api.getWeight());

        Product api2 = mapper.entityToApi(entity);

        assertThat(api2.getProductId()).isEqualTo(api.getProductId());
        assertThat(api2.getName()).isEqualTo(api.getName());
        assertThat(api2.getWeight()).isEqualTo(api.getWeight());
        assertThat(api2.getServiceAddress()).isNull();
    }

}