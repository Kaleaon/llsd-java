llsd-java
=========

This is a modernized Java implementation of [LLSD (Linden Lab Structured Data)](http://wiki.secondlife.com/wiki/LLSD) originally by [@Xugumad](https://github.com/Xugumad) and adopted by [@jacobilinden](https://github.com/jacobilinden).

The library has been updated to use modern Java standards and best practices, targeting Java 17+ with comprehensive test coverage and improved documentation.

## Features

- **Full LLSD XML Support**: Complete support for all LLSD data types (boolean, integer, real, string, date, URI, UUID, array, map)
- **Binary Data Support**: Handles binary data with proper base64 encoding/decoding
- **JSON LLSD Format**: Native support for JSON-formatted LLSD with proper type encoding
- **Modern Java Patterns**: Uses Java 17+ features and modern best practices
- **Comprehensive Test Coverage**: Extensive unit tests covering all functionality
- **Thread-Safe Parsing**: Proper exception handling and resource management
- **Utility Functions**: Helper methods for navigating, validating, and manipulating LLSD data
- **Multiple Serialization Formats**: Support for XML and JSON output formats

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

## JSON LLSD Format

The library supports a JSON representation of LLSD data with special type encodings:

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
