/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews  
 * Updated 2024 based on Second Life viewer and LibreMetaverse implementations
 */

package lindenlab.llsd;

import java.io.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * LLSD Notation format serializer
 * Implements the LLSD notation format similar to LibreMetaverse
 */
public class LLSDNotationSerializer extends LLSDSerializer {
    
    private static final char UNDEF_NOTATION = '!';
    private static final char TRUE_NOTATION = 't';
    private static final char FALSE_NOTATION = 'f';
    private static final char INTEGER_NOTATION = 'i';
    private static final char REAL_NOTATION = 'r';
    private static final char UUID_NOTATION = 'u';
    private static final char BINARY_NOTATION = 'b';
    private static final char STRING_NOTATION = 's';
    private static final char URI_NOTATION = 'l';
    private static final char DATE_NOTATION = 'd';
    
    private static final char ARRAY_BEGIN = '[';
    private static final char ARRAY_END = ']';
    private static final char MAP_BEGIN = '{';
    private static final char MAP_END = '}';
    private static final char DELIMITER = ',';
    private static final char KEY_DELIMITER = ':';
    
    private static final char SIZE_BEGIN = '(';
    private static final char SIZE_END = ')';
    private static final char DOUBLE_QUOTE = '"';
    private static final char SINGLE_QUOTE = '\'';
    
    private final SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    public LLSDNotationSerializer() {
        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    @Override
    public LLSDFormat getFormat() {
        return LLSDFormat.NOTATION;
    }
    
    @Override
    public void serialize(LLSD llsd, OutputStream output) throws IOException, LLSDException {
        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        serializeElement(writer, llsd.getContent());
        writer.flush();
    }
    
    @Override
    public String serializeToString(LLSD llsd) throws LLSDException {
        StringWriter writer = new StringWriter();
        try {
            serializeElement(writer, llsd.getContent());
        } catch (IOException e) {
            throw new LLSDException("Error serializing to string", e);
        }
        return writer.toString();
    }
    
    @Override
    public LLSD deserialize(InputStream input) throws IOException, LLSDException {
        InputStreamReader reader = new InputStreamReader(input, "UTF-8");
        PushbackReader pushbackReader = new PushbackReader(reader, 1);
        Object content = deserializeElement(pushbackReader);
        return new LLSD(content);
    }
    
    @Override
    public LLSD deserializeFromString(String data) throws LLSDException {
        StringReader reader = new StringReader(data);
        PushbackReader pushbackReader = new PushbackReader(reader, 1);
        try {
            Object content = deserializeElement(pushbackReader);
            return new LLSD(content);
        } catch (IOException e) {
            throw new LLSDException("Error deserializing from string", e);
        }
    }
    
    private void serializeElement(Writer writer, Object element) throws IOException, LLSDException {
        if (element == null) {
            writer.write(UNDEF_NOTATION);
            return;
        }
        
        if (element instanceof Boolean) {
            writer.write(((Boolean) element) ? TRUE_NOTATION : FALSE_NOTATION);
        } else if (element instanceof Integer) {
            writer.write(INTEGER_NOTATION);
            writer.write(element.toString());
        } else if (element instanceof Double || element instanceof Float) {
            writer.write(REAL_NOTATION);
            writer.write(element.toString());
        } else if (element instanceof UUID) {
            writer.write(UUID_NOTATION);
            writer.write(element.toString());
        } else if (element instanceof String) {
            writer.write(SINGLE_QUOTE);
            writer.write(escapeString((String) element, SINGLE_QUOTE));
            writer.write(SINGLE_QUOTE);
        } else if (element instanceof byte[]) {
            writer.write(BINARY_NOTATION);
            writer.write("64");
            writer.write(DOUBLE_QUOTE);
            writer.write(encodeBase64((byte[]) element));
            writer.write(DOUBLE_QUOTE);
        } else if (element instanceof Date) {
            writer.write(DATE_NOTATION);
            writer.write(DOUBLE_QUOTE);
            writer.write(iso8601Format.format((Date) element));
            writer.write(DOUBLE_QUOTE);
        } else if (element instanceof URI) {
            writer.write(URI_NOTATION);
            writer.write(DOUBLE_QUOTE);
            writer.write(escapeString(element.toString(), DOUBLE_QUOTE));
            writer.write(DOUBLE_QUOTE);
        } else if (element instanceof List) {
            serializeArray(writer, (List<?>) element);
        } else if (element instanceof Map) {
            serializeMap(writer, (Map<String, ?>) element);
        } else if (element instanceof LLSDUndefined) {
            writer.write(UNDEF_NOTATION);
        } else {
            throw new LLSDException("Cannot serialize type: " + element.getClass().getName());
        }
    }
    
    private void serializeArray(Writer writer, List<?> array) throws IOException, LLSDException {
        writer.write(ARRAY_BEGIN);
        for (int i = 0; i < array.size(); i++) {
            serializeElement(writer, array.get(i));
            if (i < array.size() - 1) {
                writer.write(DELIMITER);
            }
        }
        writer.write(ARRAY_END);
    }
    
    private void serializeMap(Writer writer, Map<String, ?> map) throws IOException, LLSDException {
        writer.write(MAP_BEGIN);
        int count = 0;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            writer.write(SINGLE_QUOTE);
            writer.write(escapeString(entry.getKey(), SINGLE_QUOTE));
            writer.write(SINGLE_QUOTE);
            writer.write(KEY_DELIMITER);
            serializeElement(writer, entry.getValue());
            if (count < map.size() - 1) {
                writer.write(DELIMITER);
            }
            count++;
        }
        writer.write(MAP_END);
    }
    
