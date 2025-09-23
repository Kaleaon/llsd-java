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
 * Serializer for LLSD documents to Notation format.
 * 
 * <p>This serializer converts LLSD objects into notation-formatted documents using
 * the compact LLSD notation syntax.</p>
 * 
 * <p>Notation format output examples:
 * <ul>
 * <li>Boolean: {@code 1} (true), {@code 0} (false)</li>
 * <li>Integer: {@code i42}</li>
 * <li>Real: {@code r3.14159}</li>
 * <li>String: {@code s'hello world'}</li>
 * <li>UUID: {@code u550e8400-e29b-41d4-a716-446655440000}</li>
 * <li>Date: {@code d2024-01-01T00:00:00Z}</li>
 * <li>URI: {@code lhttp://example.com}</li>
 * <li>Binary: {@code b64"SGVsbG8="}</li>
 * <li>Array: {@code [i1,i2,i3]}</li>
 * <li>Map: {@code {key:s'value',num:i42}}</li>
 * </ul></p>
 * 
 * @since 1.0
 * @see LLSD
 * @see LLSDNotationParser
 */
public class LLSDNotationSerializer {
    private final DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Constructs a new LLSD Notation serializer.
     */
    public LLSDNotationSerializer() {
        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Serializes an LLSD document to Notation format.
     *
     * @param llsd the LLSD document to serialize
     * @param writer the writer to output notation to
     * @throws IOException if there was a problem writing to the writer
     * @throws LLSDException if there was a problem serializing the LLSD data
     */
    public void serialize(LLSD llsd, Writer writer) throws IOException, LLSDException {
        serializeValue(llsd.getContent(), writer);
    }

    /**
     * Serializes a single LLSD value to notation.
     */
    private void serializeValue(Object value, Writer writer) throws IOException, LLSDException {
        if (value == null || (value instanceof String && ((String) value).isEmpty())) {
            writer.write("!");
            return;
        }

        if (value instanceof Map) {
            serializeMap(value, writer);
        } else if (value instanceof List) {
            serializeArray(value, writer);
        } else if (value instanceof String) {
            serializeString((String) value, writer);
        } else if (value instanceof Integer) {
            writer.write("i");
            writer.write(value.toString());
        } else if (value instanceof Double) {
            Double d = (Double) value;
            writer.write("r");
            if (d.isNaN()) {
                writer.write("nan");
            } else if (d.isInfinite()) {
                writer.write(d > 0 ? "inf" : "-inf");
            } else {
                writer.write(d.toString());
            }
        } else if (value instanceof Boolean) {
            writer.write(((Boolean) value) ? "1" : "0");
        } else if (value instanceof Date) {
            writer.write("d");
            writer.write(iso8601Format.format((Date) value));
        } else if (value instanceof URI) {
            writer.write("l");
            writer.write(value.toString());
        } else if (value instanceof UUID) {
            writer.write("u");
            writer.write(value.toString());
        } else if (value instanceof byte[]) {
            serializeBinary((byte[]) value, writer);
        } else if (value instanceof LLSDUndefined) {
            writer.write("!");
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
            
            // Serialize key
            String key = entry.getKey();
            if (isValidIdentifier(key)) {
                writer.write(key);
            } else {
                serializeString(key, writer);
            }
            
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
        writer.write("s'");
        // Escape single quotes and backslashes
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\'':
                    writer.write("\\'");
                    break;
                case '\\':
                    writer.write("\\\\");
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
                    writer.write(c);
                    break;
            }
        }
        writer.write("'");
    }

    private void serializeBinary(byte[] data, Writer writer) throws IOException {
        writer.write("b64\"");
        writer.write(Base64.getEncoder().encodeToString(data));
        writer.write("\"");
    }

    /**
     * Checks if a string is a valid identifier (can be used as an unquoted map key).
     */
    private boolean isValidIdentifier(String str) {
        if (str.isEmpty()) {
            return false;
        }
        
        char first = str.charAt(0);
        if (!Character.isLetter(first) && first != '_') {
            return false;
        }
        
        for (int i = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }
        
        return true;
    }
}