llsd-java
=========

This is a modernized Java implementation of [LLSD (Linden Lab Structured Data)](http://wiki.secondlife.com/wiki/LLSD) originally by [@Xugumad](https://github.com/Xugumad) and adopted by [@jacobilinden](https://github.com/jacobilinden).

The library has been updated to use modern Java standards and best practices, targeting Java 17+ with comprehensive test coverage and improved documentation.

## Features

- Full support for all LLSD data types (boolean, integer, real, string, date, URI, UUID, array, map)
- Modern Java patterns and practices
- Comprehensive unit test coverage
- Thread-safe parsing
- Proper exception handling and resource management
- Updated to Java 17+ with modern tooling

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

### Serializing LLSD Documents

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
- **undef**: Undefined values (represented as empty strings)

## Limitations

**Binary data is not supported** in XML serialization. As noted by the original author:

> "binary" node type not implemented because it's a stupid idea that breaks how XML works. In specific, XML has a character set, binary data does not, and mixing the two is a recipe for disaster. Linden Labs should have used base 64 encode if they absolutely must, or attached binary content using a MIME multipart type.

For binary data, consider base64 encoding it as a string before including in LLSD documents.

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
