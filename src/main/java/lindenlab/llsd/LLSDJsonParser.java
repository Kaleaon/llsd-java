/*
 * LLSDJ - LLSD in Java implementation
 *
 * Copyright(C) 2024 - Modernized implementation
 */

package lindenlab.llsd;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Parser for LLSD documents in JSON format.
 * 
 * <p>This parser converts JSON-formatted LLSD documents into Java objects.
 * It supports all standard LLSD data types with proper JSON representation.</p>
 * 
 * <p>JSON LLSD format uses specific conventions:
 * <ul>
 * <li>Dates are represented as ISO 8601 strings with "d" prefix: {"d":"2024-01-01T00:00:00Z"}</li>
 * <li>URIs are represented with "u" prefix: {"u":"http://example.com"}</li>
 * <li>UUIDs are represented with "i" prefix: {"i":"550e8400-e29b-41d4-a716-446655440000"}</li>
 * <li>Binary data is base64 encoded with "b" prefix: {"b":"SGVsbG8gV29ybGQ="}</li>
 * <li>Undefined values are represented as null</li>
 * </ul></p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * LLSDJsonParser parser = new LLSDJsonParser();
 * try (InputStream input = Files.newInputStream(Paths.get("document.json"))) {
 *     LLSD document = parser.parse(input);
 *     Object content = document.getContent();
 *     // Process the content...
 * }
 * }</pre>
 * 
 * @since 1.0
 * @see LLSD
 * @see LLSDParser
 */
public class LLSDJsonParser {
    private final DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final Pattern uuidPattern = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", Pattern.CASE_INSENSITIVE);

    /**
     * Constructs a new LLSD JSON parser.
     */
    public LLSDJsonParser() {
        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Parses an LLSD document from the given JSON input stream.
     *
     * @param jsonInput the JSON input stream to read and parse as LLSD.
     * @return the parsed LLSD document
     * @throws IOException if there was a problem reading from the input stream.
     * @throws LLSDException if the document is valid JSON, but invalid LLSD.
     */
    public LLSD parse(final InputStream jsonInput) throws IOException, LLSDException {
        // Read entire stream into string for simpler parsing
        StringBuilder sb = new StringBuilder();
        try (Reader reader = new InputStreamReader(jsonInput, StandardCharsets.UTF_8)) {
            char[] buffer = new char[1024];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, charsRead);
            }
        }
        
