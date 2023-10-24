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
