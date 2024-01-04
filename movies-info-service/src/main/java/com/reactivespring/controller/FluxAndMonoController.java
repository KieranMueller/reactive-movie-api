package com.reactivespring.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class FluxAndMonoController {

    @GetMapping(value = "flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Integer> getFlux() {
        return Flux
                .just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .delayElements(Duration.ofMillis(200))
                .log();
    }

    @GetMapping("mono")
    public Mono<String> getMono() {
        return Mono.just("Jasper").log();
    }

    @GetMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> stream() {
        return Flux
                .interval(Duration.ofSeconds(1))
                .log();
    }

    @GetMapping(value = "otherStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> otherStream() {
        return Flux
                .interval(Duration.ofMillis(200))
                .log();
    }
}
