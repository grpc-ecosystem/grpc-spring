package net.devh.boot.grpc.examples.observability.frontend;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.examples.observability.proto.BidiStreamingRequest;
import net.devh.boot.grpc.examples.observability.proto.BidiStreamingResponse;
import net.devh.boot.grpc.examples.observability.proto.ClientStreamingRequest;
import net.devh.boot.grpc.examples.observability.proto.ClientStreamingResponse;
import net.devh.boot.grpc.examples.observability.proto.ExampleServiceGrpc.ExampleServiceStub;
import net.devh.boot.grpc.examples.observability.proto.ServerStreamingRequest;
import net.devh.boot.grpc.examples.observability.proto.ServerStreamingResponse;
import net.devh.boot.grpc.examples.observability.proto.UnaryRequest;
import net.devh.boot.grpc.examples.observability.proto.UnaryResponse;

@SpringBootApplication
public class FrontendApplication implements CommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(FrontendApplication.class.getName());

    // Define constants for byte array sizes
    private static final int MIN_BYTE_ARRAY_SIZE = 10240;
    private static final int MAX_BYTE_ARRAY_SIZE = 20480;

    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }

    @GrpcClient("backend")
    private ExampleServiceStub stub;

    private void CallUnaryRpc() {
        byte[] bytes = new byte[ThreadLocalRandom.current().nextInt(MIN_BYTE_ARRAY_SIZE, MAX_BYTE_ARRAY_SIZE)];
        ThreadLocalRandom.current().nextBytes(bytes);
        UnaryRequest request = UnaryRequest.newBuilder().setMessage(new String(bytes)).build();
        stub.unaryRpc(request, new StreamObserver<>() {
            @Override
            public void onNext(UnaryResponse value) {}

            @Override
            public void onError(Throwable t) {
                LOGGER.severe(Status.fromThrowable(t).toString());
                CallUnaryRpc();
            }

            @Override
            public void onCompleted() {
                CallUnaryRpc();
            }
        });
    }

    private void CallClientStreamingRpc() {
        byte[] bytes = new byte[ThreadLocalRandom.current().nextInt(MIN_BYTE_ARRAY_SIZE, MAX_BYTE_ARRAY_SIZE)];
        ThreadLocalRandom.current().nextBytes(bytes);
        ClientStreamingRequest request = ClientStreamingRequest.newBuilder()
                .setMessage(new String(bytes)).build();
        StreamObserver<ClientStreamingRequest> requestStreamObserver = stub.clientStreamingRpc(
                new StreamObserver<>() {
                    @Override
                    public void onNext(ClientStreamingResponse value) {}

                    @Override
                    public void onError(Throwable t) {
                        CallClientStreamingRpc();
                    }

                    @Override
                    public void onCompleted() {
                        CallClientStreamingRpc();
                    }
                });
        requestStreamObserver.onNext(request);
        requestStreamObserver.onCompleted();
    }

    private void CallServerStreamingRpc() {
        byte[] bytes = new byte[ThreadLocalRandom.current().nextInt(MIN_BYTE_ARRAY_SIZE, MAX_BYTE_ARRAY_SIZE)];
        ThreadLocalRandom.current().nextBytes(bytes);
        ServerStreamingRequest request = ServerStreamingRequest.newBuilder()
                .setMessage(new String(bytes)).build();
        stub.serverStreamingRpc(request, new StreamObserver<>() {
            @Override
            public void onNext(ServerStreamingResponse value) {}

            @Override
            public void onError(Throwable t) {
                CallServerStreamingRpc();
            }

            @Override
            public void onCompleted() {
                CallServerStreamingRpc();
            }
        });
    }

    private void CallBidStreamingRpc() {
        byte[] bytes = new byte[ThreadLocalRandom.current().nextInt(MIN_BYTE_ARRAY_SIZE, MAX_BYTE_ARRAY_SIZE)];
        ThreadLocalRandom.current().nextBytes(bytes);
        BidiStreamingRequest request = BidiStreamingRequest.newBuilder()
                .setMessage(new String(bytes)).build();
        StreamObserver<BidiStreamingRequest> requestStreamObserver = stub.bidiStreamingRpc(
                new StreamObserver<>() {
                    @Override
                    public void onNext(BidiStreamingResponse value) {}

                    @Override
                    public void onError(Throwable t) {
                        CallBidStreamingRpc();
                    }

                    @Override
                    public void onCompleted() {
                        CallBidStreamingRpc();
                    }
                });
        requestStreamObserver.onNext(request);
        requestStreamObserver.onCompleted();
    }

    @Override
    public void run(String... args) {
        CallUnaryRpc();
        CallServerStreamingRpc();
        CallClientStreamingRpc();
        CallBidStreamingRpc();
    }
}
