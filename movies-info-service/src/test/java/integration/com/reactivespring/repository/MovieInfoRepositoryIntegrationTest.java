package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntegrationTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

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
    void findAll() {
        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findAll().log();
        StepVerifier.create(movieInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        Mono<MovieInfo> movieInfo = movieInfoRepository.findById("abc").log();
        StepVerifier.create(movieInfo)
                .assertNext(movie -> {
                    assertEquals(2012, movie.getYear());
                }).verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        MovieInfo movieInfo = new MovieInfo(null, "Jasper Time",
                2023, List.of("Jasper", "Kieran", "Terra"), LocalDate.parse("2023-10-14"));
        Mono<MovieInfo> savedMovieInfo = movieInfoRepository.save(movieInfo).log();
        StepVerifier.create(savedMovieInfo)
                .assertNext(movie -> {
                    assertNotNull(movie.getMovieInfoId());
                }).verifyComplete();
    }

    @Test
    void updateExistingMovieInfo() {
        MovieInfo movieInfo = movieInfoRepository.findById("abc").block();
        assertNotNull(movieInfo);
        movieInfo.setYear(2013);
        Mono<MovieInfo> savedMovieInfo = movieInfoRepository.save(movieInfo);
        StepVerifier.create(savedMovieInfo)
                .assertNext(movie -> {
                    assertEquals(2013, movie.getYear());
                }).verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        assertNotNull(movieInfoRepository.findById("abc").block());
        movieInfoRepository.deleteById("abc").block();
        assertNull(movieInfoRepository.findById("abc").block());
    }
}
