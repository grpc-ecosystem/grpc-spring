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

package net.devh.boot.grpc.common.util;

/**
 * Class that contains shared constants
 */
public final class Constants {

    /**
     * A constant that defines the current version of the library.
     */
    public static final String VERSION = "v"+Constants.class.getPackage().getImplementationVersion();


    /**
     * A constant that defines the library name that can be used as metric tags
     */
    public static final String LIBRARY_NAME = "grpc-spring";


    private Constants() {}

}
