/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.springboot.autoconfigure.grpc.common.codec;

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
