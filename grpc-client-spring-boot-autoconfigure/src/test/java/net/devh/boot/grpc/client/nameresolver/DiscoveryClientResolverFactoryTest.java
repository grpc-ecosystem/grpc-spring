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

package net.devh.boot.grpc.client.nameresolver;

import static java.util.Arrays.asList;
import static net.devh.boot.grpc.client.nameresolver.DiscoveryClientResolverFactory.DISCOVERY_SCHEME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.NameResolver.Listener2;
import io.grpc.NameResolver.ResolutionResult;
import io.grpc.NameResolver.ServiceConfigParser;
import io.grpc.ProxyDetector;
import io.grpc.Status;
import io.grpc.SynchronizationContext;

/**
 * Tests {@link DiscoveryClientResolverFactory}.
 */
class DiscoveryClientResolverFactoryTest {

    private static final String SERVICE_NAME = "TEST";

    private static final URI SERVICE_URI = URI.create(DISCOVERY_SCHEME + ":///" + SERVICE_NAME);

    private static final NameResolver.Args NRA = NameResolver.Args.newBuilder()
            .setDefaultPort(80)
            .setProxyDetector(mock(ProxyDetector.class))
            .setServiceConfigParser(mock(ServiceConfigParser.class))
            .setSynchronizationContext(new SynchronizationContext(mock(UncaughtExceptionHandler.class)))
            .build();

    private static final ServiceInstance I1 = new DefaultServiceInstance("I1", SERVICE_NAME, "127.0.0.1", 80, false);
    private static final ServiceInstance I2 = new DefaultServiceInstance("I2", SERVICE_NAME, "127.0.0.2", 80, false);
    private static final ServiceInstance I3 = new DefaultServiceInstance("I3", SERVICE_NAME, "127.0.0.3", 80, false);
    private static final ServiceInstance I4 = new DefaultServiceInstance("I4", SERVICE_NAME, "127.0.0.4", 80, false);
    private static final List<ServiceInstance> I__ = asList();
    private static final List<ServiceInstance> I12 = asList(I1, I2);
    private static final List<ServiceInstance> I34 = asList(I3, I4);

