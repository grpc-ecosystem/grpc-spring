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

package net.devh.boot.grpc.test.advice;

import io.grpc.Metadata;

public class GrpcMetaDataUtils {

    private GrpcMetaDataUtils() {
        throw new UnsupportedOperationException("Util class not to be instantiated.");
    }

    public static Metadata createExpectedAsciiHeader() {

        return createAsciiHeader("HEADER_KEY", "HEADER_VALUE");
    }


    public static Metadata createAsciiHeader(String key, String value) {

        Metadata metadata = new Metadata();
        Metadata.Key<String> asciiKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(asciiKey, value);
        return metadata;
    }
}
