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
    public Product createProduct(Product body) {
        try {
            ProductEntity entity = mapper.apiToEntity(body);
            ProductEntity newEntity = repository.save(entity);

            LOG.debug("createProduct: entity created for productId: {}", body.getProductId());
            return mapper.entityToApi(newEntity);
        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId());
        }
    }

    @Override
    public Product getProduct(int productId) {
        LOG.debug("getProduct: tries to find an entity with productId: {}", productId);

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        ProductEntity entity = repository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

        Product response = mapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());

        LOG.debug("getProduct: found productId={}", response.getProductId());
        return response;
    }

    @Override
    public void deleteProduct(int productId) {
        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        repository.findByProductId(productId).ifPresent(repository::delete);
    }
}