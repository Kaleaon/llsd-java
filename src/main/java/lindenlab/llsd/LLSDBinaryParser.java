/*
 * LLSDJ - LLSD in Java implementation
 *
 * Copyright(C) 2024 - Modernized implementation
 */

package lindenlab.llsd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Parser for LLSD documents in Binary format.
 * 
 * <p>This parser converts binary-formatted LLSD documents into Java objects.
 * The binary format is an efficient binary serialization of LLSD data.</p>
 * 
 * <p>Binary LLSD format uses specific byte markers:
 * <ul>
 * <li>Header: {@code "<?llsd/binary?>"}</li>
 * <li>Boolean: {@code 0x31} (true), {@code 0x30} (false)</li>
 * <li>Integer: {@code 'i'} + 4 bytes (big-endian)</li>
 * <li>Real: {@code 'r'} + 8 bytes (big-endian double)</li>
 * <li>String: {@code 's'} + 4 bytes length + UTF-8 data</li>
 * <li>UUID: {@code 'u'} + 16 bytes</li>
 * <li>Date: {@code 'd'} + 8 bytes (seconds since epoch as double)</li>
 * <li>URI: {@code 'l'} + 4 bytes length + UTF-8 data</li>
 * <li>Binary: {@code 'b'} + 4 bytes length + raw data</li>
 * <li>Array: {@code '['} + elements + {@code ']'}</li>
 * <li>Map: {@code '{'} + key-value pairs + {@code '}'}</li>
 * <li>Map Key: {@code 'k'} + 4 bytes length + UTF-8 data</li>
 * <li>Undefined: {@code '!'}</li>
 * </ul></p>
 * 
 * @since 1.0
 * @see LLSD
 * @see LLSDBinarySerializer
 */
public class LLSDBinaryParser {
    private static final String LLSD_BINARY_HEADER = "<?llsd/binary?>";
    private static final byte[] LLSD_BINARY_HEADER_BYTES = LLSD_BINARY_HEADER.getBytes(StandardCharsets.US_ASCII);
    
