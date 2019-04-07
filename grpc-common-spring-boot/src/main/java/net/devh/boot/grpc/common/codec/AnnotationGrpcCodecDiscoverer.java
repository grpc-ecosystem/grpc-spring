/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
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
