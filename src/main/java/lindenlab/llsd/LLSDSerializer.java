/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews
 * Updated 2024 based on Second Life viewer and LibreMetaverse implementations
 */

package lindenlab.llsd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

/**
 * Abstract base class for LLSD serializers
 */
public abstract class LLSDSerializer {
    
    /**
     * Serialize LLSD to an output stream
     * @param llsd the LLSD object to serialize
     * @param output the output stream to write to
     * @throws IOException if writing fails
     * @throws LLSDException if serialization fails
     */
    public abstract void serialize(LLSD llsd, OutputStream output) throws IOException, LLSDException;
    
    /**
     * Deserialize LLSD from an input stream
     * @param input the input stream to read from
     * @return the parsed LLSD object
     * @throws IOException if reading fails
     * @throws LLSDException if parsing fails
     */
    public abstract LLSD deserialize(InputStream input) throws IOException, LLSDException;
    
    /**
     * Serialize LLSD to string
     * @param llsd the LLSD object to serialize
     * @return the serialized string
     * @throws LLSDException if serialization fails
     */
    public abstract String serializeToString(LLSD llsd) throws LLSDException;
    
    /**
     * Deserialize LLSD from string
     * @param data the string data to parse
     * @return the parsed LLSD object
     * @throws LLSDException if parsing fails
     */
    public abstract LLSD deserializeFromString(String data) throws LLSDException;
    
    /**
     * Get the format this serializer handles
     * @return the LLSD format
     */
    public abstract LLSDFormat getFormat();
    
    /**
     * Utility method to encode binary data as base64
     * @param data the binary data
     * @return base64 encoded string
     */
    protected static String encodeBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
    
    /**
     * Utility method to decode base64 string to binary data
     * @param data the base64 encoded string
     * @return binary data
     */
    protected static byte[] decodeBase64(String data) {
        return Base64.getDecoder().decode(data);
    }
}