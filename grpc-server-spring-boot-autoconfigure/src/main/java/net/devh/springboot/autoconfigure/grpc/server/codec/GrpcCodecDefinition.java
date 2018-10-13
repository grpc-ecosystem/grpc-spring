package net.devh.springboot.autoconfigure.grpc.server.codec;

import io.grpc.Codec;
import lombok.Getter;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 10/13/18
 */
@Getter
public class GrpcCodecDefinition {

    private final Codec codec;

    private final boolean advertised;

    private final CodecType codecType;

    public GrpcCodecDefinition(Codec codec, boolean advertised, CodecType codecType) {
        this.codec = codec;
        this.advertised = advertised;
        this.codecType = codecType;
    }
}
