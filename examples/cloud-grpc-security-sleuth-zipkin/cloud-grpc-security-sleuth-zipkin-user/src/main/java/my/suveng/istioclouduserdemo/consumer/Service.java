package my.suveng.istioclouduserdemo.consumer;

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

	@GrpcClient("pay")
	private PayServiceGrpc.PayServiceBlockingStub payServiceBlockingStub;

	@GrpcClient("order")
	private OrderServiceGrpc.OrderServiceBlockingStub orderServiceBlockingStub;

	@Autowired
	Tracer tracer;


	/**
	 * 异步是没问题的
	 * @see my.suveng.istioclouduserdemo.config.ThreadConfig
	 */
	@Async
	public void asyncPayNoResult(){
		PayResponse result = payServiceBlockingStub.getRealNameByUsername(PayRequest.newBuilder().setMoney("123").build());
		log.info("pay: getRealNameByUsername ");
	}

	@Async
	public void asyncOrderNoResult(){
		OrderResponse orderResult = orderServiceBlockingStub.getRealNameByUsername(OrderRequest.newBuilder().setPay("true").build());
		log.info("order: getRealNameByUsername");
	}

	@Async
	public Future<String> asyncPayWithResult(){
		PayResponse result = payServiceBlockingStub.getRealNameByUsername(PayRequest.newBuilder().setMoney("123").build());
		log.info("pay async: getRealNameByUsername");
		return AsyncResult.forValue("pay async");
	}

	@Async
	public Future<String> asyncOrderWithResult(){
		OrderResponse orderResult = orderServiceBlockingStub.getRealNameByUsername(OrderRequest.newBuilder().setPay("true").build());
		log.info("order async: getRealNameByUsername");
		return AsyncResult.forValue("order async");
	}

}
