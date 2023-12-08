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
 * The name config for service and method.
 *
 * <p>
 * If both the service and method name are empty, then this applies to all requests.
 * </p>
 *
 * <p>
 * If only the method name is empty, then this applies to all methods in the given service.
 * </p>
 *
 * <p>
 * For the exact specification refer to:
 * <a href="https://github.com/grpc/proposal/blob/master/A6-client-retries.md">A6-client-retries</a>
 * </p>
 *
 * @author wushengju
 */
@Data
public class NameConfig {

    /**
     * The service name as defined in your proto file. May be empty to match all services, but may never be null.
     */
    private String service = "";
    /**
     * The method name which you will call. May be empty to match all method within the service, but may never be null.
     */
    private String method = "";

    /**
     * Creates a copy of this instance.
     *
     * @return The newly created copy.
     */
    public NameConfig copy() {
        final NameConfig copy = new NameConfig();
        copy.service = this.service;
        copy.method = this.method;
        return copy;
    }

    /**
     * Creates a copy of the given instances.
     *
     * @param configs The configs to copy.
     * @return The copied instances.
     */
    public static List<NameConfig> copy(final List<NameConfig> configs) {
        return requireNonNull(configs, "configs").stream()
                .map(NameConfig::copy)
                .collect(Collectors.toList());
    }

    /**
     * Builds a json like map from this instance.
     *
     * @return The json like map representation of this instance.
     */
    public Map<String, Object> buildMap() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("service", this.service);
        map.put("method", this.method);
        return map;
    }

    /**
     * Builds a json like map from the given instances.
     *
     * @param configs The configs to convert.
     * @return The json like array of maps representation of the instances.
     */
    public static List<Map<String, Object>> buildMaps(final List<NameConfig> configs) {
        return requireNonNull(configs, "configs").stream()
                .map(NameConfig::buildMap)
                .collect(Collectors.toList());
    }

}
