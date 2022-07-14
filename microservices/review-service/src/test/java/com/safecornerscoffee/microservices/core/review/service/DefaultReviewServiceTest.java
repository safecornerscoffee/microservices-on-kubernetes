package com.safecornerscoffee.microservices.core.review.service;

import com.safecornerscoffee.microservices.api.core.review.Review;
import com.safecornerscoffee.microservices.core.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:review-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "eureka.client.enabled=false"
})
class DefaultReviewServiceTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }
    @Test
    void getReviewsByProductId() {

        int productId = 1;

        assertThat(repository.findByProductId(productId)).isEmpty();

        postAndVerifyReview(productId, 1, OK);
        postAndVerifyReview(productId, 2, OK);
        postAndVerifyReview(productId, 3, OK);

        assertThat(repository.findByProductId(productId)).hasSize(3);

        getAndVerifyReviewsByProductId(productId, OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productId").isEqualTo(productId)
                .jsonPath("$[2].reviewId").isEqualTo(3);
    }

    @Test
    void duplicateError() {

        int productId = 1;
        int reviewId = 1;

        assertThat(repository.count()).isZero();

        postAndVerifyReview(productId, reviewId, OK)
                .jsonPath("$.productId").isEqualTo(productId)
                .jsonPath("$.reviewId").isEqualTo(reviewId);

        assertThat(repository.count()).isEqualTo(1);

        postAndVerifyReview(productId, reviewId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Review Id:1");

        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void deleteReviews() {

        int productId = 1;
        int recommendationId = 1;

        postAndVerifyReview(productId, recommendationId, OK);
        assertThat(repository.findByProductId(productId)).hasSize(1);

        deleteAndVerifyReviewsByProductId(productId, OK);
        assertThat(repository.findByProductId(productId)).isEmpty();

        deleteAndVerifyReviewsByProductId(productId, OK);
    }

    @Test
    void getReviewsMissingParameter() {

        getAndVerifyReviewsByProductId("", BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
    }

    @Test
    void getReviewsInvalidParameter() {

        getAndVerifyReviewsByProductId("?productId=no-integer", BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getReviewsNotFound() {

        int productIdNotFound = 213;

        getAndVerifyReviewsByProductId(productIdNotFound, OK)
            .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getReviewsInvalidParameterNegativeValue() {

        int productIdInvalid = -1;

        getAndVerifyReviewsByProductId(productIdInvalid, UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
        return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productIdQuery, HttpStatus expectedStatus) {
        return webTestClient.get()
                .uri("/review" + productIdQuery)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyReview(int productId, int reviewId, HttpStatus expectedStatus) {
        Review review = new Review(productId, reviewId,
                "a" + reviewId, "s" + reviewId, "c" + reviewId, "sa");
        return webTestClient.post()
                .uri("/review")
                .body(Mono.just(review), Review.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
        return webTestClient.delete()
                .uri("/review?productId=" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}