    private final DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

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
     * Constructs a new LLSD Binary parser.
     */
    public LLSDBinaryParser() {
        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Parses an LLSD document from the given binary input stream.
     *
     * @param binaryInput the binary input stream to read and parse as LLSD.
     * @return the parsed LLSD document
     * @throws IOException if there was a problem reading from the input stream.
     * @throws LLSDException if the document is valid binary, but invalid LLSD.
     */
    public LLSD parse(final InputStream binaryInput) throws IOException, LLSDException {
        BinaryReader reader = new BinaryReader(binaryInput);
        
        // Check for optional header
        skipWhitespace(reader);
        if (reader.peek() == '<') {
            // Read and verify header
            byte[] header = reader.readBytes(LLSD_BINARY_HEADER_BYTES.length);
            if (!Arrays.equals(header, LLSD_BINARY_HEADER_BYTES)) {
                throw new LLSDException("Invalid binary LLSD header");
            }
            skipWhitespace(reader);
        }
        
        Object parsedBinary = parseBinaryValue(reader);
        return new LLSD(parsedBinary);
    }

    private static class BinaryReader {
        private final InputStream input;
        private byte[] buffer = new byte[1];
        private boolean hasPeeked = false;
        private byte peekedByte;

        public BinaryReader(InputStream input) {
            this.input = input;
        }

        public byte peek() throws IOException, LLSDException {
            if (!hasPeeked) {
                int result = input.read(buffer);
                if (result == -1) {
                    throw new LLSDException("Unexpected end of binary stream");
                }
                peekedByte = buffer[0];
                hasPeeked = true;
            }
            return peekedByte;
        }

        public byte readByte() throws IOException, LLSDException {
            if (hasPeeked) {
                hasPeeked = false;
                return peekedByte;
            }
            
            int result = input.read(buffer);
            if (result == -1) {
                throw new LLSDException("Unexpected end of binary stream");
            }
            return buffer[0];
        }

        public byte[] readBytes(int count) throws IOException, LLSDException {
            byte[] bytes = new byte[count];
            int totalRead = 0;
            
            // First use any peeked byte
            if (hasPeeked && count > 0) {
                bytes[0] = peekedByte;
                totalRead = 1;
                hasPeeked = false;
            }
            
            while (totalRead < count) {
                int bytesRead = input.read(bytes, totalRead, count - totalRead);
                if (bytesRead == -1) {
                    throw new LLSDException("Unexpected end of binary stream");
                }
                totalRead += bytesRead;
            }
            
            return bytes;
        }

        public int readInt32() throws IOException, LLSDException {
            byte[] bytes = readBytes(4);
            return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
        }

        public double readDouble() throws IOException, LLSDException {
            byte[] bytes = readBytes(8);
            return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getDouble();
        }

        public String readString() throws IOException, LLSDException {
            int length = readInt32();
            if (length == 0) {
                return "";
            }
            byte[] bytes = readBytes(length);
            return new String(bytes, StandardCharsets.UTF_8);
        }

        public boolean isAvailable() throws IOException {
            return input.available() > 0 || hasPeeked;
        }
    }

    private void skipWhitespace(BinaryReader reader) throws IOException {
        try {
            while (reader.isAvailable()) {
                byte b = reader.peek();
                if (Character.isWhitespace((char) b)) {
                    reader.readByte(); // consume whitespace
                } else {
                    break;
                }
            }
        } catch (LLSDException e) {
            // End of stream is ok when skipping whitespace
        }
    }

    private Object parseBinaryValue(BinaryReader reader) throws IOException, LLSDException {
        byte marker = reader.readByte();

        switch (marker) {
            case UNDEF_MARKER:
                return ""; // Undefined value represented as empty string

            case TRUE_MARKER:
                return Boolean.TRUE;

            case FALSE_MARKER:
                return Boolean.FALSE;

            case INTEGER_MARKER:
                return reader.readInt32();

            case REAL_MARKER:
                return reader.readDouble();

            case STRING_MARKER:
                return reader.readString();

            case UUID_MARKER:
                return parseUUID(reader);

            case DATE_MARKER:
                return parseDate(reader);

            case URI_MARKER:
                return parseURI(reader);

            case BINARY_MARKER:
                return parseBinary(reader);

            case ARRAY_BEGIN_MARKER:
                return parseArray(reader);

            case MAP_BEGIN_MARKER:
                return parseMap(reader);

            default:
                throw new LLSDException("Unknown binary LLSD marker: 0x" + 
                    Integer.toHexString(marker & 0xFF).toUpperCase());
        }
    }

    private UUID parseUUID(BinaryReader reader) throws IOException, LLSDException {
        byte[] bytes = reader.readBytes(16);
        
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();
        
        return new UUID(mostSigBits, leastSigBits);
    }

    private Date parseDate(BinaryReader reader) throws IOException, LLSDException {
        double secondsSinceEpoch = reader.readDouble();
        long millisSinceEpoch = (long) (secondsSinceEpoch * 1000.0);
        return new Date(millisSinceEpoch);
    }

    private URI parseURI(BinaryReader reader) throws IOException, LLSDException {
        String uriString = reader.readString();
        try {
            return new URI(uriString);
        } catch (URISyntaxException e) {
            throw new LLSDException("Invalid URI in binary LLSD: " + uriString, e);
        }
    }

    private byte[] parseBinary(BinaryReader reader) throws IOException, LLSDException {
        int length = reader.readInt32();
        if (length == 0) {
            return new byte[0];
        }
        return reader.readBytes(length);
    }

    private List<Object> parseArray(BinaryReader reader) throws IOException, LLSDException {
        List<Object> array = new ArrayList<>();

        while (reader.isAvailable()) {
            byte marker = reader.peek();
            if (marker == ARRAY_END_MARKER) {
                reader.readByte(); // consume ']'
                break;
            }
            
            Object value = parseBinaryValue(reader);
            array.add(value);
        }

        return array;
    }

    private Map<String, Object> parseMap(BinaryReader reader) throws IOException, LLSDException {
        Map<String, Object> map = new HashMap<>();

        while (reader.isAvailable()) {
            byte marker = reader.peek();
            if (marker == MAP_END_MARKER) {
                reader.readByte(); // consume '}'
                break;
            }

            // Read key
            if (reader.readByte() != KEY_MARKER) {
                throw new LLSDException("Expected key marker 'k' in binary LLSD map");
            }
            
            String key = reader.readString();
            Object value = parseBinaryValue(reader);
            map.put(key, value);
        }

        return map;
    }
}