package net.devh.springboot.autoconfigure.grpc.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.grpc.internal.GrpcUtil;
import io.grpc.netty.NegotiationType;
import lombok.Data;

/**
 * The channel properties for a single named grpc channel or service reference.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Data
public class GrpcChannelProperties {

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final Integer DEFAULT_PORT = 9090;
    private static final List<String> DEFAULT_HOSTS = Arrays.asList(DEFAULT_HOST);
    private static final List<Integer> DEFAULT_PORTS = Arrays.asList(DEFAULT_PORT);

    public static final GrpcChannelProperties DEFAULT = new GrpcChannelProperties();

    /**
     * A list of hosts to connect to. These entries should be kept in tandem with the port entries.
     * Defaults to {@link #DEFAULT_HOST}.
     */
    private List<String> host = new ArrayList<>(DEFAULT_HOSTS);

    /**
     * A list of ports to connect to. These entries should be kept in tandem with the host entries.
     * Defaults to {@link #DEFAULT_PORT}.
     */
    private List<Integer> port = new ArrayList<>(DEFAULT_PORTS);

    /**
     * Setting to enable keepalive. Default to {@code false}.
     */
    private boolean enableKeepAlive = false;

    /**
     * Sets whether keepalive will be performed when there are no outstanding RPC on a connection.
     * Defaults to {@code false}.
     */
    private boolean keepAliveWithoutCalls = false;

    /**
     * The default delay in seconds before we send a keepalive. Defaults to {@code 60}.
     */
    private long keepAliveTime = 60;

    /**
     * The default timeout in seconds for a keepalive ping request. Defaults to {@code 20}.
     */
    private long keepAliveTimeout = 20;

    /**
     * The maximum message size in bytes allowed to be received on the channel. If not set (<tt>-2</tt>)
     * then it will default to {@link GrpcUtil#DEFAULT_MAX_MESSAGE_SIZE DEFAULT_MAX_MESSAGE_SIZE}. If
     * set to <tt>-1</tt> then it will use {@link Integer#MAX_VALUE} as limit.
     */
    private int maxInboundMessageSize = -2;

    private boolean fullStreamDecompression = false;

    /**
     * The negotiation type to use on the connection. Either of {@link NegotiationType#TLS TLS}
     * (recommended), {@link NegotiationType#PLAINTEXT_UPGRADE PLAINTEXT_UPGRADE} or
     * {@link NegotiationType#PLAINTEXT PLAINTEXT}. Defaults to TLS.
     */
    private NegotiationType negotiationType = NegotiationType.TLS;

}
