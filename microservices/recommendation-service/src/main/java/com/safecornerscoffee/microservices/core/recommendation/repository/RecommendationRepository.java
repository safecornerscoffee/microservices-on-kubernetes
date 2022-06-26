package com.safecornerscoffee.microservices.core.recommendation.repository;

import com.safecornerscoffee.microservices.core.recommendation.entity.RecommendationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RecommendationRepository extends ReactiveCrudRepository<RecommendationEntity, String> {
    Flux<RecommendationEntity> findByProductId(int productId);
}
