package com.reactivespring.controller;

import com.reactivespring.client.MoviesInfoRestClient;
import com.reactivespring.client.ReviewsRestClient;
import com.reactivespring.domain.Movie;
import com.reactivespring.domain.Review;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/v1/movies")
public class MoviesController {

    private final MoviesInfoRestClient moviesInfoRestClient;
    private final ReviewsRestClient reviewsRestClient;

    public MoviesController(MoviesInfoRestClient moviesInfoRestClient, ReviewsRestClient reviewsRestClient) {
        this.moviesInfoRestClient = moviesInfoRestClient;
        this.reviewsRestClient = reviewsRestClient;
    }

    @GetMapping("/{id}")
    public Mono<Movie> getMovieById(@PathVariable("id") String id) {

        return moviesInfoRestClient.getMovieInfo(id)
                .flatMap(movieInfo -> {
                    Mono<List<Review>> reviewsListMono = reviewsRestClient.getReviews(id).collectList();
                    return reviewsListMono.map(reviewList -> new Movie(movieInfo, reviewList));
                });
    }
}
