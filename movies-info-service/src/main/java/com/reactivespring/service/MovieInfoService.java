package com.reactivespring.service;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@AllArgsConstructor
public class MovieInfoService {

    private MovieInfoRepository movieInfoRepository;

    public Flux<MovieInfo> getAllMovieInfo() {
        return movieInfoRepository.findAll();
    }

    public Flux<MovieInfo> getAllMovieInfosByYear(Integer year) {
        return movieInfoRepository.findByYear(year);
    }

    public Mono<MovieInfo> getMovieInfoById(String id) {
        return movieInfoRepository.findById(id);
    }

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
        log.info("Saving addMovieInfo request with body {}", movieInfo);
        return movieInfoRepository.save(movieInfo);
    }

    public Mono<MovieInfo> updateMovieInfo(MovieInfo movieInfo, String id) {
        return movieInfoRepository.findById(id)
                .flatMap(movie -> {
                    if (!(movieInfo.getName() == null || movieInfo.getName().isBlank()))
                        movie.setName(movieInfo.getName());
                    if (!(movieInfo.getYear() == null || movieInfo.getYear() < 1900))
                        movie.setYear(movieInfo.getYear());
                    if (!(movieInfo.getCast() == null || movieInfo.getCast().isEmpty()))
                        movie.setCast(movieInfo.getCast());
                    if (!(movieInfo.getReleaseDate() == null))
                        movie.setReleaseDate(movieInfo.getReleaseDate());
                    return movieInfoRepository.save(movie);
                });
    }

    public Mono<Void> deleteMovieInfo(String id) {
        return movieInfoRepository.deleteById(id);
    }
}
