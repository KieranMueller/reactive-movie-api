package com.reactivespring.router;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewsRoute(ReviewHandler reviewHandler) {
//        one way of doing
//        return route()
//                .GET("/v1/reviews", request -> reviewHandler.findAll(request))
//                .POST("/v1/reviews", request -> reviewHandler.addReview(request))
//                .build();

        return route()
                .nest(path("/v1/reviews"), builder -> {
                    builder
                            .GET("", request -> reviewHandler.findAll(request))
                            .GET("/{id}", request -> reviewHandler.findReviewsByMovieInfoId(request))
                            .POST("", request -> reviewHandler.addReview(request))
                            .PUT("/{id}", request -> reviewHandler.updateReview(request))
                            .DELETE("/{id}", request -> reviewHandler.deleteById(request));
                }).build();
    }
}
