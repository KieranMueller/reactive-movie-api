package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReviewHandler {

    private ReviewReactorRepository reviewReactorRepository;

    public ReviewHandler(ReviewReactorRepository reviewReactorRepository) {
        this.reviewReactorRepository = reviewReactorRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .flatMap(reviewReactorRepository::save)
                .flatMap(savedReview -> ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview));
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        Flux<Review> reviews = reviewReactorRepository.findAll();
        return ServerResponse.ok().body(reviews, Review.class);
    }

    public Mono<ServerResponse> findReviewsByMovieInfoId(ServerRequest request) {
        Flux<Review> reviews =
                reviewReactorRepository.findByMovieInfoId(Long.valueOf(request.pathVariable("id")));
        return ServerResponse.ok().body(reviews, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        Mono<Review> exReview = reviewReactorRepository.findById(request.pathVariable("id"));
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
}
