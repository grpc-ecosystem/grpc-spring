package my.suveng.istiocloudorderdemo.consumer;

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
 *
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
	 *
	 */
	@Async
	public void t1(){
		PayResponse result = payServiceBlockingStub.getRealNameByUsername(PayRequest.newBuilder().setMoney("123").build());
		log.info("t1");
	}

	@Async
	public void t2(){
		OrderResponse orderResult = orderServiceBlockingStub.getRealNameByUsername(OrderRequest.newBuilder().setPay("true").build());
		log.info("t2");
	}

	@Async
	public Future<String> at1(){
		PayResponse result = payServiceBlockingStub.getRealNameByUsername(PayRequest.newBuilder().setMoney("123").build());
		log.info("at1");
		return AsyncResult.forValue("t1");
	}

	@Async
	public Future<String> at2(){
		OrderResponse orderResult = orderServiceBlockingStub.getRealNameByUsername(OrderRequest.newBuilder().setPay("true").build());
		log.info("at2");
		return AsyncResult.forValue("t2");
	}

}
