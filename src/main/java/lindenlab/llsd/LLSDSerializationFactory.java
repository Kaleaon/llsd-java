/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews
 * Updated 2024 based on Second Life viewer and LibreMetaverse implementations
 */

package lindenlab.llsd;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for LLSD serializers and deserializers
 */
public class LLSDSerializationFactory {
    
    private static final Map<LLSDFormat, LLSDSerializer> serializers = new HashMap<>();
    
    static {
        serializers.put(LLSDFormat.XML, new LLSDXMLSerializer());
        serializers.put(LLSDFormat.NOTATION, new LLSDNotationSerializer());
        serializers.put(LLSDFormat.BINARY, new LLSDBinarySerializer());
    }
    
    /**
     * Get a serializer for the specified format
     * @param format the LLSD format
     * @return the serializer
     */
    public static LLSDSerializer getSerializer(LLSDFormat format) {
        return serializers.get(format);
    }
    
    /**
     * Serialize LLSD to a string using the specified format
     * @param llsd the LLSD object
     * @param format the format to use
     * @return serialized string
     * @throws LLSDException if serialization fails
     */
    public static String serialize(LLSD llsd, LLSDFormat format) throws LLSDException {
        LLSDSerializer serializer = getSerializer(format);
        if (serializer == null) {
            throw new LLSDException("Unsupported format: " + format);
        }
        return serializer.serializeToString(llsd);
    }
    
    /**
     * Serialize LLSD to an output stream using the specified format
     * @param llsd the LLSD object
     * @param format the format to use
     * @param output the output stream
     * @throws IOException if writing fails
     * @throws LLSDException if serialization fails
     */
    public static void serialize(LLSD llsd, LLSDFormat format, OutputStream output) throws IOException, LLSDException {
        LLSDSerializer serializer = getSerializer(format);
        if (serializer == null) {
            throw new LLSDException("Unsupported format: " + format);
        }
        serializer.serialize(llsd, output);
    }
    
    /**
     * Deserialize LLSD from a string, auto-detecting the format
     * @param data the string data
     * @return the LLSD object
     * @throws LLSDException if parsing fails
     */
    public static LLSD deserialize(String data) throws LLSDException {
        LLSDFormat format = detectFormat(data);
        LLSDSerializer serializer = getSerializer(format);
        return serializer.deserializeFromString(data);
    }
    
    /**
     * Deserialize LLSD from a string using the specified format
     * @param data the string data
     * @param format the format to use
     * @return the LLSD object
     * @throws LLSDException if parsing fails
     */
    public static LLSD deserialize(String data, LLSDFormat format) throws LLSDException {
        LLSDSerializer serializer = getSerializer(format);
        if (serializer == null) {
            throw new LLSDException("Unsupported format: " + format);
        }
        return serializer.deserializeFromString(data);
    }
    
    /**
     * Deserialize LLSD from an input stream, auto-detecting the format
     * @param input the input stream
     * @return the LLSD object
     * @throws IOException if reading fails
     * @throws LLSDException if parsing fails
     */
    public static LLSD deserialize(InputStream input) throws IOException, LLSDException {
        // Need buffered input stream for format detection
        BufferedInputStream bis;
        if (input instanceof BufferedInputStream) {
            bis = (BufferedInputStream) input;
        } else {
            bis = new BufferedInputStream(input);
        }
        
        LLSDFormat format = detectFormat(bis);
        LLSDSerializer serializer = getSerializer(format);
        return serializer.deserialize(bis);
    }
    
    /**
     * Deserialize LLSD from an input stream using the specified format
     * @param input the input stream
     * @param format the format to use
     * @return the LLSD object
     * @throws IOException if reading fails
     * @throws LLSDException if parsing fails
     */
    public static LLSD deserialize(InputStream input, LLSDFormat format) throws IOException, LLSDException {
        LLSDSerializer serializer = getSerializer(format);
        if (serializer == null) {
            throw new LLSDException("Unsupported format: " + format);
        }
        return serializer.deserialize(input);
    }
    
    /**
     * Detect the LLSD format from a string
     * @param data the string data
     * @return the detected format
     */
    public static LLSDFormat detectFormat(String data) {
        if (data == null || data.isEmpty()) {
            return LLSDFormat.XML; // Default
        }
        
        String trimmed = data.trim();
        
        if (trimmed.startsWith("<? llsd/binary ?>")) {
            return LLSDFormat.BINARY;
        } else if (trimmed.startsWith("<?xml") || trimmed.startsWith("<llsd")) {
            return LLSDFormat.XML;
        } else {
            // If it starts with known notation markers, it's notation
            char first = trimmed.charAt(0);
            if (first == '[' || first == '{' || first == '\'' || first == '"' ||
                first == 'i' || first == 'r' || first == 'u' || first == 's' ||
                first == 'b' || first == 'd' || first == 'l' || first == '!' ||
                first == 't' || first == 'f' || Character.isDigit(first)) {
                return LLSDFormat.NOTATION;
            }
            
            // Default to XML for safety
            return LLSDFormat.XML;
        }
    }
    
    /**
     * Detect the LLSD format from an input stream
     * @param input the buffered input stream (must support mark/reset)
     * @return the detected format
     * @throws IOException if reading fails
     */
    public static LLSDFormat detectFormat(BufferedInputStream input) throws IOException {
        input.mark(32); // Mark for reset
        
        byte[] header = new byte[32];
        int read = input.read(header);
        input.reset(); // Reset to beginning
        
        if (read <= 0) {
            return LLSDFormat.XML; // Default
        }
        
        String headerStr = new String(header, 0, read, "UTF-8").trim();
        
        if (headerStr.startsWith("<? llsd/binary ?>")) {
            return LLSDFormat.BINARY;
        } else if (headerStr.startsWith("<?xml") || headerStr.startsWith("<llsd")) {
            return LLSDFormat.XML;
        } else {
            // Check for notation format markers
            if (headerStr.length() > 0) {
                char first = headerStr.charAt(0);
                if (first == '[' || first == '{' || first == '\'' || first == '"' ||
                    first == 'i' || first == 'r' || first == 'u' || first == 's' ||
                    first == 'b' || first == 'd' || first == 'l' || first == '!' ||
                    first == 't' || first == 'f' || Character.isDigit(first)) {
                    return LLSDFormat.NOTATION;
                }
            }
            
            // Default to XML
            return LLSDFormat.XML;
        }
    }
}