    /**
     * Tests for the {@link DiscoveryClientNameResolver}s returned from
     * {@link DiscoveryClientResolverFactory#newNameResolver(URI, Args)}.
     *
     * @return The tests.
     */
    @TestFactory
    List<DynamicTest> testFactory() {
        return asList(
                // Simple test
                dynamicTest("simple", () -> {
                    // Setup
                    final DiscoveryClient client = mock(DiscoveryClient.class);
                    when(client.getInstances(SERVICE_NAME)).thenReturn(I12);
                    final DiscoveryClientResolverFactory factory = new DiscoveryClientResolverFactory(client);

                    // Prepare
                    final TestListener listener = new TestListener();
                    final int ref = listener.getUpdateCount();

                    // New
                    final NameResolver nameResolver = factory.newNameResolver(SERVICE_URI, NRA);

                    // Resolve
                    nameResolver.start(listener);

                    // Verify
                    assertTrue(listener.await(1000, ref));
                    assertTargetsEqual(listener, I12);
                    verify(client, times(1)).getInstances(anyString());

                    // Cleanup
                    nameResolver.shutdown();
                    factory.destroy();
                }),
                // Slight delay in initialization
                dynamicTest("simple-delayed", () -> {
                    // Setup
                    final DiscoveryClient client = mock(DiscoveryClient.class);
                    final CountDownLatch latch = new CountDownLatch(1);
                    when(client.getInstances(SERVICE_NAME)).thenAnswer(inv -> {
                        latch.await();
                        return I12;
                    });
                    final DiscoveryClientResolverFactory factory = new DiscoveryClientResolverFactory(client);

                    // Prepare
                    final TestListener listener = new TestListener();
                    final int ref = listener.getUpdateCount();

                    // New
                    final NameResolver nameResolver = factory.newNameResolver(SERVICE_URI, NRA);

                    // Resolve
                    nameResolver.start(listener);

                    assertFalse(listener.await(1000, ref));

                    latch.countDown();

                    // Verify
                    assertTrue(listener.await(1000, ref));
                    assertTargetsEqual(listener, I12);
                    verify(client, times(1)).getInstances(anyString());

                    // Cleanup
                    nameResolver.shutdown();
                    factory.destroy();
                }),
                // Slight delay in initialization
                dynamicTest("simple-error", () -> {
                    // Setup
                    final DiscoveryClient client = mock(DiscoveryClient.class);
                    when(client.getInstances(SERVICE_NAME)).thenReturn(I__);
                    final DiscoveryClientResolverFactory factory = new DiscoveryClientResolverFactory(client);

                    // Prepare
                    final TestListener listener = new TestListener();
                    final int ref = listener.getUpdateCount();

                    // New
                    final NameResolver nameResolver = factory.newNameResolver(SERVICE_URI, NRA);

                    // Resolve
                    nameResolver.start(listener);

                    // Verify
                    assertTrue(listener.await(1000, ref));
                    assertError(listener);
                    verify(client, times(1)).getInstances(anyString());

                    // Cleanup
                    nameResolver.shutdown();
                    factory.destroy();
                }),
                // NameResolver starting simultaneously
                dynamicTest("new+new", () -> {
                    // Setup
                    final DiscoveryClient client = mock(DiscoveryClient.class);
                    final CountDownLatch latch = new CountDownLatch(1);
                    when(client.getInstances(SERVICE_NAME)).thenAnswer(inv -> {
                        latch.await();
                        return I12;
                    });
                    final DiscoveryClientResolverFactory factory = new DiscoveryClientResolverFactory(client);

                    // Prepare
                    final TestListener listener = new TestListener();
                    final int ref = listener.getUpdateCount();

                    final TestListener listener2 = new TestListener();
                    final int ref2 = listener.getUpdateCount();

                    // New
                    final NameResolver nameResolver = factory.newNameResolver(SERVICE_URI, NRA);
                    final NameResolver nameResolver2 = factory.newNameResolver(SERVICE_URI, NRA);

                    // Resolve
                    nameResolver.start(listener);
                    nameResolver2.start(listener2);
                    latch.countDown();

                    // Verify
                    assertTrue(listener.await(1000, ref));
                    assertTrue(listener2.await(0, ref2));
                    assertTargetsEqual(listener, I12);
                    assertTargetsEqual(listener2, I12);
                    verify(client, times(1)).getInstances(anyString());

                    // Cleanup
                    nameResolver.shutdown();
                    nameResolver2.shutdown();
                    factory.destroy();
                }),
                // NameResolver currently resolving and then new one started
                dynamicTest("resolving+new", () -> {
                    // Setup
                    final DiscoveryClient client = mock(DiscoveryClient.class);
                    final CountDownLatch latch = new CountDownLatch(1);
                    when(client.getInstances(SERVICE_NAME)).thenAnswer(inv -> {
                        latch.await();
                        return I12;
                    });
                    final DiscoveryClientResolverFactory factory = new DiscoveryClientResolverFactory(client);

                    // Prepare + New + Resolve (1)
                    final TestListener listener = new TestListener();
                    final int ref = listener.getUpdateCount();

                    final NameResolver nameResolver = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver.start(listener);

                    // Prepare + New + Resolve (2)
                    final TestListener listener2 = new TestListener();
                    final int ref2 = listener.getUpdateCount();

                    final NameResolver nameResolver2 = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver2.start(listener2);

                    latch.countDown();

                    // Verify
                    assertTrue(listener.await(1000, ref));
                    assertTrue(listener2.await(1000, ref2));
                    assertTargetsEqual(listener, I12);
                    assertTargetsEqual(listener2, I12);
                    verify(client, times(1)).getInstances(anyString());

                    // Cleanup
                    nameResolver.shutdown();
                    nameResolver2.shutdown();
                    factory.destroy();
                }),
                // NameResolver already resolved and then new one started
                dynamicTest("resolved+new", () -> {
                    // Setup
                    final DiscoveryClient client = mock(DiscoveryClient.class);
                    when(client.getInstances(SERVICE_NAME)).thenReturn(I12);
                    final DiscoveryClientResolverFactory factory = new DiscoveryClientResolverFactory(client);

                    // Prepare + New + Resolve (1)
                    final TestListener listener = new TestListener();
                    final int ref = listener.getUpdateCount();

                    final NameResolver nameResolver = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver.start(listener);

                    // Wait for the first run to complete
                    assertTrue(listener.await(1000, ref));

                    // Prepare + New + Resolve (2)
                    final TestListener listener2 = new TestListener();

                    final NameResolver nameResolver2 = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver2.start(listener2);

                    // Verify
                    // Cannot await the update since the previous run contains the same data
                    // Thus we need to wait a bit for the second run to finish
                    Thread.sleep(100);
                    assertTargetsEqual(listener, I12);
                    assertTargetsEqual(listener2, I12);
                    verify(client, times(2)).getInstances(anyString());

                    // Cleanup
                    nameResolver.shutdown();
                    nameResolver2.shutdown();
                    factory.destroy();
                }),
                // NameResolver already shutdown and then new one started
                dynamicTest("shutdown+new", () -> {
                    // Setup
                    final DiscoveryClient client = mock(DiscoveryClient.class);
                    when(client.getInstances(SERVICE_NAME)).thenReturn(I12);
                    final DiscoveryClientResolverFactory factory = new DiscoveryClientResolverFactory(client);

                    // Prepare + New + Resolve (1)
                    final TestListener listener = new TestListener();
                    final int ref = listener.getUpdateCount();

                    final NameResolver nameResolver = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver.start(listener);

                    // Wait for the first run to complete
                    assertTrue(listener.await(1000, ref));
                    assertTargetsEqual(listener, I12);
                    nameResolver.shutdown();

                    when(client.getInstances(SERVICE_NAME)).thenReturn(I34);

                    // Prepare + New + Resolve (2)
                    final TestListener listener2 = new TestListener();

                    final NameResolver nameResolver2 = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver2.start(listener2);
                    final int ref2 = listener2.getUpdateCount();

                    // Verify
                    assertTrue(listener2.await(1000, ref2));
                    assertTargetsEqual(listener, I12);
                    assertTargetsEqual(listener2, I34);
                    verify(client, times(2)).getInstances(anyString());

                    // Cleanup
                    nameResolver2.shutdown();
                    factory.destroy();
                }),
                // NameResolver already resolved and the new one causes an error
                dynamicTest("resolved+error", () -> {
                    // Setup
                    final DiscoveryClient client = mock(DiscoveryClient.class);
                    when(client.getInstances(SERVICE_NAME)).thenReturn(I12);
                    final DiscoveryClientResolverFactory factory = new DiscoveryClientResolverFactory(client);

                    // Prepare + New + Resolve (1)
                    final TestListener listener = new TestListener();
                    final int ref = listener.getUpdateCount();

                    final NameResolver nameResolver = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver.start(listener);

                    // Wait for the first run to complete
                    assertTrue(listener.await(1000, ref));
                    assertTargetsEqual(listener, I12);

                    when(client.getInstances(SERVICE_NAME)).thenReturn(I__);

                    // Prepare + New + Resolve (2)
                    final TestListener listener2 = new TestListener();
                    final int ref2 = listener.getUpdateCount();

                    final NameResolver nameResolver2 = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver2.start(listener2);

                    // Verify
                    assertTrue(listener2.await(1000, ref2));
                    assertError(listener);
                    assertError(listener2);
                    verify(client, times(2)).getInstances(anyString());

                    // Cleanup
                    nameResolver.shutdown();
                    nameResolver2.shutdown();
                    factory.destroy();
                }),
                // NameResolver already resolved and the resolving one causes an error
                dynamicTest("re-resolving+error", () -> {
                    // Setup
                    final DiscoveryClient client = mock(DiscoveryClient.class);
                    when(client.getInstances(SERVICE_NAME)).thenReturn(I12);
                    final DiscoveryClientResolverFactory factory = new DiscoveryClientResolverFactory(client);

                    // Prepare + New + Resolve (1)
                    final TestListener listener = new TestListener();
                    final int ref = listener.getUpdateCount();

                    final NameResolver nameResolver = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver.start(listener);

                    // Wait for the first run to complete
                    assertTrue(listener.await(1000, ref));
                    assertTargetsEqual(listener, I12);

                    final CountDownLatch latch = new CountDownLatch(1);
                    when(client.getInstances(SERVICE_NAME)).thenAnswer(inv -> {
                        latch.await();
                        return I__;
                    });
                    factory.refresh(SERVICE_NAME, false);

                    // Prepare + New + Resolve (2)
                    final TestListener listener2 = new TestListener();
                    final int ref2 = listener.getUpdateCount();

                    final NameResolver nameResolver2 = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver2.start(listener2);
                    assertTargetsEqual(listener2, I12); // result from cache

                    latch.countDown();
                    // Verify
                    assertTrue(listener2.await(1000, ref2));
                    assertError(listener);
                    assertError(listener2);
                    verify(client, times(2)).getInstances(anyString());

                    // Cleanup
                    nameResolver.shutdown();
                    nameResolver2.shutdown();
                    factory.destroy();
                }),
                // NameResolver already errored and then new one is started
                dynamicTest("error+new", () -> {
                    // Setup
                    final DiscoveryClient client = mock(DiscoveryClient.class);
                    when(client.getInstances(SERVICE_NAME)).thenReturn(I__);
                    final DiscoveryClientResolverFactory factory = new DiscoveryClientResolverFactory(client);

                    // Prepare + New + Resolve (1)
                    final TestListener listener = new TestListener();
                    final int ref = listener.getUpdateCount();

                    final NameResolver nameResolver = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver.start(listener);

                    // Wait for the first run to complete
                    assertTrue(listener.await(1000, ref));
                    assertError(listener);

                    final CountDownLatch latch = new CountDownLatch(1);
                    when(client.getInstances(SERVICE_NAME)).thenAnswer(inv -> {
                        latch.await();
                        return I12;
                    });

                    // Prepare + New + Resolve (2)
                    final TestListener listener2 = new TestListener();
                    final int ref2 = listener2.getUpdateCount();

                    final NameResolver nameResolver2 = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver2.start(listener2);
                    assertError(listener2);

                    latch.countDown();

                    // Verify
                    assertTrue(listener.await(1000, ref));
                    assertTrue(listener2.await(1000, ref2));
                    assertTargetsEqual(listener, I12);
                    assertTargetsEqual(listener2, I12);
                    verify(client, times(2)).getInstances(anyString());

                    // Cleanup
                    nameResolver.shutdown();
                    nameResolver2.shutdown();
                    factory.destroy();
                }),
                // NameResolver already errored and a refresh is started before the new one is created
                dynamicTest("error+resolving", () -> {
                    // Setup
                    final DiscoveryClient client = mock(DiscoveryClient.class);
                    when(client.getInstances(SERVICE_NAME)).thenReturn(I__);
                    final DiscoveryClientResolverFactory factory = new DiscoveryClientResolverFactory(client);

                    // Prepare + New + Resolve (1)
                    final TestListener listener = new TestListener();
                    final int ref = listener.getUpdateCount();

                    final NameResolver nameResolver = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver.start(listener);

                    // Wait for the first run to complete
                    assertTrue(listener.await(1000, ref));
                    assertError(listener);

                    final CountDownLatch latch = new CountDownLatch(1);
                    when(client.getInstances(SERVICE_NAME)).thenAnswer(inv -> {
                        latch.await();
                        return I12;
                    });
                    factory.refresh(SERVICE_NAME, false);

                    // Prepare + New + Resolve (2)
                    final TestListener listener2 = new TestListener();
                    final int ref2 = listener2.getUpdateCount();

                    final NameResolver nameResolver2 = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver2.start(listener2);
                    assertError(listener2);

                    latch.countDown();

                    // Verify
                    assertTrue(listener.await(1000, ref));
                    assertTrue(listener2.await(1000, ref2));
                    assertTargetsEqual(listener, I12);
                    assertTargetsEqual(listener2, I12);
                    verify(client, times(2)).getInstances(anyString());

                    // Cleanup
                    nameResolver.shutdown();
                    nameResolver2.shutdown();
                    factory.destroy();
                }),
                // NameResolver already outdated and then new one is started
                dynamicTest("outdated+new", () -> {
                    // Setup
                    final DiscoveryClient client = mock(DiscoveryClient.class);
                    when(client.getInstances(SERVICE_NAME)).thenReturn(I12);
                    final DiscoveryClientResolverFactory factory = new DiscoveryClientResolverFactory(client);

                    // Prepare + New + Resolve (1)
                    final TestListener listener = new TestListener();
                    int ref = listener.getUpdateCount();

                    final NameResolver nameResolver = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver.start(listener);

                    // Wait for the first run to complete
                    assertTrue(listener.await(1000, ref));
                    assertTargetsEqual(listener, I12);
                    ref = listener.getUpdateCount();

                    final CountDownLatch latch = new CountDownLatch(1);
                    when(client.getInstances(SERVICE_NAME)).thenAnswer(inv -> {
                        latch.await();
                        return I34;
                    });

                    // Prepare + New + Resolve (2)
                    final TestListener listener2 = new TestListener();
                    final int ref2 = listener2.getUpdateCount();

                    final NameResolver nameResolver2 = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver2.start(listener2);
                    assertTargetsEqual(listener2, I12); // resolve from cache

                    latch.countDown();

                    // Verify
                    assertTrue(listener.await(1000, ref));
                    assertTrue(listener2.await(1000, ref2));
                    assertTargetsEqual(listener, I34);
                    assertTargetsEqual(listener2, I34);
                    verify(client, times(2)).getInstances(anyString());

                    // Cleanup
                    nameResolver.shutdown();
                    nameResolver2.shutdown();
                    factory.destroy();
                }),
                // NameResolver already outdated and a refresh is started before the new one is created
                dynamicTest("outdated+resolving", () -> {
                    // Setup
                    final DiscoveryClient client = mock(DiscoveryClient.class);
                    when(client.getInstances(SERVICE_NAME)).thenReturn(I12);
                    final DiscoveryClientResolverFactory factory = new DiscoveryClientResolverFactory(client);

                    // Prepare + New + Resolve (1)
                    final TestListener listener = new TestListener();
                    int ref = listener.getUpdateCount();

                    final NameResolver nameResolver = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver.start(listener);

                    // Wait for the first run to complete
                    assertTrue(listener.await(1000, ref));
                    assertTargetsEqual(listener, I12);
                    ref = listener.getUpdateCount();

                    final CountDownLatch latch = new CountDownLatch(1);
                    when(client.getInstances(SERVICE_NAME)).thenAnswer(inv -> {
                        latch.await();
                        return I34;
                    });
                    factory.refresh(SERVICE_NAME, false);

                    // Prepare + New + Resolve (2)
                    final TestListener listener2 = new TestListener();
                    final int ref2 = listener2.getUpdateCount();

                    final NameResolver nameResolver2 = factory.newNameResolver(SERVICE_URI, NRA);
                    nameResolver2.start(listener2);
                    assertTargetsEqual(listener2, I12); // resolve from cache

                    latch.countDown();

                    // Verify
                    assertTrue(listener.await(1000, ref));
                    assertTrue(listener2.await(1000, ref2));
                    assertTargetsEqual(listener, I34);
                    assertTargetsEqual(listener2, I34);
                    verify(client, times(2)).getInstances(anyString());

                    // Cleanup
                    nameResolver.shutdown();
                    nameResolver2.shutdown();
                    factory.destroy();
                }));
    }

