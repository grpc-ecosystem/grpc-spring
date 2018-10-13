package net.devh.springboot.autoconfigure.grpc.server.codec;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 10/13/18
 */
public enum CodecType {

    COMPRESS,
    DECOMPRESS,
    ALL;

    CodecType() {
    }
}
