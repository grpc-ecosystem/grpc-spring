/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
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
