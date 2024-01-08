package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionHandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactorRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReviewReactorRepository reviewReactorRepositoryMock;

    final static String REVIEWS_URI = "/v1/reviews";

    @Test
    void findAll() {
        Flux<Review> reviews = Flux.fromIterable(List.of(
                Review.builder().reviewId("723").movieInfoId(1L).comment("Great Movie").rating(4.2).build(),
                Review.builder().reviewId("17").movieInfoId(21L).comment("Bad Movie").rating(2.7).build(),
                Review.builder().reviewId("121").movieInfoId(13L).comment("Superb Movie").rating(4.6).build()));

        when(reviewReactorRepositoryMock.findAll()).thenReturn(reviews);

        webTestClient.get()
                .uri(REVIEWS_URI)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3)
                .contains(Review.builder().reviewId("723").movieInfoId(1L).comment("Great Movie").rating(4.2).build());
    }

    @Test
    void findReviewsByMovieInfoId() {
        Flux<Review> reviews = Flux.fromIterable(List.of(
                new Review("22", 19L, "Good movie", 4.3),
                new Review("81", 19L, "Great movie", 4.9),
                new Review("107", 19L, "Bad movie", 2.1)));

        when(reviewReactorRepositoryMock.findByMovieInfoId(anyLong())).thenReturn(reviews);

        webTestClient.get()
                .uri(REVIEWS_URI + "/19")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void findNonExistentReviewByMovieInfoId() {
        when(reviewReactorRepositoryMock.findByMovieInfoId(anyLong())).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(REVIEWS_URI + "/1")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void addReview() {
        Review review = new Review("22", 19L, "Good movie", 4.3);

        when(reviewReactorRepositoryMock.save(isA(Review.class))).thenReturn(Mono.just(review));

        webTestClient.post()
                .uri(REVIEWS_URI)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(res -> {
                    Review reviewResponse = res.getResponseBody();
                    Assertions.assertNotNull(reviewResponse);
                    Assertions.assertEquals("22", reviewResponse.getReviewId());
                });
    }

    @Test
    void addReviewValidation() {
        Review invalidReview = new Review(null, 0L, "", 5.1);

        webTestClient.post()
                .uri(REVIEWS_URI)
                .bodyValue(invalidReview)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void updateReview() {
        Mono<Review> review = Mono.just(new Review("1", 4L, "good", 2.2));

        when(reviewReactorRepositoryMock.findById(anyString())).thenReturn(review);
        when(reviewReactorRepositoryMock.save(isA(Review.class))).thenReturn(review);

        webTestClient.put()
                .uri(REVIEWS_URI + "/2")
                .bodyValue(new Review("1", 2L, "yo", 2.3))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(res -> {
                    Review reviewResponse = res.getResponseBody();
                    Assertions.assertNotNull(reviewResponse);
                    Assertions.assertEquals(2L, reviewResponse.getMovieInfoId());
                });
    }

    @Test
    void updateNonExistentReview() {
        when(reviewReactorRepositoryMock.findById(anyString())).thenReturn(Mono.empty());

        webTestClient.put()
                .uri(REVIEWS_URI)
                .bodyValue(new Review("99", 2L, "test", 2.2))
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void deleteById() {
        when(reviewReactorRepositoryMock.deleteById(anyString())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(REVIEWS_URI + "/123")
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