    private Object deserializeElement(Reader reader) throws IOException, LLSDException {
        int ch = skipWhitespace(reader);
        if (ch == -1) {
            return null;
        }
        
        switch ((char) ch) {
            case UNDEF_NOTATION:
                return null;
            case TRUE_NOTATION:
                return Boolean.TRUE;
            case FALSE_NOTATION:
                return Boolean.FALSE;
            case INTEGER_NOTATION:
                return parseInteger(reader);
            case REAL_NOTATION:
                return parseReal(reader);
            case UUID_NOTATION:
                return parseUUID(reader);
            case BINARY_NOTATION:
                return parseBinary(reader);
            case STRING_NOTATION:
                return parseString(reader);
            case SINGLE_QUOTE:
                return parseQuotedString(reader, SINGLE_QUOTE);
            case DOUBLE_QUOTE:
                return parseQuotedString(reader, DOUBLE_QUOTE);
            case URI_NOTATION:
                return parseURI(reader);
            case DATE_NOTATION:
                return parseDate(reader);
            case ARRAY_BEGIN:
                return parseArray(reader);
            case MAP_BEGIN:
                return parseMap(reader);
            default:
                // Try to parse as a number if it starts with digit or -
                if (Character.isDigit((char) ch) || ch == '-') {
                    // Put back the character and try to parse as integer
                    if (reader instanceof PushbackReader) {
                        ((PushbackReader) reader).unread(ch);
                    }
                    return parseDirectInteger(reader);
                }
                throw new LLSDException("Unknown notation marker: " + (char) ch);
        }
    }
    
    private Integer parseDirectInteger(Reader reader) throws IOException, LLSDException {
        StringBuilder sb = new StringBuilder();
        int ch;
        
        // Read number including negative sign
        while ((ch = reader.read()) != -1) {
            char c = (char) ch;
            if (Character.isDigit(c) || c == '-') {
                sb.append(c);
            } else {
                if (reader instanceof PushbackReader) {
                    ((PushbackReader) reader).unread(ch);
                }
                break;
            }
        }
        
        try {
            return Integer.parseInt(sb.toString());
        } catch (NumberFormatException e) {
            throw new LLSDException("Invalid integer: " + sb.toString(), e);
        }
    }
    
