/*
 * Copyright 2016 Google, Inc.
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
 *
 */

package net.devh.springboot.autoconfigure.grpc.client;

/**
 * Created by rayt on 5/17/16.
 */
public class GrpcChannelProperties {

    public static final GrpcChannelProperties DEFAULT = new GrpcChannelProperties();

    private boolean discover = false;
    private String host = "localhost";
    private int port = 9090;
    private boolean plaintext = true;

    public boolean isDiscover() {
        return discover;
    }

    @Override
    public String toString() {
        return "GrpcChannelProperties{" +
                "discover=" + discover +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", plaintext=" + plaintext +
                '}';
    }

    public void setDiscover(boolean discover) {
        this.discover = discover;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isPlaintext() {
        return plaintext;
    }

    public void setPlaintext(boolean plaintext) {
        this.plaintext = plaintext;
    }

}
