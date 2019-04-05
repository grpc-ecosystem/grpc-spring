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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.grpc.Codec;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorConfigurer;
import net.devh.boot.grpc.common.codec.CodecType;
import net.devh.boot.grpc.common.codec.GrpcCodecDefinition;
import net.devh.boot.grpc.common.codec.GrpcCodecDiscoverer;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorConfigurer;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.ServiceConfiguration;

@SpringBootTest(properties = "grpc.client.test.negotiationType=PLAINTEXT")
@SpringJUnitConfig(
        classes = {CustomCodecTest.CustomConfiguration.class, ServiceConfiguration.class, BaseAutoConfiguration.class})
@DirtiesContext
public class CustomCodecTest extends AbstractCodecTest {

    private static final String CODEC = "custom";

    public CustomCodecTest() {
        super(CODEC);
    }

    @Configuration
    public static class CustomConfiguration {

        @Bean
        GlobalClientInterceptorConfigurer gcic() {
            return registry -> registry.addClientInterceptors(new CodecValidatingClientInterceptor(CODEC));
        }

        @Bean
        GlobalServerInterceptorConfigurer gsic() {
            return registry -> registry.addServerInterceptors(new CodecValidatingServerInterceptor(CODEC));
        }

        @Bean
        GrpcCodecDiscoverer grpcCodecDiscoverer() {
            return () -> Arrays.asList(new GrpcCodecDefinition(new CustomCodecTest.CustomCodec(), true, CodecType.ALL));
        }

    }

    public static final class CustomCodec implements Codec {

        @Override
        public String getMessageEncoding() {
            return CODEC;
        }

        @Override
        public OutputStream compress(final OutputStream os) throws IOException {
            return new GZIPOutputStream(os);
        }

        @Override
        public InputStream decompress(final InputStream is) throws IOException {
            return new GZIPInputStream(is);
        }

    }

}