    private Integer parseInteger(Reader reader) throws IOException, LLSDException {
        StringBuilder sb = new StringBuilder();
        int ch;
        
        // Check for negative sign
        ch = reader.read();
        if (ch == '-') {
            sb.append((char) ch);
            ch = reader.read();
        }
        
        // Read digits
        while (ch != -1 && Character.isDigit((char) ch)) {
            sb.append((char) ch);
            ch = reader.read();
        }
        
        // Put back the last character
        if (ch != -1 && reader instanceof PushbackReader) {
            ((PushbackReader) reader).unread(ch);
        }
        
        try {
            return Integer.parseInt(sb.toString());
        } catch (NumberFormatException e) {
            throw new LLSDException("Invalid integer: " + sb.toString(), e);
        }
    }
    
    private Double parseReal(Reader reader) throws IOException, LLSDException {
        StringBuilder sb = new StringBuilder();
        int ch;
        
        // Read number (including scientific notation)
        while ((ch = reader.read()) != -1) {
            char c = (char) ch;
            if (Character.isDigit(c) || c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
                sb.append(c);
            } else {
                if (reader instanceof PushbackReader) {
                    ((PushbackReader) reader).unread(ch);
                }
                break;
            }
        }
        
        try {
            return Double.parseDouble(sb.toString());
        } catch (NumberFormatException e) {
            throw new LLSDException("Invalid real: " + sb.toString(), e);
        }
    }
    
    private UUID parseUUID(Reader reader) throws IOException, LLSDException {
        char[] buffer = new char[36];
        int read = reader.read(buffer, 0, 36);
        if (read != 36) {
            throw new LLSDException("Invalid UUID: expected 36 characters");
        }
        
        try {
            return UUID.fromString(new String(buffer));
        } catch (IllegalArgumentException e) {
            throw new LLSDException("Invalid UUID format", e);
        }
    }
    
    private byte[] parseBinary(Reader reader) throws IOException, LLSDException {
        // Read encoding type (should be "64" for base64)
        char[] encoding = new char[2];
        if (reader.read(encoding, 0, 2) != 2) {
            throw new LLSDException("Invalid binary encoding");
        }
        
        String encodingStr = new String(encoding);
        if (!"64".equals(encodingStr)) {
            throw new LLSDException("Unsupported binary encoding: " + encodingStr);
        }
        
        // Skip quote
        int quote = reader.read();
        if (quote != DOUBLE_QUOTE) {
            throw new LLSDException("Expected quote after binary encoding");
        }
        
        // Read base64 data
        String base64Data = parseQuotedString(reader, DOUBLE_QUOTE);
        
        try {
            return decodeBase64(base64Data);
        } catch (IllegalArgumentException e) {
            throw new LLSDException("Invalid base64 data", e);
        }
    }
    
    private String parseString(Reader reader) throws IOException, LLSDException {
        // Read length in parentheses
        int lengthParenOpen = reader.read();
        if (lengthParenOpen != SIZE_BEGIN) {
            throw new LLSDException("Expected '(' for string length");
        }
        
        StringBuilder lengthStr = new StringBuilder();
        int ch;
        while ((ch = reader.read()) != -1 && ch != SIZE_END) {
            lengthStr.append((char) ch);
        }
        
        int length;
        try {
            length = Integer.parseInt(lengthStr.toString());
        } catch (NumberFormatException e) {
            throw new LLSDException("Invalid string length: " + lengthStr.toString(), e);
        }
        
        // Skip quote
        int quote = reader.read();
        if (quote != DOUBLE_QUOTE) {
            throw new LLSDException("Expected quote after string length");
        }
        
        // Read string data
        char[] buffer = new char[length];
        if (reader.read(buffer, 0, length) != length) {
            throw new LLSDException("Unexpected end of string");
        }
        
        // Skip closing quote
        quote = reader.read();
        if (quote != DOUBLE_QUOTE) {
            throw new LLSDException("Expected closing quote");
        }
        
        return new String(buffer);
    }
    
