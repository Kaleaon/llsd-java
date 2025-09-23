/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews
 * Updated 2024 based on Second Life viewer and LibreMetaverse implementations
 */

package lindenlab.llsd;

import java.io.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * LLSD XML format serializer - improved version of the original XML serializer
 */
public class LLSDXMLSerializer extends LLSDSerializer {
    
    private final SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    public LLSDXMLSerializer() {
        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    @Override
    public LLSDFormat getFormat() {
        return LLSDFormat.XML;
    }
    
    @Override
    public void serialize(LLSD llsd, OutputStream output) throws IOException, LLSDException {
        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        serialize(llsd, writer);
        writer.flush();
    }
    
    /**
     * Serialize LLSD to a Writer
     * @param llsd the LLSD object to serialize
     * @param writer the writer to write to
     * @throws IOException if writing fails
     * @throws LLSDException if serialization fails
     */
    public void serialize(LLSD llsd, Writer writer) throws IOException, LLSDException {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<llsd>\n");
        if (llsd.getContent() != null) {
            serializeElement(writer, llsd.getContent(), "");
        }
        writer.write("</llsd>\n");
    }
    
    @Override
    public String serializeToString(LLSD llsd) throws LLSDException {
        StringWriter writer = new StringWriter();
        try {
            serialize(llsd, writer);
        } catch (IOException e) {
            throw new LLSDException("Error serializing to string", e);
        }
        return writer.toString();
    }
    
    @Override
    public LLSD deserialize(InputStream input) throws IOException, LLSDException {
        // Use existing LLSDParser for XML deserialization
        try {
            LLSDParser parser = new LLSDParser();
            return parser.parse(input);
        } catch (Exception e) {
            throw new LLSDException("Error deserializing XML", e);
        }
    }
    
    @Override
    public LLSD deserializeFromString(String data) throws LLSDException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes("UTF-8"));
            return deserialize(bais);
        } catch (IOException e) {
            throw new LLSDException("Error deserializing from string", e);
        }
    }
    
    private void serializeElement(Writer writer, Object element, String indent) throws IOException, LLSDException {
        if (element == null) {
            writer.write(indent + "<undef />\n");
            return;
        }
        
        if (element instanceof Boolean) {
            writer.write(indent + "<boolean>" + element.toString() + "</boolean>\n");
        } else if (element instanceof Integer) {
            writer.write(indent + "<integer>" + element.toString() + "</integer>\n");
        } else if (element instanceof Double) {
            Double d = (Double) element;
            if (d.isNaN()) {
                writer.write(indent + "<real>nan</real>\n");
            } else {
                writer.write(indent + "<real>" + d.toString() + "</real>\n");
            }
        } else if (element instanceof Float) {
            Float f = (Float) element;
            if (f.isNaN()) {
                writer.write(indent + "<real>nan</real>\n");
            } else {
                writer.write(indent + "<real>" + f.toString() + "</real>\n");
            }
        } else if (element instanceof UUID) {
            writer.write(indent + "<uuid>" + element.toString() + "</uuid>\n");
        } else if (element instanceof String) {
            writer.write(indent + "<string>" + escapeXML((String) element) + "</string>\n");
        } else if (element instanceof byte[]) {
            // Use base64 encoding for binary data to be compatible with existing parser
            writer.write(indent + "<string>" + escapeXML(Base64.getEncoder().encodeToString((byte[]) element)) + "</string>\n");
        } else if (element instanceof Date) {
            writer.write(indent + "<date>" + iso8601Format.format((Date) element) + "</date>\n");
        } else if (element instanceof URI) {
            writer.write(indent + "<uri>" + escapeXML(element.toString()) + "</uri>\n");
        } else if (element instanceof List) {
            serializeArray(writer, (List<?>) element, indent);
        } else if (element instanceof Map) {
            serializeMap(writer, (Map<String, ?>) element, indent);
        } else if (element instanceof LLSDUndefined) {
            serializeUndefined(writer, (LLSDUndefined) element, indent);
        } else {
            throw new LLSDException("Cannot serialize type: " + element.getClass().getName());
        }
    }
    
    private void serializeArray(Writer writer, List<?> array, String indent) throws IOException, LLSDException {
        writer.write(indent + "<array>\n");
        String childIndent = indent + "  ";
        for (Object element : array) {
            serializeElement(writer, element, childIndent);
        }
        writer.write(indent + "</array>\n");
    }
    
    private void serializeMap(Writer writer, Map<String, ?> map, String indent) throws IOException, LLSDException {
        writer.write(indent + "<map>\n");
        String childIndent = indent + "  ";
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            writer.write(childIndent + "<key>" + escapeXML(entry.getKey()) + "</key>\n");
            serializeElement(writer, entry.getValue(), childIndent);
        }
        writer.write(indent + "</map>\n");
    }
    
    private void serializeUndefined(Writer writer, LLSDUndefined undefined, String indent) throws IOException {
        switch (undefined) {
            case BINARY:
                writer.write(indent + "<binary><undef /></binary>\n");
                break;
            case BOOLEAN:
                writer.write(indent + "<boolean><undef /></boolean>\n");
                break;
            case DATE:
                writer.write(indent + "<date><undef /></date>\n");
                break;
            case INTEGER:
                writer.write(indent + "<integer><undef /></integer>\n");
                break;
            case REAL:
                writer.write(indent + "<real><undef /></real>\n");
                break;
            case STRING:
                writer.write(indent + "<string><undef /></string>\n");
                break;
            case URI:
                writer.write(indent + "<uri><undef /></uri>\n");
                break;
            case UUID:
                writer.write(indent + "<uuid><undef /></uuid>\n");
                break;
            default:
                writer.write(indent + "<undef />\n");
                break;
        }
    }
    
    private String escapeXML(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }
}