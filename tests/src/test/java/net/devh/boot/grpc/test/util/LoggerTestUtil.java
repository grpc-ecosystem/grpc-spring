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

package net.devh.boot.grpc.test.util;

import java.util.Arrays;

import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * Class to test proper @Slf4j logging.
 * 
 * @author Andjelko Perisic (andjelko.perisic@gmail.com)
 */
public class LoggerTestUtil {

    private LoggerTestUtil() {
        throw new UnsupportedOperationException("Util class not to be instantiated.");
    }


    public static ListAppender<ILoggingEvent> getListAppenderForClasses(@Nullable Class<?>... classList) {

        ListAppender<ILoggingEvent> loggingEventListAppender = new ListAppender<>();
        loggingEventListAppender.start();

        if (classList == null) {
            return loggingEventListAppender;
        }

        Arrays.stream(classList)
                .map(clazz -> (Logger) LoggerFactory.getLogger(clazz))
                .forEach(log -> log.addAppender(loggingEventListAppender));

        return loggingEventListAppender;
    }

}
