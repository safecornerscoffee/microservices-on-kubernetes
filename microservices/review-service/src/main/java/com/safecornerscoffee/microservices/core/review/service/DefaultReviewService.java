package com.safecornerscoffee.microservices.core.review.service;

import com.safecornerscoffee.microservices.api.core.review.Review;
import com.safecornerscoffee.microservices.api.core.review.ReviewService;
import com.safecornerscoffee.microservices.core.review.entity.ReviewEntity;
import com.safecornerscoffee.microservices.core.review.mapper.ReviewMapper;
import com.safecornerscoffee.microservices.core.review.repository.ReviewRepository;
import com.safecornerscoffee.microservices.util.exception.InvalidInputException;
import com.safecornerscoffee.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;

@RestController
public class DefaultReviewService implements ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultReviewService.class);

    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final ServiceUtil serviceUtil;
    private final Scheduler scheduler;

    public DefaultReviewService(ReviewRepository repository, ReviewMapper mapper, ServiceUtil serviceUtil, Scheduler scheduler) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
        this.scheduler = scheduler;
    }

    @Override
    public Mono<Review> createReview(Review body) {

        ReviewEntity entity = mapper.apiToEntity(body);
        return Mono.fromCallable(() -> repository.save(entity))
                .publishOn(scheduler)
                .onErrorMap(DataIntegrityViolationException.class, dive -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Review Id:" + body.getReviewId()))
                .map(mapper::entityToApi)
                .log();
    }


    @Override
    public Flux<Review> getReviews(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return asyncFlux(getByProductId(productId));
    }

    protected List<Review> getByProductId(int productId) {
        return repository.findByProductId(productId)
                .stream()
                .map(mapper::entityToApi)
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;})
                .toList();
    }

    private <T> Flux<T> asyncFlux(Iterable<T> iterable) {
        return Flux.fromIterable(iterable).publishOn(scheduler);
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        return  Mono.fromRunnable(() -> {
            repository.deleteAll(repository.findByProductId(productId));
        }).publishOn(scheduler).then();
    }
}
