package net.devh.cloud_user_demo.controller;

import brave.Tracer;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.cloud_user_demo.consumer.Service;
import net.devh.grpc.api.order.OrderRequest;
import net.devh.grpc.api.order.OrderResponse;
import net.devh.grpc.api.order.OrderServiceGrpc;
import net.devh.grpc.api.pay.PayRequest;
import net.devh.grpc.api.pay.PayResponse;
import net.devh.grpc.api.pay.PayServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;

/**
 *
 * @author suwenguang
 **/
@RestController
@Slf4j
public class PayController {

    @Autowired
    Tracer tracer;
    @Autowired
    Service service;
    @GrpcClient("pay")
    private PayServiceGrpc.PayServiceBlockingStub payServiceBlockingStub;
    @GrpcClient("order")
    private OrderServiceGrpc.OrderServiceBlockingStub orderServiceBlockingStub;

    @GetMapping("/api/pay")
    public Object pay() throws ExecutionException, InterruptedException {
        // 使用原生的异步线程/线程池, sleuth无法传递traceId
        FutureTask<String> futureTask = new FutureTask<>(() -> {
            OrderResponse orderResult = orderServiceBlockingStub.getRealNameByUsername(OrderRequest.newBuilder().setPay("true").build());
            if (tracer == null) {
                log.info("tracer 为空, thread={}", Thread.currentThread().getName());
            } else {
                log.info("tracerId={}", tracer.toString());
            }
            return "hello";
        });
        new Thread(futureTask).start();

        String s = futureTask.get();


        FutureTask<String> task = new FutureTask<>(() -> {
            PayResponse result = payServiceBlockingStub.getRealNameByUsername(PayRequest.newBuilder().setMoney("123").build());
            if (tracer == null) {
                log.info("tracer 为空, thread={}", Thread.currentThread().getName());
            } else {
                log.info("tracerId={}", tracer.toString());
            }
            return "123";
        });

        new Thread(task).start();
        String s1 = task.get();

        // 配置sleuth提供线程池, 配合@Async注解使用 能够成功
        service.asyncPayNoResult();
        service.asyncOrderNoResult();

        Future<String> asyncPayWithResult = service.asyncPayWithResult();

        try {
            String s2 = asyncPayWithResult.get(5000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Future<String> asyncOrderWithResult = service.asyncOrderWithResult();
        try {
            String s2 = asyncOrderWithResult.get(5000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return "traceId=" + tracer.currentSpan().context().traceIdString();
    }


}
