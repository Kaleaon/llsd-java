package lindenlab.llsd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@DisplayName("LLSD Parser Tests")
class LLSDParserTest {
    private static final String VALID_DOCUMENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<llsd>\n<map>\n  <key>region_id</key>\n    <uuid>67153d5b-3659-afb4-8510-adda2c034649</uuid>\n  <key>scale</key>\n    <string>one minute</string>\n  <key>simulator statistics</key>\n  <map>\n    <key>time dilation</key><real>0.9878624</real>\n    <key>sim fps</key><real>44.38898</real>\n    <key>pysics fps</key><real>44.38906</real>\n    <key>agent updates per second</key><real>nan</real>\n    <key>lsl instructions per second</key><real>0</real>\n    <key>total task count</key><real>4</real>\n    <key>active task count</key><real>0</real>\n    <key>active script count</key><real>4</real>\n    <key>main agent count</key><real>0</real>\n    <key>child agent count</key><real>0</real>\n    <key>inbound packets per second</key><real>1.228283</real>\n    <key>outbound packets per second</key><real>1.277508</real>\n    <key>pending downloads</key><real>0</real>\n    <key>pending uploads</key><real>0.0001096525</real>\n    <key>frame ms</key><real>0.7757886</real>\n    <key>net ms</key><real>0.3152919</real>\n    <key>sim other ms</key><real>0.1826937</real>\n    <key>sim physics ms</key><real>0.04323055</real>\n    <key>agent ms</key><real>0.01599029</real>\n    <key>image ms</key><real>0.01865955</real>\n    <key>script ms</key><real>0.1338836</real>\n  </map>\n</map>\n</llsd>";
    
    private static final String UNDEF_DOCUMENT ="<?xml version=\"1.0\" ?><llsd><map><key>volume_serial</key><undef/><key>event_type</key><string>login</string><key>success</key><boolean>true</boolean><key>session_id</key><uuid>8e56ef5c-a7be-4b92-a85c-3f1dee7a58f7</uuid><key>source</key><string>openid_do_login</string><key>done</key><boolean>true</boolean><key>agent_id</key><string>82d18a94-8c8d-44f7-a893-9dd3d7b6245d</string><key>mac_address</key><undef/><key>grid</key><string>agni</string><key>validation_stage</key><string>logged_in</string><key>ip_address</key><string>93.37.137.201</string><key>user_agent</key><string>Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36</string></map></llsd>";

    @Test
    @DisplayName("Should parse valid LLSD document without errors")
    void testParse() throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(VALID_DOCUMENT.getBytes(StandardCharsets.UTF_8))) {
            LLSDParser parser = new LLSDParser();
            LLSD result = parser.parse(inputStream);
            assertNotNull(result, "Parser should return a non-null LLSD object");
            assertNotNull(result.getContent(), "LLSD content should not be null");
        }
    }
    
    @Test
    @DisplayName("Should parse LLSD document with undefined values correctly")
    void testParseUndef() throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(UNDEF_DOCUMENT.getBytes(StandardCharsets.UTF_8))) {
            LLSDParser parser = new LLSDParser();
            LLSD output = parser.parse(inputStream);
            
            assertNotNull(output, "Parser should return a non-null LLSD object");
            Object content = output.getContent();
            assertTrue(content instanceof Map, "Content should be a Map");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> contentMap = (Map<String, Object>) content;
            
            assertTrue(contentMap.containsKey("volume_serial"), "Map should contain volume_serial key");
            Object volumeSerial = contentMap.get("volume_serial");
            assertNotNull(volumeSerial, "volume_serial should not be null");
            assertEquals("", volumeSerial.toString(), "volume_serial should be empty string");
        }
    }
}