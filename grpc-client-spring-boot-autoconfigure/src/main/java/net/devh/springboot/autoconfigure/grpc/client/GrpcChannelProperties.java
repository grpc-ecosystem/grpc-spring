package net.devh.springboot.autoconfigure.grpc.client;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
@Data
public class GrpcChannelProperties {

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final Integer DEFAULT_PORT = 9090;

    public static final GrpcChannelProperties DEFAULT = new GrpcChannelProperties();

    private List<String> host = new ArrayList<String>() {
        private static final long serialVersionUID = -8367871342050560040L;

        {
            add(DEFAULT_HOST);
        }
    };
    private List<Integer> port = new ArrayList<Integer>() {
        private static final long serialVersionUID = 4705083089654936515L;

        {
            add(DEFAULT_PORT);
        }
    };
    private boolean plaintext = true;
}
