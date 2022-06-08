package com.safecornerscoffee.microservices.composite.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safecornerscoffee.microservices.api.core.product.Product;
import com.safecornerscoffee.microservices.api.core.product.ProductService;
import com.safecornerscoffee.microservices.api.core.recommendation.Recommendation;
import com.safecornerscoffee.microservices.api.core.recommendation.RecommendationService;
import com.safecornerscoffee.microservices.api.core.review.Review;
import com.safecornerscoffee.microservices.api.core.review.ReviewService;
import com.safecornerscoffee.microservices.util.exception.InvalidInputException;
import com.safecornerscoffee.microservices.util.exception.NotFoundException;
import com.safecornerscoffee.microservices.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") int productServicePort,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort
    ) {

        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
        this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
        this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";

    }

    @Override
    public Product getProduct(int productId) {
        try {
            String url = productServiceUrl + productId;
            LOG.debug("Will call getProduct API on URL: {}", url);

            Product product = restTemplate.getForObject(url, Product.class);
            LOG.debug("Found a product with id: {}", product.getProductId());

            return product;

        } catch (HttpClientErrorException e){

            switch (e.getStatusCode()) {

                case NOT_FOUND:
                    throw new NotFoundException(getErrorMessage(e));

                case UNPROCESSABLE_ENTITY :
                    throw new InvalidInputException(getErrorMessage(e));

                default:
                    LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", e.getStatusCode());
                    LOG.warn("Error body: {}", e.getResponseBodyAsString());
                    throw e;
            }
        }
    }

    private String getErrorMessage(HttpClientErrorException e) {
        try {
            return objectMapper.readValue(e.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioException) {
            return e.getMessage();
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + productId;

            LOG.debug("Will call getRecommendations API on URL: {}", url);
            List<Recommendation> recommendations = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {}).getBody();

            LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
            return recommendations;

        } catch (Exception e) {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + productId;

            LOG.debug("Will call getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {}).getBody();

            LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
            return reviews;

        } catch (Exception e) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
