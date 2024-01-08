package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    @Autowired
    private Validator validator;

    private final ReviewReactorRepository reviewReactorRepository;

    public ReviewHandler(ReviewReactorRepository reviewReactorRepository) {
        this.reviewReactorRepository = reviewReactorRepository;
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        Flux<Review> reviews = reviewReactorRepository.findAll();
        return ServerResponse.ok().body(reviews, Review.class);
    }

    public Mono<ServerResponse> findReviewsByMovieInfoId(ServerRequest request) {
        Flux<Review> reviews =
                reviewReactorRepository.findByMovieInfoId(Long.valueOf(request.pathVariable("id")))
                        .switchIfEmpty(Mono.error(new ReviewNotFoundException(
                "Review not found for id " + request.pathVariable("id"))));
        return ServerResponse.ok().body(reviews, Review.class);
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(review -> validate(review))
                .flatMap(reviewReactorRepository::save)
                .flatMap(savedReview -> ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview));
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        Mono<Review> exReview = reviewReactorRepository.findById(request.pathVariable("id"))
                .switchIfEmpty(Mono.error(new ReviewNotFoundException(
                        "Review not found for id " + request.pathVariable("id"))));

        return exReview.flatMap(exRev -> request.bodyToMono(Review.class)
                        .map(newRev -> {
                            if (newRev.getMovieInfoId() != null && newRev.getMovieInfoId() > 0)
                                exRev.setMovieInfoId(newRev.getMovieInfoId());
                            if (!(newRev.getComment() == null || newRev.getComment().isBlank()))
                                exRev.setComment(newRev.getComment());
                            if (newRev.getRating() != null && newRev.getRating() > 0)
                                exRev.setRating(newRev.getRating());
                            return exRev;
                        }))
                .flatMap(reviewReactorRepository::save)
                .flatMap(updatedReview -> ServerResponse.status(HttpStatus.OK).bodyValue(updatedReview));
    }

    public Mono<ServerResponse> deleteById(ServerRequest request) {
        reviewReactorRepository.deleteById(request.pathVariable("id"));
        return ServerResponse.noContent().build();
    }

    private void validate(Review review) {
        Set<ConstraintViolation<Review>> violations = validator.validate(review);

        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessage);
        }
    }
}
