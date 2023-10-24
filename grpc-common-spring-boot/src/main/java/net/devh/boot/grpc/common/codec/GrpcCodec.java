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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

import io.grpc.Codec;

/**
 * Annotation that marks gRPC codecs that should be registered with a gRPC server. This annotation should only be added
 * to beans that implement {@link Codec}.
 *
 * @author Michael (yidongnan@gmail.com)
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface GrpcCodec {

    /**
     * Advertised codecs will be listed in the {@code Accept-Encoding} header. Defaults to false.
     *
     * @return True, of the codec should be advertised. False otherwise.
     */
    boolean advertised() default false;

    /**
     * Gets the type of codec the annotated bean should be used for.
     *
     * @return The type of codec.
     */
    CodecType codecType() default CodecType.ALL;

}
