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

package net.devh.boot.grpc.client.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.CallCredentials;
import io.grpc.stub.AbstractStub;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.StubTransformer;
import net.devh.boot.grpc.client.security.CallCredentialsHelper;

/**
 * The security auto configuration for the client.
 *
 * <p>
 * You can disable this config by using:
 * </p>
 *
 * <pre>
 * <code>@ImportAutoConfiguration(exclude = GrpcClientSecurityAutoConfiguration.class)</code>
 * </pre>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(GrpcClientAutoConfiguration.class)
public class GrpcClientSecurityAutoConfiguration {

    /**
     * Creates a {@link StubTransformer} bean that will add the call credentials to the created stubs.
     *
     * <p>
     * <b>Note:</b> This method will only be applied if exactly one {@link CallCredentials} is in the application
     * context, and another StubTransformer isn't provided.
     * </p>
     *
     * @param credentials The call credentials to configure in the stubs.
     * @return The StubTransformer bean that will add the given credentials.
     * @see AbstractStub#withCallCredentials(CallCredentials)
     * @sse {@link CallCredentialsHelper#fixedCredentialsStubTransformer(CallCredentials)}
     */
    @ConditionalOnSingleCandidate(CallCredentials.class)
    @ConditionalOnMissingBean
    @Bean
    StubTransformer stubCallCredentialsTransformer(final CallCredentials credentials) {
        log.info("Found single CallCredentials in the context, automatically using it for all stubs");
        return CallCredentialsHelper.fixedCredentialsStubTransformer(credentials);
    }

}
