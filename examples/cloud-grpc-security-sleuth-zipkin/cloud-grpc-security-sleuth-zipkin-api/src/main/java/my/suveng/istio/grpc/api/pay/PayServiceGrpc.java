package my.suveng.istio.grpc.api.pay;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * rpc 方法定义, 4种方式
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.29.0)",
    comments = "Source: pay-api.proto")
public final class PayServiceGrpc {

  private PayServiceGrpc() {}

  public static final String SERVICE_NAME = "PayService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.pay.PayRequest,
      my.suveng.istio.grpc.api.pay.PayResponse> getGetRealNameByUsernameMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getRealNameByUsername",
      requestType = my.suveng.istio.grpc.api.pay.PayRequest.class,
      responseType = my.suveng.istio.grpc.api.pay.PayResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.pay.PayRequest,
      my.suveng.istio.grpc.api.pay.PayResponse> getGetRealNameByUsernameMethod() {
    io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.pay.PayRequest, my.suveng.istio.grpc.api.pay.PayResponse> getGetRealNameByUsernameMethod;
    if ((getGetRealNameByUsernameMethod = PayServiceGrpc.getGetRealNameByUsernameMethod) == null) {
      synchronized (PayServiceGrpc.class) {
        if ((getGetRealNameByUsernameMethod = PayServiceGrpc.getGetRealNameByUsernameMethod) == null) {
          PayServiceGrpc.getGetRealNameByUsernameMethod = getGetRealNameByUsernameMethod =
              io.grpc.MethodDescriptor.<my.suveng.istio.grpc.api.pay.PayRequest, my.suveng.istio.grpc.api.pay.PayResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getRealNameByUsername"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.pay.PayRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.pay.PayResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PayServiceMethodDescriptorSupplier("getRealNameByUsername"))
              .build();
        }
      }
    }
    return getGetRealNameByUsernameMethod;
  }

