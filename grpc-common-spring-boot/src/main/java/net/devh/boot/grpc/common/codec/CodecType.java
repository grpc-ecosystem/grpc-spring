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

/**
 * The type of the codec.
 *
 * @author Michael (yidongnan@gmail.com)
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public enum CodecType {

    /**
     * The codec should be used for compression only.
     */
    COMPRESS(true, false),

    /**
     * The codec should be used for decompression only.
     */
    DECOMPRESS(false, true),

    /**
     * The codec should be used for both compression and decompression.
     */
    ALL(true, true);

    private final boolean forCompression;
    private final boolean forDecompression;

    private CodecType(final boolean forCompression, final boolean forDecompression) {
        this.forCompression = forCompression;
        this.forDecompression = forDecompression;
    }

    /**
     * Whether the associated codec should be used for compression.
     *
     * @return True, if the codec can be used for compression. False otherwise.
     */
    public boolean isForCompression() {
        return this.forCompression;
    }

    /**
     * Whether the associated codec should be used for decompression.
     *
     * @return True, if the codec can be used for decompression. False otherwise.
     */
    public boolean isForDecompression() {
        return this.forDecompression;
    }

}
