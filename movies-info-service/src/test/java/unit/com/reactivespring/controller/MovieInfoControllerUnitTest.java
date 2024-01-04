package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MovieInfoController.class)
@AutoConfigureWebTestClient
public class MovieInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MovieInfoService movieInfoServiceMock;

    final static String MOVIE_INFO_ENDPOINT = "/v1/movie-infos";

    @Test
    void getAllMovieInfo() {
        List<MovieInfo> movieInfoData = List.of(
                new MovieInfo(null, "Batman Begins", 2005,
                        List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-09-15")),
                new MovieInfo("null", "The Dark Knight", 2008,
                        List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012,
                        List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        );

        when(movieInfoServiceMock.getAllMovieInfo()).thenReturn(Flux.fromIterable(movieInfoData));

        webTestClient.get()
                .uri(MOVIE_INFO_ENDPOINT)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        MovieInfo movie = new MovieInfo("abc", "Dark Knight Rises", 2012,
                List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        when(movieInfoServiceMock.getMovieInfoById("abc")).thenReturn(Mono.just(movie));

        webTestClient.get()
                .uri(MOVIE_INFO_ENDPOINT + "/abc")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(res -> {
                    MovieInfo movieReceived = res.getResponseBody();
                    Assertions.assertNotNull(movieReceived);
                    Assertions.assertEquals(2012, movieReceived.getYear());
                });

        // Another way
        webTestClient.get()
                .uri(MOVIE_INFO_ENDPOINT + "/abc")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.year").isEqualTo(2012);

        webTestClient.get()
                .uri(MOVIE_INFO_ENDPOINT + "/123")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(res -> {
                    MovieInfo movieReceived = res.getResponseBody();
                    Assertions.assertNull(movieReceived);
                });
    }

    @Test
    void addMovieInfo() {
        when(movieInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(
                Mono.just(new MovieInfo("mockId", "Movie", 2012,
                        List.of("Kieran", "Jasper"), LocalDate.parse("2012-02-13"))));

        webTestClient.post()
                .uri(MOVIE_INFO_ENDPOINT)
                .bodyValue(new MovieInfo(null, "Movie", 2023, List.of("K", "M"), LocalDate.now()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(res -> {
                    Assertions.assertNotNull(res.getResponseBody());
                    Assertions.assertEquals("mockId", res.getResponseBody().getMovieInfoId());
                });
    }

    @Test
    void addMovieInfoValidation() {
        MovieInfo body = new MovieInfo(null, "", -2023,
                List.of("K", "M"), LocalDate.parse("2023-12-10"));

        webTestClient.post()
                .uri(MOVIE_INFO_ENDPOINT)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void updateMovieInfo() {
        when(movieInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(
                Mono.just(new MovieInfo("1", "Movie",
                        2012, List.of("K", "M"), LocalDate.parse("2012-01-12"))));

        webTestClient.put()
                .uri(MOVIE_INFO_ENDPOINT + "/1")
                .bodyValue(new MovieInfo(null, "Movie", 2023, List.of("K", "M"), LocalDate.now()))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(res -> {
                    Assertions.assertNotNull(res.getResponseBody());
                    Assertions.assertEquals("1", res.getResponseBody().getMovieInfoId());
                });
    }

    @Test
    void deleteMovieInfo() {
        when(movieInfoServiceMock.deleteMovieInfo("1")).thenReturn(Mono.empty());
        webTestClient.delete()
                .uri(MOVIE_INFO_ENDPOINT + "/1")
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody(Mono.class)
                .consumeWith(res -> {
                    Assertions.assertNull(res.getResponseBody());
                });
    }
}
