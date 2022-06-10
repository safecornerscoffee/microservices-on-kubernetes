package com.safecornerscoffee.microservices.composite.product.service;

import com.safecornerscoffee.microservices.api.composite.product.*;
import com.safecornerscoffee.microservices.api.core.product.Product;
import com.safecornerscoffee.microservices.api.core.recommendation.Recommendation;
import com.safecornerscoffee.microservices.api.core.review.Review;
import com.safecornerscoffee.microservices.util.exception.NotFoundException;
import com.safecornerscoffee.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DefaultProductCompositeService implements ProductCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProductCompositeService.class);
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    public DefaultProductCompositeService(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public void createCompositeProduct(ProductAggregate body) {
        try {
            LOG.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(product);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                    integration.createRecommendation(recommendation);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
                    integration.createReview(review);
                });
            }

            LOG.debug("createCompositeProduct: composite entites created for productId: {}", body.getProductId());

        } catch (RuntimeException re) {
            LOG.warn("createCompositeProduct failed", re);
            throw re;
        }
    }

    @Override
    public ProductAggregate getCompositeProduct(int productId) {

        LOG.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);

        Product product = integration.getProduct(productId);
        if (product == null) throw new NotFoundException("No product found for productId:" + productId);

        List<Recommendation> recommendations = integration.getRecommendations(productId);

        List<Review> reviews = integration.getReviews(productId);

        LOG.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);

        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public void deleteCompositeProduct(int productId) {

        LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

        integration.deleteProduct(productId);

        integration.deleteRecommendations(productId);

        integration.deleteReviews(productId);

        LOG.debug("getCompositeProduct: aggregate entities deleted for productId: {}", productId);
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {

        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
                recommendations.stream()
                .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                .toList();

        List<ReviewSummary> reviewSummaries = (reviews == null) ? null : reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                .toList();

        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
