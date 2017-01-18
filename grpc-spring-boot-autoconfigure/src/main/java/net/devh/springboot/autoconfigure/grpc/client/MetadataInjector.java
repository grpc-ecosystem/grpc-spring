package net.devh.springboot.autoconfigure.grpc.client;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanInjector;

import io.grpc.Metadata;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
class MetadataInjector implements SpanInjector<Metadata> {

    @Override
    public void inject(Span span, Metadata carrier) {
        setIdMetadata(carrier, Span.TRACE_ID_NAME, span.getTraceId());
        setIdMetadata(carrier, Span.SPAN_ID_NAME, span.getSpanId());
        setMetadata(carrier, Span.SPAN_NAME_NAME, span.getName());
        setMetadata(carrier, Span.SAMPLED_NAME, span.isExportable() ? Span.SPAN_SAMPLED : Span.SPAN_NOT_SAMPLED);
        setMetadata(carrier, Span.PROCESS_ID_NAME, span.getProcessId() == null ? "null" : span.getProcessId());
        Long parentId = getParentId(span);
        if (parentId != null) {
            setIdMetadata(carrier, Span.PARENT_ID_NAME, parentId);
        }
    }

    private void setMetadata(Metadata metadata, String name, String value) {
        metadata.put(Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER), value);
    }

    private void setIdMetadata(Metadata metadata, String name, Long value) {
        if (value != null) {
            setMetadata(metadata, name, Span.idToHex(value));
        }
    }

    private Long getParentId(Span span) {
        return !span.getParents().isEmpty() ? span.getParents().get(0) : null;
    }
}
