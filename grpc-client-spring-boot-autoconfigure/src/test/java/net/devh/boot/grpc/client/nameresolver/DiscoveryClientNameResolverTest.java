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

package net.devh.boot.grpc.client.nameresolver;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties;

import io.grpc.NameResolver;
import io.grpc.Status;
import io.grpc.SynchronizationContext;
import io.grpc.internal.AutoConfiguredLoadBalancerFactory;
import io.grpc.internal.GrpcUtil;
import io.grpc.internal.ScParser;
import net.devh.boot.grpc.common.util.GrpcUtils;

/**
 * Test for {@link DiscoveryClientNameResolver}.
 */
public class DiscoveryClientNameResolverTest {

    private final NameResolver.Args args = NameResolver.Args.newBuilder()
            .setDefaultPort(1212)
            .setProxyDetector(GrpcUtil.DEFAULT_PROXY_DETECTOR)
            .setSynchronizationContext(
                    new SynchronizationContext((t, e) -> {
                        throw new AssertionError(e);
                    }))
            .setServiceConfigParser(new ScParser(true, 10, 10, new AutoConfiguredLoadBalancerFactory("pick_first")))
            .setOffloadExecutor(Runnable::run)
            .build();

    @Test
    void testValidServiceConfig() {
        String validServiceConfig = """
                {
                  "loadBalancingConfig": [
                    {"round_robin": {}}
                  ],
                  "methodConfig": [
                    {
                      "name": [{}],
                      "retryPolicy": {
                        "maxAttempts": 5,
                        "initialBackoff": "0.05s",
                        "maxBackoff": "1s",
                        "backoffMultiplier": 2,
                        "retryableStatusCodes": [
                          "UNAVAILABLE",
                          "ABORTED",
                          "DATA_LOSS",
                          "INTERNAL",
                          "DEADLINE_EXCEEDED"
                        ]
                      },
                      "timeout": "5s"
                    }
                  ]
                }
                """;
        TestableListener listener = resolveServiceAndVerify("test1", validServiceConfig);
        NameResolver.ConfigOrError serviceConf = listener.getResult().getServiceConfig();
        assertThat(serviceConf).isNotNull();
        assertThat(serviceConf.getConfig()).isNotNull();
        assertThat(serviceConf.getError()).isNull();
    }

    @Test
    void testBrokenServiceConfig() {
        TestableListener listener = resolveServiceAndVerify("test2", "intentionally invalid service config");
        NameResolver.ConfigOrError serviceConf = listener.getResult().getServiceConfig();
        assertThat(serviceConf).isNotNull();
        assertThat(serviceConf.getConfig()).isNull();
        assertThat(serviceConf.getError()).extracting(Status::getCode).isEqualTo(Status.Code.UNKNOWN);
    }

    private TestableListener resolveServiceAndVerify(String serviceName, String serviceConfig) {
        SimpleDiscoveryProperties props = new SimpleDiscoveryProperties();
        DefaultServiceInstance service = new DefaultServiceInstance(
                serviceName + "-1", serviceName, "127.0.0.1", 3322, false);
        Map<String, String> meta = service.getMetadata();
        meta.put(GrpcUtils.CLOUD_DISCOVERY_METADATA_PORT, "6688");
        meta.put(GrpcUtils.CLOUD_DISCOVERY_METADATA_SERVICE_CONFIG, serviceConfig);
        props.setInstances(Map.of(serviceName, List.of(service)));
        SimpleDiscoveryClient disco = new SimpleDiscoveryClient(props);
        DiscoveryClientNameResolver dcnr = new DiscoveryClientNameResolver(serviceName, disco, args, null, null);

        TestableListener listener = new TestableListener();
        dcnr.start(listener);

        assertThat(listener.isErrorWasSet()).isFalse();
        assertThat(listener.isResultWasSet()).isTrue();
        InetSocketAddress addr = (InetSocketAddress) listener.getResult().getAddresses().get(0).getAddresses().get(0);
        assertThat(addr.getPort()).isEqualTo(6688);
        assertThat(addr.getHostString()).isEqualTo("127.0.0.1");
        return listener;
    }
}
