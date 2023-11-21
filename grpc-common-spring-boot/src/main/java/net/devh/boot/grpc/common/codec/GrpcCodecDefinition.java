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

import com.google.common.collect.ImmutableList;

import io.grpc.Codec;
import lombok.Getter;

/**
 * Container class that contains all relevant information about a grpc codec.
 *
 * @see GrpcCodec
 *
 * @author Michael (yidongnan@gmail.com)
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Getter
public class GrpcCodecDefinition {

    /**
     * The codec definition for gzip.
     */
    public static final GrpcCodecDefinition GZIP_DEFINITION =
            new GrpcCodecDefinition(new Codec.Gzip(), true, CodecType.ALL);
    /**
     * The codec definition for identity (no-op).
     */
    public static final GrpcCodecDefinition IDENTITY_DEFINITION =
            new GrpcCodecDefinition(Codec.Identity.NONE, false, CodecType.ALL);
    /**
     * The default encodings used by gRPC.
     */
    public static final Collection<GrpcCodecDefinition> DEFAULT_DEFINITIONS =
            ImmutableList.<GrpcCodecDefinition>builder()
                    .add(GZIP_DEFINITION)
                    .add(IDENTITY_DEFINITION)
                    .build();

    private final Codec codec;
    private final boolean advertised;
    private final CodecType codecType;

    /**
     * Creates a new GrpcCodecDefinition.
     *
     * @param codec The codec bean.
     * @param advertised Whether the codec should be advertised in the headers.
     * @param codecType The type of the codec.
     */
    public GrpcCodecDefinition(final Codec codec, final boolean advertised, final CodecType codecType) {
        this.codec = codec;
        this.advertised = advertised;
        this.codecType = codecType;
    }

}
