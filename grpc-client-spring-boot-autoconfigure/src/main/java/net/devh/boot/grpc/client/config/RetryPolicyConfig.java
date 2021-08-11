package net.devh.boot.grpc.client.config;

import io.grpc.Status;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @Title 重试配置信息
 * @Description RetryPolicyProperties
 * @Program spring-cloud-tcl-starter
 * @Author wushengju
 * @Version 1.0
 * @Date 2021-08-09 15:25
 * @Copyright Copyright (c) 2021 TCL Inc. All rights reserved
 **/
@ToString
@EqualsAndHashCode
public class RetryPolicyConfig {
    /**
     * The maximum number of RPC attempts, including the original RPC.
     * This field is required and must be two or greater.
     */
    String maxAttempts;
    /**
     * Exponential backoff parameters. The initial retry attempt will occur at
     * random(0, initialBackoff). In general, the nth attempt since the last
     * server pushback response (if any), will occur at random(0, min(initialBackoff*backoffMultiplier**(n-1), maxBackoff)).
     * Exponential backoff parameters,Duration is seconds
     * Required. Must be greater than zero
     */
    Double initialBackoff;
    /**
     * Required. Must be greater than zero,Duration is seconds
     */
    Double maxBackoff;
    /**
     * Required. Must be greater than zero.
     */
    Double backoffMultiplier;
    /**
     * The set of status codes which may be retried.
     * Status codes are specified in the integer form or the case-insensitive string form (eg. [14], ["UNAVAILABLE"] or ["unavailable"])
     * This field is required and must be non-empty.
     */
    Set<Status.Code> retryableStatusCodes;

    public Map<String, Object> buildMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("maxAttempts", Double.valueOf(this.getMaxAttempts()));
        map.put("initialBackoff", String.format("%ss", this.getInitialBackoff()));
        map.put("maxBackoff", String.format("%ss", this.getMaxBackoff()));
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

    public String getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(String maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public double getInitialBackoff() {
        return initialBackoff;
    }

    public void setInitialBackoff(double initialBackoff) {
        this.initialBackoff = initialBackoff;
    }

    public double getMaxBackoff() {
        return maxBackoff;
    }

    public void setMaxBackoff(double maxBackoff) {
        this.maxBackoff = maxBackoff;
    }

    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public void setBackoffMultiplier(double backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
    }

    public Set<Status.Code> getRetryableStatusCodes() {
        return retryableStatusCodes;
    }

    public void setRetryableStatusCodes(Set<Status.Code> retryableStatusCodes) {
        this.retryableStatusCodes = retryableStatusCodes;
    }
}
