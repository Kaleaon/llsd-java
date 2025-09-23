/*
 * LLSDJ - LLSD in Java implementation
 *
 * Copyright(C) 2024 - Modernized implementation
 */

package lindenlab.llsd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Serializer for LLSD documents to Binary format.
 * 
 * <p>This serializer converts LLSD objects into binary-formatted documents using
 * the efficient binary LLSD protocol.</p>
 * 
 * @since 1.0
 * @see LLSD
 * @see LLSDBinaryParser
 */
public class LLSDBinarySerializer {
    private static final String LLSD_BINARY_HEADER = "<?llsd/binary?>";
    private static final byte[] LLSD_BINARY_HEADER_BYTES = LLSD_BINARY_HEADER.getBytes(StandardCharsets.US_ASCII);

    // Binary markers
    private static final byte UNDEF_MARKER = (byte) '!';
    private static final byte TRUE_MARKER = (byte) '1';
    private static final byte FALSE_MARKER = (byte) '0';
    private static final byte INTEGER_MARKER = (byte) 'i';
    private static final byte REAL_MARKER = (byte) 'r';
    private static final byte STRING_MARKER = (byte) 's';
    private static final byte UUID_MARKER = (byte) 'u';
    private static final byte DATE_MARKER = (byte) 'd';
    private static final byte URI_MARKER = (byte) 'l';
    private static final byte BINARY_MARKER = (byte) 'b';
    private static final byte ARRAY_BEGIN_MARKER = (byte) '[';
    private static final byte ARRAY_END_MARKER = (byte) ']';
    private static final byte MAP_BEGIN_MARKER = (byte) '{';
    private static final byte MAP_END_MARKER = (byte) '}';
    private static final byte KEY_MARKER = (byte) 'k';

    /**
     * Constructs a new LLSD Binary serializer.
     */
    public LLSDBinarySerializer() {
    }

    /**
     * Serializes an LLSD document to Binary format.
     *
     * @param llsd the LLSD document to serialize
     * @param output the output stream to write binary data to
     * @param includeHeader whether to include the binary LLSD header
     * @throws IOException if there was a problem writing to the output stream
     * @throws LLSDException if there was a problem serializing the LLSD data
     */
    public void serialize(LLSD llsd, OutputStream output, boolean includeHeader) 
            throws IOException, LLSDException {
        if (includeHeader) {
            output.write(LLSD_BINARY_HEADER_BYTES);
        }
        serializeValue(llsd.getContent(), output);
    }

    /**
     * Serializes an LLSD document to Binary format with header.
     *
     * @param llsd the LLSD document to serialize
     * @param output the output stream to write binary data to
     * @throws IOException if there was a problem writing to the output stream
     * @throws LLSDException if there was a problem serializing the LLSD data
     */
    public void serialize(LLSD llsd, OutputStream output) throws IOException, LLSDException {
        serialize(llsd, output, true);
    }

    /**
     * Serializes an LLSD document to binary byte array.
     *
     * @param llsd the LLSD document to serialize
     * @param includeHeader whether to include the binary LLSD header
     * @return the serialized binary data
     * @throws LLSDException if there was a problem serializing the LLSD data
     */
    public byte[] serialize(LLSD llsd, boolean includeHeader) throws LLSDException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            serialize(llsd, output, includeHeader);
            return output.toByteArray();
        } catch (IOException e) {
            throw new LLSDException("Failed to serialize binary LLSD", e);
        }
    }

    /**
     * Serializes an LLSD document to binary byte array with header.
     *
     * @param llsd the LLSD document to serialize
     * @return the serialized binary data
     * @throws LLSDException if there was a problem serializing the LLSD data
     */
    public byte[] serialize(LLSD llsd) throws LLSDException {
        return serialize(llsd, true);
    }

    private void serializeValue(Object value, OutputStream output) throws IOException, LLSDException {
        if (value == null || (value instanceof String && ((String) value).isEmpty())) {
            output.write(UNDEF_MARKER);
            return;
        }

        if (value instanceof Map) {
            serializeMap(value, output);
        } else if (value instanceof List) {
            serializeArray(value, output);
        } else if (value instanceof String) {
            serializeString((String) value, output);
        } else if (value instanceof Integer) {
            serializeInteger((Integer) value, output);
        } else if (value instanceof Double) {
            serializeReal((Double) value, output);
        } else if (value instanceof Boolean) {
            serializeBoolean((Boolean) value, output);
        } else if (value instanceof Date) {
            serializeDate((Date) value, output);
        } else if (value instanceof URI) {
            serializeURI((URI) value, output);
        } else if (value instanceof UUID) {
            serializeUUID((UUID) value, output);
        } else if (value instanceof byte[]) {
            serializeBinary((byte[]) value, output);
        } else if (value instanceof LLSDUndefined) {
            output.write(UNDEF_MARKER);
        } else {
            // Fallback to string representation
            serializeString(value.toString(), output);
        }
    }

    private void serializeBoolean(Boolean value, OutputStream output) throws IOException {
        output.write(value ? TRUE_MARKER : FALSE_MARKER);
    }

    private void serializeInteger(Integer value, OutputStream output) throws IOException {
        output.write(INTEGER_MARKER);
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(value);
        output.write(buffer.array());
    }

    private void serializeReal(Double value, OutputStream output) throws IOException {
        output.write(REAL_MARKER);
        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        buffer.putDouble(value);
        output.write(buffer.array());
    }

    private void serializeString(String value, OutputStream output) throws IOException {
        output.write(STRING_MARKER);
        byte[] stringBytes = value.getBytes(StandardCharsets.UTF_8);
        writeInt32(output, stringBytes.length);
        output.write(stringBytes);
    }

    private void serializeUUID(UUID value, OutputStream output) throws IOException {
        output.write(UUID_MARKER);
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(value.getMostSignificantBits());
        buffer.putLong(value.getLeastSignificantBits());
        output.write(buffer.array());
    }

    private void serializeDate(Date value, OutputStream output) throws IOException {
        output.write(DATE_MARKER);
        double secondsSinceEpoch = value.getTime() / 1000.0;
        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        buffer.putDouble(secondsSinceEpoch);
        output.write(buffer.array());
    }

    private void serializeURI(URI value, OutputStream output) throws IOException {
        output.write(URI_MARKER);
        byte[] uriBytes = value.toString().getBytes(StandardCharsets.UTF_8);
        writeInt32(output, uriBytes.length);
        output.write(uriBytes);
    }

    private void serializeBinary(byte[] value, OutputStream output) throws IOException {
        output.write(BINARY_MARKER);
        writeInt32(output, value.length);
        output.write(value);
    }

    @SuppressWarnings("unchecked")
    private void serializeArray(Object value, OutputStream output) throws IOException, LLSDException {
        List<Object> list = (List<Object>) value;
        
        output.write(ARRAY_BEGIN_MARKER);
        for (Object item : list) {
            serializeValue(item, output);
        }
        output.write(ARRAY_END_MARKER);
    }

    @SuppressWarnings("unchecked")
    private void serializeMap(Object value, OutputStream output) throws IOException, LLSDException {
        Map<String, Object> map = (Map<String, Object>) value;
        
        output.write(MAP_BEGIN_MARKER);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            // Write key
            output.write(KEY_MARKER);
            byte[] keyBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
            writeInt32(output, keyBytes.length);
            output.write(keyBytes);
            
            // Write value
            serializeValue(entry.getValue(), output);
        }
        output.write(MAP_END_MARKER);
    }

    private void writeInt32(OutputStream output, int value) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(value);
        output.write(buffer.array());
    }
}