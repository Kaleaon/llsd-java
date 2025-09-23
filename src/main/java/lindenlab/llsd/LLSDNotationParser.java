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
 * Parser for LLSD documents in Notation format.
 * 
 * <p>This parser converts notation-formatted LLSD documents into Java objects.
 * The notation format is a compact, human-readable text representation of LLSD data.</p>
 * 
 * <p>Notation format examples:
 * <ul>
 * <li>Boolean: {@code 1} (true), {@code 0} (false), {@code TRUE}, {@code FALSE}</li>
 * <li>Integer: {@code i42} (integer 42)</li>
 * <li>Real: {@code r3.14159} (real number)</li>
 * <li>String: {@code s'hello world'} or {@code s"hello world"}</li>
 * <li>UUID: {@code u550e8400-e29b-41d4-a716-446655440000}</li>
 * <li>Date: {@code d2024-01-01T00:00:00Z}</li>
 * <li>URI: {@code lhttp://example.com}</li>
 * <li>Binary: {@code b64"SGVsbG8="} (base64 encoded)</li>
 * <li>Array: {@code [i1,i2,i3]}</li>
 * <li>Map: {@code {key:s'value',num:i42}}</li>
 * <li>Undefined: {@code !}</li>
 * </ul></p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * LLSDNotationParser parser = new LLSDNotationParser();
 * try (InputStream input = Files.newInputStream(Paths.get("document.llsd"))) {
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
public class LLSDNotationParser {
    private final DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final Pattern uuidPattern = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", Pattern.CASE_INSENSITIVE);

    /**
     * Constructs a new LLSD Notation parser.
     */
    public LLSDNotationParser() {
        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Parses an LLSD document from the given notation input stream.
     *
     * @param notationInput the notation input stream to read and parse as LLSD.
     * @return the parsed LLSD document
     * @throws IOException if there was a problem reading from the input stream.
     * @throws LLSDException if the document is valid notation, but invalid LLSD.
     */
    public LLSD parse(final InputStream notationInput) throws IOException, LLSDException {
        // Read entire stream into string for simpler parsing
        StringBuilder sb = new StringBuilder();
        try (Reader reader = new InputStreamReader(notationInput, StandardCharsets.UTF_8)) {
            char[] buffer = new char[1024];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, charsRead);
            }
        }
        
        String notationString = sb.toString().trim();
        NotationTokenizer tokenizer = new NotationTokenizer(notationString);
        Object parsedNotation = parseNotationValue(tokenizer);
        return new LLSD(parsedNotation);
    }

    /**
     * Simple tokenizer for Notation parsing.
     */
    private static class NotationTokenizer {
        private final String notation;
        private int position = 0;

        public NotationTokenizer(String notation) {
            this.notation = notation;
        }

        public void skipWhitespace() {
            while (position < notation.length() && Character.isWhitespace(notation.charAt(position))) {
                position++;
            }
        }

        public char peek() throws LLSDException {
            skipWhitespace();
            if (position >= notation.length()) {
                throw new LLSDException("Unexpected end of notation input");
            }
            return notation.charAt(position);
        }

        public char consume() throws LLSDException {
            skipWhitespace();
            if (position >= notation.length()) {
                throw new LLSDException("Unexpected end of notation input");
            }
            return notation.charAt(position++);
        }

        public void expect(char expected) throws LLSDException {
            char actual = consume();
            if (actual != expected) {
                throw new LLSDException("Expected '" + expected + "' but got '" + actual + "'");
            }
        }

        public boolean hasMore() {
            skipWhitespace();
            return position < notation.length();
        }

        public String consumeString(char delimiter) throws LLSDException {
            // Note: delimiter should already be consumed by caller
            StringBuilder sb = new StringBuilder();
            
            while (position < notation.length()) {
                char c = notation.charAt(position++);
                if (c == delimiter) {
                    return sb.toString();
                } else if (c == '\\') {
                    if (position >= notation.length()) {
                        throw new LLSDException("Unterminated string escape");
                    }
                    c = notation.charAt(position++);
                    switch (c) {
                        case '"':
                        case '\'':
                        case '\\':
                            sb.append(c);
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
                        default:
                            sb.append('\\').append(c); // Keep original if not recognized
                            break;
                    }
                } else {
                    sb.append(c);
                }
            }
            
            throw new LLSDException("Unterminated string");
        }

        public Object consumeNumber(char typeMarker) throws LLSDException {
            StringBuilder sb = new StringBuilder();
            boolean hasDecimal = false;
            
            while (position < notation.length()) {
                char c = notation.charAt(position);
                if (Character.isDigit(c) || c == '-' || c == '+') {
                    sb.append(c);
                    position++;
                } else if (c == '.' && !hasDecimal && typeMarker == 'r') {
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
                if (typeMarker == 'i') {
                    return Integer.valueOf(numStr);
                } else if (typeMarker == 'r') {
                    return Double.valueOf(numStr);
                } else {
                    throw new LLSDException("Unknown number type marker: " + typeMarker);
                }
            } catch (NumberFormatException e) {
                throw new LLSDException("Invalid number format: " + numStr, e);
            }
        }

        public String consumeUntil(char... delimiters) {
            StringBuilder sb = new StringBuilder();
            Set<Character> delimiterSet = new HashSet<>();
            for (char delimiter : delimiters) {
                delimiterSet.add(delimiter);
            }
            
            while (position < notation.length()) {
                char c = notation.charAt(position);
                if (delimiterSet.contains(c) || Character.isWhitespace(c)) {
                    break;
                }
                sb.append(c);
                position++;
            }
            
            return sb.toString();
        }
    }

    private Object parseNotationValue(NotationTokenizer tokenizer) throws LLSDException {
        if (!tokenizer.hasMore()) {
            throw new LLSDException("Expected value but found end of input");
        }

        char ch = tokenizer.peek();

        switch (ch) {
            case '!':
                tokenizer.consume(); // consume '!'
                return ""; // Undefined value represented as empty string
            case '1':
            case 't':
            case 'T':
                return parseBoolean(tokenizer, true);
            case '0':
            case 'f':
            case 'F':
                return parseBoolean(tokenizer, false);
            case 'i':
                tokenizer.consume(); // consume 'i'
                return tokenizer.consumeNumber('i');
            case 'r':
                tokenizer.consume(); // consume 'r'
                return tokenizer.consumeNumber('r');
            case 's':
                return parseString(tokenizer);
            case 'u':
                return parseUUID(tokenizer);
            case 'd':
                return parseDate(tokenizer);
            case 'l':
                return parseURI(tokenizer);
            case 'b':
                return parseBinary(tokenizer);
            case '[':
                return parseArray(tokenizer);
            case '{':
                return parseMap(tokenizer);
            default:
                throw new LLSDException("Unexpected character in notation: " + ch);
        }
    }

    private Boolean parseBoolean(NotationTokenizer tokenizer, boolean expectedValue) throws LLSDException {
        char ch = tokenizer.consume();
        
        if (ch == '1' && expectedValue) {
            return Boolean.TRUE;
        } else if (ch == '0' && !expectedValue) {
            return Boolean.FALSE;
        } else if (ch == 't' || ch == 'T') {
            // Check for full word
            String remaining = tokenizer.consumeUntil(',', ']', '}', ' ', '\t', '\n', '\r');
            String fullWord = ch + remaining;
            if (fullWord.equalsIgnoreCase("true")) {
                return Boolean.TRUE;
            } else {
                throw new LLSDException("Invalid boolean value: " + fullWord);
            }
        } else if (ch == 'f' || ch == 'F') {
            // Check for full word
            String remaining = tokenizer.consumeUntil(',', ']', '}', ' ', '\t', '\n', '\r');
            String fullWord = ch + remaining;
            if (fullWord.equalsIgnoreCase("false")) {
                return Boolean.FALSE;
            } else {
                throw new LLSDException("Invalid boolean value: " + fullWord);
            }
        }
        
        throw new LLSDException("Invalid boolean notation: " + ch);
    }

    private String parseString(NotationTokenizer tokenizer) throws LLSDException {
        tokenizer.expect('s'); // consume 's'
        char delimiter = tokenizer.peek();
        if (delimiter == '\'' || delimiter == '"') {
            tokenizer.consume(); // consume the delimiter
            return tokenizer.consumeString(delimiter);
        } else {
            throw new LLSDException("Expected string delimiter (' or \") after 's' but got: " + delimiter);
        }
    }

    private UUID parseUUID(NotationTokenizer tokenizer) throws LLSDException {
        tokenizer.expect('u'); // consume 'u'
        String uuidStr = tokenizer.consumeUntil(',', ']', '}', ' ', '\t', '\n', '\r');
        
        if (!uuidPattern.matcher(uuidStr).matches()) {
            throw new LLSDException("Invalid UUID format: " + uuidStr);
        }
        
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            throw new LLSDException("Invalid UUID: " + uuidStr, e);
        }
    }

    private Date parseDate(NotationTokenizer tokenizer) throws LLSDException {
        tokenizer.expect('d'); // consume 'd'
        String dateStr = tokenizer.consumeUntil(',', ']', '}', ' ', '\t', '\n', '\r');
        
        try {
            return iso8601Format.parse(dateStr);
        } catch (ParseException e) {
            throw new LLSDException("Invalid date format: " + dateStr, e);
        }
    }

    private URI parseURI(NotationTokenizer tokenizer) throws LLSDException {
        tokenizer.expect('l'); // consume 'l'
        String uriStr = tokenizer.consumeUntil(',', ']', '}', ' ', '\t', '\n', '\r');
        
        try {
            return new URI(uriStr);
        } catch (URISyntaxException e) {
            throw new LLSDException("Invalid URI format: " + uriStr, e);
        }
    }

    private byte[] parseBinary(NotationTokenizer tokenizer) throws LLSDException {
        tokenizer.expect('b'); // consume 'b'
        
        // Check for size specification b64"data" or b(size)"data"
        char next = tokenizer.peek();
        if (Character.isDigit(next)) {
            // Handle b64"data" format
            String sizeStr = tokenizer.consumeUntil('"', '\'');
            if (sizeStr.equals("64")) {
                char delimiter = tokenizer.consume(); // consume quote
                if (delimiter == '"' || delimiter == '\'') {
                    String base64Data = tokenizer.consumeString(delimiter);
                    try {
                        return Base64.getDecoder().decode(base64Data);
                    } catch (IllegalArgumentException e) {
                        throw new LLSDException("Invalid base64 data: " + base64Data, e);
                    }
                } else {
                    throw new LLSDException("Expected quote delimiter after b64 but got: " + delimiter);
                }
            } else {
                throw new LLSDException("Unsupported binary size specification: " + sizeStr);
            }
        } else if (next == '(') {
            // Handle b(size)"data" format  
            tokenizer.consume(); // consume '('
            String sizeStr = tokenizer.consumeUntil(')');
            tokenizer.expect(')');
            int size = Integer.parseInt(sizeStr);
            
            char delimiter = tokenizer.consume(); // should be quote
            if (delimiter == '"' || delimiter == '\'') {
                String binaryData = tokenizer.consumeString(delimiter);
                
                if (size != binaryData.length()) {
                    throw new LLSDException("Binary size mismatch: expected " + size + " but got " + binaryData.length());
                }
                
                return binaryData.getBytes(StandardCharsets.UTF_8);
            } else {
                throw new LLSDException("Expected quote delimiter after size specification but got: " + delimiter);
            }
        } else {
            throw new LLSDException("Invalid binary notation format - expected digit or '(' after 'b' but got: " + next);
        }
    }

    private List<Object> parseArray(NotationTokenizer tokenizer) throws LLSDException {
        List<Object> list = new ArrayList<>();
        tokenizer.expect('[');

        if (tokenizer.peek() == ']') {
            tokenizer.consume(); // consume ']'
            return list; // Empty array
        }

        while (true) {
            Object value = parseNotationValue(tokenizer);
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

    private Map<String, Object> parseMap(NotationTokenizer tokenizer) throws LLSDException {
        Map<String, Object> map = new HashMap<>();
        tokenizer.expect('{');

        if (tokenizer.peek() == '}') {
            tokenizer.consume(); // consume '}'
            return map; // Empty map
        }

        while (true) {
            // Parse key (should be an identifier or string)
            String key;
            char keyStart = tokenizer.peek();
            if (keyStart == 's') {
                // Could be a string key or an identifier starting with 's'
                // Look ahead to see if next char is a quote
                int savedPosition = tokenizer.position;
                tokenizer.consume(); // consume 's'
                if (tokenizer.position < tokenizer.notation.length()) {
                    char nextChar = tokenizer.notation.charAt(tokenizer.position);
                    if (nextChar == '\'' || nextChar == '"') {
                        // It's a string key
                        tokenizer.consume(); // consume delimiter
                        key = tokenizer.consumeString(nextChar);
                    } else {
                        // It's an identifier starting with 's', backtrack
                        tokenizer.position = savedPosition;
                        key = tokenizer.consumeUntil(':', ' ', '\t', '\n', '\r');
                    }
                } else {
                    // End of input, backtrack
                    tokenizer.position = savedPosition;
                    key = tokenizer.consumeUntil(':', ' ', '\t', '\n', '\r');
                }
            } else if (Character.isLetter(keyStart) || keyStart == '_') {
                // Regular identifier key
                key = tokenizer.consumeUntil(':', ' ', '\t', '\n', '\r');
            } else {
                throw new LLSDException("Invalid map key format, got: " + keyStart);
            }

            tokenizer.expect(':');
            Object value = parseNotationValue(tokenizer);
            map.put(key, value);

            char next = tokenizer.peek();
            if (next == '}') {
                tokenizer.consume();
                break;
            } else if (next == ',') {
                tokenizer.consume();
                continue;
            } else {
                throw new LLSDException("Expected ',' or '}' in map, got: " + next);
            }
        }

        return map;
    }
}