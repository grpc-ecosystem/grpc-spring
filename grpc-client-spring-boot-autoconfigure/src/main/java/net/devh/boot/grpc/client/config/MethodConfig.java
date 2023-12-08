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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;


/**
 * The method config for retry policy config.
 *
 * <p>
 * For the exact specification refer to:
 * <a href="https://github.com/grpc/proposal/blob/master/A6-client-retries.md">A6-client-retries</a>
 * </p>
 *
 * @author wushengju
 */
@Data
public class MethodConfig {

    /**
     * retry policy config
     */
    private RetryPolicyConfig retryPolicy;
    /**
     * name for list
     */
    private List<NameConfig> name;


    /**
     * Creates a copy of this instance.
     *
     * @return The newly created copy.
     */
    public MethodConfig copy() {
        final MethodConfig copy = new MethodConfig();
        copy.retryPolicy = requireNonNull(this.retryPolicy, "retryPolicy").copy();
        copy.name = NameConfig.copy(this.name);
        return copy;
    }

    /**
     * Creates a copy of the given instances.
     *
     * @param configs The configs to copy.
     * @return The copied instances.
     */
    public static List<MethodConfig> copy(final List<MethodConfig> configs) {
        return requireNonNull(configs, "configs").stream()
                .map(MethodConfig::copy)
                .collect(Collectors.toList());
    }

    /**
     * Builds a json like map from this instance.
     *
     * @return The json like map representation of this instance.
     */
    public Map<String, Object> buildMap() {
        final Map<String, Object> map = new LinkedHashMap<>();
        if (this.name != null && !this.name.isEmpty()) {
            map.put("name", NameConfig.buildMaps(this.name));
        }
        if (this.retryPolicy != null) {
            map.put("retryPolicy", this.retryPolicy.buildMap());
        }
        return map;
    }

    /**
     * Builds a json like map from the given instances.
     *
     * @param configs The configs to convert.
     * @return The json like array of maps representation of the instances.
     */
    public static List<Map<String, Object>> buildMaps(final List<MethodConfig> configs) {
        return requireNonNull(configs, "configs").stream()
                .map(MethodConfig::buildMap)
                .collect(Collectors.toList());
    }

}
