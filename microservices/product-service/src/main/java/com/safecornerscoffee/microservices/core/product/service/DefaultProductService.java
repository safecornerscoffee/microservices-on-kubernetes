package com.safecornerscoffee.microservices.core.product.service;

import com.safecornerscoffee.microservices.api.core.product.Product;
import com.safecornerscoffee.microservices.api.core.product.ProductService;
import com.safecornerscoffee.microservices.core.product.entity.ProductEntity;
import com.safecornerscoffee.microservices.core.product.mapper.ProductMapper;
import com.safecornerscoffee.microservices.core.product.repository.ProductRepository;
import com.safecornerscoffee.microservices.util.exception.InvalidInputException;
import com.safecornerscoffee.microservices.util.exception.NotFoundException;
import com.safecornerscoffee.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DefaultProductService implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProductService.class);
    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    public DefaultProductService(ServiceUtil serviceUtil, ProductRepository repository, ProductMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Product> createProduct(Product body) {
            ProductEntity entity = mapper.apiToEntity(body);

            return repository.save(entity)
                    .onErrorMap(DuplicateKeyException.class,
                            dke -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()))
                    .map(mapper::entityToApi);
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        LOG.debug("getProduct: tries to find an entity with productId: {}", productId);

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log()
                .map(mapper::entityToApi)
                .map(product -> {
                    product.setServiceAddress(serviceUtil.getServiceAddress());
                    return product;
                });

    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        return repository.findByProductId(productId)
                .flatMap(repository::delete);
    }
}