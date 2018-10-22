package net.devh.springboot.autoconfigure.grpc.client;

import java.util.List;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;

/**
 * This factory creates grpc {@link Channel}s for a given service name. Implementations are
 * encouraged to utilize connection pooling and thus {@link #close() close} should be called before
 * disposing it.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
public interface GrpcChannelFactory extends AutoCloseable {

    /**
     * Creates a new channel for the given service name. The returned channel will use all globally
     * registered {@link ClientInterceptor}s.
     *
     * <p>
     * <b>Note:</b> The underlying implementation might reuse existing {@link ManagedChannel}s allow
     * connection reuse.
     * </p>
     *
     * @param name The name of the service.
     * @return The newly created channel for the given service.
     */
    Channel createChannel(String name);

    /**
     * Creates a new channel for the given service name. The returned channel will use all globally
     * registered {@link ClientInterceptor}s.
     *
     * <p>
     * <b>Note:</b> The underlying implementation might reuse existing {@link ManagedChannel}s allow
     * connection reuse.
     * </p>
     *
     * <p>
     * <b>Note:</b> The given interceptors will be applied after the global interceptors. But the
     * interceptors that were applied last, will be called first.
     * </p>
     *
     * @param name The name of the service.
     * @param interceptors A list of additional client interceptors that should be added to the channel.
     * @return The newly created channel for the given service.
     */
    Channel createChannel(String name, List<ClientInterceptor> interceptors);

}