    private String parseQuotedString(Reader reader, char quote) throws IOException, LLSDException {
        StringBuilder sb = new StringBuilder();
        int ch;
        boolean escaped = false;
        
        while ((ch = reader.read()) != -1) {
            char c = (char) ch;
            
            if (escaped) {
                switch (c) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case '\\': sb.append('\\'); break;
                    default: sb.append(c); break;
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == quote) {
                break;
            } else {
                sb.append(c);
            }
        }
        
        return sb.toString();
    }
    
    private URI parseURI(Reader reader) throws IOException, LLSDException {
        // Skip quote
        int quote = reader.read();
        if (quote != DOUBLE_QUOTE) {
            throw new LLSDException("Expected quote for URI");
        }
        
        String uriStr = parseQuotedString(reader, DOUBLE_QUOTE);
        
        try {
            return new URI(uriStr);
        } catch (Exception e) {
            throw new LLSDException("Invalid URI: " + uriStr, e);
        }
    }
    
    private Date parseDate(Reader reader) throws IOException, LLSDException {
        // Skip quote
        int quote = reader.read();
        if (quote != DOUBLE_QUOTE) {
            throw new LLSDException("Expected quote for date");
        }
        
        String dateStr = parseQuotedString(reader, DOUBLE_QUOTE);
        
        try {
            return iso8601Format.parse(dateStr);
        } catch (Exception e) {
            throw new LLSDException("Invalid date: " + dateStr, e);
        }
    }
    
    private List<Object> parseArray(Reader reader) throws IOException, LLSDException {
        List<Object> array = new ArrayList<>();
        int ch;
        
        while ((ch = skipWhitespace(reader)) != -1 && ch != ARRAY_END) {
            if (reader instanceof PushbackReader) {
                ((PushbackReader) reader).unread(ch);
            }
            
            array.add(deserializeElement(reader));
            
            ch = skipWhitespace(reader);
            if (ch == DELIMITER) {
                continue;
            } else if (ch == ARRAY_END) {
                break;
            } else if (ch != -1 && reader instanceof PushbackReader) {
                ((PushbackReader) reader).unread(ch);
            }
        }
        
        return array;
    }
    
    private Map<String, Object> parseMap(Reader reader) throws IOException, LLSDException {
        Map<String, Object> map = new HashMap<>();
        int ch;
        
        while ((ch = skipWhitespace(reader)) != -1 && ch != MAP_END) {
            if (reader instanceof PushbackReader) {
                ((PushbackReader) reader).unread(ch);
            }
            
            // Parse key
            Object keyObj = deserializeElement(reader);
            if (!(keyObj instanceof String)) {
                throw new LLSDException("Map key must be string");
            }
            String key = (String) keyObj;
            
            // Skip colon
            ch = skipWhitespace(reader);
            if (ch != KEY_DELIMITER) {
                throw new LLSDException("Expected ':' after map key");
            }
            
            // Parse value
            Object value = deserializeElement(reader);
            map.put(key, value);
            
            ch = skipWhitespace(reader);
            if (ch == DELIMITER) {
                continue;
            } else if (ch == MAP_END) {
                break;
            } else if (ch != -1 && reader instanceof PushbackReader) {
                ((PushbackReader) reader).unread(ch);
            }
        }
        
        return map;
    }
    
    private int skipWhitespace(Reader reader) throws IOException {
        int ch;
        while ((ch = reader.read()) != -1) {
            char c = (char) ch;
            if (!Character.isWhitespace(c)) {
                return ch;
            }
        }
        return -1;
    }
    
    private String escapeString(String str, char quote) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                case '\r': sb.append("\\r"); break;
                case '\\': sb.append("\\\\"); break;
                default:
                    if (c == quote) {
                        sb.append("\\").append(quote);
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }
}