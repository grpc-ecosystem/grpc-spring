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

package net.devh.boot.grpc.common.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.Codec;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.common.codec.AnnotationGrpcCodecDiscoverer;
import net.devh.boot.grpc.common.codec.GrpcCodecDefinition;
import net.devh.boot.grpc.common.codec.GrpcCodecDiscoverer;

/**
 * The auto configuration used by Spring-Boot that contains all codec related beans for clients/servers.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Codec.class)
public class GrpcCommonCodecAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public GrpcCodecDiscoverer defaultGrpcCodecDiscoverer() {
        return new AnnotationGrpcCodecDiscoverer();
    }

    @ConditionalOnBean(GrpcCodecDiscoverer.class)
    @ConditionalOnMissingBean
    @Bean
    public CompressorRegistry defaultCompressorRegistry(final GrpcCodecDiscoverer codecDiscoverer) {
        log.debug("Found GrpcCodecDiscoverer -> Creating custom CompressorRegistry");
        final CompressorRegistry registry = CompressorRegistry.getDefaultInstance();
        for (final GrpcCodecDefinition definition : codecDiscoverer.findGrpcCodecs()) {
            if (definition.getCodecType().isForCompression()) {
                final Codec codec = definition.getCodec();
                log.debug("Registering compressor: '{}' ({})", codec.getMessageEncoding(), codec.getClass().getName());
                registry.register(codec);
            }
        }
        return registry;
    }

    @ConditionalOnBean(GrpcCodecDiscoverer.class)
    @ConditionalOnMissingBean
    @Bean
    public DecompressorRegistry defaultDecompressorRegistry(final GrpcCodecDiscoverer codecDiscoverer) {
        log.debug("Found GrpcCodecDiscoverer -> Creating custom DecompressorRegistry");
        DecompressorRegistry registry = DecompressorRegistry.getDefaultInstance();
        for (final GrpcCodecDefinition definition : codecDiscoverer.findGrpcCodecs()) {
            if (definition.getCodecType().isForDecompression()) {
                final Codec codec = definition.getCodec();
                final boolean isAdvertised = definition.isAdvertised();
                log.debug("Registering {} decompressor: '{}' ({})",
                        isAdvertised ? "advertised" : "", codec.getMessageEncoding(), codec.getClass().getName());
                registry = registry.with(codec, isAdvertised);
            }
        }
        return registry;
    }

}
