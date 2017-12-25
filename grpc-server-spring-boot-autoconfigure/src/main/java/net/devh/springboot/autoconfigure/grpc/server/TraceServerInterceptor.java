package net.devh.springboot.autoconfigure.grpc.server;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanExtractor;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.util.ExceptionUtils;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 2016/12/8
 */
public class TraceServerInterceptor implements ServerInterceptor {

    private Tracer tracer;

    private SpanExtractor<Metadata> spanExtractor;
    private static final String GRPC_COMPONENT = "gRPC";

    public TraceServerInterceptor(Tracer tracer, SpanExtractor<Metadata> spanExtractor) {
        this.tracer = tracer;
        this.spanExtractor = spanExtractor;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        final Span span = spanExtractor.joinTrace(headers);
        tracer.continueSpan(span);
        return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {

            private Span gRPCSpan;

            @Override
            public void request(int numMessages) {
                gRPCSpan = tracer.createSpan("gRPC:" + call.getMethodDescriptor().getFullMethodName());
                gRPCSpan.logEvent(Span.SERVER_RECV);
                gRPCSpan.tag(Span.SPAN_LOCAL_COMPONENT_TAG_NAME, GRPC_COMPONENT);
                super.request(numMessages);
            }

            @SuppressWarnings("ConstantConditions")
            @Override
            public void close(Status status, Metadata trailers) {
                gRPCSpan.logEvent(Span.SERVER_SEND);
                Status.Code statusCode = status.getCode();
                tracer.addTag("gRPC status code", String.valueOf(statusCode.value()));
                if (!status.isOk()) {
                    tracer.addTag(Span.SPAN_ERROR_TAG_NAME, ExceptionUtils.getExceptionMessage(status.getCause()));
                }
                tracer.close(gRPCSpan);
                super.close(status, trailers);
            }
        }, headers);
    }
}
