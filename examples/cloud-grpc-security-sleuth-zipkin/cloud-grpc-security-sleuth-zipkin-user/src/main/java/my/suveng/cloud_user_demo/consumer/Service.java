package my.suveng.cloud_user_demo.consumer;

import brave.Tracer;
import lombok.extern.slf4j.Slf4j;
import my.suveng.istio.grpc.api.order.OrderRequest;
import my.suveng.istio.grpc.api.order.OrderResponse;
import my.suveng.istio.grpc.api.order.OrderServiceGrpc;
import my.suveng.istio.grpc.api.pay.PayRequest;
import my.suveng.istio.grpc.api.pay.PayResponse;
import my.suveng.istio.grpc.api.pay.PayServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.concurrent.Future;

/**
 * grpc servic 调用
 * @author suwenguang
 **/
@org.springframework.stereotype.Service
@Slf4j
public class Service {

    @Autowired
    Tracer tracer;
    @GrpcClient("pay")
    private PayServiceGrpc.PayServiceBlockingStub payServiceBlockingStub;
    @GrpcClient("order")
    private OrderServiceGrpc.OrderServiceBlockingStub orderServiceBlockingStub;

    @Async
    public void asyncPayNoResult() {
        log.info("pay: no result begin ... ");
        PayResponse result = payServiceBlockingStub.getRealNameByUsername(PayRequest.newBuilder().setMoney("123").build());
        log.info("pay: no result end! result={}", result);
    }

    @Async
    public void asyncOrderNoResult() {
        log.info("order: no result begin ...");
        OrderResponse orderResult = orderServiceBlockingStub.getRealNameByUsername(OrderRequest.newBuilder().setPay("true").build());
        log.info("order: no result end! result={}", orderResult);
    }

    @Async
    public Future<String> asyncPayWithResult() {
        log.info("pay: with result begin ...");
        PayResponse result = payServiceBlockingStub.getRealNameByUsername(PayRequest.newBuilder().setMoney("123").build());
        log.info("pay: with result end! result={}", result);
        return AsyncResult.forValue("pay async");
    }

    @Async
    public Future<String> asyncOrderWithResult() {
        log.info("order: with result begin...");
        OrderResponse orderResult = orderServiceBlockingStub.getRealNameByUsername(OrderRequest.newBuilder().setPay("true").build());
        log.info("order: with result end! result={}", orderResult);
        return AsyncResult.forValue("order async");
    }

}
