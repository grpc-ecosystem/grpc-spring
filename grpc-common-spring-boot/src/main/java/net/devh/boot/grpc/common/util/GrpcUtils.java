/*
 * Copyright (c) 2016-2021 Michael Zhang <yidongnan@gmail.com>
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

package net.devh.boot.grpc.common.util;

import io.grpc.MethodDescriptor;

/**
 * Utility class that contains methods to extract some information from grpc classes.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class GrpcUtils {

    /**
     * A constant that defines, the scheme of a Unix domain socket address.
     */
    public static final String DOMAIN_SOCKET_ADDRESS_SCHEME = "unix";

    /**
     * A constant that defines, the scheme prefix of a Unix domain socket address.
     */
    public static final String DOMAIN_SOCKET_ADDRESS_PREFIX = DOMAIN_SOCKET_ADDRESS_SCHEME + ":";

    /**
     * The cloud discovery metadata key used to identify the grpc port.
     */
    public static final String CLOUD_DISCOVERY_METADATA_PORT = "gRPC_port";

    /**
     * The constant for the grpc server port, -1 represents don't start an inter process server.
     */
    public static final int INTER_PROCESS_DISABLE = -1;

    /**
     * Extracts the domain socket address specific path from the given full address. The address must fulfill the
     * requirements as specified by <a href="https://grpc.github.io/grpc/cpp/md_doc_naming.html">grpc</a>.
     *
     * @param address The address to extract it from.
     * @return The extracted domain socket address specific path.
     * @throws IllegalArgumentException If the given address is not a valid address.
     */
    public static String extractDomainSocketAddressPath(final String address) {
        if (!address.startsWith(DOMAIN_SOCKET_ADDRESS_PREFIX)) {
            throw new IllegalArgumentException(address + " is not a valid domain socket address.");
        }
        String path = address.substring(DOMAIN_SOCKET_ADDRESS_PREFIX.length());
        if (path.startsWith("//")) {
            path = path.substring(2);
            // We don't check this as there is no reliable way to check that it's an absolute path,
            // especially when Windows adds support for these in the future
            // if (!path.startsWith("/")) {
            // throw new IllegalArgumentException("If the path is prefixed with '//', then the path must be absolute");
            // }
        }
        return path;
    }

    /**
     * Extracts the service name from the given method.
     *
     * @param method The method to get the service name from.
     * @return The extracted service name.
     * @see MethodDescriptor#extractFullServiceName(String)
     * @see #extractMethodName(MethodDescriptor)
     */
    public static String extractServiceName(final MethodDescriptor<?, ?> method) {
        return MethodDescriptor.extractFullServiceName(method.getFullMethodName());
    }

    /**
     * Extracts the method name from the given method.
     *
     * @param method The method to get the method name from.
     * @return The extracted method name.
     * @see #extractServiceName(MethodDescriptor)
     */
    public static String extractMethodName(final MethodDescriptor<?, ?> method) {
        // This method is the equivalent of MethodDescriptor.extractFullServiceName
        final String fullMethodName = method.getFullMethodName();
        final int index = fullMethodName.lastIndexOf('/');
        if (index == -1) {
            return fullMethodName;
        }
        return fullMethodName.substring(index + 1);
    }

    private GrpcUtils() {}

}
