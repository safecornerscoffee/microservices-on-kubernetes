package com.safecornerscoffee.microservices.core.review.queue;

import com.safecornerscoffee.microservices.api.core.review.Review;
import com.safecornerscoffee.microservices.api.core.review.ReviewService;
import com.safecornerscoffee.microservices.api.event.Event;
import com.safecornerscoffee.microservices.util.exception.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@EnableBinding(Sink.class)
public class MessageProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final ReviewService reviewService;

    public MessageProcessor(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Review> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getType()) {

            case CREATE:
                Review review = event.getData();
                LOG.info("Create review with ID: {}/{}", review.getProductId(), review.getReviewId());
                reviewService.createReview(review).subscribe();
                break;

            case DELETE:
                int productId = event.getKey();
                LOG.info("Delete reviews with ProductID: {}", productId);
                reviewService.deleteReviews(productId).subscribe();
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
