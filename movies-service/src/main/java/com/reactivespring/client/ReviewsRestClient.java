package com.reactivespring.client;

import com.reactivespring.domain.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class ReviewsRestClient {

    private final WebClient webClient;

    @Value("${restClient.reviewsUrl}")
    private String REVIEWS_URI;

    public ReviewsRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> getReviews(String movieId) {
        String uri = REVIEWS_URI.concat("/{id}");

        return webClient
                .get()
                .uri(uri, movieId)
                .retrieve()
                .bodyToFlux(Review.class);
    }
}
