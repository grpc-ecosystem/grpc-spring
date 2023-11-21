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

package net.devh.boot.grpc.common.codec;

import java.util.Collection;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.ImmutableList;

import io.grpc.Codec;
import lombok.extern.slf4j.Slf4j;

/**
 * A {@link GrpcCodecDiscoverer} that searches for beans with the {@link GrpcCodec} annotations.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class AnnotationGrpcCodecDiscoverer implements ApplicationContextAware, GrpcCodecDiscoverer {

    private ApplicationContext applicationContext;
    private Collection<GrpcCodecDefinition> definitions;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Collection<GrpcCodecDefinition> findGrpcCodecs() {
        if (this.definitions == null) {
            log.debug("Searching for codecs...");
            final String[] beanNames = this.applicationContext.getBeanNamesForAnnotation(GrpcCodec.class);
            final ImmutableList.Builder<GrpcCodecDefinition> builder = ImmutableList.builder();
            for (final String beanName : beanNames) {
                final Codec codec = this.applicationContext.getBean(beanName, Codec.class);
                final GrpcCodec annotation = this.applicationContext.findAnnotationOnBean(beanName, GrpcCodec.class);
                builder.add(new GrpcCodecDefinition(codec, annotation.advertised(), annotation.codecType()));
                log.debug("Found gRPC codec: {}, bean: {}, class: {}",
                        codec.getMessageEncoding(), beanName, codec.getClass().getName());
            }
            this.definitions = builder.build();
            log.debug("Done");
        }
        return this.definitions;
    }

}
