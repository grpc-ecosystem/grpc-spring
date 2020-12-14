/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.test.setup;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.exceptionhandling.GrpcServiceAdvice;
import net.devh.boot.grpc.server.service.exceptionhandling.GrpcServiceAdviceExceptionHandler;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.GrpcServiceAdviceConfig;
import net.devh.boot.grpc.test.config.InProcessConfiguration;

/**
 * A test checking that the server picks up a {@link GrpcServiceAdvice} annotated bean from a {@link Configuration}.
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig(classes = {
        InProcessConfiguration.class,
        GrpcServiceAdviceConfig.class,
        BaseAutoConfiguration.class,
        GrpcServiceAdviceExceptionHandler.class})
@DirtiesContext
class GrpcServiceAdviceTest extends AbstractSimpleServerClientTest {

    public GrpcServiceAdviceTest() {
        log.info("--- GrpcServiceAdviceTest ---");
    }


    // @GrpcServiceAdvice
    // class TestAdvice {
    //
    // @GrpcExceptionHandler
    // public StatusRuntimeException throwSomeError(IllegalArgumentException e) {
    // return Status.NOT_FOUND.withDescription("Something not found").withCause(e).asRuntimeException();
    // }
    // }

    @Autowired
    GrpcServiceAdviceExceptionHandler grpcServiceAdviceExceptionHandler;

    @Autowired
    AtomicBoolean invoked;

    // @Override
    @Test
    void testSuccessfulCall() throws InterruptedException, ExecutionException {

        assertFalse(this.invoked.get());
        super.testSuccessfulCall();
        assertTrue(this.invoked.get());
    }

}
