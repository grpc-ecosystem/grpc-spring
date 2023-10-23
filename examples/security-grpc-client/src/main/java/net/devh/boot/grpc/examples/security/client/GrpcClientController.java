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

package net.devh.boot.grpc.examples.security.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * A simple rest controller that can be used to send a request and show its response.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@RestController
public class GrpcClientController {

    @Value("${auth.username}")
    private String username;

    @Autowired
    private GrpcClientService grpcClientService;

    @RequestMapping(path = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    public String printMessage(@RequestParam(defaultValue = "Michael") final String name) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Input:\n")
                .append("- name: " + name + " (Changeable via URL param ?name=X)\n")
                .append("Request-Context:\n")
                .append("- auth user: " + this.username + " (Configure via application.yml)\n")
                .append("Response:\n")
                .append(this.grpcClientService.sendMessage(name));
        return sb.toString();
    }

}
