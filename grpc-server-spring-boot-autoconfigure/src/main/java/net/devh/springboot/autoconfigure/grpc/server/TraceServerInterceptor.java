/*
package net.devh.springboot.autoconfigure.grpc.server;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanExtractor;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.util.ExceptionUtils;

import brave.Tracing;
import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

*/
/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 2016/12/8
 *//*

@Slf4j
public class TraceServerInterceptor implements ServerInterceptor {

    private Tracing tracing;

    private SpanExtractor<Metadata> spanExtractor;
    private static final String GRPC_COMPONENT = "gRPC";

    public TraceServerInterceptor(Tracing tracing, SpanExtractor<Metadata> spanExtractor) {
        this.tracing = tracing;
        this.spanExtractor = spanExtractor;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        final Span span = spanExtractor.joinTrace(headers);
        tracing.continueSpan(span);
        return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {

            private Span gRPCSpan;

            @Override
            public void request(int numMessages) {
                gRPCSpan = tracing.createSpan("gRPC:" + call.getMethodDescriptor().getFullMethodName());
                gRPCSpan.logEvent(Span.SERVER_RECV);
                gRPCSpan.tag(Span.SPAN_LOCAL_COMPONENT_TAG_NAME, GRPC_COMPONENT);
                super.request(numMessages);
            }

            @SuppressWarnings("ConstantConditions")
            @Override
            public void close(Status status, Metadata trailers) {
                gRPCSpan.logEvent(Span.SERVER_SEND);
                Status.Code statusCode = status.getCode();
                tracing.addTag("gRPC status code", String.valueOf(statusCode.value()));
                if (!status.isOk()) {
                    tracing.addTag(Span.SPAN_ERROR_TAG_NAME, ExceptionUtils.getExceptionMessage(status.getCause()));
                }
                tracing.close(gRPCSpan);
                super.close(status, trailers);
            }
        }, headers);
    }
}
*/
