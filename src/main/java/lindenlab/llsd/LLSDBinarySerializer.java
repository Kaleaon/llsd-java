/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews
 * Updated 2024 based on Second Life viewer and LibreMetaverse implementations
 */

package lindenlab.llsd;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * LLSD Binary format serializer
 * Basic implementation of binary LLSD format
 */
public class LLSDBinarySerializer extends LLSDSerializer {
    
    // Binary format markers
    private static final byte UNDEF_MARKER = '!';
    private static final byte FALSE_MARKER = '0';
    private static final byte TRUE_MARKER = '1';
    private static final byte INTEGER_MARKER = 'i';
    private static final byte REAL_MARKER = 'r';
    private static final byte UUID_MARKER = 'u';
    private static final byte BINARY_MARKER = 'b';
    private static final byte STRING_MARKER = 's';
    private static final byte URI_MARKER = 'l';
    private static final byte DATE_MARKER = 'd';
    private static final byte ARRAY_MARKER = '[';
    private static final byte ARRAY_END_MARKER = ']';
    private static final byte MAP_MARKER = '{';
    private static final byte MAP_END_MARKER = '}';
    private static final byte KEY_MARKER = 'k';
    
    private final SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    public LLSDBinarySerializer() {
        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    @Override
    public LLSDFormat getFormat() {
        return LLSDFormat.BINARY;
    }
    
    @Override
    public void serialize(LLSD llsd, OutputStream output) throws IOException, LLSDException {
        // Write binary header
        output.write("<? llsd/binary ?>\n".getBytes("UTF-8"));
        serializeElement(output, llsd.getContent());
    }
    
    @Override
    public String serializeToString(LLSD llsd) throws LLSDException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            serialize(llsd, baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new LLSDException("Error serializing to string", e);
        }
    }
    
    @Override
    public LLSD deserialize(InputStream input) throws IOException, LLSDException {
        // Skip binary header if present
        BufferedInputStream bis = new BufferedInputStream(input);
        bis.mark(20);
        byte[] headerBytes = new byte[18];
        int read = bis.read(headerBytes);
        String header = new String(headerBytes, 0, read, "UTF-8");
        if (!header.startsWith("<? llsd/binary ?>")) {
            bis.reset();
        }
        
        Object content = deserializeElement(bis);
        return new LLSD(content);
    }
    
