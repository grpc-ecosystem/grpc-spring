/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.devh.springboot.autoconfigure.grpc.client;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanInjector;
import org.springframework.http.HttpRequest;

import io.grpc.Metadata;

/**
 * Span injector that injects tracing info to {@link HttpRequest}
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
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
