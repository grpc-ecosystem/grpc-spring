package net.devh.springboot.autoconfigure.grpc.client;

import java.util.List;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public interface GrpcChannelFactory {

    Channel createChannel(String name);

    Channel createChannel(String name, List<ClientInterceptor> interceptors);
}
