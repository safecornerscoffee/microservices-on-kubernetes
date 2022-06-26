package com.safecornerscoffee.microservices.core.product.repository;

import com.safecornerscoffee.microservices.core.product.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class ProductRepositoryTest {

    @Autowired
    ProductRepository repository;

    ProductEntity savedEntity;

    @BeforeEach
    void setUp() {

        StepVerifier.create(repository.deleteAll()).verifyComplete();

        ProductEntity entity = new ProductEntity(1, "name", 1);

        StepVerifier.create(repository.save(entity))
                .expectNextMatches(createdEntity -> {
                    savedEntity = createdEntity;
                    assertThat(savedEntity).isEqualTo(entity);
                    return true;
                }).verifyComplete();
    }

    @Test
    void create() {

        ProductEntity newEntity = new ProductEntity(2, "name", 2);

        StepVerifier.create(repository.save(newEntity))
                .expectNextMatches(createdEntity -> newEntity.getProductId() == createdEntity.getProductId())
                .verifyComplete();

        StepVerifier.create(repository.findById(newEntity.getId()))
                .expectNextMatches(foundEntity -> {
                    assertEqualsProduct(foundEntity, newEntity);
                    return true;
                })
                .verifyComplete();

        StepVerifier.create(repository.count())
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void update() {
        assertThat(savedEntity.getVersion()).isZero();
        savedEntity.setName("name2");

        StepVerifier.create(repository.save(savedEntity))
                .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("name2"))
                .verifyComplete();

        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity -> {
                    assertThat(foundEntity.getVersion()).isEqualTo(1);
                    assertThat(foundEntity.getName()).isEqualTo("name2");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void delete() {
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
        StepVerifier.create(repository.existsById(savedEntity.getId()))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void getByProductId() {

        StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
                .expectNextMatches(foundEntity -> {
                    assertEqualsProduct(foundEntity, savedEntity);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void duplicateError() {
        ProductEntity newEntity = new ProductEntity(savedEntity.getProductId(), "name", 1);

        StepVerifier.create(repository.save(newEntity))
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    void optimisticLockError() {
        ProductEntity entity1 = repository.findById(savedEntity.getId()).block();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).block();

        entity1.setName("name1");
        repository.save(entity1).block();

        entity2.setName("name2");
        StepVerifier.create(repository.save(entity2))
                .expectError(OptimisticLockingFailureException.class)
                .verify();

        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(updatedEntity -> {
                    assertThat(updatedEntity.getVersion()).isEqualTo(savedEntity.getVersion() + 1);
                    assertThat(updatedEntity.getName()).isEqualTo("name1");
                    return true;
                })
                .verifyComplete();

    }

    private void assertEqualsProduct(ProductEntity actual, ProductEntity expected) {
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getVersion()).isEqualTo(expected.getVersion());
        assertThat(actual.getProductId()).isEqualTo(expected.getProductId());
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getWeight()).isEqualTo(expected.getWeight());
    }

}