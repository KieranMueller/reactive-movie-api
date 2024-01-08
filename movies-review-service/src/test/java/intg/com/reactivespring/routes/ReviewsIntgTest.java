package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactorRepository reviewReactorRepository;

    static final String REVIEWS_URI = "/v1/reviews";

    @BeforeEach
    void setUp() {
        List<Review> reviews = List.of(
                Review.builder().reviewId(null).movieInfoId(1L).comment("Great Movie").rating(4.2).build(),
                Review.builder().reviewId("17").movieInfoId(21L).comment("Bad Movie").rating(2.7).build(),
                Review.builder().reviewId("abc").movieInfoId(13L).comment("Superb Movie").rating(4.6).build());

        reviewReactorRepository.saveAll(reviews).blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactorRepository.deleteAll().block();
    }

    @Test
    void findAll() {
        webTestClient.get()
                .uri(REVIEWS_URI)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void findReviewsByMovieInfoId() {
        webTestClient.get()
                .uri(REVIEWS_URI + "/21")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(1);
    }

    @Test
    void findNonExistentReviewByMovieInfoId() {
        webTestClient.get()
                .uri(REVIEWS_URI + "/99")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void addReview() {
        Review review = new Review("123", 456L, "yo", 2.9);
        webTestClient.post()
                .uri(REVIEWS_URI)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(rev -> {
                    Assertions.assertNotNull(rev.getResponseBody());
                    Assertions.assertEquals("123", rev.getResponseBody().getReviewId());
                });
    }

    @Test
    void updateReview() {
        Review updatedReview = new Review(null, 10L, "Spectacular Movie", null);
        webTestClient.put()
                .uri(REVIEWS_URI + "/abc")
                .bodyValue(updatedReview)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(res -> {
                    Review reviewRes = res.getResponseBody();
                    Assertions.assertNotNull(reviewRes);
                    Assertions.assertEquals("abc", reviewRes.getReviewId());
                    Assertions.assertEquals(10L, reviewRes.getMovieInfoId());
                    Assertions.assertEquals("Spectacular Movie", reviewRes.getComment());
                    Assertions.assertEquals(4.6, reviewRes.getRating());
                });
    }

    @Test
    void updateNonExistentReview() {
        Review review = new Review("99", 2L, "test", 2.2);

        webTestClient.put()
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void deleteById() {
        webTestClient.delete()
                .uri(REVIEWS_URI + "/abc")
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
