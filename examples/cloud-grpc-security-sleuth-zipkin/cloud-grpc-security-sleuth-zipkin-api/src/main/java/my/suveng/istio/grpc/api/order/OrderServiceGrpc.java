package my.suveng.istio.grpc.api.order;

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
    comments = "Source: order-api.proto")
public final class OrderServiceGrpc {

  private OrderServiceGrpc() {}

  public static final String SERVICE_NAME = "OrderService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.order.OrderRequest,
      my.suveng.istio.grpc.api.order.OrderResponse> getGetRealNameByUsernameMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getRealNameByUsername",
      requestType = my.suveng.istio.grpc.api.order.OrderRequest.class,
      responseType = my.suveng.istio.grpc.api.order.OrderResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.order.OrderRequest,
      my.suveng.istio.grpc.api.order.OrderResponse> getGetRealNameByUsernameMethod() {
    io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.order.OrderRequest, my.suveng.istio.grpc.api.order.OrderResponse> getGetRealNameByUsernameMethod;
    if ((getGetRealNameByUsernameMethod = OrderServiceGrpc.getGetRealNameByUsernameMethod) == null) {
      synchronized (OrderServiceGrpc.class) {
        if ((getGetRealNameByUsernameMethod = OrderServiceGrpc.getGetRealNameByUsernameMethod) == null) {
          OrderServiceGrpc.getGetRealNameByUsernameMethod = getGetRealNameByUsernameMethod =
              io.grpc.MethodDescriptor.<my.suveng.istio.grpc.api.order.OrderRequest, my.suveng.istio.grpc.api.order.OrderResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getRealNameByUsername"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.order.OrderRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.order.OrderResponse.getDefaultInstance()))
              .setSchemaDescriptor(new OrderServiceMethodDescriptorSupplier("getRealNameByUsername"))
              .build();
        }
      }
    }
    return getGetRealNameByUsernameMethod;
  }

