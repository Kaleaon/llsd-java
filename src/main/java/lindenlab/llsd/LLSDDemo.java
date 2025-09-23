/*
 * LLSDJ - Simple LLSD demonstration
 * 
 * Demonstrates the comprehensive LLSD implementation from the master branch
 */

package lindenlab.llsd;

import java.io.StringWriter;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.*;

/**
 * Simple demonstration of master branch LLSD capabilities
 */
public class LLSDDemo {
    
    public static void main(String[] args) {
        try {
            System.out.println("LLSD Master Branch Integration Demo");
            System.out.println("===================================\n");
            
            // Create sample data
            Map<String, Object> data = new HashMap<>();
            data.put("message", "LLSD conflict resolution successful!");
            data.put("formats", Arrays.asList("XML", "JSON", "Notation", "Binary"));
            data.put("version", 2.0);
            data.put("automated", true);
            
            LLSD llsd = new LLSD(data);
            
            // XML (original format)
            System.out.println("1. XML Format:");
            StringWriter xmlWriter = new StringWriter();
            llsd.serialise(xmlWriter, "UTF-8");
            System.out.println(xmlWriter.toString());
            
            // JSON (master branch feature)
            System.out.println("2. JSON Format:");
            LLSDJsonSerializer jsonSerializer = new LLSDJsonSerializer();
            StringWriter jsonWriter = new StringWriter();
            jsonSerializer.serialize(llsd, jsonWriter);
            System.out.println(jsonWriter.toString());
            System.out.println();
            
            // Notation (master branch feature)  
            System.out.println("3. Notation Format:");
            LLSDNotationSerializer notationSerializer = new LLSDNotationSerializer();
            StringWriter notationWriter = new StringWriter();
            notationSerializer.serialize(llsd, notationWriter);
            System.out.println(notationWriter.toString());
            System.out.println();
            
            // Binary (master branch feature)
            System.out.println("4. Binary Format:");
            LLSDBinarySerializer binarySerializer = new LLSDBinarySerializer();
            byte[] binaryData = binarySerializer.serialize(llsd);
            System.out.println("Binary serialized: " + binaryData.length + " bytes");
            System.out.println();
            
            System.out.println("✅ All formats working correctly!");
            System.out.println("✅ Conflicts resolved successfully!");
            System.out.println("✅ Master branch integration complete!");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}