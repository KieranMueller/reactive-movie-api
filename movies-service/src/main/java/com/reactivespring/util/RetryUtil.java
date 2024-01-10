package com.reactivespring.util;

import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsServerException;
import reactor.core.Exceptions;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

public class RetryUtil {

    public static RetryBackoffSpec retryBackoffSpec() {
        return Retry.backoff(3, Duration.ofMillis(300))
                .filter(e -> e instanceof MoviesInfoServerException || e instanceof ReviewsServerException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure()));
    }
}
