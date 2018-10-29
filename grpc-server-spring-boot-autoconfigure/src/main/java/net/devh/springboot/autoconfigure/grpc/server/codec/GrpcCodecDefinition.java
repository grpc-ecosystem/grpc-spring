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

package net.devh.springboot.autoconfigure.grpc.server.codec;

import io.grpc.Codec;
import lombok.Getter;

/**
 * Container class that contains all relevant information about a grpc codec.
 *
 * @see GrpcCodec
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 10/13/18
 */
@Getter
public class GrpcCodecDefinition {

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
