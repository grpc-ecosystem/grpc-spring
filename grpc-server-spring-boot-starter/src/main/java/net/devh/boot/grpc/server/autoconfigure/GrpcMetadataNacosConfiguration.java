/*
 * Copyright (c) 2016-2023 The gRPC-Spring Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.devh.boot.grpc.server.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.alibaba.cloud.nacos.registry.NacosRegistration;

import jakarta.annotation.PostConstruct;
import net.devh.boot.grpc.common.util.GrpcUtils;
import net.devh.boot.grpc.server.config.GrpcServerProperties;

/**
 * Configuration class that configures the required beans for grpc discovery via Nacos.
 *
 * @author Michael (yidongnan@gmail.com)
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnClass({NacosRegistration.class})
public class GrpcMetadataNacosConfiguration {

    @Autowired(required = false)
    private NacosRegistration nacosRegistration;

    @Autowired
    private GrpcServerProperties grpcProperties;

    @PostConstruct
    public void init() {
        if (nacosRegistration != null) {
            final int port = grpcProperties.getPort();
            if (GrpcUtils.INTER_PROCESS_DISABLE != port) {
                nacosRegistration.getMetadata()
                        .put(GrpcUtils.CLOUD_DISCOVERY_METADATA_PORT, Integer.toString(port));
            }
        }

    }

}
