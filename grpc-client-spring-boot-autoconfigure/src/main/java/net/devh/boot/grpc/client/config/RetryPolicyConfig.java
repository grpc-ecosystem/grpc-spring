/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.devh.boot.grpc.client.config;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.Min;

import org.springframework.boot.convert.DurationUnit;

import io.grpc.Status;
import io.grpc.Status.Code;
import lombok.Data;

/**
 * The retry policy config.
 *
 * <p>
 * For the exact specification refer to:
 * <a href="https://github.com/grpc/proposal/blob/master/A6-client-retries.md">A6-client-retries</a>
 * </p>
 *
 * @author wushengju
 */
@Data
public class RetryPolicyConfig {

    /**
     * The maximum number of RPC attempts, including the original RPC. This field is required and must be one or
     * greater. A value of {@code 1} indicates, no retries after the initial call.
     */
    @Min(1)
    private int maxAttempts;

    /**
     * Exponential backoff parameter: Defines the upper limit for the first backoff time.
     *
     * <p>
     * The initial retry attempt will occur at {@code random(0, initialBackoff)}. In general, the n-th attempt since the
     * last server pushback response (if any), will occur at
     * {@code random(0, min(initialBackoff*backoffMultiplier**(n-1), maxBackoff))}.
     * </p>
     *
     * <p>
     * Default unit seconds. Must be greater than zero.
     * </p>
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration initialBackoff;

    /**
     * Exponential backoff parameter: The upper duration limit for backing off an attempt.
     *
     * <p>
     * Default unit seconds. Must be greater than zero.
     * </p>
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration maxBackoff;

    /**
     * Exponential backoff parameter: The multiplier to apply to the (initial) backoff. Values below {@code 1.0}, will
     * result in faster retries the more attempts have been made. {@code 1.0} will result in averagely same retry
     * frequency. Values above {@code 1.0} will slow down the requests with each attempt.
     *
     * <p>
     * Must be greater than zero.
     * </p>
     */
    private double backoffMultiplier;

    /**
     * The set of status codes which may be retried. Status codes are specified in the integer form or the
     * case-insensitive string form (e.g. {@code 14}, {@code "UNAVAILABLE"} or {@code "unavailable"}). This field is
     * required and must be non-empty.
     */
    private Set<Status.Code> retryableStatusCodes;

    /**
     * Creates a copy of this instance.
     *
     * @return The newly created copy.
     */
    public RetryPolicyConfig copy() {
        final RetryPolicyConfig copy = new RetryPolicyConfig();
        copy.maxAttempts = this.maxAttempts;
        copy.initialBackoff = this.initialBackoff;
        copy.maxBackoff = this.maxBackoff;
        copy.backoffMultiplier = this.backoffMultiplier;
        copy.retryableStatusCodes =
                new LinkedHashSet<>(requireNonNull(this.retryableStatusCodes, "retryableStatusCodes"));
        return copy;
    }

    /**
     * Builds a json like map from this instance.
     *
     * @return The json like map representation of this instance.
     */
    public Map<String, Object> buildMap() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("maxAttempts", (double) this.maxAttempts);
        map.put("initialBackoff", formatDuration(requireNonNull(this.initialBackoff, "initialBackoff")));
        map.put("maxBackoff", formatDuration(requireNonNull(this.maxBackoff, "maxBackoff")));
        map.put("backoffMultiplier", this.backoffMultiplier);
        map.put("retryableStatusCodes", requireNonNull(this.retryableStatusCodes, "retryableStatusCodes").stream()
                .map(Code::name)
                .collect(Collectors.toList()));
        return map;
    }

    private static String formatDuration(final Duration duration) {
        if (duration.getNano() == 0) {
            // 1s
            return duration.getSeconds() + "s";
        } else {
            // 1.2s
            return String.format("%d.%09ds", duration.getSeconds(), duration.getNano()).replaceAll("0+s", "s");
        }
    }

}
