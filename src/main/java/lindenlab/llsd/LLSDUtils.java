/*
 * LLSDJ - LLSD in Java implementation
 *
 * Copyright(C) 2024 - Modernized implementation
 */

package lindenlab.llsd;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class providing convenient methods for working with LLSD data structures.
 * 
 * <p>This class provides static utility methods for common operations on LLSD data
 * such as navigation, validation, and manipulation of nested structures.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * LLSD document = parser.parse(inputStream);
 * 
 * // Navigate nested structures safely
 * String name = LLSDUtils.getString(document.getContent(), "user.profile.name", "Unknown");
 * int age = LLSDUtils.getInteger(document.getContent(), "user.profile.age", 0);
 * 
 * // Convert to specific types with validation
 * Map<String, Object> userMap = LLSDUtils.asMap(document.getContent());
 * List<Object> itemsList = LLSDUtils.asList(userMap.get("items"));
 * }</pre>
 * 
 * @since 1.0
 * @see LLSD
 */
public final class LLSDUtils {
    
    private LLSDUtils() {
        // Utility class - no instances
    }

    /**
     * Safely extracts a string value from a nested LLSD structure using a dot-separated path.
     * 
     * @param root the root LLSD object to navigate
     * @param path the dot-separated path (e.g., "user.profile.name")
     * @param defaultValue the default value if the path doesn't exist or isn't a string
     * @return the string value or the default value
     */
    public static String getString(Object root, String path, String defaultValue) {
        Object value = navigatePath(root, path);
        return value instanceof String ? (String) value : defaultValue;
    }

    /**
     * Safely extracts an integer value from a nested LLSD structure.
     * 
     * @param root the root LLSD object to navigate
     * @param path the dot-separated path
     * @param defaultValue the default value if the path doesn't exist or isn't an integer
     * @return the integer value or the default value
     */
    public static int getInteger(Object root, String path, int defaultValue) {
        Object value = navigatePath(root, path);
        return value instanceof Integer ? (Integer) value : defaultValue;
    }

    /**
     * Safely extracts a double value from a nested LLSD structure.
     * 
     * @param root the root LLSD object to navigate
     * @param path the dot-separated path
     * @param defaultValue the default value if the path doesn't exist or isn't a double
     * @return the double value or the default value
     */
    public static double getDouble(Object root, String path, double defaultValue) {
        Object value = navigatePath(root, path);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        return defaultValue;
    }

    /**
     * Safely extracts a boolean value from a nested LLSD structure.
     * 
     * @param root the root LLSD object to navigate
     * @param path the dot-separated path
     * @param defaultValue the default value if the path doesn't exist or isn't a boolean
     * @return the boolean value or the default value
     */
    public static boolean getBoolean(Object root, String path, boolean defaultValue) {
        Object value = navigatePath(root, path);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }

    /**
     * Safely extracts a UUID value from a nested LLSD structure.
     * 
     * @param root the root LLSD object to navigate
     * @param path the dot-separated path
     * @param defaultValue the default value if the path doesn't exist or isn't a UUID
     * @return the UUID value or the default value
     */
    public static UUID getUUID(Object root, String path, UUID defaultValue) {
        Object value = navigatePath(root, path);
        if (value instanceof UUID) {
            return (UUID) value;
        } else if (value instanceof String) {
            try {
                return UUID.fromString((String) value);
            } catch (IllegalArgumentException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Safely casts an object to a Map.
     * 
     * @param obj the object to cast
     * @return the Map if successful, or an empty Map if not
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Object obj) {
        return obj instanceof Map ? (Map<String, Object>) obj : new HashMap<>();
    }

    /**
     * Safely casts an object to a List.
     * 
     * @param obj the object to cast
     * @return the List if successful, or an empty List if not
     */
    @SuppressWarnings("unchecked")
    public static List<Object> asList(Object obj) {
        return obj instanceof List ? (List<Object>) obj : new ArrayList<>();
    }

    /**
     * Checks if an LLSD value is considered "empty" (null, empty string, empty collection, or undefined).
     * 
     * @param value the value to check
     * @return true if the value is empty, false otherwise
     */
    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return ((String) value).isEmpty();
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }
        if (value instanceof LLSDUndefined) {
            return true;
        }
        if (value instanceof byte[]) {
            return ((byte[]) value).length == 0;
        }
        return false;
    }

