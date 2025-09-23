/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews
 */

package lindenlab.llsd;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * LLSD root, returns by an LLSDParser.
 */
public class LLSD {
    private final Object content;
    private DecimalFormat decimalFormat = null;
    private DateFormat iso9601Format = null;

    /**
     * Constructs a new LLSD document around the given root element.
     *
     * @param setContent the element which goes within the &lt;llsd&gt;
     * element. May be an instance of a class extending or implementing:
     *
     * <ul>
     * <li>java.lang.Double</li>
     * <li>java.lang.Integer</li>
     * <li>java.lang.String</li>
     * <li>java.net.URI</li>
     * <li>java.util.List</li>
     * <li>java.util.Map</li>
     * <li>java.util.UUID</li>
     * </ul>
     *
     * If a list or map, must only contain one of those classes inside,
     * as well.
     */
    public  LLSD(final Object setContent) {
        this.content = setContent;
    }

    /**
     * Encodes text into HTML - this involves replacing '<', '>', '&' and
     * '"' with their HTML entity equivalents.
     *
     * @param text the text to be encoded.
     * @return the original text if no changes are made, or a new String
     * otherwise.
     */
    public    static        String        encodeXML(final String text) {
        final char[] encodeBuffer;
        int          encodeBufferSize;
        int          textLength;
        String       output;

        if (text == null) {
            return "null";
        }

        textLength = text.length();
        if (textLength == 0) {
            return text;
        }

        encodeBufferSize = textLength;
        for (int i = 0; i < textLength; i++) {
            switch (text.charAt(i)) {
            case '<':
            case '>':
                encodeBufferSize = encodeBufferSize + 3;
                break;

            case '&':
            case '-':
                encodeBufferSize = encodeBufferSize + 4;
                break;

            case '\"':
                encodeBufferSize = encodeBufferSize + 5;
                break;
                

            default:
                break;
            }
        }

        if (encodeBufferSize == textLength) {
            return text;
        }

        encodeBuffer = new char[encodeBufferSize];

        /**
         * The longest possibility from a single character is "&quot;"
         * This means that a multiplication of 6 times is enough to
         * hold the longest possible encoded string.
         */
        for (int i = 0, j = 0; i < textLength; i++, j++) {
            char currentChar = text.charAt(i);

            switch (currentChar) {
            case '<':
                encodeBuffer[j] = '&'; j++;
                encodeBuffer[j] = 'l'; j++;
                encodeBuffer[j] = 't'; j++;
                encodeBuffer[j] = ';';
                break;

            case '>':
                encodeBuffer[j] = '&'; j++;
                encodeBuffer[j] = 'g'; j++;
                encodeBuffer[j] = 't'; j++;
                encodeBuffer[j] = ';';
                break;

            case '&':
                encodeBuffer[j] = '&'; j++;
                encodeBuffer[j] = 'a'; j++;
                encodeBuffer[j] = 'm'; j++;
                encodeBuffer[j] = 'p'; j++;
                encodeBuffer[j] = ';';
                break;

            case '\"':
                encodeBuffer[j] = '&'; j++;
                encodeBuffer[j] = 'q'; j++;
                encodeBuffer[j] = 'u'; j++;
                encodeBuffer[j] = 'o'; j++;
                encodeBuffer[j] = 't'; j++;
                encodeBuffer[j] = ';';
                break;

            case '-':
                encodeBuffer[j] = '&'; j++;
                encodeBuffer[j] = '#'; j++;
                encodeBuffer[j] = '4'; j++;
                encodeBuffer[j] = '5'; j++;
                encodeBuffer[j] = ';';
                break;

            default:
                encodeBuffer[j] = currentChar;
            }
        }

        output    = new String(encodeBuffer);

        return output;
    }

    /**
     * Returns the element contained within the &lt;llsd&gt; element. May be an
     * instance of a class extending or implementing:
     *
     * <ul>
     * <li>java.lang.Double</li>
     * <li>java.lang.Integer</li>
     * <li>java.lang.String</li>
     * <li>java.net.URI</li>
     * <li>java.util.List</li>
     * <li>java.util.Map</li>
     * <li>java.util.UUID</li>
     * </ul>
     *
     * If a list or map, it will only contain one of those classes inside,
     * as well.
     */
    public  Object  getContent() {
        return this.content;
    }

