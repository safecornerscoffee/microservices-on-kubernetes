package com.safecornerscoffee.microservices.core.product.repository;

import com.safecornerscoffee.microservices.core.product.entity.ProductEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface ProductRepository extends PagingAndSortingRepository<ProductEntity, String> {

    Optional<ProductEntity> findByProductId(int productId);
}
