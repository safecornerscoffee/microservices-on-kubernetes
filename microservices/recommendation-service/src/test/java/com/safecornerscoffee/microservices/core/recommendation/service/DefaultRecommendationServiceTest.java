package com.safecornerscoffee.microservices.core.recommendation.service;

import com.safecornerscoffee.microservices.api.core.recommendation.Recommendation;
import com.safecornerscoffee.microservices.core.recommendation.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@AutoConfigureWebTestClient
class DefaultRecommendationServiceTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RecommendationRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void getRecommendationsByProductId() {

        int productId = 1;

        postAndVerifyRecommendation(productId, 1, OK);
        postAndVerifyRecommendation(productId, 2, OK);
        postAndVerifyRecommendation(productId, 3, OK);

        assertThat(repository.findByProductId(productId).collectList().block()).hasSize(3);

        getAndVerifyRecommendation(productId, OK);
    }

    @Test
    void duplicateError() {
        int productId = 1;
        int recommendationId = 1;

        postAndVerifyRecommendation(productId, recommendationId , OK)
                .jsonPath("$.productId").isEqualTo(productId)
                .jsonPath("$.recommendationId").isEqualTo(recommendationId);

        assertThat(repository.count()).isEqualTo(1);

        postAndVerifyRecommendation(productId, recommendationId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Recommendation Id:1");

        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void deleteRecommendations() {
        int productId = 1;
        int recommendationId = 1;

        postAndVerifyRecommendation(productId, recommendationId, OK);
        assertThat(repository.findByProductId(productId).collectList().block()).hasSize(1);

        deleteAndVerifyRecommendationsByProductId(productId, OK);
        assertThat(repository.findByProductId(productId).collectList().block()).isEmpty();

        deleteAndVerifyRecommendationsByProductId(productId, OK);
    }

    @Test
    void getRecommendationsMissingParameter() {

        getAndVerifyRecommendation("", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
    }

    @Test
    void getRecommendationsInvalidParameter() {

        getAndVerifyRecommendation("?productId=no-integer", BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getRecommendationsNotFound() {

        int productIdNotFound = 113;

        getAndVerifyRecommendation(productIdNotFound, OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getRecommendationsInvalidParameterNegativeValue() {

        int productIdInvalid = -1;

        getAndVerifyRecommendation(productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendation(int productId, HttpStatus expectedStatus) {
        return getAndVerifyRecommendation("?productId=" + productId, expectedStatus);
    }
    private WebTestClient.BodyContentSpec getAndVerifyRecommendation(String productIdQuery, HttpStatus expectedStatus) {
        return webTestClient.get()
                .uri("/recommendation" + productIdQuery)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productId, int recommendationId, HttpStatus expectedStatus) {
        Recommendation recommendation = new Recommendation(productId, recommendationId, "Author " + recommendationId, recommendationId, "Content" + recommendationId, "sa");
        return webTestClient.post()
                .uri("/recommendation")
                .body(Mono.just(recommendation), Recommendation.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
        return webTestClient.delete()
                .uri("/recommendation?productId=" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}