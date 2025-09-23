/*
 * LLSD Serialization Framework - Java implementation based on Second Life/Firestorm C++ code
 *
 * Converted from indra/llcommon/llsdserialize.h/cpp  
 * Copyright (C) 2010, Linden Research, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer;

import lindenlab.llsd.LLSD;
import lindenlab.llsd.LLSDException;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Abstract base class for LLSD serialization, converted from C++ LLSDSerialize.
 * 
 * <p>This class provides the framework for serializing and deserializing LLSD data
 * in various formats, with advanced features from the Second Life viewer implementation
 * including size limits, depth limits, and line-based parsing.</p>
 * 
 * <p>Key enhancements over basic LLSD serialization:</p>
 * <ul>
 * <li>Configurable byte and depth limits for security</li>
 * <li>Line-based parsing for better XML handling</li>
 * <li>Reset functionality for parser reuse</li>
 * <li>Advanced error handling and recovery</li>
 * </ul>
 * 
 * @since 1.0
 * @see LLSD
 */
public abstract class LLSDViewerSerializer {
    
    /**
     * Parser failure constant.
     */
    public static final int PARSE_FAILURE = -1;
    
    /**
     * Unlimited size constant.
     */
    public static final long SIZE_UNLIMITED = -1;
    
    /**
     * Default maximum parsing depth.
     */
    public static final int DEFAULT_MAX_DEPTH = 64;
    
    protected boolean checkLimits = true;
    protected long maxBytesLeft = SIZE_UNLIMITED;
    protected boolean parseLines = false;
    
