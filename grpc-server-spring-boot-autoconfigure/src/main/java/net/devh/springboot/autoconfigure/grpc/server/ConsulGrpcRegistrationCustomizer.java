package net.devh.springboot.autoconfigure.grpc.server;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistrationCustomizer;


/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 7/21/2018
 */
public class ConsulGrpcRegistrationCustomizer implements ConsulRegistrationCustomizer {

    private GrpcServerProperties grpcServerProperties;

    public ConsulGrpcRegistrationCustomizer(GrpcServerProperties grpcServerProperties) {
        this.grpcServerProperties = grpcServerProperties;
    }

    @Override
    public void customize(ConsulRegistration registration) {
        List<String> tags = registration.getService().getTags();
        if (tags == null) {
            tags = new ArrayList<>();
        }
        tags.add("gRPC.port=" + grpcServerProperties.getPort());
        registration.getService().setTags(tags);
    }
}
