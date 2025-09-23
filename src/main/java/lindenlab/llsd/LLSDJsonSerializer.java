/*
 * LLSDJ - LLSD in Java implementation
 *
 * Copyright(C) 2024 - Modernized implementation
 */

package lindenlab.llsd;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Serializer for LLSD documents to JSON format.
 * 
 * <p>This serializer converts LLSD objects into JSON-formatted documents using
 * the LLSD JSON conventions for special data types.</p>
 * 
 * <p>JSON LLSD format conventions:
 * <ul>
 * <li>Dates: {"d":"2024-01-01T00:00:00Z"}</li>
 * <li>URIs: {"u":"http://example.com"}</li>
 * <li>UUIDs: {"i":"550e8400-e29b-41d4-a716-446655440000"}</li>
 * <li>Binary data: {"b":"SGVsbG8gV29ybGQ="}</li>
 * <li>Undefined values: null</li>
 * </ul></p>
 * 
 * @since 1.0
 * @see LLSD
 * @see LLSDJsonParser
 */
public class LLSDJsonSerializer {
    private final DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Constructs a new LLSD JSON serializer.
     */
    public LLSDJsonSerializer() {
        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Serializes an LLSD document to JSON format.
     *
     * @param llsd the LLSD document to serialize
     * @param writer the writer to output JSON to
     * @throws IOException if there was a problem writing to the writer
     * @throws LLSDException if there was a problem serializing the LLSD data
     */
    public void serialize(LLSD llsd, Writer writer) throws IOException, LLSDException {
        serializeValue(llsd.getContent(), writer);
    }

    /**
     * Serializes a single LLSD value to JSON.
     */
    private void serializeValue(Object value, Writer writer) throws IOException, LLSDException {
        if (value == null || (value instanceof String && ((String) value).isEmpty())) {
            writer.write("null");
            return;
        }

        if (value instanceof Map) {
            serializeMap(value, writer);
        } else if (value instanceof List) {
            serializeArray(value, writer);
        } else if (value instanceof String) {
            serializeString((String) value, writer);
        } else if (value instanceof Integer) {
            writer.write(value.toString());
        } else if (value instanceof Double) {
            Double d = (Double) value;
            if (d.isNaN()) {
                writer.write("\"NaN\""); // Special case for NaN
            } else if (d.isInfinite()) {
                writer.write(d > 0 ? "\"Infinity\"" : "\"-Infinity\"");
            } else {
                writer.write(d.toString());
            }
        } else if (value instanceof Boolean) {
            writer.write(value.toString());
        } else if (value instanceof Date) {
            writer.write("{\"d\":\"");
            writer.write(iso8601Format.format((Date) value));
            writer.write("\"}");
        } else if (value instanceof URI) {
            writer.write("{\"u\":");
            serializeString(value.toString(), writer);
            writer.write("}");
        } else if (value instanceof UUID) {
            writer.write("{\"i\":");
            serializeString(value.toString(), writer);
            writer.write("}");
        } else if (value instanceof byte[]) {
            writer.write("{\"b\":");
            serializeString(Base64.getEncoder().encodeToString((byte[]) value), writer);
            writer.write("}");
        } else if (value instanceof LLSDUndefined) {
            writer.write("null");
        } else {
            // Fallback to string representation
            serializeString(value.toString(), writer);
        }
    }

    @SuppressWarnings("unchecked")
    private void serializeMap(Object value, Writer writer) throws IOException, LLSDException {
        Map<String, Object> map = (Map<String, Object>) value;
        
        writer.write("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                writer.write(",");
            }
            first = false;
            
            serializeString(entry.getKey(), writer);
            writer.write(":");
            serializeValue(entry.getValue(), writer);
        }
        writer.write("}");
    }

    @SuppressWarnings("unchecked")
    private void serializeArray(Object value, Writer writer) throws IOException, LLSDException {
        List<Object> list = (List<Object>) value;
        
        writer.write("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) {
                writer.write(",");
            }
            first = false;
            
            serializeValue(item, writer);
        }
        writer.write("]");
    }

    private void serializeString(String str, Writer writer) throws IOException {
        writer.write("\"");
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"':
                    writer.write("\\\"");
                    break;
                case '\\':
                    writer.write("\\\\");
                    break;
                case '\b':
                    writer.write("\\b");
                    break;
                case '\f':
                    writer.write("\\f");
                    break;
                case '\n':
                    writer.write("\\n");
                    break;
                case '\r':
                    writer.write("\\r");
                    break;
                case '\t':
                    writer.write("\\t");
                    break;
                default:
                    if (c < 0x20 || c > 0x7e) {
                        writer.write(String.format("\\u%04x", (int) c));
                    } else {
                        writer.write(c);
                    }
                    break;
            }
        }
        writer.write("\"");
    }
}