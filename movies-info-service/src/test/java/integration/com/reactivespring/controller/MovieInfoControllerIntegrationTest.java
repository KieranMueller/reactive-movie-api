package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MovieInfoControllerIntegrationTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    final static String MOVIE_INFO_ENDPOINT = "/v1/movie-infos";

    @BeforeEach
    void setUp() {
        List<MovieInfo> movieInfoData = List.of(
                new MovieInfo(null, "Batman Begins", 2005,
                        List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-09-15")),
                new MovieInfo("null", "The Dark Knight", 2008,
                        List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012,
                        List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        );
        movieInfoRepository.saveAll(movieInfoData).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void getAllMovieInfo() {
        webTestClient.get()
                .uri(MOVIE_INFO_ENDPOINT)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getAllMovieInfoByYear() {
        URI uri = UriComponentsBuilder.fromUriString(MOVIE_INFO_ENDPOINT)
                .queryParam("year", 2005)
                .buildAndExpand().toUri();

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);

    }

    @Test
    void getMovieInfoById() {
        String id = "abc";

        webTestClient.get()
                .uri(MOVIE_INFO_ENDPOINT + "/" + id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(res -> {
                    MovieInfo movie = res.getResponseBody();
                    assertNotNull(movie);
                    assertEquals(id, movie.getMovieInfoId());
                });
    }

    @Test
    void getNonExistentMovieInfoById() {
        webTestClient.get()
                .uri(MOVIE_INFO_ENDPOINT + "/nope")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void addMovieInfo() {
        MovieInfo movie = new MovieInfo(null, "Jasper Movie", 2005,
                List.of("Jasper", "Kieran"), LocalDate.parse("2005-09-15"));

        webTestClient.post()
                .uri(MOVIE_INFO_ENDPOINT)
                .bodyValue(movie)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(res -> {
                    MovieInfo movieReceived = res.getResponseBody();
                    assertNotNull(movieReceived);
                    assertNotNull(movieReceived.getMovieInfoId());
                    System.out.println(movieReceived);
                });
    }

    @Test
    void updateMovieInfo() {
        String id = "abc";
        MovieInfo movie = new MovieInfo("123", "new movie",
                2023, List.of("person 1", "person 2"), LocalDate.parse("2023-10-21"));

        webTestClient.put()
                .uri(MOVIE_INFO_ENDPOINT + "/" + id)
                .bodyValue(movie)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(res -> {
                    MovieInfo updatedMovie = res.getResponseBody();
                    assertNotNull(updatedMovie);
                    assertEquals(id, updatedMovie.getMovieInfoId());
                    assertEquals("new movie", updatedMovie.getName());
                    assertEquals(2023, updatedMovie.getYear());
                    assertEquals(List.of("person 1", "person 2"), updatedMovie.getCast());
                    assertEquals(LocalDate.parse("2023-10-21"), updatedMovie.getReleaseDate());
                });
    }

    @Test
    void updateNonExistentMovie() {
        webTestClient.put()
                .uri(MOVIE_INFO_ENDPOINT + "/nope")
                .bodyValue(new MovieInfo(null, "Japs",
                        2023, List.of("K", "M"), LocalDate.parse("2023-05-22")))
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void deleteMovieInfo() {
        String id = "abc";

        webTestClient.get()
                .uri(MOVIE_INFO_ENDPOINT + "/" + id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(res -> {
                    MovieInfo movie = res.getResponseBody();
                    assertNotNull(movie);
                });

        webTestClient.delete()
                .uri(MOVIE_INFO_ENDPOINT + "/" + id)
                .exchange()
                .expectStatus()
                .isNoContent();

        webTestClient.get()
                .uri(MOVIE_INFO_ENDPOINT + "/" + id)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void findByYear() {
        movieInfoRepository.save(new MovieInfo(null, "test1",
                2012, List.of("K", "M"), LocalDate.parse("2020-10-11"))).block();

        Flux<MovieInfo> movies = movieInfoRepository.findByYear(2012).log();
        StepVerifier.create(movies)
                .assertNext(movie -> {
                    Assertions.assertNotNull(movie);
                    assertEquals(2012, movie.getYear());
                }).expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByName() {
        Mono<MovieInfo> movies = movieInfoRepository.findByName("Dark Knight Rises").log();
        StepVerifier.create(movies)
                .expectNextCount(1)
                .verifyComplete();
    }
}
