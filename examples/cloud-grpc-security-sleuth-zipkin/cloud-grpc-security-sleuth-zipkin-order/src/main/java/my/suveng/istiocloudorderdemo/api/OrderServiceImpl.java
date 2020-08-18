package my.suveng.istiocloudorderdemo.api;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import my.suveng.istio.grpc.api.order.OrderRequest;
import my.suveng.istio.grpc.api.order.OrderResponse;
import my.suveng.istio.grpc.api.order.OrderServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 *
 * @author suwenguang
 **/
@GrpcService
@Slf4j
public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {

	@Override
	public void getRealNameByUsername(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
		log.info("order 接受到参数: " + request + "");
		OrderResponse response = OrderResponse.newBuilder().setOrderId("order").build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void getRealNameByUsernameResStream(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
		log.info("order 接受到参数: " + request + "");
		OrderResponse response = OrderResponse.newBuilder().setOrderId("123").build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public StreamObserver<OrderRequest> getRealNameByUsernameReqStream(StreamObserver<OrderResponse> responseObserver) {
		// 流式,使用的是异步回调,事件驱动回调
		return new StreamObserver<OrderRequest>() {
			// 查看父类说明
			@Override
			public void onNext(OrderRequest myRequest) {
				// 在这里获取参数
				log.info("order onNext: " + myRequest.getPay());
			}

			@Override
			public void onError(Throwable throwable) {
				// 当在onNext 或者 onCompleted 发生异常时, 会被回调
				throwable.printStackTrace();
			}

			@Override
			public void onCompleted() {
				// 这里编写正常业务逻辑
				OrderResponse suveng = OrderResponse.newBuilder().setOrderId("order").build();
				responseObserver.onNext(suveng);
				responseObserver.onCompleted();
			}
		};
	}

	@Override
	public StreamObserver<OrderRequest> getRealNameByUsernameStreamAll(StreamObserver<OrderResponse> responseObserver) {
		// 流式,使用的是异步回调,事件驱动回调
		return new StreamObserver<OrderRequest>() {
			// 查看父类说明
			@Override
			public void onNext(OrderRequest myRequest) {
				// 在这里获取参数
				log.info("order onNext: " + myRequest.getPay());
			}

			@Override
			public void onError(Throwable throwable) {
				// 当在onNext 或者 onCompleted 发生异常时, 会被回调
				throwable.printStackTrace();
				responseObserver.onCompleted();
			}

			@Override
			public void onCompleted() {
				// 这里编写正常业务逻辑
				OrderResponse suveng = OrderResponse.newBuilder().setOrderId("order").build();
				responseObserver.onNext(suveng);
				responseObserver.onCompleted();
			}
		};
	}
}