    /**
     * Creates a deep copy of an LLSD structure.
     * 
     * @param obj the LLSD object to copy
     * @return a deep copy of the object
     */
    @SuppressWarnings("unchecked")
    public static Object deepCopy(Object obj) {
        if (obj == null) {
            return null;
        }
        
        if (obj instanceof Map) {
            Map<String, Object> original = (Map<String, Object>) obj;
            Map<String, Object> copy = new HashMap<>();
            for (Map.Entry<String, Object> entry : original.entrySet()) {
                copy.put(entry.getKey(), deepCopy(entry.getValue()));
            }
            return copy;
        }
        
        if (obj instanceof List) {
            List<Object> original = (List<Object>) obj;
            return original.stream()
                    .map(LLSDUtils::deepCopy)
                    .collect(Collectors.toList());
        }
        
        if (obj instanceof byte[]) {
            byte[] original = (byte[]) obj;
            return Arrays.copyOf(original, original.length);
        }
        
        // Immutable types can be returned as-is
        return obj;
    }

    /**
     * Merges two LLSD maps recursively.
     * 
     * @param target the target map to merge into
     * @param source the source map to merge from
     * @return the merged map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> mergeMaps(Map<String, Object> target, Map<String, Object> source) {
        if (target == null) {
            return source != null ? new HashMap<>(source) : new HashMap<>();
        }
        if (source == null) {
            return target;
        }
        
        Map<String, Object> result = new HashMap<>(target);
        
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object sourceValue = entry.getValue();
            Object targetValue = result.get(key);
            
            if (targetValue instanceof Map && sourceValue instanceof Map) {
                // Recursively merge nested maps
                result.put(key, mergeMaps((Map<String, Object>) targetValue, (Map<String, Object>) sourceValue));
            } else {
                // Overwrite with source value
                result.put(key, deepCopy(sourceValue));
            }
        }
        
        return result;
    }

    /**
     * Validates that an LLSD structure contains all required fields.
     * 
     * @param obj the LLSD object to validate
     * @param requiredFields the list of required field paths
     * @return a list of missing field paths, empty if all fields are present
     */
    public static List<String> validateRequiredFields(Object obj, String... requiredFields) {
        List<String> missing = new ArrayList<>();
        
        for (String field : requiredFields) {
            if (isEmpty(navigatePath(obj, field))) {
                missing.add(field);
            }
        }
        
        return missing;
    }

    /**
     * Converts an LLSD structure to a human-readable string representation.
     * 
     * @param obj the LLSD object to format
     * @param indent the indentation level
     * @return a formatted string representation
     */
    public static String prettyPrint(Object obj, int indent) {
        StringBuilder sb = new StringBuilder();
        prettyPrintRecursive(obj, indent, 0, sb);
        return sb.toString();
    }

    /**
     * Converts an LLSD structure to a human-readable string representation with default indentation.
     * 
     * @param obj the LLSD object to format
     * @return a formatted string representation
     */
    public static String prettyPrint(Object obj) {
        return prettyPrint(obj, 2);
    }

    /**
     * Navigates a dot-separated path in an LLSD structure.
     */
    private static Object navigatePath(Object root, String path) {
        if (root == null || path == null || path.isEmpty()) {
            return null;
        }
        
        String[] parts = path.split("\\.");
        Object current = root;
        
        for (String part : parts) {
            if (current instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) current;
                current = map.get(part);
            } else {
                return null; // Path doesn't exist
            }
        }
        
        return current;
    }

    /**
     * Recursive helper for pretty printing.
     */
    @SuppressWarnings("unchecked")
    private static void prettyPrintRecursive(Object obj, int indentSize, int currentLevel, StringBuilder sb) {
        String indent = " ".repeat(indentSize * currentLevel);
        
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            sb.append("{\n");
            boolean first = true;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!first) sb.append(",\n");
                first = false;
                sb.append(indent).append("  \"").append(entry.getKey()).append("\": ");
                prettyPrintRecursive(entry.getValue(), indentSize, currentLevel + 1, sb);
            }
            sb.append("\n").append(indent).append("}");
        } else if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            sb.append("[\n");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",\n");
                first = false;
                sb.append(indent).append("  ");
                prettyPrintRecursive(item, indentSize, currentLevel + 1, sb);
            }
            sb.append("\n").append(indent).append("]");
        } else if (obj instanceof String) {
            sb.append("\"").append(obj).append("\"");
        } else if (obj instanceof byte[]) {
            byte[] bytes = (byte[]) obj;
            sb.append("binary[").append(bytes.length).append(" bytes]");
        } else {
            sb.append(obj.toString());
        }
    }
}