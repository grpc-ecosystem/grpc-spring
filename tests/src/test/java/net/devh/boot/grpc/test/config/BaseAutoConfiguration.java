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

package net.devh.boot.grpc.test.config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.common.autoconfigure.GrpcCommonCodecAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerSecurityAutoConfiguration;

@Configuration
@ImportAutoConfiguration({GrpcCommonCodecAutoConfiguration.class, GrpcServerAutoConfiguration.class,
        GrpcServerFactoryAutoConfiguration.class, GrpcServerSecurityAutoConfiguration.class,
        GrpcClientAutoConfiguration.class})
public class BaseAutoConfiguration {

}
