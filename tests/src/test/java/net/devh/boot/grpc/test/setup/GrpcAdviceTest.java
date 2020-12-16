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

import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.test.config.BaseAutoConfiguration;
import net.devh.boot.grpc.test.config.BaseExceptionAdviceAutoConfiguration;
import net.devh.boot.grpc.test.config.GrpcServiceAdviceConfig;
import net.devh.boot.grpc.test.config.GrpcServiceAdviceConfig.TestGrpcService;
import net.devh.boot.grpc.test.config.InProcessConfiguration;

/**
 * A test checking that the server picks up a {@link GrpcAdvice} annotated bean from a {@link Configuration}.
 *
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig(classes = {
        InProcessConfiguration.class,
        GrpcServiceAdviceConfig.class,
        BaseAutoConfiguration.class,
        BaseExceptionAdviceAutoConfiguration.class})
@DirtiesContext
class GrpcAdviceTest extends AbstractSimpleServerClientTest {

    public GrpcAdviceTest() {
        log.info("--- GrpcServiceAdviceTest ---");
    }

    @Autowired
    private GrpcServiceAdviceConfig.TestAdvice testAdvice;

    @Autowired
    private TestGrpcService testGrpcService;


    @Disabled
    @Test
    @Override
    void testSuccessfulCall() throws InterruptedException, ExecutionException {

        // TODO - @Aspect in GrpcServiceAdviceExceptionHandler not triggered
        Assertions.assertThatThrownBy(super::testSuccessfulCall)
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessage(Status.NOT_FOUND.toString())
                .getCause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trigger GrpcServiceAdvice");
    }

}
