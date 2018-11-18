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
