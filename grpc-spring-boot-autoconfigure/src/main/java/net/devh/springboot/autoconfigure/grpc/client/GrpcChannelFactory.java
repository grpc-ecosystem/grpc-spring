package net.devh.springboot.autoconfigure.grpc.client;

import io.grpc.Channel;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public interface GrpcChannelFactory {

    Channel createChannel(String name);
}
