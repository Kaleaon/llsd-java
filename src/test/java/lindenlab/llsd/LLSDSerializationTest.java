/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews
 * Updated 2024 based on Second Life viewer and LibreMetaverse implementations
 */

package lindenlab.llsd;

import junit.framework.TestCase;
import java.util.*;
import java.net.URI;

/**
 * Tests for the new LLSD serialization features
 */
public class LLSDSerializationTest extends TestCase {
    
    private LLSD createTestData() {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("boolean_true", Boolean.TRUE);
        testMap.put("boolean_false", Boolean.FALSE);
        testMap.put("integer", 42);
        testMap.put("real", 3.14159);
        testMap.put("string", "Hello, World!");
        testMap.put("uuid", UUID.randomUUID());
        testMap.put("date", new Date());
        
        try {
            testMap.put("uri", new URI("http://secondlife.com/"));
        } catch (Exception e) {
            // ignore
        }
        
        testMap.put("binary", "test binary data".getBytes());
        
        List<Object> testArray = new ArrayList<>();
        testArray.add(1);
        testArray.add(2);
        testArray.add(3);
        testMap.put("array", testArray);
        
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("nested_key", "nested_value");
        testMap.put("nested_map", nestedMap);
        
        return new LLSD(testMap);
    }
    
    public void testXMLSerialization() throws Exception {
        LLSD testData = createTestData();
        
        String xmlString = LLSDSerializationFactory.serialize(testData, LLSDFormat.XML);
        assertNotNull(xmlString);
        assertTrue(xmlString.contains("<?xml"));
        assertTrue(xmlString.contains("<llsd>"));
        assertTrue(xmlString.contains("Hello, World!"));
        
        LLSD deserialized = LLSDSerializationFactory.deserialize(xmlString, LLSDFormat.XML);
        assertNotNull(deserialized);
        assertNotNull(deserialized.getContent());
    }
    
    public void testNotationSerialization() throws Exception {
        LLSD testData = createTestData();
        
        String notationString = LLSDSerializationFactory.serialize(testData, LLSDFormat.NOTATION);
        assertNotNull(notationString);
        assertTrue(notationString.contains("'Hello, World!'"));
        
        LLSD deserialized = LLSDSerializationFactory.deserialize(notationString, LLSDFormat.NOTATION);
        assertNotNull(deserialized);
        assertNotNull(deserialized.getContent());
    }
    
    public void testBinarySerialization() throws Exception {
        LLSD testData = createTestData();
        
        String binaryString = LLSDSerializationFactory.serialize(testData, LLSDFormat.BINARY);
        assertNotNull(binaryString);
        
        LLSD deserialized = LLSDSerializationFactory.deserialize(binaryString, LLSDFormat.BINARY);
        assertNotNull(deserialized);
        assertNotNull(deserialized.getContent());
    }
    
    public void testFormatDetection() throws Exception {
        assertEquals(LLSDFormat.XML, LLSDSerializationFactory.detectFormat("<?xml version='1.0'?><llsd></llsd>"));
        assertEquals(LLSDFormat.XML, LLSDSerializationFactory.detectFormat("<llsd><string>test</string></llsd>"));
        assertEquals(LLSDFormat.NOTATION, LLSDSerializationFactory.detectFormat("{'key':'value'}"));
        assertEquals(LLSDFormat.NOTATION, LLSDSerializationFactory.detectFormat("['item1','item2']"));
        assertEquals(LLSDFormat.NOTATION, LLSDSerializationFactory.detectFormat("i42"));
        assertEquals(LLSDFormat.NOTATION, LLSDSerializationFactory.detectFormat("r3.14"));
        assertEquals(LLSDFormat.NOTATION, LLSDSerializationFactory.detectFormat("'string'"));
        assertEquals(LLSDFormat.BINARY, LLSDSerializationFactory.detectFormat("<? llsd/binary ?>\n..."));
    }
    
    public void testSimpleTypes() throws Exception {
        // Test boolean
        LLSD boolLLSD = new LLSD(Boolean.TRUE);
        String notation = LLSDSerializationFactory.serialize(boolLLSD, LLSDFormat.NOTATION);
        assertEquals("t", notation);
        
        LLSD parsed = LLSDSerializationFactory.deserialize("t", LLSDFormat.NOTATION);
        assertEquals(Boolean.TRUE, parsed.getContent());
        
        // Test integer
        LLSD intLLSD = new LLSD(42);
        notation = LLSDSerializationFactory.serialize(intLLSD, LLSDFormat.NOTATION);
        assertEquals("i42", notation);
        
        parsed = LLSDSerializationFactory.deserialize("i42", LLSDFormat.NOTATION);
        assertEquals(Integer.valueOf(42), parsed.getContent());
        
        // Test string
        LLSD stringLLSD = new LLSD("test");
        notation = LLSDSerializationFactory.serialize(stringLLSD, LLSDFormat.NOTATION);
        assertEquals("'test'", notation);
        
        parsed = LLSDSerializationFactory.deserialize("'test'", LLSDFormat.NOTATION);
        assertEquals("test", parsed.getContent());
    }
    
    public void testArraySerialization() throws Exception {
        List<Object> array = new ArrayList<>();
        array.add(1);
        array.add("test");
        array.add(Boolean.FALSE);
        
        LLSD arrayLLSD = new LLSD(array);
        
        String notation = LLSDSerializationFactory.serialize(arrayLLSD, LLSDFormat.NOTATION);
        assertTrue(notation.startsWith("["));
        assertTrue(notation.endsWith("]"));
        assertTrue(notation.contains("i1"));
        assertTrue(notation.contains("'test'"));
        assertTrue(notation.contains("f"));
        
        LLSD parsed = LLSDSerializationFactory.deserialize(notation, LLSDFormat.NOTATION);
        assertTrue(parsed.getContent() instanceof List);
        List<?> parsedArray = (List<?>) parsed.getContent();
        assertEquals(3, parsedArray.size());
        assertEquals(Integer.valueOf(1), parsedArray.get(0));
        assertEquals("test", parsedArray.get(1));
        assertEquals(Boolean.FALSE, parsedArray.get(2));
    }
    
    public void testMapSerialization() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 42);
        
        LLSD mapLLSD = new LLSD(map);
        
        String notation = LLSDSerializationFactory.serialize(mapLLSD, LLSDFormat.NOTATION);
        assertTrue(notation.startsWith("{"));
        assertTrue(notation.endsWith("}"));
        assertTrue(notation.contains("'key1':'value1'"));
        assertTrue(notation.contains("'key2':i42"));
        
        LLSD parsed = LLSDSerializationFactory.deserialize(notation, LLSDFormat.NOTATION);
        assertTrue(parsed.getContent() instanceof Map);
        Map<?, ?> parsedMap = (Map<?, ?>) parsed.getContent();
        assertEquals(2, parsedMap.size());
        assertEquals("value1", parsedMap.get("key1"));
        assertEquals(Integer.valueOf(42), parsedMap.get("key2"));
    }
}