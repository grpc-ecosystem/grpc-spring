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

package net.devh.boot.grpc.server.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import net.devh.boot.grpc.common.util.GrpcUtils;
import net.devh.boot.grpc.server.config.GrpcServerProperties;

/**
 * Configuration class that configures the required beans for grpc discovery via Eureka.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnClass({EurekaRegistration.class})
public class GrpcMetadataEurekaConfiguration {

    @Autowired(required = false)
    private EurekaRegistration eurekaRegistration;

    @Autowired
    private GrpcServerProperties grpcProperties;

    @PostConstruct
    public void init() {
        if (eurekaRegistration != null) {
            final int port = grpcProperties.getPort();
            if (GrpcUtils.INTER_PROCESS_DISABLE != port) {
                eurekaRegistration.getInstanceConfig().getMetadataMap()
                        .put(GrpcUtils.CLOUD_DISCOVERY_METADATA_PORT, Integer.toString(port));
            }
        }
    }

}
