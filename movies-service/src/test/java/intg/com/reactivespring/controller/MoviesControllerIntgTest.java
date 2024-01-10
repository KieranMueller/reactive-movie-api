package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
                    assertNotNull(movieRes);
                    Assertions.assertEquals(2, movieRes.getReviewList().size());
                });
    }

    @Test
    @DisplayName("findMovieById when movie with ID does not exist")
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

        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movie-infos/" + movieId)));
    }

    @Test
    @DisplayName("findMovieById when movie-infos server is down")
    void findMovieById5xx() {
        stubFor(get(urlEqualTo("/v1/movie-infos/1"))
                .willReturn(aResponse().withStatus(500).withBody("server down")));

        webTestClient.get()
                .uri("/v1/movies/1")
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Error connecting to MoviesInfoService: server down");

        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movie-infos/1")));
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
                    assertNotNull(movie);
                    Assertions.assertEquals(0, movie.getReviewList().size());
                });
    }

    @Test
    void findMovieByIdWhenReviewsIsDown5xx() {
        String movieId = "97";

        stubFor(get(urlEqualTo("/v1/movie-infos/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieInfo.json")));

        stubFor(get(urlEqualTo("/v1/reviews/" + movieId))
                .willReturn(aResponse().withStatus(500).withBody("reviews server down")));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server exception in reviews service: reviews server down");

        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/reviews/" + movieId)));
    }
}
