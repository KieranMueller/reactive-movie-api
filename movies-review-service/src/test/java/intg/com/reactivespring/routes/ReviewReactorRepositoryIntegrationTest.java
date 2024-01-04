package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@DataMongoTest
@ActiveProfiles("test")
public class ReviewReactorRepositoryIntegrationTest {

    @Autowired
    ReviewReactorRepository reviewReactorRepository;

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
        Flux<Review> reviews = reviewReactorRepository.findAll();
        StepVerifier.create(reviews)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        Mono<Review> review = reviewReactorRepository.findById("17");
        StepVerifier.create(review)
                .consumeNextWith(rev -> {
                    Assertions.assertNotNull(rev);
                    Assertions.assertEquals("17", rev.getReviewId());
                }).verifyComplete();
    }

    @Test
    void findByMovieInfoId() {
        Flux<Review> reviews = reviewReactorRepository.findByMovieInfoId(21L);
        StepVerifier.create(reviews)
                .expectNextCount(1)
                .verifyComplete();
    }
}
