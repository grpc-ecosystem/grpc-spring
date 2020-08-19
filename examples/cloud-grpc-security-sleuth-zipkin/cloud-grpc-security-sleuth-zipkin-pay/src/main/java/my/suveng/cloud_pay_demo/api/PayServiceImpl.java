package my.suveng.cloud_pay_demo.api;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import my.suveng.istio.grpc.api.pay.PayRequest;
import my.suveng.istio.grpc.api.pay.PayResponse;
import my.suveng.istio.grpc.api.pay.PayServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.access.annotation.Secured;

/**
 *
 * @author suwenguang
 **/
@GrpcService
@Slf4j
public class PayServiceImpl extends PayServiceGrpc.PayServiceImplBase {

	//@Secured("ROLE_admin") // have right to access
	@Secured("ROLE_uuu") // no right to access
	@Override
	public void getRealNameByUsername(PayRequest request, StreamObserver<PayResponse> responseObserver) {
		log.info("pay 接受到参数: " + request + "");
		PayResponse response = PayResponse.newBuilder().setSuccess(true).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	//@Secured("ROLE_GG")
	@Override
	public void getRealNameByUsernameResStream(PayRequest request, StreamObserver<PayResponse> responseObserver) {
		log.info("pay 接受到参数: " + request + "");
		PayResponse response = PayResponse.newBuilder().setSuccess(false).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public StreamObserver<PayRequest> getRealNameByUsernameReqStream(StreamObserver<PayResponse> responseObserver) {
		// 流式,使用的是异步回调,事件驱动回调
		return new StreamObserver<PayRequest>() {
			// 查看父类说明
			@Override
			public void onNext(PayRequest myRequest) {
				// 在这里获取参数
				log.info("pay onNext: " + myRequest.getMoney());
			}

			@Override
			public void onError(Throwable throwable) {
				// 当在onNext 或者 onCompleted 发生异常时, 会被回调
				throwable.printStackTrace();
			}

			@Override
			public void onCompleted() {
				// 这里编写正常业务逻辑
				PayResponse suveng = PayResponse.newBuilder().setSuccess(true).build();
				responseObserver.onNext(suveng);
				responseObserver.onCompleted();
			}
		};
	}

	@Override
	public StreamObserver<PayRequest> getRealNameByUsernameStreamAll(StreamObserver<PayResponse> responseObserver) {
		// 流式,使用的是异步回调,事件驱动回调
		return new StreamObserver<PayRequest>() {
			// 查看父类说明
			@Override
			public void onNext(PayRequest myRequest) {
				// 在这里获取参数
				log.info("pay onNext: " + myRequest.getMoney());
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
				PayResponse suveng = PayResponse.newBuilder().setSuccess(true).build();
				responseObserver.onNext(suveng);
				responseObserver.onCompleted();
			}
		};
	}
}
