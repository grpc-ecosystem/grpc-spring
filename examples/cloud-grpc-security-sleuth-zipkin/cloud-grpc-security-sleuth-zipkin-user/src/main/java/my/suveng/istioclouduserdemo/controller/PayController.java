package my.suveng.istioclouduserdemo.controller;

import brave.Tracer;
import lombok.extern.slf4j.Slf4j;
import my.suveng.istio.grpc.api.order.OrderServiceGrpc;
import my.suveng.istio.grpc.api.pay.PayServiceGrpc;
import my.suveng.istioclouduserdemo.consumer.Service;
import my.suveng.model.common.interfaces.response.IMessage;
import my.suveng.model.common.response.Message;
import net.devh.boot.grpc.client.inject.GrpcClient;
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

	@GrpcClient("pay")
	private PayServiceGrpc.PayServiceBlockingStub payServiceBlockingStub;

	@GrpcClient("order")
	private OrderServiceGrpc.OrderServiceBlockingStub orderServiceBlockingStub;

	@Autowired
	Tracer tracer;

	@Autowired
	Service service;

	@GetMapping("/api/pay")
	public IMessage<String> pay() throws ExecutionException, InterruptedException {
		// 使用原生的异步线程/线程池, sleuth无法传递traceId
		//FutureTask<String> futureTask = new FutureTask<>(new Callable<String>() {
		//	@Override
		//	public String call() throws Exception {
		//		OrderResponse orderResult = orderServiceBlockingStub.getRealNameByUsername(OrderRequest.newBuilder().setPay("true").build());
		//		//System.out.println(tracer.currentSpan().context().traceId());
		//		return "hello";
		//	}
		//});
		//new Thread(futureTask).start();
		//
		//String s = futureTask.get();
		//
		//FutureTask<String> task = new FutureTask<>(new Callable<String>() {
		//	@Override
		//	public String call() throws Exception {
		//		PayResponse result = payServiceBlockingStub.getRealNameByUsername(PayRequest.newBuilder().setMoney("123").build());
		//		//System.out.println(tracer.currentSpan().context().traceId());
		//
		//		return "123";
		//	}
		//});
		//new Thread(task).start();
		//String s1 = task.get();

		// 配置sleuth提供线程池, 配合@Async注解使用 能够成功
		service.asyncPayNoResult();
		service.asyncOrderNoResult();

		Future<String> asyncPayWithResult = service.asyncPayWithResult();

		try {
			String s2 = asyncPayWithResult.get(100, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		Future<String> asyncOrderWithResult = service.asyncOrderWithResult();
		try {
			String s2 = asyncOrderWithResult.get(100, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		return Message.successWithData(tracer.currentSpan().context().traceIdString());
	}


}
