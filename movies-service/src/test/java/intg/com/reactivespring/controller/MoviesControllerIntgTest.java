package com.reactivespring.controller;

import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl=http://localhost:8084/v1/movie-infos",
                "restClient.reviewsUrl=http://localhost:8084/v1/reviews"
        })
public class MoviesControllerIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void findMovieById() {
        String movieId = "97";

        stubFor(get(urlEqualTo("/v1/movie-infos/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("/movieInfo.json")));

        stubFor(get(urlEqualTo("/v1/reviews/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("/reviews.json")));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(res -> {
                    Movie movieRes = res.getResponseBody();
                    Assertions.assertNotNull(movieRes);
                    Assertions.assertEquals(2, movieRes.getReviewList().size());
                });
    }

    @Test
    void findMovieById404() {
        String movieId = "101";

        stubFor(get(urlEqualTo("/v1/movie-infos/" + movieId))
                .willReturn(aResponse().withStatus(404)));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .isEqualTo("Unable to find movie with id 101");
    }

    @Test
    void findMovieByIdWhereReviewsIs404() {
        String movieId = "97";

        stubFor(get(urlEqualTo("/v1/movie-infos/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieInfo.json")));

        stubFor(get(urlEqualTo("/v1/reviews/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movie.class)
                .consumeWith(res -> {
                    Movie movie = res.getResponseBody();
                    Assertions.assertNotNull(movie);
                    Assertions.assertEquals(0, movie.getReviewList().size());
                });
    }
}
