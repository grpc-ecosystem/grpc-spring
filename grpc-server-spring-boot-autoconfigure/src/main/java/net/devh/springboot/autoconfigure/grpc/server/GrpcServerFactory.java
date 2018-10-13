package net.devh.springboot.autoconfigure.grpc.server;

import io.grpc.Server;
import net.devh.springboot.autoconfigure.grpc.server.codec.GrpcCodecDefinition;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public interface GrpcServerFactory {

    Server createServer();

    String getAddress();

    int getPort();

    void addService(GrpcServiceDefinition service);

    void addCodec(GrpcCodecDefinition codec);

    void destroy();
}
