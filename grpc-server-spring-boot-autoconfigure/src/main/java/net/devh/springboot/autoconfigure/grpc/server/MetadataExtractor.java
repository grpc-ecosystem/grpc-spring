package net.devh.springboot.autoconfigure.grpc.server;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanExtractor;
import org.springframework.util.StringUtils;

import io.grpc.Metadata;
import lombok.extern.slf4j.Slf4j;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 2016/12/9
 */
@Slf4j
public class MetadataExtractor implements SpanExtractor<Metadata> {

    private static final String GRPC_COMPONENT = "gRPC";

    @Override
    public Span joinTrace(Metadata carrier) {
        if (getMetadata(carrier, Span.TRACE_ID_NAME) == null) {
            // can't build a Span without trace id
            return null;
        }
        boolean skip = Span.SPAN_NOT_SAMPLED.equals(getMetadata(carrier, Span.SAMPLED_NAME));
        long traceId = Span.hexToId(getMetadata(carrier, Span.TRACE_ID_NAME));
        long spanId = spanId(carrier, traceId);
        return buildParentSpan(carrier, skip, traceId, spanId);
    }

    private String getMetadata(Metadata metadata, String name) {
        return metadata.get(Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER));
    }


    private long spanId(Metadata carrier, long traceId) {
        String spanId = getMetadata(carrier, Span.SPAN_ID_NAME);
        if (spanId == null) {
            log.debug("Request is missing a span id but it has a trace id. We'll assume that this is a root span with span id equal to trace id");
            return traceId;
        } else {
            return Span.hexToId(spanId);
        }
    }

    private Span buildParentSpan(Metadata carrier, boolean skip, long traceId, long spanId) {
        Span.SpanBuilder span = Span.builder().traceId(traceId).spanId(spanId);
        String processId = getMetadata(carrier, Span.PROCESS_ID_NAME);
        String parentName = getMetadata(carrier, Span.SPAN_NAME_NAME);
        if (StringUtils.hasText(parentName)) {
            span.name(parentName);
        } else {
            span.name(GRPC_COMPONENT + ":/parent" + parentName);
        }
        if (StringUtils.hasText(processId)) {
            span.processId(processId);
        }
        if (getMetadata(carrier, Span.PARENT_ID_NAME) != null) {
            span.parent(Span.hexToId(getMetadata(carrier, Span.PARENT_ID_NAME)));
        }
        span.remote(true);
        if (skip) {
            span.exportable(false);
        }
        return span.build();
    }
}
