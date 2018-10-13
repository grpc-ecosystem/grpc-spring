package net.devh.springboot.autoconfigure.grpc.server;

import java.util.Collection;

import net.devh.springboot.autoconfigure.grpc.server.codec.GrpcCodecDefinition;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public interface GrpcServiceDiscoverer {

    Collection<GrpcServiceDefinition> findGrpcServices();

    Collection<GrpcCodecDefinition> findGrpcCodec();
}
