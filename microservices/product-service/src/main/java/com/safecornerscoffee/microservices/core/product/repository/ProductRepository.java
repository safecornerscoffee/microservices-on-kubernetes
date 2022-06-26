package com.safecornerscoffee.microservices.core.product.repository;

import com.safecornerscoffee.microservices.core.product.entity.ProductEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveCrudRepository<ProductEntity, String> {

    Mono<ProductEntity> findByProductId(int productId);
}
