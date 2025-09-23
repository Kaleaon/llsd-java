llsd-java
=========

This is a modernized Java implementation of [LLSD (Linden Lab Structured Data)](http://wiki.secondlife.com/wiki/LLSD) originally by [@Xugumad](https://github.com/Xugumad) and adopted by [@jacobilinden](https://github.com/jacobilinden).

The library has been updated to use modern Java standards and best practices, targeting Java 17+ with comprehensive test coverage and improved documentation.

**NEW:** This repository now includes Java conversions of LLSD functionality from the Second Life and Firestorm viewer C++ implementations, providing enhanced features and viewer-specific utilities.

## Features

- **Full LLSD XML Support**: Complete support for all LLSD data types (boolean, integer, real, string, date, URI, UUID, array, map)
- **Binary Data Support**: Handles binary data with proper base64 encoding/decoding
- **JSON LLSD Format**: Native support for JSON-formatted LLSD with proper type encoding
- **Notation LLSD Format**: Compact text-based format (e.g., `s'string'`, `i42`, `[i1,i2,i3]`)
- **Binary LLSD Format**: Efficient binary serialization format for high-performance applications
- **Modern Java Patterns**: Uses Java 17+ features and modern best practices
- **Comprehensive Test Coverage**: Extensive unit tests covering all functionality (90+ tests)
- **Thread-Safe Parsing**: Proper exception handling and resource management
- **Utility Functions**: Helper methods for navigating, validating, and manipulating LLSD data
- **Multiple Serialization Formats**: Support for XML, JSON, Notation, and Binary output formats

### NEW: Viewer Extensions

- **Enhanced LLSD Utilities**: Advanced utilities converted from Second Life/Firestorm C++ code
- **Type System Extensions**: Enhanced type detection and conversion from viewer implementations
- **Serialization Framework**: Advanced parsing with limits, depth checking, and line-based reading
- **Second Life Integration**: SL-specific data structures and protocols
- **Firestorm Extensions**: Firestorm-specific features including RLV, radar, bridge communication
- **Performance Optimizations**: Viewer-tested performance enhancements and caching

## Requirements

- Java 17 or later
- Maven 3.6+

## Installation

Clone the repository and build with Maven:

```bash
git clone https://github.com/Kaleaon/llsd-java.git
cd llsd-java
mvn compile
```

To run tests:
```bash
mvn test
```

To create JAR packages:
```bash
mvn package
```

## Usage

### Parsing LLSD Documents

### Parsing LLSD Documents

#### XML Format (Traditional)

```java
import lindenlab.llsd.LLSD;
import lindenlab.llsd.LLSDParser;
import lindenlab.llsd.LLSDException;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

// Parse an LLSD document from a file
try {
    LLSDParser parser = new LLSDParser();
    
    try (InputStream input = Files.newInputStream(Paths.get("document.xml"))) {
        LLSD document = parser.parse(input);
        Object content = document.getContent();
        
        // Handle different content types
        if (content instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) content;
            // Process map content...
        } else if (content instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) content;
            // Process array content...
        }
        // Handle other types (String, Integer, Double, Boolean, UUID, etc.)
    }
} catch (LLSDException e) {
    System.err.println("Invalid LLSD document: " + e.getMessage());
} catch (Exception e) {
    System.err.println("Error parsing LLSD: " + e.getMessage());
}
```

#### JSON Format (Modern)

```java
import lindenlab.llsd.LLSDJsonParser;

// Parse JSON-formatted LLSD
try {
    LLSDJsonParser jsonParser = new LLSDJsonParser();
    
    try (InputStream input = Files.newInputStream(Paths.get("document.json"))) {
        LLSD document = jsonParser.parse(input);
        // Process document same as XML format
    }
} catch (Exception e) {
    System.err.println("Error parsing JSON LLSD: " + e.getMessage());
}
```

#### Notation Format (Compact)

```java
import lindenlab.llsd.LLSDNotationParser;

// Parse notation-formatted LLSD
try {
    LLSDNotationParser notationParser = new LLSDNotationParser();
    
    try (InputStream input = Files.newInputStream(Paths.get("document.llsd"))) {
        LLSD document = notationParser.parse(input);
        // Process document same as other formats
    }
} catch (Exception e) {
    System.err.println("Error parsing Notation LLSD: " + e.getMessage());
}
```

#### Binary Format (High Performance)

```java
import lindenlab.llsd.LLSDBinaryParser;

// Parse binary-formatted LLSD
try {
    LLSDBinaryParser binaryParser = new LLSDBinaryParser();
    
    try (InputStream input = Files.newInputStream(Paths.get("document.llsd-binary"))) {
        LLSD document = binaryParser.parse(input);
        // Process document same as other formats
    }
} catch (Exception e) {
    System.err.println("Error parsing Binary LLSD: " + e.getMessage());
}
```

### Working with Binary Data

```java
// Binary data is now supported with base64 encoding
byte[] binaryData = "Hello World".getBytes(StandardCharsets.UTF_8);

// Create LLSD with binary data
LLSD binaryDocument = new LLSD(binaryData);

// Serialize to XML (automatically base64 encoded)
try (StringWriter writer = new StringWriter()) {
    binaryDocument.serialise(writer, "UTF-8");
    String xml = writer.toString();
    // Results in: <llsd><binary>SGVsbG8gV29ybGQ=</binary></llsd>
}

// Parse binary data from XML
LLSDParser parser = new LLSDParser();
try (InputStream input = new ByteArrayInputStream(xml.getBytes())) {
    LLSD parsed = parser.parse(input);
    byte[] recoveredData = (byte[]) parsed.getContent();
    String recoveredText = new String(recoveredData, StandardCharsets.UTF_8);
    // recoveredText equals "Hello World"
}
```

### Serializing LLSD Documents

### Serializing LLSD Documents

#### XML Format

```java
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

// Create and serialize an LLSD document
Map<String, Object> data = new HashMap<>();
data.put("name", "Test Object");
data.put("id", 12345);
data.put("active", true);

LLSD document = new LLSD(data);

try (StringWriter writer = new StringWriter()) {
    document.serialise(writer, "UTF-8");
    String xmlOutput = writer.toString();
    System.out.println(xmlOutput);
} catch (Exception e) {
    System.err.println("Error serializing LLSD: " + e.getMessage());
}
```

#### JSON Format

```java
import lindenlab.llsd.LLSDJsonSerializer;

// Serialize to JSON format
LLSD document = new LLSD(data);
LLSDJsonSerializer jsonSerializer = new LLSDJsonSerializer();

try (StringWriter writer = new StringWriter()) {
    jsonSerializer.serialize(document, writer);
    String jsonOutput = writer.toString();
    System.out.println(jsonOutput);
} catch (Exception e) {
    System.err.println("Error serializing to JSON: " + e.getMessage());
}
```

#### Notation Format

```java
import lindenlab.llsd.LLSDNotationSerializer;

// Serialize to compact notation format
LLSD document = new LLSD(data);
LLSDNotationSerializer notationSerializer = new LLSDNotationSerializer();

try (StringWriter writer = new StringWriter()) {
    notationSerializer.serialize(document, writer);
    String notationOutput = writer.toString();
    System.out.println(notationOutput);
    // Output: {name:s'Test Object',id:i12345,active:1}
} catch (Exception e) {
    System.err.println("Error serializing to Notation: " + e.getMessage());
}
```

#### Binary Format

```java
import lindenlab.llsd.LLSDBinarySerializer;

// Serialize to efficient binary format
LLSD document = new LLSD(data);
LLSDBinarySerializer binarySerializer = new LLSDBinarySerializer();

try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
    binarySerializer.serialize(document, output, true); // with header
    byte[] binaryData = output.toByteArray();
    System.out.println("Binary LLSD size: " + binaryData.length + " bytes");
} catch (Exception e) {
    System.err.println("Error serializing to Binary: " + e.getMessage());
}
```

### Using LLSD Utilities

```java
import lindenlab.llsd.LLSDUtils;

// Navigate nested LLSD structures safely
LLSD document = parser.parse(inputStream);

// Extract values with defaults
String userName = LLSDUtils.getString(document.getContent(), "user.profile.name", "Anonymous");
int userAge = LLSDUtils.getInteger(document.getContent(), "user.profile.age", 0);
UUID userId = LLSDUtils.getUUID(document.getContent(), "user.id", UUID.randomUUID());

// Validate required fields
List<String> missing = LLSDUtils.validateRequiredFields(
    document.getContent(), 
    "user.name", "user.email", "user.id"
);
if (!missing.isEmpty()) {
    System.err.println("Missing required fields: " + missing);
}

// Pretty print LLSD data
String formatted = LLSDUtils.prettyPrint(document.getContent());
System.out.println(formatted);

// Merge LLSD maps
Map<String, Object> defaults = createDefaults();
Map<String, Object> userPrefs = loadUserPreferences();
Map<String, Object> merged = LLSDUtils.mergeMaps(defaults, userPrefs);
```

## NEW: Viewer-Specific Extensions

### Enhanced LLSD Utilities (from C++ viewer code)

```java
import lindenlab.llsd.viewer.LLSDViewerUtils;

// Advanced type conversions
long unsignedValue = LLSDViewerUtils.toU32(llsdData);
byte[] binaryData = LLSDViewerUtils.binaryFromString(base64String);

// Template-based validation (from viewer protocols)
Map<String, Object> template = createProtocolTemplate();
Map<String, Object> result = new HashMap<>();
boolean valid = LLSDViewerUtils.compareWithTemplate(incomingData, template, result);

// Deep equality with floating-point precision control
boolean equal = LLSDViewerUtils.llsdEquals(data1, data2, 10); // 10-bit precision

// Structure matching for protocol validation
String errors = LLSDViewerUtils.llsdMatches(prototype, data, "");
if (!errors.isEmpty()) {
    System.err.println("Protocol validation failed: " + errors);
}

// Deep cloning with filtering
Map<String, Boolean> filter = Map.of("sensitive", false, "*", true);
Object filtered = LLSDViewerUtils.llsdClone(data, filter);
```

### Enhanced Type System

```java
import lindenlab.llsd.viewer.LLSDViewerTypes;
import lindenlab.llsd.viewer.LLSDViewerTypes.Type;

// Advanced type detection
Type detectedType = LLSDViewerTypes.TypeDetection.detectType(someObject);

// Type compatibility checking
boolean canConvert = LLSDViewerTypes.TypeDetection.canConvertTo(data, Type.STRING);

// Type-safe builders
Map<String, Object> complexMap = LLSDViewerTypes.Factory.map()
    .put("name", "Agent")
    .put("position", LLSDViewerTypes.Factory.array(128.5, 128.5, 23.0).build())
    .put("active", true)
    .build();
```

### Advanced Serialization Framework

```java
import lindenlab.llsd.viewer.LLSDViewerSerializer;

// Enhanced parser with security limits
LLSDViewerSerializer parser = new MyEnhancedParser();
LLSD result = parser.parse(inputStream, 1024*1024, 32); // 1MB limit, 32 depth limit

// Line-based parsing for better XML handling
LLSD xmlResult = parser.parseLines(xmlInputStream);

// Reusable parser instances
parser.reset(); // Reset for next parse operation
```

### Second Life Integration

```java
import lindenlab.llsd.viewer.secondlife.SecondLifeLLSDUtils;

// Create SL-specific data structures
Map<String, Object> agentData = SecondLifeLLSDUtils.createAgentData(
    agentId, 
    new double[]{128.0, 128.0, 23.0}, // position
    new double[]{0.0, 0.0, 0.0, 1.0}, // rotation
    new double[]{0.0, 0.0, 0.0}       // velocity
);

// Handle chat messages
Map<String, Object> chatMsg = SecondLifeLLSDUtils.createChatMessage(
    fromId, "Avatar Name", "Hello world!", 0, 0, position
);

// Validate SL message protocols
SecondLifeLLSDUtils.SLValidationRules rules = new SecondLifeLLSDUtils.SLValidationRules()
    .requireMap()
    .requireField("AgentID", UUID.class)
    .requireField("Message", String.class);

SecondLifeLLSDUtils.ValidationResult validation = 
    SecondLifeLLSDUtils.validateSLStructure(messageData, rules);

if (!validation.isValid()) {
    System.err.println("SL protocol validation failed: " + validation.getErrors());
}
```

### Firestorm Extensions

```java
import lindenlab.llsd.viewer.firestorm.FirestormLLSDUtils;
import lindenlab.llsd.viewer.firestorm.FirestormLLSDUtils.RLVCommand;

// RLV (Restrained Life Viewer) support
RLVCommand rlvCmd = new RLVCommand("@sit", "ground", "=force", sourceId);
Map<String, Object> rlvData = rlvCmd.toLLSD();

// Enhanced radar functionality
Map<String, Object> radarData = FirestormLLSDUtils.createRadarData(
    agentId, displayName, userName, position, distance, isTyping, attachments
);

// Bridge communication protocol
Map<String, Object> bridgeMsg = FirestormLLSDUtils.createBridgeMessage(
    "get_avatar_data", parameters, requestId, priority
);

// Performance monitoring
Map<String, Object> perfStats = FirestormLLSDUtils.createPerformanceStats(
    fps, bandwidth, memoryUsage, renderTime, scriptTime, triangles
);

// Firestorm-specific validation
FirestormLLSDUtils.FSValidationRules fsRules = new FirestormLLSDUtils.FSValidationRules()
    .requireMap()
    .requireFSVersion("6.0.0")
    .requireRLV()
    .requireField("ViewerVersion", String.class);

FirestormLLSDUtils.FSValidationResult fsResult = 
    FirestormLLSDUtils.validateFSStructure(data, fsRules);

// Thread-safe caching for performance
FirestormLLSDUtils.FSLLSDCache cache = new FirestormLLSDUtils.FSLLSDCache(300000); // 5min
cache.put("key", expensiveData);
Object cached = cache.get("key");

// Deep copy LLSD structures
Object copy = LLSDUtils.deepCopy(document.getContent());
```

### Supported Data Types

The library supports all standard LLSD data types:

- **boolean**: `true` or `false`
- **integer**: 32-bit signed integers
- **real**: Double-precision floating point (including NaN)
- **string**: UTF-8 text strings
- **date**: ISO 8601 formatted dates
- **uri**: Uniform Resource Identifiers
- **uuid**: Universally Unique Identifiers
- **array**: Ordered lists of LLSD values
- **map**: Key-value pairs with string keys
- **binary**: Base64-encoded binary data
- **undef**: Undefined values (represented as empty strings in XML, null in JSON)

## LLSD Format Support

### XML LLSD (Traditional)
Standard XML representation as specified in the original LLSD documentation.
- **Use case**: Legacy compatibility, human-readable
- **File extension**: `.xml`

### JSON LLSD (Modern)
JSON representation with special type encodings for LLSD-specific types:
```json
{
  "name": "John Doe",
  "age": 30,
  "id": {"i": "550e8400-e29b-41d4-a716-446655440000"},
  "homepage": {"u": "https://example.com"},
  "joined": {"d": "2024-01-01T00:00:00Z"},
  "avatar": {"b": "SGVsbG8gV29ybGQ="},
  "active": true,
  "score": 98.5,
  "items": [1, 2, 3],
  "undefined_field": null
}
```

Special JSON encodings:
- `{"d": "ISO8601"}` - Date values
- `{"u": "URI"}` - URI values  
- `{"i": "UUID"}` - UUID values
- `{"b": "base64"}` - Binary data
- `null` - Undefined values
- **Use case**: Web APIs, modern applications, JavaScript compatibility
- **File extension**: `.json`

### Notation LLSD (Compact)
Compact text-based format inspired by programming language literals:
```
{name:s'John Doe',age:i30,active:1,score:r98.5,items:[i1,i2,i3]}
```

Notation syntax:
- `!` - undefined
- `1` / `0` - boolean true/false  
- `i42` - integer
- `r3.14159` - real number
- `s'text'` - string (single or double quotes)
- `u550e8400-...` - UUID
- `d2024-01-01T00:00:00Z` - date
- `lhttp://example.com` - URI
- `b64"SGVsbG8="` - binary (base64)
- `[...]` - arrays
- `{key:value,...}` - maps
- **Use case**: Configuration files, compact data exchange, debugging
- **File extension**: `.llsd`

### Binary LLSD (High Performance)
Efficient binary encoding with specific byte markers:
- **Header**: `<?llsd/binary?>`
- **Compact encoding**: Uses single-byte type markers + binary data
- **Network byte order**: Big-endian for cross-platform compatibility
- **Use case**: High-performance applications, network protocols, large datasets
- **File extension**: `.llsd-binary`

## Limitations

**Binary data in XML** uses base64 encoding as recommended by XML standards. The original library authors correctly noted the issues with mixing binary data and XML character encoding.

For binary data, the library now properly handles base64 encoding/decoding automatically, making it safe and standards-compliant.

## Testing

The library includes comprehensive test coverage for all data types and error conditions:

```bash
mvn test
```

Test results include:
- Data type parsing and serialization
- Error handling for malformed documents
- Edge cases (NaN, empty values, etc.)
- Resource management

## Contributing

Contributions are welcome! Please ensure:
- All tests pass (`mvn test`)
- Code follows modern Java practices
- New features include appropriate test coverage
- Documentation is updated as needed

## License

This project maintains the original licensing terms from the University of St. Andrews implementation.