        String jsonString = sb.toString().trim();
        JsonTokenizer tokenizer = new JsonTokenizer(jsonString);
        Object parsedJson = parseJsonValue(tokenizer);
        Object llsdContent = convertJsonToLlsd(parsedJson);
        return new LLSD(llsdContent);
    }

    /**
     * Simple tokenizer for JSON parsing.
     */
    private static class JsonTokenizer {
        private final String json;
        private int position = 0;

        public JsonTokenizer(String json) {
            this.json = json;
        }

        public void skipWhitespace() {
            while (position < json.length() && Character.isWhitespace(json.charAt(position))) {
                position++;
            }
        }

        public char peek() throws LLSDException {
            skipWhitespace();
            if (position >= json.length()) {
                throw new LLSDException("Unexpected end of JSON input");
            }
            return json.charAt(position);
        }

        public char consume() throws LLSDException {
            skipWhitespace();
            if (position >= json.length()) {
                throw new LLSDException("Unexpected end of JSON input");
            }
            return json.charAt(position++);
        }

        public void expect(char expected) throws LLSDException {
            char actual = consume();
            if (actual != expected) {
                throw new LLSDException("Expected '" + expected + "' but got '" + actual + "'");
            }
        }

        public boolean hasMore() {
            skipWhitespace();
            return position < json.length();
        }

        public String consumeString() throws LLSDException {
            expect('"');
            StringBuilder sb = new StringBuilder();
            
            while (position < json.length()) {
                char c = json.charAt(position++);
                if (c == '"') {
                    return sb.toString();
                } else if (c == '\\') {
                    if (position >= json.length()) {
                        throw new LLSDException("Unterminated string escape");
                    }
                    c = json.charAt(position++);
                    switch (c) {
                        case '"':
                        case '\\':
                        case '/':
                            sb.append(c);
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            // Unicode escape
                            if (position + 4 > json.length()) {
                                throw new LLSDException("Invalid unicode escape");
                            }
                            String hex = json.substring(position, position + 4);
                            position += 4;
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                            } catch (NumberFormatException e) {
                                throw new LLSDException("Invalid unicode escape: " + hex);
                            }
                            break;
                        default:
                            throw new LLSDException("Invalid escape character: " + c);
                    }
                } else {
                    sb.append(c);
                }
            }
            
            throw new LLSDException("Unterminated string");
        }

        public Object consumeNumber() throws LLSDException {
            StringBuilder sb = new StringBuilder();
            boolean hasDecimal = false;
            
            while (position < json.length()) {
                char c = json.charAt(position);
                if (Character.isDigit(c) || c == '-' || c == '+') {
                    sb.append(c);
                    position++;
                } else if (c == '.' && !hasDecimal) {
                    hasDecimal = true;
                    sb.append(c);
                    position++;
                } else if (c == 'e' || c == 'E') {
                    sb.append(c);
                    position++;
                } else {
                    break;
                }
            }

            String numStr = sb.toString();
            try {
                if (hasDecimal || numStr.contains("e") || numStr.contains("E")) {
                    return Double.valueOf(numStr);
                } else {
                    return Integer.valueOf(numStr);
                }
            } catch (NumberFormatException e) {
                throw new LLSDException("Invalid number format: " + numStr, e);
            }
        }

        public Object consumeLiteral() throws LLSDException {
            StringBuilder sb = new StringBuilder();
            while (position < json.length() && Character.isLetter(json.charAt(position))) {
                sb.append(json.charAt(position++));
            }

            String literal = sb.toString();
            switch (literal) {
                case "true":
                    return Boolean.TRUE;
                case "false":
                    return Boolean.FALSE;
                case "null":
                    return null;
                default:
                    throw new LLSDException("Invalid literal: " + literal);
            }
        }
    }

    /**
     * Simple JSON parser implementation for LLSD purposes.
     * Note: This is a basic implementation. For production use, consider using a 
     * proper JSON library like Jackson or Gson.
     */
    private Object parseJsonValue(JsonTokenizer tokenizer) throws LLSDException {
        char ch = tokenizer.peek();

        switch (ch) {
            case '{':
                return parseJsonObject(tokenizer);
            case '[':
                return parseJsonArray(tokenizer);
            case '"':
                return tokenizer.consumeString();
            case 't':
            case 'f':
            case 'n':
                return tokenizer.consumeLiteral();
            default:
                if (Character.isDigit(ch) || ch == '-') {
                    return tokenizer.consumeNumber();
                }
                throw new LLSDException("Unexpected character in JSON: " + ch);
        }
    }

    private Map<String, Object> parseJsonObject(JsonTokenizer tokenizer) throws LLSDException {
        Map<String, Object> map = new HashMap<>();
        tokenizer.expect('{');

        if (tokenizer.peek() == '}') {
            tokenizer.consume(); // consume '}'
            return map; // Empty object
        }

        while (true) {
            // Parse key
            String key = tokenizer.consumeString();
            tokenizer.expect(':');
            Object value = parseJsonValue(tokenizer);
            map.put(key, value);

            char next = tokenizer.peek();
            if (next == '}') {
                tokenizer.consume();
                break;
            } else if (next == ',') {
                tokenizer.consume();
                continue;
            } else {
                throw new LLSDException("Expected ',' or '}' in object, got: " + next);
            }
        }

        return map;
    }

    private List<Object> parseJsonArray(JsonTokenizer tokenizer) throws LLSDException {
        List<Object> list = new ArrayList<>();
        tokenizer.expect('[');

        if (tokenizer.peek() == ']') {
            tokenizer.consume(); // consume ']'
            return list; // Empty array
        }

        while (true) {
            Object value = parseJsonValue(tokenizer);
            list.add(value);

            char next = tokenizer.peek();
            if (next == ']') {
                tokenizer.consume();
                break;
            } else if (next == ',') {
                tokenizer.consume();
                continue;
            } else {
                throw new LLSDException("Expected ',' or ']' in array, got: " + next);
            }
        }

        return list;
    }

    /**
     * Converts a parsed JSON object to LLSD format by interpreting special prefixed objects.
     */
    private Object convertJsonToLlsd(Object jsonObj) throws LLSDException {
        if (jsonObj == null) {
            return "";  // LLSD represents undefined as empty string
        }
        
        if (jsonObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = (Map<String, Object>) jsonObj;
            
            // Check for LLSD type indicators
            if (jsonMap.size() == 1) {
                Map.Entry<String, Object> entry = jsonMap.entrySet().iterator().next();
                String key = entry.getKey();
                Object value = entry.getValue();
                
                switch (key) {
                    case "d": // Date
                        if (value instanceof String) {
                            return parseDate((String) value);
                        }
                        break;
                    case "u": // URI
                        if (value instanceof String) {
                            return parseUri((String) value);
                        }
                        break;
                    case "i": // UUID
                        if (value instanceof String) {
                            return parseUuid((String) value);
                        }
                        break;
                    case "b": // Binary (base64)
                        if (value instanceof String) {
                            return parseBinary((String) value);
                        }
                        break;
                }
            }
            
            // Regular map - convert all values recursively
            Map<String, Object> llsdMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                llsdMap.put(entry.getKey(), convertJsonToLlsd(entry.getValue()));
            }
            return llsdMap;
        }
        
        if (jsonObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> jsonList = (List<Object>) jsonObj;
            List<Object> llsdList = new ArrayList<>();
            for (Object item : jsonList) {
                llsdList.add(convertJsonToLlsd(item));
            }
            return llsdList;
        }
        
        // Primitive types remain as-is
        return jsonObj;
    }

    private Date parseDate(String dateStr) throws LLSDException {
        try {
            return iso8601Format.parse(dateStr);
        } catch (ParseException e) {
            throw new LLSDException("Invalid date format: " + dateStr, e);
        }
    }

    private URI parseUri(String uriStr) throws LLSDException {
        try {
            return new URI(uriStr);
        } catch (URISyntaxException e) {
            throw new LLSDException("Invalid URI format: " + uriStr, e);
        }
    }

    private UUID parseUuid(String uuidStr) throws LLSDException {
        if (!uuidPattern.matcher(uuidStr).matches()) {
            throw new LLSDException("Invalid UUID format: " + uuidStr);
        }
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            throw new LLSDException("Invalid UUID: " + uuidStr, e);
        }
    }

    private byte[] parseBinary(String base64Str) throws LLSDException {
        try {
            return Base64.getDecoder().decode(base64Str);
        } catch (IllegalArgumentException e) {
            throw new LLSDException("Invalid base64 data: " + base64Str, e);
        }
    }
}