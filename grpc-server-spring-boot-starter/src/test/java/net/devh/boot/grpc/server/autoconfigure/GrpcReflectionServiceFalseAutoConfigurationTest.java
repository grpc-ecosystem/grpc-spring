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

package net.devh.boot.grpc.server.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;

@SpringBootTest(classes = GrpcReflectionServiceDefaultAutoConfigurationTest.TestConfig.class,
        properties = "grpc.server.reflection-service-enabled=false")
@ImportAutoConfiguration({
        GrpcServerAutoConfiguration.class,
        GrpcServerFactoryAutoConfiguration.class,
        GrpcReflectionServiceAutoConfiguration.class})
@DirtiesContext
class GrpcReflectionServiceFalseAutoConfigurationTest extends GrpcReflectionServiceDefaultAutoConfigurationTest {

    @Override
    void checkResult(final AwaitableStreamObserver<ServerReflectionResponse> resultObserver) {
        final Throwable error = assertDoesNotThrow(resultObserver::getError);
        assertThat(error).asInstanceOf(type(StatusRuntimeException.class))
                .extracting(StatusRuntimeException::getStatus)
                .extracting(Status::getCode)
                .isEqualTo(Code.UNIMPLEMENTED);
    }

}
