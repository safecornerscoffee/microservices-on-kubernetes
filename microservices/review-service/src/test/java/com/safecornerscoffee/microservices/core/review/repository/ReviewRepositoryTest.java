package com.safecornerscoffee.microservices.core.review.repository;

import com.safecornerscoffee.microservices.core.review.entity.ReviewEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository repository;

    private ReviewEntity savedEntity;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
        savedEntity = repository.save(entity);

        assertEqualsReview(savedEntity, entity);
    }

    @Test
    void create() {
        ReviewEntity newEntity = new ReviewEntity(1, 3, "a", "s", "c'");

        repository.save(newEntity);

        ReviewEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsReview(foundEntity, newEntity);

        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity);

        ReviewEntity foundEntity = repository.findById(savedEntity.getId()).get();
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
        List<ReviewEntity> entityList = repository.findByProductId(savedEntity.getProductId());

        assertThat(entityList).hasSize(1);
        assertEqualsReview(entityList.get(0), savedEntity);
    }

    @Test
    void duplicateError() {
        ReviewEntity newEntity = new ReviewEntity(1, 2, "a", "s", "c");
        assertThatThrownBy(() -> {
            repository.save(newEntity);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void optimisticLockError() {
        ReviewEntity entity1 = repository.findById(savedEntity.getId()).get();
        ReviewEntity entity2 = repository.findById(savedEntity.getId()).get();

        entity1.setAuthor("a1");
        repository.save(entity1);

        entity2.setAuthor("a2");
        assertThatThrownBy(() -> {
            repository.save(entity2);
        }).isInstanceOf(OptimisticLockingFailureException.class);

        ReviewEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertThat(updatedEntity.getVersion()).isEqualTo(1);
        assertThat(updatedEntity.getAuthor()).isEqualTo("a1");
    }
    private void assertEqualsReview(ReviewEntity actualEntity, ReviewEntity expectedEntity) {
        assertThat(actualEntity.getId()).isEqualTo(expectedEntity.getId());
        assertThat(actualEntity.getVersion()).isEqualTo(expectedEntity.getVersion());
        assertThat(actualEntity.getProductId()).isEqualTo(expectedEntity.getProductId());
        assertThat(actualEntity.getReviewId()).isEqualTo(expectedEntity.getReviewId());
        assertThat(actualEntity.getAuthor()).isEqualTo(expectedEntity.getAuthor());
        assertThat(actualEntity.getSubject()).isEqualTo(expectedEntity.getSubject());
        assertThat(actualEntity.getContent()).isEqualTo(expectedEntity.getContent());
    }
}