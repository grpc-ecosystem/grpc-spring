package net.devh.cloud_order_demo.consumer;

import brave.Tracer;
import lombok.extern.slf4j.Slf4j;

import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.grpc.api.order.OrderRequest;
import net.devh.grpc.api.order.OrderResponse;
import net.devh.grpc.api.order.OrderServiceGrpc;
import net.devh.grpc.api.pay.PayRequest;
import net.devh.grpc.api.pay.PayResponse;
import net.devh.grpc.api.pay.PayServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.Future;

/**
 * service 测试主逻辑
 * @author suwenguang
 **/
@org.springframework.stereotype.Service
@Slf4j
public class Service {

    @Autowired
    Tracer tracer;
    /**
     * 注入PayService的grpc client
     */
    @GrpcClient("pay")
    private PayServiceGrpc.PayServiceBlockingStub payServiceBlockingStub;
    /**
     * 注入OrderService的grpc client
     */
    @GrpcClient("order")
    private OrderServiceGrpc.OrderServiceBlockingStub orderServiceBlockingStub;

    /**
     * 异步是没问题的
     */
    @Async
    public void asyncPayNoResult() {
        log.info("pay: no result begin ... ");
        PayResponse result = payServiceBlockingStub.getRealNameByUsername(PayRequest.newBuilder().setMoney("123").build());
        log.info("pay: no result end! result={}", result);
    }

    @Async
    public void asyncOrderNoResult() {
        log.info("order: no result begin ... ");
        OrderResponse orderResult = orderServiceBlockingStub.getRealNameByUsername(OrderRequest.newBuilder().setPay("true").build());
        log.info("order: not result end!");
    }

    @Async
    public Future<String> asyncPayWithResult() {
        log.info("pay: with result begin ... ");
        PayResponse orderResult = payServiceBlockingStub.getRealNameByUsername(PayRequest.newBuilder().setMoney("123").build());
        ListenableFuture<String> result = AsyncResult.forValue(orderResult + "");
        log.info("pay: with result end! result={}", orderResult);
        return result;
    }

    @Async
    public Future<String> asyncOrderWithResult() {
        log.info("order: with result beging ...");
        OrderResponse orderResult = orderServiceBlockingStub.getRealNameByUsername(OrderRequest.newBuilder().setPay("true").build());
        log.info("order: with result end! result={}", orderResult);
        return AsyncResult.forValue(orderResult + "");
    }

}
