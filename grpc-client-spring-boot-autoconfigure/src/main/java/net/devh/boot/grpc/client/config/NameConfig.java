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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The name config for service and method.
 *
 * @author wushengju
 * @since 8/12/2021
 */
@ToString
@EqualsAndHashCode
public class NameConfig {
    /**
     * the service name which defined in xx.proto
     */
    private String service;
    /**
     * the method name which you will call
     */
    private String method;

    public Map<String, Object> buildMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("service", this.getService());
        map.put("method", this.getMethod());
        return map;
    }

    public void copyDefaultsFrom(final NameConfig config) {
        if (this == config) {
            return;
        }
        if (this.service == null) {
            this.service = config.service;
        }
        if (this.method == null) {
            this.method = config.method;
        }
    }

    public static void copyDefaultsFrom(List<NameConfig> nameConfigs, final List<NameConfig> config) {
        if (nameConfigs == null || nameConfigs.isEmpty()) {
            nameConfigs = new ArrayList<>();
            if (config != null && !config.isEmpty()) {
                List<NameConfig> finalNameConfigs = nameConfigs;
                config.forEach(nameConfig -> {
                    NameConfig newConfig = new NameConfig();
                    newConfig.copyDefaultsFrom(nameConfig);
                    finalNameConfigs.add(newConfig);
                });
            }
        }
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
