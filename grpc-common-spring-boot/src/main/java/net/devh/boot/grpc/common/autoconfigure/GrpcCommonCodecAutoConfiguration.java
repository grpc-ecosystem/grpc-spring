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
