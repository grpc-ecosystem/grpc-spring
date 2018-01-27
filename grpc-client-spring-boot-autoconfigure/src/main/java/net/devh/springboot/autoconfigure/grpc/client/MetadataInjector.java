package net.devh.springboot.autoconfigure.grpc.client;

import io.grpc.Metadata;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanInjector;

import java.util.Map;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public class MetadataInjector implements SpanInjector<Metadata> {

    private static final String HEADER_DELIMITER = "-";

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
        for (Map.Entry<String, String> entry : span.baggageItems()) {
            setMetadata(carrier, prefixedKey(entry.getKey()), entry.getValue());
        }
    }


    private String prefixedKey(String key) {
        if (key.startsWith(Span.SPAN_BAGGAGE_HEADER_PREFIX + HEADER_DELIMITER)) {
            return key;
        }
        return Span.SPAN_BAGGAGE_HEADER_PREFIX + HEADER_DELIMITER + key;
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