  private static volatile io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.order.OrderRequest,
      my.suveng.istio.grpc.api.order.OrderResponse> getGetRealNameByUsernameResStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getRealNameByUsernameResStream",
      requestType = my.suveng.istio.grpc.api.order.OrderRequest.class,
      responseType = my.suveng.istio.grpc.api.order.OrderResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.order.OrderRequest,
      my.suveng.istio.grpc.api.order.OrderResponse> getGetRealNameByUsernameResStreamMethod() {
    io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.order.OrderRequest, my.suveng.istio.grpc.api.order.OrderResponse> getGetRealNameByUsernameResStreamMethod;
    if ((getGetRealNameByUsernameResStreamMethod = OrderServiceGrpc.getGetRealNameByUsernameResStreamMethod) == null) {
      synchronized (OrderServiceGrpc.class) {
        if ((getGetRealNameByUsernameResStreamMethod = OrderServiceGrpc.getGetRealNameByUsernameResStreamMethod) == null) {
          OrderServiceGrpc.getGetRealNameByUsernameResStreamMethod = getGetRealNameByUsernameResStreamMethod =
              io.grpc.MethodDescriptor.<my.suveng.istio.grpc.api.order.OrderRequest, my.suveng.istio.grpc.api.order.OrderResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getRealNameByUsernameResStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.order.OrderRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.order.OrderResponse.getDefaultInstance()))
              .setSchemaDescriptor(new OrderServiceMethodDescriptorSupplier("getRealNameByUsernameResStream"))
              .build();
        }
      }
    }
    return getGetRealNameByUsernameResStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.order.OrderRequest,
      my.suveng.istio.grpc.api.order.OrderResponse> getGetRealNameByUsernameReqStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getRealNameByUsernameReqStream",
      requestType = my.suveng.istio.grpc.api.order.OrderRequest.class,
      responseType = my.suveng.istio.grpc.api.order.OrderResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.order.OrderRequest,
      my.suveng.istio.grpc.api.order.OrderResponse> getGetRealNameByUsernameReqStreamMethod() {
    io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.order.OrderRequest, my.suveng.istio.grpc.api.order.OrderResponse> getGetRealNameByUsernameReqStreamMethod;
    if ((getGetRealNameByUsernameReqStreamMethod = OrderServiceGrpc.getGetRealNameByUsernameReqStreamMethod) == null) {
      synchronized (OrderServiceGrpc.class) {
        if ((getGetRealNameByUsernameReqStreamMethod = OrderServiceGrpc.getGetRealNameByUsernameReqStreamMethod) == null) {
          OrderServiceGrpc.getGetRealNameByUsernameReqStreamMethod = getGetRealNameByUsernameReqStreamMethod =
              io.grpc.MethodDescriptor.<my.suveng.istio.grpc.api.order.OrderRequest, my.suveng.istio.grpc.api.order.OrderResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getRealNameByUsernameReqStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.order.OrderRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.order.OrderResponse.getDefaultInstance()))
              .setSchemaDescriptor(new OrderServiceMethodDescriptorSupplier("getRealNameByUsernameReqStream"))
              .build();
        }
      }
    }
    return getGetRealNameByUsernameReqStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.order.OrderRequest,
      my.suveng.istio.grpc.api.order.OrderResponse> getGetRealNameByUsernameStreamAllMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getRealNameByUsernameStreamAll",
      requestType = my.suveng.istio.grpc.api.order.OrderRequest.class,
      responseType = my.suveng.istio.grpc.api.order.OrderResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.order.OrderRequest,
      my.suveng.istio.grpc.api.order.OrderResponse> getGetRealNameByUsernameStreamAllMethod() {
    io.grpc.MethodDescriptor<my.suveng.istio.grpc.api.order.OrderRequest, my.suveng.istio.grpc.api.order.OrderResponse> getGetRealNameByUsernameStreamAllMethod;
    if ((getGetRealNameByUsernameStreamAllMethod = OrderServiceGrpc.getGetRealNameByUsernameStreamAllMethod) == null) {
      synchronized (OrderServiceGrpc.class) {
        if ((getGetRealNameByUsernameStreamAllMethod = OrderServiceGrpc.getGetRealNameByUsernameStreamAllMethod) == null) {
          OrderServiceGrpc.getGetRealNameByUsernameStreamAllMethod = getGetRealNameByUsernameStreamAllMethod =
              io.grpc.MethodDescriptor.<my.suveng.istio.grpc.api.order.OrderRequest, my.suveng.istio.grpc.api.order.OrderResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getRealNameByUsernameStreamAll"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.order.OrderRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  my.suveng.istio.grpc.api.order.OrderResponse.getDefaultInstance()))
              .setSchemaDescriptor(new OrderServiceMethodDescriptorSupplier("getRealNameByUsernameStreamAll"))
              .build();
        }
      }
    }
    return getGetRealNameByUsernameStreamAllMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static OrderServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OrderServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OrderServiceStub>() {
        @java.lang.Override
        public OrderServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OrderServiceStub(channel, callOptions);
        }
      };
    return OrderServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static OrderServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OrderServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OrderServiceBlockingStub>() {
        @java.lang.Override
        public OrderServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OrderServiceBlockingStub(channel, callOptions);
        }
      };
    return OrderServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static OrderServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OrderServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OrderServiceFutureStub>() {
        @java.lang.Override
        public OrderServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OrderServiceFutureStub(channel, callOptions);
        }
      };
    return OrderServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * rpc 方法定义, 4种方式
   * </pre>
   */
  public static abstract class OrderServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     *     1. 普通请求返回
     * </pre>
     */
    public void getRealNameByUsername(my.suveng.istio.grpc.api.order.OrderRequest request,
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetRealNameByUsernameMethod(), responseObserver);
    }

    /**
     * <pre>
     *      2. 流式请求,普通返回
     * </pre>
     */
    public void getRealNameByUsernameResStream(my.suveng.istio.grpc.api.order.OrderRequest request,
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetRealNameByUsernameResStreamMethod(), responseObserver);
    }

    /**
     * <pre>
     *      3. 普通请求,流式返回
     * </pre>
     */
    public io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderRequest> getRealNameByUsernameReqStream(
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getGetRealNameByUsernameReqStreamMethod(), responseObserver);
    }

    /**
     * <pre>
     *      3. 流式请求, 流式返回
     * </pre>
     */
    public io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderRequest> getRealNameByUsernameStreamAll(
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getGetRealNameByUsernameStreamAllMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetRealNameByUsernameMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                my.suveng.istio.grpc.api.order.OrderRequest,
                my.suveng.istio.grpc.api.order.OrderResponse>(
                  this, METHODID_GET_REAL_NAME_BY_USERNAME)))
          .addMethod(
            getGetRealNameByUsernameResStreamMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                my.suveng.istio.grpc.api.order.OrderRequest,
                my.suveng.istio.grpc.api.order.OrderResponse>(
                  this, METHODID_GET_REAL_NAME_BY_USERNAME_RES_STREAM)))
          .addMethod(
            getGetRealNameByUsernameReqStreamMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                my.suveng.istio.grpc.api.order.OrderRequest,
                my.suveng.istio.grpc.api.order.OrderResponse>(
                  this, METHODID_GET_REAL_NAME_BY_USERNAME_REQ_STREAM)))
          .addMethod(
            getGetRealNameByUsernameStreamAllMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                my.suveng.istio.grpc.api.order.OrderRequest,
                my.suveng.istio.grpc.api.order.OrderResponse>(
                  this, METHODID_GET_REAL_NAME_BY_USERNAME_STREAM_ALL)))
          .build();
    }
  }

  /**
   * <pre>
   * rpc 方法定义, 4种方式
   * </pre>
   */
  public static final class OrderServiceStub extends io.grpc.stub.AbstractAsyncStub<OrderServiceStub> {
    private OrderServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OrderServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OrderServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     *     1. 普通请求返回
     * </pre>
     */
    public void getRealNameByUsername(my.suveng.istio.grpc.api.order.OrderRequest request,
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetRealNameByUsernameMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *      2. 流式请求,普通返回
     * </pre>
     */
    public void getRealNameByUsernameResStream(my.suveng.istio.grpc.api.order.OrderRequest request,
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderResponse> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetRealNameByUsernameResStreamMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *      3. 普通请求,流式返回
     * </pre>
     */
    public io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderRequest> getRealNameByUsernameReqStream(
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderResponse> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getGetRealNameByUsernameReqStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     *      3. 流式请求, 流式返回
     * </pre>
     */
    public io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderRequest> getRealNameByUsernameStreamAll(
        io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getGetRealNameByUsernameStreamAllMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   * rpc 方法定义, 4种方式
   * </pre>
   */
  public static final class OrderServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<OrderServiceBlockingStub> {
    private OrderServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OrderServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OrderServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     *     1. 普通请求返回
     * </pre>
     */
    public my.suveng.istio.grpc.api.order.OrderResponse getRealNameByUsername(my.suveng.istio.grpc.api.order.OrderRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetRealNameByUsernameMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *      2. 流式请求,普通返回
     * </pre>
     */
    public java.util.Iterator<my.suveng.istio.grpc.api.order.OrderResponse> getRealNameByUsernameResStream(
        my.suveng.istio.grpc.api.order.OrderRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getGetRealNameByUsernameResStreamMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * rpc 方法定义, 4种方式
   * </pre>
   */
  public static final class OrderServiceFutureStub extends io.grpc.stub.AbstractFutureStub<OrderServiceFutureStub> {
    private OrderServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OrderServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OrderServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     *     1. 普通请求返回
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<my.suveng.istio.grpc.api.order.OrderResponse> getRealNameByUsername(
        my.suveng.istio.grpc.api.order.OrderRequest request) {
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
    private final OrderServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(OrderServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_REAL_NAME_BY_USERNAME:
          serviceImpl.getRealNameByUsername((my.suveng.istio.grpc.api.order.OrderRequest) request,
              (io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderResponse>) responseObserver);
          break;
        case METHODID_GET_REAL_NAME_BY_USERNAME_RES_STREAM:
          serviceImpl.getRealNameByUsernameResStream((my.suveng.istio.grpc.api.order.OrderRequest) request,
              (io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderResponse>) responseObserver);
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
              (io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderResponse>) responseObserver);
        case METHODID_GET_REAL_NAME_BY_USERNAME_STREAM_ALL:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.getRealNameByUsernameStreamAll(
              (io.grpc.stub.StreamObserver<my.suveng.istio.grpc.api.order.OrderResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class OrderServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    OrderServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return my.suveng.istio.grpc.api.order.Order.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("OrderService");
    }
  }

  private static final class OrderServiceFileDescriptorSupplier
      extends OrderServiceBaseDescriptorSupplier {
    OrderServiceFileDescriptorSupplier() {}
  }

  private static final class OrderServiceMethodDescriptorSupplier
      extends OrderServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    OrderServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (OrderServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new OrderServiceFileDescriptorSupplier())
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