  private static volatile io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.pay.PayRequest,
      my.suveng.istio.grpc.api.pay.PayResponse> getGetRealNameByUsernameResStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getRealNameByUsernameResStream",
      requestType = my.suveng.istio.grpc.api.pay.PayRequest.class,
      responseType = my.suveng.istio.grpc.api.pay.PayResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.pay.PayRequest,
      my.suveng.istio.grpc.api.pay.PayResponse> getGetRealNameByUsernameResStreamMethod() {
    io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.pay.PayRequest, my.suveng.istio.grpc.api.pay.PayResponse> getGetRealNameByUsernameResStreamMethod;
    if ((getGetRealNameByUsernameResStreamMethod = PayServiceGrpc.getGetRealNameByUsernameResStreamMethod) == null) {
      synchronized (PayServiceGrpc.class) {
        if ((getGetRealNameByUsernameResStreamMethod = PayServiceGrpc.getGetRealNameByUsernameResStreamMethod) == null) {
          PayServiceGrpc.getGetRealNameByUsernameResStreamMethod = getGetRealNameByUsernameResStreamMethod =
              io.grpc.MethodDescriptor.<my.suveng.istio.grpc.api.pay.PayRequest, my.suveng.istio.grpc.api.pay.PayResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getRealNameByUsernameResStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.pay.PayRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.pay.PayResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PayServiceMethodDescriptorSupplier("getRealNameByUsernameResStream"))
              .build();
        }
      }
    }
    return getGetRealNameByUsernameResStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.pay.PayRequest,
      my.suveng.istio.grpc.api.pay.PayResponse> getGetRealNameByUsernameReqStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getRealNameByUsernameReqStream",
      requestType = my.suveng.istio.grpc.api.pay.PayRequest.class,
      responseType = my.suveng.istio.grpc.api.pay.PayResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.pay.PayRequest,
      my.suveng.istio.grpc.api.pay.PayResponse> getGetRealNameByUsernameReqStreamMethod() {
    io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.pay.PayRequest, my.suveng.istio.grpc.api.pay.PayResponse> getGetRealNameByUsernameReqStreamMethod;
    if ((getGetRealNameByUsernameReqStreamMethod = PayServiceGrpc.getGetRealNameByUsernameReqStreamMethod) == null) {
      synchronized (PayServiceGrpc.class) {
        if ((getGetRealNameByUsernameReqStreamMethod = PayServiceGrpc.getGetRealNameByUsernameReqStreamMethod) == null) {
          PayServiceGrpc.getGetRealNameByUsernameReqStreamMethod = getGetRealNameByUsernameReqStreamMethod =
              io.grpc.MethodDescriptor.<my.suveng.istio.grpc.api.pay.PayRequest, my.suveng.istio.grpc.api.pay.PayResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getRealNameByUsernameReqStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.pay.PayRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.pay.PayResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PayServiceMethodDescriptorSupplier("getRealNameByUsernameReqStream"))
              .build();
        }
      }
    }
    return getGetRealNameByUsernameReqStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.pay.PayRequest,
      my.suveng.istio.grpc.api.pay.PayResponse> getGetRealNameByUsernameStreamAllMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getRealNameByUsernameStreamAll",
      requestType = my.suveng.istio.grpc.api.pay.PayRequest.class,
      responseType = my.suveng.istio.grpc.api.pay.PayResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.pay.PayRequest,
      my.suveng.istio.grpc.api.pay.PayResponse> getGetRealNameByUsernameStreamAllMethod() {
    io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.pay.PayRequest, my.suveng.istio.grpc.api.pay.PayResponse> getGetRealNameByUsernameStreamAllMethod;
    if ((getGetRealNameByUsernameStreamAllMethod = PayServiceGrpc.getGetRealNameByUsernameStreamAllMethod) == null) {
      synchronized (PayServiceGrpc.class) {
        if ((getGetRealNameByUsernameStreamAllMethod = PayServiceGrpc.getGetRealNameByUsernameStreamAllMethod) == null) {
          PayServiceGrpc.getGetRealNameByUsernameStreamAllMethod = getGetRealNameByUsernameStreamAllMethod =
              io.grpc.MethodDescriptor.<my.suveng.istio.grpc.api.pay.PayRequest, my.suveng.istio.grpc.api.pay.PayResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getRealNameByUsernameStreamAll"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.pay.PayRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.pay.PayResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PayServiceMethodDescriptorSupplier("getRealNameByUsernameStreamAll"))
              .build();
        }
      }
    }
    return getGetRealNameByUsernameStreamAllMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PayServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PayServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PayServiceStub>() {
        @java.lang.Override
        public PayServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PayServiceStub(channel, callOptions);
        }
      };
    return PayServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PayServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PayServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PayServiceBlockingStub>() {
        @java.lang.Override
        public PayServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PayServiceBlockingStub(channel, callOptions);
        }
      };
    return PayServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PayServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PayServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PayServiceFutureStub>() {
        @java.lang.Override
        public PayServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PayServiceFutureStub(channel, callOptions);
        }
      };
    return PayServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * rpc 方法定义, 4种方式
   * </pre>
   */
  public static abstract class PayServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     *     1. 普通请求返回
     * </pre>
     */
    public void getRealNameByUsername(my.suveng.istio.grpc.api.pay.PayRequest request,
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetRealNameByUsernameMethod(), responseObserver);
    }

    /**
     * <pre>
     *      2. 流式请求,普通返回
     * </pre>
     */
    public void getRealNameByUsernameResStream(my.suveng.istio.grpc.api.pay.PayRequest request,
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetRealNameByUsernameResStreamMethod(), responseObserver);
    }

    /**
     * <pre>
     *      3. 普通请求,流式返回
     * </pre>
     */
    public io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayRequest> getRealNameByUsernameReqStream(
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getGetRealNameByUsernameReqStreamMethod(), responseObserver);
    }

    /**
     * <pre>
     *      3. 流式请求, 流式返回
     * </pre>
     */
    public io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayRequest> getRealNameByUsernameStreamAll(
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getGetRealNameByUsernameStreamAllMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetRealNameByUsernameMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                my.suveng.istio.grpc.api.pay.PayRequest,
                my.suveng.istio.grpc.api.pay.PayResponse>(
                  this, METHODID_GET_REAL_NAME_BY_USERNAME)))
          .addMethod(
            getGetRealNameByUsernameResStreamMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                my.suveng.istio.grpc.api.pay.PayRequest,
                my.suveng.istio.grpc.api.pay.PayResponse>(
                  this, METHODID_GET_REAL_NAME_BY_USERNAME_RES_STREAM)))
          .addMethod(
            getGetRealNameByUsernameReqStreamMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                my.suveng.istio.grpc.api.pay.PayRequest,
                my.suveng.istio.grpc.api.pay.PayResponse>(
                  this, METHODID_GET_REAL_NAME_BY_USERNAME_REQ_STREAM)))
          .addMethod(
            getGetRealNameByUsernameStreamAllMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                my.suveng.istio.grpc.api.pay.PayRequest,
                my.suveng.istio.grpc.api.pay.PayResponse>(
                  this, METHODID_GET_REAL_NAME_BY_USERNAME_STREAM_ALL)))
          .build();
    }
  }

  /**
   * <pre>
   * rpc 方法定义, 4种方式
   * </pre>
   */
  public static final class PayServiceStub extends io.grpc.stub.AbstractAsyncStub<PayServiceStub> {
    private PayServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PayServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PayServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     *     1. 普通请求返回
     * </pre>
     */
    public void getRealNameByUsername(my.suveng.istio.grpc.api.pay.PayRequest request,
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetRealNameByUsernameMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *      2. 流式请求,普通返回
     * </pre>
     */
    public void getRealNameByUsernameResStream(my.suveng.istio.grpc.api.pay.PayRequest request,
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayResponse> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetRealNameByUsernameResStreamMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *      3. 普通请求,流式返回
     * </pre>
     */
    public io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayRequest> getRealNameByUsernameReqStream(
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayResponse> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getGetRealNameByUsernameReqStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     *      3. 流式请求, 流式返回
     * </pre>
     */
    public io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayRequest> getRealNameByUsernameStreamAll(
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getGetRealNameByUsernameStreamAllMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   * rpc 方法定义, 4种方式
   * </pre>
   */
  public static final class PayServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<PayServiceBlockingStub> {
    private PayServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PayServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PayServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     *     1. 普通请求返回
     * </pre>
     */
    public my.suveng.istio.grpc.api.pay.PayResponse getRealNameByUsername(my.suveng.istio.grpc.api.pay.PayRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetRealNameByUsernameMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *      2. 流式请求,普通返回
     * </pre>
     */
    public java.util.Iterator<my.suveng.istio.grpc.api.pay.PayResponse> getRealNameByUsernameResStream(
        my.suveng.istio.grpc.api.pay.PayRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getGetRealNameByUsernameResStreamMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * rpc 方法定义, 4种方式
   * </pre>
   */
  public static final class PayServiceFutureStub extends io.grpc.stub.AbstractFutureStub<PayServiceFutureStub> {
    private PayServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PayServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PayServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     *     1. 普通请求返回
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<my.suveng.istio.grpc.api.pay.PayResponse> getRealNameByUsername(
        my.suveng.istio.grpc.api.pay.PayRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetRealNameByUsernameMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_REAL_NAME_BY_USERNAME = 0;
  private static final int METHODID_GET_REAL_NAME_BY_USERNAME_RES_STREAM = 1;
  private static final int METHODID_GET_REAL_NAME_BY_USERNAME_REQ_STREAM = 2;
  private static final int METHODID_GET_REAL_NAME_BY_USERNAME_STREAM_ALL = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final PayServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(PayServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_REAL_NAME_BY_USERNAME:
          serviceImpl.getRealNameByUsername((my.suveng.istio.grpc.api.pay.PayRequest) request,
              (io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayResponse>) responseObserver);
          break;
        case METHODID_GET_REAL_NAME_BY_USERNAME_RES_STREAM:
          serviceImpl.getRealNameByUsernameResStream((my.suveng.istio.grpc.api.pay.PayRequest) request,
              (io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_REAL_NAME_BY_USERNAME_REQ_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.getRealNameByUsernameReqStream(
              (io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayResponse>) responseObserver);
        case METHODID_GET_REAL_NAME_BY_USERNAME_STREAM_ALL:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.getRealNameByUsernameStreamAll(
              (io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.pay.PayResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class PayServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PayServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return my.suveng.istio.grpc.api.pay.Pay.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("PayService");
    }
  }

  private static final class PayServiceFileDescriptorSupplier
      extends PayServiceBaseDescriptorSupplier {
    PayServiceFileDescriptorSupplier() {}
  }

  private static final class PayServiceMethodDescriptorSupplier
      extends PayServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    PayServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (PayServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PayServiceFileDescriptorSupplier())
              .addMethod(getGetRealNameByUsernameMethod())
              .addMethod(getGetRealNameByUsernameResStreamMethod())
              .addMethod(getGetRealNameByUsernameReqStreamMethod())
              .addMethod(getGetRealNameByUsernameStreamAllMethod())
              .build();
        }
      }
    }
    return result;
  }
}
