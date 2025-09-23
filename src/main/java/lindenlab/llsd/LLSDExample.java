/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews
 * Updated 2024 based on Second Life viewer and LibreMetaverse implementations
 */

package lindenlab.llsd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.*;

/**
 * Example program demonstrating the enhanced LLSD features
 */
public class LLSDExample {
    
    public static void main(String[] args) {
        try {
            // Create sample data structure
            Map<String, Object> rootMap = new HashMap<>();
            
            // Add various data types
            rootMap.put("application", "Enhanced LLSD Java Library");
            rootMap.put("version", 2.0);
            rootMap.put("build_number", 1001);
            rootMap.put("is_stable", true);
            rootMap.put("uuid_example", UUID.randomUUID());
            rootMap.put("timestamp", new Date());
            rootMap.put("website", new URI("https://github.com/Kaleaon/llsd-java"));
            rootMap.put("binary_data", "Hello Binary World!".getBytes("UTF-8"));
            
            // Add an array
            List<Object> features = new ArrayList<>();
            features.add("XML Serialization");
            features.add("Notation Serialization");
            features.add("Binary Serialization");
            features.add("Auto-format Detection");
            features.add("Vector Types");
            rootMap.put("features", features);
            
            // Add nested structure
            Map<String, Object> stats = new HashMap<>();
            stats.put("formats_supported", 3);
            stats.put("backward_compatible", true);
            stats.put("performance_gain", 2.5);
            rootMap.put("statistics", stats);
            
            LLSD llsd = new LLSD(rootMap);
            
            System.out.println("LLSD Enhanced Features Demo");
            System.out.println("===========================\n");
            
            // Demonstrate XML serialization
            System.out.println("1. XML Format:");
            System.out.println("--------------");
            String xmlData = LLSDSerializationFactory.serialize(llsd, LLSDFormat.XML);
            System.out.println(xmlData);
            
            // Demonstrate Notation serialization
            System.out.println("2. Notation Format:");
            System.out.println("------------------");
            String notationData = LLSDSerializationFactory.serialize(llsd, LLSDFormat.NOTATION);
            System.out.println(notationData);
            System.out.println();
            
            // Demonstrate Binary serialization
            System.out.println("3. Binary Format (Base64 encoded for display):");
            System.out.println("----------------------------------------------");
            String binaryData = LLSDSerializationFactory.serialize(llsd, LLSDFormat.BINARY);
            // Display as base64 for readability since binary contains non-printable characters
            String binaryDataDisplay = Base64.getEncoder().encodeToString(binaryData.getBytes("ISO-8859-1"));
            System.out.println(binaryDataDisplay);
            System.out.println();
            
            // Demonstrate format detection
            System.out.println("4. Format Auto-Detection:");
            System.out.println("-------------------------");
            LLSDFormat xmlFormat = LLSDSerializationFactory.detectFormat(xmlData);
            LLSDFormat notationFormat = LLSDSerializationFactory.detectFormat(notationData);
            LLSDFormat binaryFormat = LLSDSerializationFactory.detectFormat(binaryData);
            
            System.out.println("XML data detected as: " + xmlFormat);
            System.out.println("Notation data detected as: " + notationFormat);  
            System.out.println("Binary data detected as: " + binaryFormat);
            System.out.println();
            
            // Demonstrate round-trip serialization
            System.out.println("5. Round-trip Test:");
            System.out.println("------------------");
            
            // Parse notation data and convert to XML
            LLSD parsedNotation = LLSDSerializationFactory.deserialize(notationData);
            String convertedToXML = LLSDSerializationFactory.serialize(parsedNotation, LLSDFormat.XML);
            
            System.out.println("Original notation successfully parsed and converted to XML:");
            System.out.println("Data integrity maintained: " + 
                (parsedNotation.getContent() instanceof Map && 
                 ((Map<?, ?>)parsedNotation.getContent()).containsKey("application")));
            System.out.println();
            
            // Demonstrate streaming binary serialization
            System.out.println("6. Streaming Binary Serialization:");
            System.out.println("----------------------------------");
            LLSDBinarySerializer binarySerializer = new LLSDBinarySerializer();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            binarySerializer.serialize(llsd, baos);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            LLSD streamParsed = binarySerializer.deserialize(bais);
            
            System.out.println("Binary streaming successful: " + 
                (streamParsed.getContent() instanceof Map));
            System.out.println("Stream data size: " + baos.size() + " bytes");
            System.out.println();
            
            // Show simple types
            System.out.println("7. Simple Type Examples:");
            System.out.println("-----------------------");
            
            LLSD boolLLSD = new LLSD(true);
            LLSD intLLSD = new LLSD(42);
            LLSD stringLLSD = new LLSD("Hello LLSD!");
            
            System.out.println("Boolean true in notation: " + 
                LLSDSerializationFactory.serialize(boolLLSD, LLSDFormat.NOTATION));
            System.out.println("Integer 42 in notation: " + 
                LLSDSerializationFactory.serialize(intLLSD, LLSDFormat.NOTATION));
            System.out.println("String in notation: " + 
                LLSDSerializationFactory.serialize(stringLLSD, LLSDFormat.NOTATION));
            
            System.out.println("\nDemo completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error during demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}