    void assertTargetsEqual(final TestListener listener, final List<ServiceInstance> sis) {
        final ResolutionResult result = listener.getResult();
        assertNotNull(result, "listener not successful");
        assertTargetsEqual(result.getAddresses(), sis);
    }

    void assertTargetsEqual(final List<EquivalentAddressGroup> eags, final List<ServiceInstance> sis) {
        assertTrue(eags.size() == sis.size());
        for (int i = 0; i < eags.size(); i++) {
            final EquivalentAddressGroup eag = eags.get(i);
            final ServiceInstance si = sis.get(i);
            assertTargetEquals(eag, si);
        }
    }

    void assertTargetEquals(final EquivalentAddressGroup eag, final ServiceInstance si) {
        final List<SocketAddress> addresses = eag.getAddresses();
        assertTrue(addresses.size() == 1);
        final SocketAddress socketAddress = addresses.get(0);
        assertTrue(socketAddress.toString().equals('/' + si.getHost() + ':' + si.getPort()));
    }

    void assertError(final TestListener listener) {
        assertEquals(listener.getError().getCode(), Status.Code.UNAVAILABLE);
    }

    private static class TestListener extends Listener2 {

        private ResolutionResult result = null;
        private Status error = Status.UNAVAILABLE.withDescription("Not initialized");
        private int updateCount;

        @Override
        public synchronized void onResult(final ResolutionResult resolutionResult) {
            if (resolutionResult.getAddresses().isEmpty()) {
                onError(Status.UNAVAILABLE.withDescription("Empty addresses"));
            } else {
                this.result = resolutionResult;
                this.error = null;
                this.updateCount++;
                notifyAll();
            }
        }

        @Override
        public synchronized void onError(final Status error) {
            this.result = null;
            this.error = error;
            this.updateCount++;
            notifyAll();
        }

        public synchronized boolean await(final long duration, final int ref) throws InterruptedException {
            long now = System.currentTimeMillis();
            final long limit = now + duration;
            while (limit > now && ref == this.updateCount) {
                wait(limit - now);
                now = System.currentTimeMillis();
            }
            return ref != this.updateCount;
        }

        public ResolutionResult getResult() {
            return this.result;
        }

        public Status getError() {
            return this.error;
        }

        public int getUpdateCount() {
            return this.updateCount;
        }

    }

}
