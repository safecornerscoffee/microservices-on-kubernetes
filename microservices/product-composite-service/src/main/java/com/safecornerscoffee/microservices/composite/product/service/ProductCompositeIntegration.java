package com.safecornerscoffee.microservices.composite.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safecornerscoffee.microservices.api.core.product.Product;
import com.safecornerscoffee.microservices.api.core.product.ProductService;
import com.safecornerscoffee.microservices.api.core.recommendation.Recommendation;
import com.safecornerscoffee.microservices.api.core.recommendation.RecommendationService;
import com.safecornerscoffee.microservices.api.core.review.Review;
import com.safecornerscoffee.microservices.api.core.review.ReviewService;
import com.safecornerscoffee.microservices.api.event.Event;
import com.safecornerscoffee.microservices.util.exception.InvalidInputException;
import com.safecornerscoffee.microservices.util.exception.NotFoundException;
import com.safecornerscoffee.microservices.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static com.safecornerscoffee.microservices.api.event.Event.Type.CREATE;
import static com.safecornerscoffee.microservices.api.event.Event.Type.DELETE;

@EnableBinding(ProductCompositeIntegration.MessageSources.class)
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final static String scheme = "http://";
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    private MessageSources messageSources;
    public interface MessageSources {

        String OUTPUT_PRODUCTS = "output-products";
        String OUTPUT_RECOMMENDATIONS = "output-recommendations";
        String OUTPUT_REVIEWS = "output-reviews";

        @Output(OUTPUT_PRODUCTS)
        MessageChannel outputProducts();

        @Output(OUTPUT_RECOMMENDATIONS)
        MessageChannel outputRecommendations();
        @Output(OUTPUT_REVIEWS)
        MessageChannel outputReviews();
    }
    public ProductCompositeIntegration(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            MessageSources messageSources,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") int productServicePort,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort
    ) {

        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.messageSources = messageSources;

        this.productServiceUrl = scheme + productServiceHost + ":" + productServicePort + "/product";
        this.recommendationServiceUrl = scheme + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
        this.reviewServiceUrl = scheme + reviewServiceHost + ":" + reviewServicePort + "/review";

    }

    @Override
    public Mono<Product> createProduct(Product body) {

        messageSources.outputProducts().send(
                MessageBuilder.withPayload(new Event<>(CREATE, body.getProductId(), body)).build()
        );

        return Mono.just(body);

    }

    @Override
    public Mono<Product> getProduct(int productId) {
        String url = productServiceUrl + "/" + productId;

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .log()
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {

        messageSources.outputProducts().send(
                MessageBuilder.withPayload(new Event<>(DELETE, productId, null)).build()
        );

        return Mono.never().then();
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {

        messageSources.outputRecommendations().send(
                MessageBuilder.withPayload(
                        new Event<>(CREATE, body.getProductId(), body)
                ).build()
        );

        return Mono.just(body);

    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        String url = recommendationServiceUrl + "?productId=" + productId;

        LOG.debug("Will call the getRecommendations API on URL: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .onErrorResume(error -> Flux.empty());
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        messageSources.outputRecommendations().send(
                MessageBuilder.withPayload(new Event<>(DELETE, productId, null)).build());
        return Mono.never().then();
    }

    @Override
    public Mono<Review> createReview(Review body) {
        messageSources.outputReviews().send(
                MessageBuilder.withPayload(new Event<>(CREATE, body.getProductId(), body)).build());
        return Mono.just(body);
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        String url = reviewServiceUrl + "?productId=" + productId;

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class)
                .onErrorResume(error -> Flux.empty());

    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        messageSources.outputReviews().send(
                MessageBuilder.withPayload(new Event<>(DELETE, productId, null)).build()
        );
        return Mono.never().then();
    }

    private Throwable handleException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;

        switch (wcre.getStatusCode()) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));

            case UNPROCESSABLE_ENTITY :
                return new InvalidInputException(getErrorMessage(wcre));

            default:
                LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }
}
