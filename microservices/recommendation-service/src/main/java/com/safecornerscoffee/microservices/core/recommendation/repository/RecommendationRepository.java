package com.safecornerscoffee.microservices.core.recommendation.repository;

import com.safecornerscoffee.microservices.core.recommendation.entity.RecommendationEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RecommendationRepository extends CrudRepository<RecommendationEntity, String> {
    List<RecommendationEntity> findByProductId(int productId);
}
