package com.safecornerscoffee.microservices.core.recommendation.repository;

import com.safecornerscoffee.microservices.core.recommendation.entity.RecommendationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest
class RecommendationRepositoryTest {

    @Autowired
    RecommendationRepository repository;

    RecommendationEntity savedEntity;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 4, "c");
        savedEntity = repository.save(entity);

        assertEqualsRecommendation(savedEntity, entity);
    }

    @Test
    void create() {
        RecommendationEntity newEntity = new RecommendationEntity(1, 3, "a", 3, "c");
        repository.save(newEntity);

        RecommendationEntity foundEntity = repository.findById(newEntity.getId()).get();

        assertEqualsRecommendation(foundEntity, newEntity);

        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity);

        RecommendationEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertThat(foundEntity.getVersion()).isEqualTo(1);
        assertThat(foundEntity.getAuthor()).isEqualTo("a2");
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        assertThat(repository.existsById(savedEntity.getId())).isFalse();
    }

    @Test
    void getByProductId() {
        List<RecommendationEntity> entityList = repository.findByProductId(savedEntity.getProductId());

        assertThat(entityList).hasSize(1);
        assertEqualsRecommendation(entityList.get(0), savedEntity);
    }

    @Test
    void duplicateError() {
        RecommendationEntity entity = new RecommendationEntity(savedEntity.getProductId(), savedEntity.getRecommendationId(),
                "a", 2, "c");
        assertThatThrownBy(() -> {
            repository.save(entity);
        }).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void optimisticLockError() {
        RecommendationEntity entity1 = repository.findById(savedEntity.getId()).get();
        RecommendationEntity entity2 = repository.findById(savedEntity.getId()).get();

        entity1.setAuthor("a1");
        repository.save(entity1);

        entity2.setAuthor("a2");
        assertThatThrownBy(() -> {
            repository.save(entity2);
        }).isInstanceOf(OptimisticLockingFailureException.class);

        RecommendationEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertThat(updatedEntity.getVersion()).isEqualTo(1);
        assertThat(updatedEntity.getAuthor()).isEqualTo("a1");
    }

    private void assertEqualsRecommendation(RecommendationEntity actual, RecommendationEntity expected) {
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getVersion()).isEqualTo(expected.getVersion());
        assertThat(actual.getProductId()).isEqualTo(expected.getProductId());
        assertThat(actual.getRecommendationId()).isEqualTo(expected.getRecommendationId());
        assertThat(actual.getAuthor()).isEqualTo(expected.getAuthor());
        assertThat(actual.getRating()).isEqualTo(expected.getRating());
        assertThat(actual.getContent()).isEqualTo(expected.getContent());
    }
}