    /**
     * Writes out this LLSD as an XML document to the given writer.
     *
     * @param charset the character set to specify in the XML intro.
     */
    public  void    serialise(final Writer writer, final String charset)
        throws IOException, LLSDException {
        writer.write("<?xml version=\"1.0\" encoding=\""
            + charset + "\"?>\n");
        writer.write("<llsd>\n");
        if (null != content) {
            serialiseElement(writer, content);
        }
        writer.write("</llsd>\n");
    }

    private void serialiseElement(final Writer writer, final Object toSerialise)
        throws IOException, LLSDException {
        if (null == iso9601Format) {
            iso9601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            decimalFormat = new DecimalFormat("#0.0#");
        }

        assert null != toSerialise;

        if (toSerialise instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> serialiseMap = (Map<String, Object>)toSerialise;

            writer.write("<map>\n");
            for (String key: serialiseMap.keySet()) {
                final Object value = serialiseMap.get(key);
                writer.write("\t<key>"
                    + encodeXML(key) + "</key>\n\t");
                serialiseElement(writer, value);
            }
            writer.write("</map>\n");
        } else if (toSerialise instanceof List) {
            @SuppressWarnings("unchecked")
            final List<Object> serialiseList = (List<Object>)toSerialise;
            writer.write("<array>\n");
            for (Object current: serialiseList) {
                writer.write("\t");
                serialiseElement(writer, current);
            }
            writer.write("</array>\n");
        } else if (toSerialise instanceof Boolean) {
            writer.write("<boolean>"
                + toSerialise.toString() + "</boolean>\n");
        } else if (toSerialise instanceof Integer) {
            writer.write("<integer>"
                + toSerialise.toString() + "</integer>\n");
        } else if (toSerialise instanceof Double) {
            if (toSerialise.equals(Double.NaN)) {
                writer.write("<real>nan</real>\n");
            } else {
                writer.write("<real>"
                    + decimalFormat.format(toSerialise) + "</real>\n");
            }
        } else if (toSerialise instanceof Float) {
            if (toSerialise.equals(Float.NaN)) {
                writer.write("<real>nan</real>\n");
            } else {
                writer.write("<real>"
                    + decimalFormat.format(toSerialise) + "</real>\n");
            }
        } else if (toSerialise instanceof UUID) {
            writer.write("<uuid>"
                + toSerialise.toString() + "</uuid>\n");
        } else if (toSerialise instanceof String) {
            writer.write("<string>"
                + encodeXML((String)toSerialise) + "</string>\n");
        } else if (toSerialise instanceof Date) {
            writer.write("<date>"
                + iso9601Format.format((Date)toSerialise) + "</date>");
        } else if (toSerialise instanceof URI) {
            writer.write("<uri>"
                + encodeXML(toSerialise.toString()) + "</uri>");
        } else if (toSerialise instanceof LLSDUndefined) {
            switch((LLSDUndefined)toSerialise) {
            case BINARY:
                writer.write("<binary><undef /></binary>\n");
                break;
            case BOOLEAN:
                writer.write("<boolean><undef /></boolean>\n");
                break;
            case DATE:
                writer.write("<date><undef /></date>\n");
                break;
            case INTEGER:
                writer.write("<integer><undef /></integer>\n");
                break;
            case REAL:
                writer.write("<real><undef /></real>\n");
                break;
            case STRING:
                writer.write("<string><undef /></string>\n");
                break;
            case URI:
                writer.write("<uri><undef /></uri>\n");
                break;
            case UUID:
                writer.write("<uuid><undef /></uuid>\n");
                break;
            }
        } else {
            throw new LLSDException("Unable to serialise type \""
                + toSerialise.getClass().getName() + "\".");
        }

    }

    public String toString() {
        final StringWriter writer = new StringWriter();

        try {
            serialise(writer, "UTF-8");
        } catch(IOException e) {
            return "Unable to serialise LLSD for display: " + e.getMessage();
        } catch(LLSDException e) {
            return "Unable to serialise LLSD for display: " + e.getMessage();
        }

        return writer.toString();
    }
}
