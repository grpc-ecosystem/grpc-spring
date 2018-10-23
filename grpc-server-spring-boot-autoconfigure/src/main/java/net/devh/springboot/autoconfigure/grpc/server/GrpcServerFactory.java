package net.devh.springboot.autoconfigure.grpc.server;

import io.grpc.Server;
import net.devh.springboot.autoconfigure.grpc.server.codec.GrpcCodecDefinition;

/**
 * A factory that can be used to create grpc servers.
 *
 * @author Michael (yidongnan@gmail.com)
 * @sice 5/17/16
 */
public interface GrpcServerFactory {

    /**
     * Creates a new grpc server with the stored options. The entire lifecycle management of the server
     * should be managed by the calling class. This includes starting and stopping the server.
     *
     * @return The newly created grpc server.
     */
    Server createServer();

    /**
     * Gets the IP address the created server will be bound to.
     *
     * @return The IP address the server will be bound to.
     */
    String getAddress();

    /**
     * Gets the local port the created server will use to listen to requests.
     *
     * @return Gets the local port the server will use.
     */
    int getPort();

    /**
     * Adds the given grpc service definition to this factory. The created server will serve the
     * services described by these definitions.
     *
     * <p>
     * <b>Note:</b> Adding a service does not effect servers that have already been created.
     * </p>
     *
     * @param service The service to add to the grpc server.
     */
    void addService(GrpcServiceDefinition service);

    /**
     * Adds the given grpc codec definition to this factory. The created server will use the codec
     * described by these definitions.
     *
     * <p>
     * <b>Note:</b> Adding a codec does not effect servers that have already been created.
     * </p>
     *
     * @param codec The codec to add to the grpc server.
     */
    void addCodec(GrpcCodecDefinition codec);

    /**
     * Destroys this factory. This does not destroy or shutdown any server that was created using this
     * factory.
     */
    void destroy();

}
