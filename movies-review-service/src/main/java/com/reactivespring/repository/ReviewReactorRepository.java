package com.reactivespring.repository;

import com.reactivespring.domain.Review;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ReviewReactorRepository extends ReactiveMongoRepository<Review, String> {

    Flux<Review> findByMovieInfoId(Long id);
}