    @Override
    public LLSD deserializeFromString(String data) throws LLSDException {
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            return deserialize(bais);
        } catch (IOException e) {
            throw new LLSDException("Error deserializing from string", e);
        }
    }
    
    private void serializeElement(OutputStream output, Object element) throws IOException, LLSDException {
        if (element == null) {
            output.write(UNDEF_MARKER);
            return;
        }
        
        if (element instanceof Boolean) {
            output.write(((Boolean) element) ? TRUE_MARKER : FALSE_MARKER);
        } else if (element instanceof Integer) {
            output.write(INTEGER_MARKER);
            writeInt(output, (Integer) element);
        } else if (element instanceof Double) {
            output.write(REAL_MARKER);
            writeDouble(output, (Double) element);
        } else if (element instanceof Float) {
            output.write(REAL_MARKER);
            writeDouble(output, ((Float) element).doubleValue());
        } else if (element instanceof UUID) {
            output.write(UUID_MARKER);
            writeUUID(output, (UUID) element);
        } else if (element instanceof String) {
            output.write(STRING_MARKER);
            writeString(output, (String) element);
        } else if (element instanceof byte[]) {
            output.write(BINARY_MARKER);
            writeBinary(output, (byte[]) element);
        } else if (element instanceof Date) {
            output.write(DATE_MARKER);
            writeDouble(output, ((Date) element).getTime() / 1000.0);
        } else if (element instanceof URI) {
            output.write(URI_MARKER);
            writeString(output, element.toString());
        } else if (element instanceof List) {
            serializeArray(output, (List<?>) element);
        } else if (element instanceof Map) {
            serializeMap(output, (Map<String, ?>) element);
        } else if (element instanceof LLSDUndefined) {
            output.write(UNDEF_MARKER);
        } else {
            throw new LLSDException("Cannot serialize type: " + element.getClass().getName());
        }
    }
    
    private void serializeArray(OutputStream output, List<?> array) throws IOException, LLSDException {
        output.write(ARRAY_MARKER);
        writeInt(output, array.size());
        for (Object element : array) {
            serializeElement(output, element);
        }
        output.write(ARRAY_END_MARKER);
    }
    
    private void serializeMap(OutputStream output, Map<String, ?> map) throws IOException, LLSDException {
        output.write(MAP_MARKER);
        writeInt(output, map.size());
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            output.write(KEY_MARKER);
            writeString(output, entry.getKey());
            serializeElement(output, entry.getValue());
        }
        output.write(MAP_END_MARKER);
    }
    
    private Object deserializeElement(InputStream input) throws IOException, LLSDException {
        int marker = input.read();
        if (marker == -1) {
            throw new LLSDException("Unexpected end of stream");
        }
        
        switch ((byte) marker) {
            case UNDEF_MARKER:
                return null;
            case FALSE_MARKER:
                return Boolean.FALSE;
            case TRUE_MARKER:
                return Boolean.TRUE;
            case INTEGER_MARKER:
                return readInt(input);
            case REAL_MARKER:
                return readDouble(input);
            case UUID_MARKER:
                return readUUID(input);
            case BINARY_MARKER:
                return readBinary(input);
            case STRING_MARKER:
                return readString(input);
            case URI_MARKER:
                return readURI(input);
            case DATE_MARKER:
                return readDate(input);
            case ARRAY_MARKER:
                return deserializeArray(input);
            case MAP_MARKER:
                return deserializeMap(input);
            default:
                throw new LLSDException("Unknown binary marker: " + (char) marker);
        }
    }
    
    private List<Object> deserializeArray(InputStream input) throws IOException, LLSDException {
        int size = readInt(input);
        List<Object> array = new ArrayList<>(size);
        
        for (int i = 0; i < size; i++) {
            array.add(deserializeElement(input));
        }
        
        // Read end marker
        int endMarker = input.read();
        if (endMarker != ARRAY_END_MARKER) {
            throw new LLSDException("Expected array end marker");
        }
        
        return array;
    }
    
    private Map<String, Object> deserializeMap(InputStream input) throws IOException, LLSDException {
        int size = readInt(input);
        Map<String, Object> map = new HashMap<>(size);
        
        for (int i = 0; i < size; i++) {
            // Read key marker
            int keyMarker = input.read();
            if (keyMarker != KEY_MARKER) {
                throw new LLSDException("Expected key marker in map");
            }
            
            String key = readString(input);
            Object value = deserializeElement(input);
            map.put(key, value);
        }
        
        // Read end marker
        int endMarker = input.read();
        if (endMarker != MAP_END_MARKER) {
            throw new LLSDException("Expected map end marker");
        }
        
        return map;
    }
    
    private void writeInt(OutputStream output, int value) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(value);
        output.write(buffer.array());
    }
    
    private int readInt(InputStream input) throws IOException, LLSDException {
        byte[] bytes = new byte[4];
        if (input.read(bytes) != 4) {
            throw new LLSDException("Unexpected end of stream reading integer");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getInt();
    }
    
    private void writeDouble(OutputStream output, double value) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putDouble(value);
        output.write(buffer.array());
    }
    
    private double readDouble(InputStream input) throws IOException, LLSDException {
        byte[] bytes = new byte[8];
        if (input.read(bytes) != 8) {
            throw new LLSDException("Unexpected end of stream reading double");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getDouble();
    }
    
    private void writeUUID(OutputStream output, UUID uuid) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        output.write(buffer.array());
    }
    
    private UUID readUUID(InputStream input) throws IOException, LLSDException {
        byte[] bytes = new byte[16];
        if (input.read(bytes) != 16) {
            throw new LLSDException("Unexpected end of stream reading UUID");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        long mostSig = buffer.getLong();
        long leastSig = buffer.getLong();
        return new UUID(mostSig, leastSig);
    }
    
    private void writeString(OutputStream output, String str) throws IOException {
        byte[] bytes = str.getBytes("UTF-8");
        writeInt(output, bytes.length);
        output.write(bytes);
    }
    
    private String readString(InputStream input) throws IOException, LLSDException {
        int length = readInt(input);
        byte[] bytes = new byte[length];
        if (input.read(bytes) != length) {
            throw new LLSDException("Unexpected end of stream reading string");
        }
        return new String(bytes, "UTF-8");
    }
    
    private void writeBinary(OutputStream output, byte[] data) throws IOException {
        writeInt(output, data.length);
        output.write(data);
    }
    
    private byte[] readBinary(InputStream input) throws IOException, LLSDException {
        int length = readInt(input);
        byte[] bytes = new byte[length];
        if (input.read(bytes) != length) {
            throw new LLSDException("Unexpected end of stream reading binary");
        }
        return bytes;
    }
    
    private URI readURI(InputStream input) throws IOException, LLSDException {
        String uriStr = readString(input);
        try {
            return new URI(uriStr);
        } catch (Exception e) {
            throw new LLSDException("Invalid URI: " + uriStr, e);
        }
    }
    
    private Date readDate(InputStream input) throws IOException, LLSDException {
        double timestamp = readDouble(input);
        return new Date((long) (timestamp * 1000));
    }
}