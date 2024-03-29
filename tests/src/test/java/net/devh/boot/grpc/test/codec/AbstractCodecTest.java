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

package net.devh.boot.grpc.test.codec;

import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorRegistry;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorRegistry;
import net.devh.boot.grpc.test.proto.TestServiceGrpc;

/**
 * Tests related to codecs.
 */
abstract class AbstractCodecTest {

    private final String codec;

    @GrpcClient("test")
    protected Channel channel;

    @Autowired
    private GlobalClientInterceptorRegistry clientRegistry;

    @Autowired
    private GlobalServerInterceptorRegistry serverRegistry;

    @Autowired
    private CodecValidatingClientInterceptor clientValidator;

    @Autowired
    private CodecValidatingServerInterceptor serverValidator;

    AbstractCodecTest(final String codec) {
        this.codec = codec;
    }

    @Test
    void testTransmission() {
        assumeThat(this.clientRegistry.getClientInterceptors()).contains(this.clientValidator);
        assumeThat(this.serverRegistry.getServerInterceptors()).contains(this.serverValidator);

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
