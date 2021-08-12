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

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * The method config for retry policy config.
 *
 * @author wushengju
 * @since 8/12/2021
 */
@ToString
@EqualsAndHashCode
public class MethodConfig {
    /**
     * retry policy config
     */
    private RetryPolicyConfig retryPolicy;
    /**
     * name for list
     */
    private List<NameConfig> name;

    public RetryPolicyConfig getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicyConfig retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public List<NameConfig> getName() {
        return name;
    }

    public void setName(List<NameConfig> name) {
        this.name = name;
    }

    public void copyDefaultsFrom(MethodConfig config) {
        if (this == config) {
            return;
        }
        this.retryPolicy.copyDefaultsFrom(config.retryPolicy);
        NameConfig.copyDefaultsFrom(this.name, config.getName());
    }

    public static void copyDefaultsFrom(List<MethodConfig> methodConfig, final List<MethodConfig> config) {
        if (methodConfig == null || methodConfig.isEmpty()) {
            methodConfig = new ArrayList<>();
            if (config != null && !config.isEmpty()) {
                List<MethodConfig> finalMethodConfig = methodConfig;
                config.forEach(conf -> {
                    MethodConfig newMethodConfig = new MethodConfig();
                    newMethodConfig.copyDefaultsFrom(conf);
                    finalMethodConfig.add(newMethodConfig);
                });
            }
        }
    }
}
