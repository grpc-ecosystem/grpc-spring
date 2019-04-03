/*
 * Copyright (c) 2016-2020 Michael Zhang <yidongnan@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.devh.boot.grpc.web.conversion;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.google.protobuf.MessageLite;
import com.google.protobuf.NullValue;
import com.google.protobuf.Value;

/**
 * Converter that transforms the map like data structures to protobuf instances and vice versa.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class PojoFormat {

    private final Map<Descriptor, Map<String, FieldDescriptor>> fieldNameMaps = new HashMap<>();
    private final Map<Descriptor, Supplier<? extends Builder>> builders = new HashMap<>();
    private final Map<Descriptor, ProtoDataMerger> mergers = new HashMap<>();
    private final ProtoDataMerger defaultMerger = (descriptor, input, builder) -> {
        if (input instanceof Map) {
            mergeFromMap(descriptor, (Map<?, ?>) input, builder);
        } else if (input instanceof Message) {
            builder.mergeFrom((Message) input);
        } else if (input instanceof MessageLite) {
            builder.mergeFrom((MessageLite) input);
        } else {
            throw new InvalidProtocolBufferException("Cannot convert '" + input + "' to a " + descriptor.getFullName());
        }
    };

    private final boolean ignoreUnknown;
    private final boolean strictTypeMatch;

    public PojoFormat(final boolean ignoreUnknown, final boolean strictTypeMatch) {
        this.ignoreUnknown = ignoreUnknown;
        this.strictTypeMatch = strictTypeMatch;
    }

    public void register(final Descriptor descriptor, final Supplier<? extends Builder> builder) {
        this.builders.putIfAbsent(descriptor, builder);
    }

    /**
     * Creates a new builder for the given descriptor.
     *
     * @param <B> The type of the builder.
     * @param descriptor The descriptor to the get a builder for.
     * @return A newly created builder instance for the given descriptor.
     */
    @SuppressWarnings("unchecked")
    protected <B extends Builder> B newBuilderFor(final Descriptor descriptor) {
        return (B) this.builders.get(descriptor).get();
    }

    /**
     * Gets the merger that should be used for the given descriptor.
     *
     * @param descriptor The descriptor to get the merger for.
     * @return The map merger that should be used.
     */
    protected ProtoDataMerger getMergerFor(final Descriptor descriptor) {
        return this.mergers.getOrDefault(descriptor, this.defaultMerger);
    }

    /**
     * Creates a new builder for the given descriptor and populate it with the values from the given input object.
     *
     * @param <B> The type of the builder.
     * @param descriptor The descriptor to the get a builder for.
     * @param input The input object used to populate the builder. This should usually be a map.
     * @return The populated builder instance for the given descriptor.
     * @throws InvalidProtocolBufferException If something went wrong during the population.
     */
    public <B extends Builder> B toBuilder(final Descriptor descriptor, final Object input)
            throws InvalidProtocolBufferException {
        final B builder = newBuilderFor(descriptor);
        merge(descriptor, input, builder);
        return builder;
    }

    /**
     * Creates a new message for the given descriptor that is populated with the values from the given input object.
     *
     * @param <T> The type of the message.
     * @param descriptor The descriptor to the get a message for.
     * @param input The input object used to populate the message. This should usually be a map.
     * @return The populated message instance for the given descriptor.
     * @throws InvalidProtocolBufferException If something went wrong during the population.
     */
    @SuppressWarnings("unchecked")
    public <T extends Message> T toMessage(final Descriptor descriptor, final Object input)
            throws InvalidProtocolBufferException {
        return (T) toBuilder(descriptor, input).build();
    }

    /**
     * Creates new messages for the given inputs.
     *
     * @param <T> The type of the messages.
     * @param descriptor The descriptor to the get the messages for.
     * @param inputs The input object used to populate the messages. This should usually be maps.
     * @return The populated message instances for the given descriptor.
     * @throws InvalidProtocolBufferException If something went wrong during the population.
     */
    @SuppressWarnings("unchecked")
    public <T extends Message> List<T> toManyMessages(final Descriptor descriptor, final Iterable<?> inputs)
            throws InvalidProtocolBufferException {
        final List<T> messages = new ArrayList<>();
        final Builder builder = newBuilderFor(descriptor);
        final ProtoDataMerger merger = getMergerFor(descriptor);
        for (final Object input : inputs) {
            merger.merge(descriptor, input, builder);
            messages.add((T) builder.build());
            builder.clear();
        }
        return messages;
    }

    /**
     * Creates new messages for the given input. If the input is a collection or array then this method will create a
     * message for each element. Otherwise it will try convert the input directly to the described message type.
     *
     * @param <T> The type of the messages.
     * @param descriptor The descriptor to the get the messages for.
     * @param input A single instance or a collection/array of instances that should be used to populate messages.
     * @return The populated message instances for the given descriptor.
     * @throws InvalidProtocolBufferException If something went wrong during the population.
     */
    public <T extends Message> List<T> toManyMessages(final Descriptor descriptor, final Object input)
            throws InvalidProtocolBufferException {
        if (input instanceof Iterable) {
            return toManyMessages(descriptor, (Iterable<?>) input);
        } else if (input instanceof Object[]) {
            return toManyMessages(descriptor, Arrays.asList((Object[]) input));
        } else {
            return toManyMessages(descriptor, Arrays.asList(input));
        }
    }

    /**
     * Merge the given input into the given builder.
     *
     * @param input he input object used to populate the builder. This should usually be a map.
     * @param builder The builder that should be populated.
     *
     * @throws InvalidProtocolBufferException If something went wrong during the population.
     * @see #toBuilder(Descriptor, Object)
     */
    public void merge(final Object input, final Builder builder)
            throws InvalidProtocolBufferException {
        merge(builder.getDescriptorForType(), input, builder);
    }

    /**
     * Merge the given input into the given builder.
     *
     * @param descriptor The data descriptor belonging to the builder.
     * @param input he input object used to populate the builder. This should usually be a map.
     * @param builder The builder that should be populated.
     * @throws InvalidProtocolBufferException If something went wrong during the population.
     * @see #toBuilder(Descriptor, Object)
     */
    public void merge(final Descriptor descriptor, final Object input, final Builder builder)
            throws InvalidProtocolBufferException {
        getMergerFor(descriptor).merge(descriptor, input, builder);
    }

    /**
     * Merges the data from given map into the given builder.
     *
     * @param descriptor The data descriptor belonging to the builder.
     * @param inputMap The input data used to populate the builder.
     * @param builder The builder that should be populated.
     * @throws InvalidProtocolBufferException If something went wrong during the population.
     */
    public void mergeFromMap(final Descriptor descriptor, final Map<?, ?> inputMap, final Builder builder)
            throws InvalidProtocolBufferException {
        final Map<String, FieldDescriptor> fieldNameMap = getFieldNameMap(descriptor);
        for (final Entry<?, ?> entry : inputMap.entrySet()) {
            final String key = requireNonNull(entry.getKey(), "map.key").toString();
            final FieldDescriptor field = fieldNameMap.get(key);
            if (field == null) {
                if (!this.ignoreUnknown) {
                    throw new IllegalArgumentException("Unknown field: " + key);
                }
            } else {
                mergeField(field, entry.getValue(), builder);
            }
        }
    }

    /**
     * Gets the map with all field names, their aliases and their associated field descriptors.
     *
     * @param descriptor The descriptor to resolve the fields for.
     * @return A cached map that contains all field names and their field descriptors.
     */
    private Map<String, FieldDescriptor> getFieldNameMap(final Descriptor descriptor) {
        return this.fieldNameMaps.computeIfAbsent(descriptor, this::createFieldNameMap);
    }

    /**
     * Creates a map with all field names, their aliases and their associated field descriptors.
     *
     * @param descriptor The descriptor to resolve the fields for.
     * @return A newly created map that contains all field names and their field descriptors.
     * @see #getFieldNameMap(Descriptor)
     */
    private Map<String, FieldDescriptor> createFieldNameMap(final Descriptor descriptor) {
        final Map<String, FieldDescriptor> fieldNameMap = new HashMap<>();
        for (final FieldDescriptor field : descriptor.getFields()) {
            fieldNameMap.put(field.getName(), field);
            fieldNameMap.put(field.getJsonName(), field);
        }
        return fieldNameMap;
    }

    private void mergeField(final FieldDescriptor field, final Object input, final Builder builder)
            throws InvalidProtocolBufferException {
        if (field.isMapField()) {
            // TODO mergeMapField(field, input, builder);
            throw new UnsupportedOperationException("Not yet implemented");
        } else if (field.isRepeated()) {
            // TODO mergeRepeatedField(field, input, builder);
            throw new UnsupportedOperationException("Not yet implemented");
        } else {
            final Object value = parseFieldValue(field, input, builder);
            if (value != null) {
                builder.setField(field, value);
            }
        }
    }

    private static final String VALUE_FULL_NAME = Value.getDescriptor().getFullName();

    private Object parseFieldValue(final FieldDescriptor field, final Object input, final Message.Builder builder)
            throws InvalidProtocolBufferException {
        if (input == null) {
            if (field.getJavaType() == FieldDescriptor.JavaType.MESSAGE
                    && field.getMessageType().getFullName().equals(VALUE_FULL_NAME)) {
                // For every other type, "null" means absence, but for the special
                // Value message, it means the "null_value" field has been set.
                final Value protoValue = Value.newBuilder().setNullValueValue(0).build();
                return builder.newBuilderForField(field).mergeFrom(protoValue.toByteString()).build();
            } else if (field.getJavaType() == FieldDescriptor.JavaType.ENUM
                    && field.getEnumType().getFullName().equals(NullValue.getDescriptor().getFullName())) {
                // If the type of the field is a NullValue, then the value should be explicitly set.
                return field.getEnumType().findValueByNumber(0);
            }
            return null;
        }
        switch (field.getType()) {
            case INT32:
            case SINT32:
            case SFIXED32:
                return parseInt32(input);

            case INT64:
            case SINT64:
            case SFIXED64:
                return parseInt64(input);

            case BOOL:
                return parseBool(input);

            case FLOAT:
                return parseFloat(input);

            case DOUBLE:
                return parseDouble(input);

            case UINT32:
            case FIXED32:
                // TODO return parseUint32(input);
                return parseInt32(input);

            case UINT64:
            case FIXED64:
                // TODO return parseUint64(input);
                return parseInt64(input);

            case STRING:
                return parseString(input);

            case BYTES:
                return ByteString.copyFrom(parseBytes(input));

            case ENUM:
                return parseEnum(field.getEnumType(), input);

            case MESSAGE:
            case GROUP:
                // TODO infinite depth prevention
                final Message.Builder subBuilder = builder.newBuilderForField(field);
                merge(input, subBuilder);
                return subBuilder.build();

            default:
                throw new InvalidProtocolBufferException("Invalid field type: " + field.getType());
        }
    }

    private boolean parseBool(final Object input) throws InvalidProtocolBufferException {
        if (input instanceof Boolean) {
            return (boolean) input;
        }
        if (!this.strictTypeMatch) {
            final String string = input.toString();
            if ("true".equals(string)) {
                return true;
            } else if ("false".equals(string)) {
                return false;
            }
        }
        throw new InvalidProtocolBufferException("Not a bool value: " + input);
    }

    private int parseInt32(final Object input) throws InvalidProtocolBufferException {
        if (input instanceof Integer) {
            return (int) input;
        }
        if (!this.strictTypeMatch) {
            final String string = input.toString();
            try {
                return Integer.parseInt(string);
            } catch (final NumberFormatException e) {
                // Fallthrough
            }
        }
        throw new InvalidProtocolBufferException("Not an int32 value: " + input);
    }

    private long parseInt64(final Object input) throws InvalidProtocolBufferException {
        if (input instanceof Long) {
            return (long) input;
        }
        if (!this.strictTypeMatch) {
            final String string = input.toString();
            try {
                return Long.parseLong(string);
            } catch (final NumberFormatException e) {
                // Fallthrough
            }
        }
        throw new InvalidProtocolBufferException("Not an int64 value: " + input);
    }

    private float parseFloat(final Object input) throws InvalidProtocolBufferException {
        if (input instanceof Float) {
            return (float) input;
        }
        if (!this.strictTypeMatch) {
            final String string = input.toString();
            try {
                return Float.parseFloat(string);
            } catch (final NumberFormatException e) {
                // Fallthrough
            }
        }
        throw new InvalidProtocolBufferException("Not a float value: " + input);
    }

    private double parseDouble(final Object input) throws InvalidProtocolBufferException {
        if (input instanceof Double) {
            return (double) input;
        }
        if (!this.strictTypeMatch) {
            final String string = input.toString();
            try {
                return Double.parseDouble(string);
            } catch (final NumberFormatException e) {
                // Fallthrough
            }
        }
        throw new InvalidProtocolBufferException("Not a double value: " + input);
    }

    private String parseString(final Object input) throws InvalidProtocolBufferException {
        if (input instanceof String) {
            return (String) input;
        }
        if (!this.strictTypeMatch) {
            return input.toString();
        }
        throw new InvalidProtocolBufferException("Not a string value: " + input);
    }

    @SuppressWarnings("squid:S1166")
    private byte[] parseBytes(final Object input) throws InvalidProtocolBufferException {
        if (input instanceof byte[]) {
            return (byte[]) input;
        }
        if (!this.strictTypeMatch) {
            final String string = input.toString();
            try {
                return Base64.getDecoder().decode(string);
            } catch (final IllegalArgumentException e) {
                return Base64.getUrlDecoder().decode(string);
            }
        }
        throw new InvalidProtocolBufferException("Not a byte value: " + input);
    }

    @SuppressWarnings("squid:S1166")
    private EnumValueDescriptor parseEnum(final EnumDescriptor enumDescriptor, final Object input)
            throws InvalidProtocolBufferException {
        if (input instanceof String) {
            final String value = (String) input;
            final EnumValueDescriptor result = enumDescriptor.findValueByName(value);
            if (result != null) {
                return result;
            }
        } else if (input instanceof Integer) {
            final int numericValue = (int) input;
            if (enumDescriptor.getFile().getSyntax() == FileDescriptor.Syntax.PROTO3) {
                return enumDescriptor.findValueByNumberCreatingIfUnknown(numericValue);
            } else {
                return enumDescriptor.findValueByNumber(numericValue);
            }
        }
        if (!this.strictTypeMatch) {
            final String value = input.toString();
            final EnumValueDescriptor result = enumDescriptor.findValueByName(value);
            if (result != null) {
                return result;
            }
            try {
                final int numericValue = parseInt32(input);
                if (enumDescriptor.getFile().getSyntax() == FileDescriptor.Syntax.PROTO3) {
                    return enumDescriptor.findValueByNumberCreatingIfUnknown(numericValue);
                } else {
                    return enumDescriptor.findValueByNumber(numericValue);
                }
            } catch (final Exception e) {
                // Fallthrough
            }
        }
        throw new InvalidProtocolBufferException(
                "Invalid enum value: " + input + " for enum type: " + enumDescriptor.getFullName());
    }

    // Proto->Map

    public Object convert(final Collection<? extends Message> messages) {
        final int count = messages.size();
        if (count == 1) {
            return convert(messages.iterator().next());
        }
        final List<Object> converted = new ArrayList<>(count);
        for (final Message message : messages) {
            converted.add(convert(message));
        }
        return converted;
    }

    public Object convert(final Message message) {
        if (message == message.getDefaultInstanceForType()) {
            return null;
        }
        final Map<String, Object> data = new LinkedHashMap<>();

        final Map<FieldDescriptor, Object> allFields = message.getAllFields();
        for (final Entry<FieldDescriptor, Object> field : allFields.entrySet()) {
            final FieldDescriptor descriptor = field.getKey();
            final Object value = field.getValue();
            Object converted;
            if (descriptor.isRepeated() && value instanceof Collection) {
                final Collection<?> collection = (Collection<?>) value;
                final List<Object> result = new ArrayList<>();
                for (final Object object : collection) {
                    result.add(convertField(descriptor, object));
                }
                converted = result;
            } else {
                converted = convertField(descriptor, value);
            }
            data.put(descriptor.getName(), converted);
        }

        return data;
    }

    public Object convertField(final FieldDescriptor field, final Object value) {
        switch (field.getType()) {
            case GROUP:
            case MESSAGE:
                return convert((Message) value);
            default:
                return value;
        }
    }

}
