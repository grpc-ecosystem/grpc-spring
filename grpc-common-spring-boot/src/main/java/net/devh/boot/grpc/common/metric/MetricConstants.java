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

package net.devh.boot.grpc.common.metric;

/**
 * Utility class that contains constants that are used multiple times by different classes.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class MetricConstants {

    /**
     * The total number of requests received
     */
    public static final String METRIC_NAME_SERVER_REQUESTS_RECEIVED = "grpc.server.requests.received";
    /**
     * The total number of responses sent
     */
    public static final String METRIC_NAME_SERVER_RESPONSES_SENT = "grpc.server.responses.sent";
    /**
     * The total time taken for the server to complete the call.
     */
    public static final String METRIC_NAME_SERVER_PROCESSING_DURATION = "grpc.server.processing.duration";

    /**
     * The total number of requests sent
     */
    public static final String METRIC_NAME_CLIENT_REQUESTS_SENT = "grpc.client.requests.sent";
    /**
     * The total number of responses received
     */
    public static final String METRIC_NAME_CLIENT_RESPONSES_RECEIVED = "grpc.client.responses.received";
    /**
     * The total time taken for the client to complete the call, including network delay
     */
    public static final String METRIC_NAME_CLIENT_PROCESSING_DURATION = "grpc.client.processing.duration";

    /**
     * The metrics tag key that belongs to the called service name.
     */
    public static final String TAG_SERVICE_NAME = "service";
    /**
     * The metrics tag key that belongs to the called method name.
     */
    public static final String TAG_METHOD_NAME = "method";
    /**
     * The metrics tag key that belongs to the type of the called method.
     */
    public static final String TAG_METHOD_TYPE = "methodType";
    /**
     * The metrics tag key that belongs to the result status code.
     */
    public static final String TAG_STATUS_CODE = "statusCode";

    private MetricConstants() {}

}
