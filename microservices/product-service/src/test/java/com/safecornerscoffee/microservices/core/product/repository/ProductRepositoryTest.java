package com.safecornerscoffee.microservices.core.product.repository;

import com.safecornerscoffee.microservices.core.product.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest
class ProductRepositoryTest {

    @Autowired
    ProductRepository repository;

    ProductEntity savedEntity;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        ProductEntity entity = new ProductEntity(1, "name", 1);
        savedEntity = repository.save(entity);

        assertThat(savedEntity).isEqualTo(entity);
    }

    @Test
    void create() {

        ProductEntity newEntity = new ProductEntity(2, "name", 2);
        repository.save(newEntity);

        ProductEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsProduct(foundEntity, newEntity);

        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void update() {
        assertThat(savedEntity.getVersion()).isEqualTo(0);
        savedEntity.setName("name2");
        repository.save(savedEntity);

        ProductEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertThat(foundEntity.getVersion()).isEqualTo(1);
        assertThat(foundEntity.getName()).isEqualTo("name2");
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        assertThat(repository.existsById(savedEntity.getId())).isFalse();
    }

    @Test
    void getByProductId() {
        Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId());

        assertThat(entity.isPresent()).isTrue();
        assertEqualsProduct(entity.get(), savedEntity);
    }

    @Test
    void duplicateError() {
        assertThatThrownBy(() -> {
            ProductEntity newEntity = new ProductEntity(savedEntity.getProductId(), "name", 1);
            repository.save(newEntity);
        }).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void optimisticLockError() {
        ProductEntity entity1 = repository.findById(savedEntity.getId()).get();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).get();

        entity1.setName("name1");
        repository.save(entity1);

        assertThatThrownBy(() -> {
            entity2.setName("name2");
            repository.save(entity2);
        }).isInstanceOf(OptimisticLockingFailureException.class);

        ProductEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertThat(updatedEntity.getVersion()).isEqualTo(savedEntity.getVersion() + 1);
        assertThat(updatedEntity.getName()).isEqualTo("name1");

    }

    @Test
    void paging() {

        repository.deleteAll();

        List<ProductEntity> newProducts = IntStream.rangeClosed(1001, 1010)
                .mapToObj(i -> new ProductEntity(i, "name" + i, i))
                .toList();
        repository.saveAll(newProducts);

        Pageable nextPage = PageRequest.of(0, 4, Sort.Direction.ASC, "productId");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        testNextPage(nextPage, "[1009, 1010]", false);
    }

    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
        Page<ProductEntity> productPage = repository.findAll(nextPage);
        assertThat(productPage.stream().map(ProductEntity::getProductId).toList().toString()).isEqualTo(expectedProductIds);
        assertThat(productPage.hasNext()).isEqualTo(expectsNextPage);
        return productPage.nextPageable();
    }

    private void assertEqualsProduct(ProductEntity actual, ProductEntity expected) {
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getVersion()).isEqualTo(expected.getVersion());
        assertThat(actual.getProductId()).isEqualTo(expected.getProductId());
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getWeight()).isEqualTo(expected.getWeight());
    }

}