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

package net.devh.boot.grpc.test.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Empty;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.internal.GrpcUtil;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;

public abstract class AbstractCodecTest {

    private final String codec;

    public AbstractCodecTest(final String codec) {
        this.codec = codec;
    }

    @GrpcClient("test")
    protected Channel channel;

    @Test
    public void testTransmission() {
        assertEquals("1.2.3", TestServiceGrpc.newBlockingStub(this.channel).withCompression(this.codec)
                .normal(Empty.getDefaultInstance()).getVersion());
    }

    protected static final class CodecValidatingClientInterceptor implements ClientInterceptor {

        private final String compressor;

        public CodecValidatingClientInterceptor(final String compressor) {
            this.compressor = compressor;
        }

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(final MethodDescriptor<ReqT, RespT> method,
                final CallOptions callOptions, final Channel next) {
            assertEquals(this.compressor, callOptions.getCompressor());
            return next.newCall(method, callOptions);
        }

    }

    protected static final class CodecValidatingServerInterceptor implements ServerInterceptor {

        private static final Key<String> ENCODING = Key.of(GrpcUtil.MESSAGE_ENCODING, Metadata.ASCII_STRING_MARSHALLER);

        private final String compressor;

        public CodecValidatingServerInterceptor(final String compressor) {
            this.compressor = compressor;
        }

        @Override
        public <ReqT, RespT> Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call, final Metadata headers,
                final ServerCallHandler<ReqT, RespT> next) {
            assertEquals(this.compressor, Objects.toString(headers.get(ENCODING), "identity"));
            return next.startCall(call, headers);
        }

    }

}
