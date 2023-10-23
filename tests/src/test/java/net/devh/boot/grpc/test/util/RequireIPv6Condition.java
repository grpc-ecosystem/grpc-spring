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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import lombok.extern.slf4j.Slf4j;

/**
 * A JUnit 5 condition that checks whether the current host has an IPv6 loopback address.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class RequireIPv6Condition implements ExecutionCondition {

    private static volatile ConditionEvaluationResult ipv6Result;

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (ipv6Result == null) {
            boolean result = false;
            try {
                result = hasIPv6Loopback();
            } catch (SocketException e) {
                log.warn("Could not determine presence of IPv6 loopback address", e);
            }

            if (result) {
                ipv6Result = ConditionEvaluationResult.enabled("Found IPv6 loopback");
            } else {
                ipv6Result = ConditionEvaluationResult.disabled("Could not find IPv6 loopback");
            }
        }
        return ipv6Result;
    }

    private static boolean hasIPv6Loopback() throws SocketException {
        final Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface iface : Collections.list(ifaces)) {
            final Enumeration<InetAddress> addresses = iface.getInetAddresses();
            for (InetAddress address : Collections.list(addresses)) {
                if (address instanceof Inet6Address && address.isLoopbackAddress()) {
                    return true;
                }
            }
        }
        return false;
    }

}
