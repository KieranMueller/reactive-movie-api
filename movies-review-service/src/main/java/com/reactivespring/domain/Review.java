package com.reactivespring.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Review {

    @Id
    private String reviewId;

    @NotNull(message = "Review.movieInfoId must not be null")
    @DecimalMin(value = "1", message = "Review.movieInfoId must be 1 or greater")
    private Long movieInfoId;

    @NotNull(message = "Review.comment must be present")
    @NotBlank(message = "Review.comment must be present")
    private String comment;

    @DecimalMin(value = "0.0", message = "Review.rating must be between 0 - 5")
    @DecimalMax(value = "5.0", message = "Review.rating must be between 0 - 5")
    private Double rating;
}