    /**
     * Parse LLSD from an InputStream with limits.
     * Equivalent to C++ LLSDParser::parse()
     * 
     * @param input the input stream
     * @param maxBytes maximum bytes to read (SIZE_UNLIMITED for no limit)
     * @param maxDepth maximum nesting depth (-1 for unlimited)
     * @return parsed LLSD object
     * @throws IOException if I/O error occurs
     * @throws LLSDException if parsing fails
     */
    public LLSD parse(InputStream input, long maxBytes, int maxDepth) throws IOException, LLSDException {
        this.maxBytesLeft = maxBytes;
        this.checkLimits = (maxBytes != SIZE_UNLIMITED);
        
        try {
            Object content = doParse(input, maxDepth == -1 ? DEFAULT_MAX_DEPTH : maxDepth);
            return new LLSD(content);
        } catch (Exception e) {
            if (e instanceof LLSDException) {
                throw e;
            } else if (e instanceof IOException) {
                throw e;
            } else {
                throw new LLSDException("Parse error: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Parse LLSD from an InputStream with default limits.
     * 
     * @param input the input stream
     * @return parsed LLSD object
     * @throws IOException if I/O error occurs
     * @throws LLSDException if parsing fails
     */
    public LLSD parse(InputStream input) throws IOException, LLSDException {
        return parse(input, SIZE_UNLIMITED, -1);
    }
    
    /**
     * Parse LLSD using line-based reading (better for XML).
     * Equivalent to C++ LLSDParser::parseLines()
     * 
     * @param input the input stream
     * @return parsed LLSD object
     * @throws IOException if I/O error occurs
     * @throws LLSDException if parsing fails
     */
    public LLSD parseLines(InputStream input) throws IOException, LLSDException {
        this.parseLines = true;
        try {
            return parse(input);
        } finally {
            this.parseLines = false;
        }
    }
    
    /**
     * Reset the parser for reuse.
     * Equivalent to C++ LLSDParser::reset()
     */
    public void reset() {
        doReset();
        maxBytesLeft = SIZE_UNLIMITED;
        checkLimits = true;
        parseLines = false;
    }
    
    /**
     * Abstract method for actual parsing implementation.
     * Subclasses must implement this method.
     * 
     * @param input the input stream
     * @param maxDepth maximum nesting depth
     * @return parsed LLSD content
     * @throws IOException if I/O error occurs
     * @throws LLSDException if parsing fails
     */
    protected abstract Object doParse(InputStream input, int maxDepth) throws IOException, LLSDException;
    
    /**
     * Reset method for subclasses to override.
     * Default implementation does nothing.
     */
    protected void doReset() {
        // Default: no-op
    }
    
    /**
     * Helper method to read a single byte with limit checking.
     * Equivalent to C++ LLSDParser::get()
     * 
     * @param input the input stream
     * @return the byte read, or -1 if EOF
     * @throws IOException if I/O error occurs
     * @throws LLSDException if byte limit exceeded
     */
    protected int getByte(InputStream input) throws IOException, LLSDException {
        if (checkLimits && maxBytesLeft <= 0) {
            throw new LLSDException("Byte limit exceeded during parsing");
        }
        
        int b = input.read();
        if (b != -1 && checkLimits) {
            maxBytesLeft--;
        }
        return b;
    }
    
    /**
     * Helper method to read bytes into a buffer with limit checking.
     * Equivalent to C++ LLSDParser::read()
     * 
     * @param input the input stream
     * @param buffer the buffer to read into
     * @param offset offset in buffer
     * @param length number of bytes to read
     * @return number of bytes actually read
     * @throws IOException if I/O error occurs
     * @throws LLSDException if byte limit exceeded
     */
    protected int readBytes(InputStream input, byte[] buffer, int offset, int length) throws IOException, LLSDException {
        if (checkLimits && maxBytesLeft <= 0) {
            throw new LLSDException("Byte limit exceeded during parsing");
        }
        
        if (checkLimits && length > maxBytesLeft) {
            length = (int) maxBytesLeft;
        }
        
        int bytesRead = input.read(buffer, offset, length);
        if (bytesRead > 0 && checkLimits) {
            maxBytesLeft -= bytesRead;
        }
        return bytesRead;
    }
    
    /**
     * Account for bytes read outside of helper methods.
     * Equivalent to C++ LLSDParser::account()
     * 
     * @param bytes number of bytes consumed
     * @throws LLSDException if byte limit exceeded
     */
    protected void accountBytes(long bytes) throws LLSDException {
        if (checkLimits) {
            maxBytesLeft -= bytes;
            if (maxBytesLeft < 0) {
                throw new LLSDException("Byte limit exceeded during parsing");
            }
        }
    }
    
    /**
     * Check if parsing depth limit is exceeded.
     * 
     * @param currentDepth current nesting depth
     * @param maxDepth maximum allowed depth
     * @throws LLSDException if depth limit exceeded
     */
    protected void checkDepthLimit(int currentDepth, int maxDepth) throws LLSDException {
        if (maxDepth > 0 && currentDepth > maxDepth) {
            throw new LLSDException("Maximum parsing depth exceeded: " + currentDepth + " > " + maxDepth);
        }
    }
    
    /**
     * Utility method to read a string with length prefix.
     * Common pattern in LLSD binary formats.
     * 
     * @param input the input stream
     * @return the string read
     * @throws IOException if I/O error occurs
     * @throws LLSDException if parsing fails
     */
    protected String readLengthPrefixedString(InputStream input) throws IOException, LLSDException {
        // Read 4-byte length prefix (big-endian)
        byte[] lengthBytes = new byte[4];
        if (readBytes(input, lengthBytes, 0, 4) != 4) {
            throw new LLSDException("Unexpected EOF reading string length");
        }
        
        int length = ((lengthBytes[0] & 0xFF) << 24) |
                    ((lengthBytes[1] & 0xFF) << 16) |
                    ((lengthBytes[2] & 0xFF) << 8) |
                    (lengthBytes[3] & 0xFF);
        
        if (length < 0) {
            throw new LLSDException("Invalid string length: " + length);
        }
        
        if (length == 0) {
            return "";
        }
        
        byte[] stringBytes = new byte[length];
        if (readBytes(input, stringBytes, 0, length) != length) {
            throw new LLSDException("Unexpected EOF reading string data");
        }
        
        return new String(stringBytes, StandardCharsets.UTF_8);
    }
    
    /**
     * Utility method to skip whitespace characters.
     * 
     * @param input the input stream
     * @return first non-whitespace character, or -1 if EOF
     * @throws IOException if I/O error occurs
     * @throws LLSDException if byte limit exceeded
     */
    protected int skipWhitespace(InputStream input) throws IOException, LLSDException {
        int ch;
        do {
            ch = getByte(input);
        } while (ch != -1 && Character.isWhitespace(ch));
        return ch;
    }
    
    /**
     * Utility method to read until a specific character or EOF.
     * 
     * @param input the input stream
     * @param delimiter the delimiter character
     * @return string read (not including delimiter)
     * @throws IOException if I/O error occurs
     * @throws LLSDException if byte limit exceeded
     */
    protected String readUntil(InputStream input, char delimiter) throws IOException, LLSDException {
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = getByte(input)) != -1 && ch != delimiter) {
            sb.append((char) ch);
        }
        return sb.toString();
    }
}

/**
 * Enhanced LLSD serialization utilities, converted from C++ LLSDSerialize class.
 * 
 * <p>This class provides static utility methods for common serialization operations
 * with advanced features from the viewer implementation.</p>
 */
abstract class LLSDViewerSerializationUtils {
    
    /**
     * Supported LLSD serialization formats from viewer.
     */
    public enum SerializationFormat {
        LLSD_BINARY,
        LLSD_XML, 
        LLSD_NOTATION,
        LLSD_JSON  // Extended format
    }
    
    /**
     * Serialize LLSD to output stream in specified format.
     * 
     * @param llsd the LLSD object to serialize
     * @param output the output stream
     * @param format the serialization format
     * @throws IOException if I/O error occurs
     * @throws LLSDException if serialization fails
     */
    public static void serialize(LLSD llsd, OutputStream output, SerializationFormat format) 
            throws IOException, LLSDException {
        switch (format) {
            case LLSD_XML:
                serializeXML(llsd, output);
                break;
            case LLSD_BINARY:
                serializeBinary(llsd, output);
                break;
            case LLSD_NOTATION:
                serializeNotation(llsd, output);
                break;
            case LLSD_JSON:
                serializeJSON(llsd, output);
                break;
            default:
                throw new LLSDException("Unsupported serialization format: " + format);
        }
    }
    
    /**
     * Auto-detect format and deserialize LLSD from input stream.
     * 
     * @param input the input stream
     * @param maxBytes maximum bytes to read
     * @return deserialized LLSD object
     * @throws IOException if I/O error occurs
     * @throws LLSDException if deserialization fails
     */
    public static LLSD deserialize(InputStream input, long maxBytes) throws IOException, LLSDException {
        // Use mark/reset to peek at the beginning
        if (!input.markSupported()) {
            input = new BufferedInputStream(input);
        }
        
        input.mark(16);
        byte[] header = new byte[16];
        int headerLen = input.read(header);
        input.reset();
        
        if (headerLen == 0) {
            throw new LLSDException("Empty input stream");
        }
        
        // Detect format from header
        String headerStr = new String(header, 0, Math.min(headerLen, 16), StandardCharsets.UTF_8);
        
        if (headerStr.startsWith("<?xml") || headerStr.startsWith("<llsd>")) {
            return deserializeXML(input, maxBytes);
        } else if (headerStr.startsWith("[") || headerStr.startsWith("{") || headerStr.startsWith("'") || 
                   headerStr.startsWith("\"") || Character.isDigit(headerStr.charAt(0))) {
            return deserializeNotation(input, maxBytes);
        } else if (headerStr.startsWith("{\"") || headerStr.startsWith("[{")) {
            return deserializeJSON(input, maxBytes);
        } else {
            // Assume binary format
            return deserializeBinary(input, maxBytes);
        }
    }
    
    private static void serializeXML(LLSD llsd, OutputStream output) throws IOException, LLSDException {
        // Use existing XML serializer (placeholder - would use actual implementation)
        throw new LLSDException("XML serialization not yet implemented in viewer utils");
    }
    
    private static void serializeBinary(LLSD llsd, OutputStream output) throws IOException, LLSDException {
        // Use existing binary serializer (placeholder - would use actual implementation)  
        throw new LLSDException("Binary serialization not yet implemented in viewer utils");
    }
    
    private static void serializeNotation(LLSD llsd, OutputStream output) throws IOException, LLSDException {
        // Use existing notation serializer (placeholder - would use actual implementation)
        throw new LLSDException("Notation serialization not yet implemented in viewer utils");
    }
    
    private static void serializeJSON(LLSD llsd, OutputStream output) throws IOException, LLSDException {
        // Use existing JSON serializer (placeholder - would use actual implementation)
        throw new LLSDException("JSON serialization not yet implemented in viewer utils");
    }
    
    private static LLSD deserializeXML(InputStream input, long maxBytes) throws IOException, LLSDException {
        // Use existing XML parser (placeholder - would use actual implementation)
        throw new LLSDException("XML deserialization not yet implemented in viewer utils");
    }
    
    private static LLSD deserializeBinary(InputStream input, long maxBytes) throws IOException, LLSDException {
        // Use existing binary parser (placeholder - would use actual implementation)
        throw new LLSDException("Binary deserialization not yet implemented in viewer utils");
    }
    
    private static LLSD deserializeNotation(InputStream input, long maxBytes) throws IOException, LLSDException {
        // Use existing notation parser (placeholder - would use actual implementation)
        throw new LLSDException("Notation deserialization not yet implemented in viewer utils");
    }
    
    private static LLSD deserializeJSON(InputStream input, long maxBytes) throws IOException, LLSDException {
        // Use existing JSON parser (placeholder - would use actual implementation)
        throw new LLSDException("JSON deserialization not yet implemented in viewer utils");
    }
}