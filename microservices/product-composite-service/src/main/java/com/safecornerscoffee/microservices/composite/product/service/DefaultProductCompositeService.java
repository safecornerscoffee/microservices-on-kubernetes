package com.safecornerscoffee.microservices.composite.product.service;

import com.safecornerscoffee.microservices.api.composite.product.*;
import com.safecornerscoffee.microservices.api.core.product.Product;
import com.safecornerscoffee.microservices.api.core.recommendation.Recommendation;
import com.safecornerscoffee.microservices.api.core.review.Review;
import com.safecornerscoffee.microservices.util.exception.NotFoundException;
import com.safecornerscoffee.microservices.util.http.ServiceUtil;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DefaultProductCompositeService implements ProductCompositeService {

    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    public DefaultProductCompositeService(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public ProductAggregate getProduct(int productId) {

        Product product = integration.getProduct(productId);
        if (product == null) throw new NotFoundException("No product found for productId:" + productId);

        List<Recommendation> recommendations = integration.getRecommendations(productId);

        List<Review> reviews = integration.getReviews(productId);

        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {

        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null : recommendations.stream()
                .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate()))
                .collect(Collectors.toList());

        List<ReviewSummary> reviewSummaries = (reviews == null) ? null : reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject()))
                .collect(Collectors.toList());

        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
