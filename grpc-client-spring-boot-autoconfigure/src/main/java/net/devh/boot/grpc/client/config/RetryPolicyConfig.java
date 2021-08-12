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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.springframework.boot.convert.DurationUnit;
import org.springframework.util.CollectionUtils;

import io.grpc.Status;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The retry policy config.
 *
 * @author wushengju
 * @since 8/12/2021
 */
@ToString
@EqualsAndHashCode
public class RetryPolicyConfig {
    /**
     * The maximum number of RPC attempts, including the original RPC. This field is required and must be two or
     * greater.
     */
    private Double maxAttempts;
    /**
     * Exponential backoff parameters. The initial retry attempt will occur at random(0, initialBackoff). In general,
     * the nth attempt since the last server pushback response (if any), will occur at random(0,
     * min(initialBackoff*backoffMultiplier**(n-1), maxBackoff)). Exponential backoff parameters,Duration is seconds
     * Required. Must be greater than zero
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration initialBackoff;
    /**
     * Required. Must be greater than zero,Duration is seconds
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration maxBackoff;
    /**
     * Required. Must be greater than zero.
     */
    private Double backoffMultiplier;
    /**
     * The set of status codes which may be retried. Status codes are specified in the integer form or the
     * case-insensitive string form (eg. [14], ["UNAVAILABLE"] or ["unavailable"]) This field is required and must be
     * non-empty.
     */
    private Set<Status.Code> retryableStatusCodes;

    public Map<String, Object> buildMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("maxAttempts", this.getMaxAttempts());
        map.put("initialBackoff", String.format("%ss", this.getInitialBackoff().getSeconds()));
        map.put("maxBackoff", String.format("%ss", this.getMaxBackoff().getSeconds()));
        map.put("backoffMultiplier", Double.valueOf(this.getBackoffMultiplier()));
        List<String> statusCodesList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(this.getRetryableStatusCodes())) {
            this.getRetryableStatusCodes().forEach(code -> statusCodesList.add(code.name()));
        }
        map.put("retryableStatusCodes", statusCodesList);
        return map;
    }

    public void copyDefaultsFrom(final RetryPolicyConfig config) {
        if (this == config) {
            return;
        }
        if (this.maxAttempts == null) {
            this.maxAttempts = config.maxAttempts;
        }
        if (this.initialBackoff == null) {
            this.initialBackoff = config.initialBackoff;
        }
        if (this.maxBackoff == null) {
            this.maxBackoff = config.maxBackoff;
        }
        if (this.backoffMultiplier == null) {
            this.backoffMultiplier = config.backoffMultiplier;
        }
        if (this.retryableStatusCodes == null || retryableStatusCodes.isEmpty()) {
            this.retryableStatusCodes = new HashSet<>();
            if (config.retryableStatusCodes != null && !config.retryableStatusCodes.isEmpty()) {
                this.retryableStatusCodes.addAll(config.retryableStatusCodes);
            }
        }
    }

    public Double getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Double maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Duration getInitialBackoff() {
        return initialBackoff;
    }

    public void setInitialBackoff(Duration initialBackoff) {
        this.initialBackoff = initialBackoff;
    }

    public Duration getMaxBackoff() {
        return maxBackoff;
    }

    public void setMaxBackoff(Duration maxBackoff) {
        this.maxBackoff = maxBackoff;
    }

    public Double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public void setBackoffMultiplier(Double backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
    }

    public Set<Status.Code> getRetryableStatusCodes() {
        return retryableStatusCodes;
    }

    public void setRetryableStatusCodes(Set<Status.Code> retryableStatusCodes) {
        this.retryableStatusCodes = retryableStatusCodes;
    }
}
