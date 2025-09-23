/*
 * LLSD Viewer Utilities - Java implementation based on Second Life/Firestorm C++ code
 *
 * Converted from indra/llcommon/llsdutil.h/cpp
 * Copyright (C) 2010, Linden Research, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer;

import lindenlab.llsd.LLSD;
import lindenlab.llsd.LLSDException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Enhanced LLSD utilities converted from Second Life and Firestorm viewer C++ implementation.
 * 
 * <p>This class provides additional utility functions beyond the basic LLSD library,
 * including specialized conversion functions, template matching, deep comparison,
 * and enhanced data manipulation methods found in the viewer code.</p>
 * 
 * <p>Key features from the C++ implementation:</p>
 * <ul>
 * <li>Template matching and validation</li>
 * <li>Deep equality comparisons</li>
 * <li>Enhanced data type conversions</li>
 * <li>Binary/string conversion utilities</li>
 * <li>Network address handling</li>
 * <li>Array and map construction helpers</li>
 * </ul>
 * 
 * @since 1.0
 * @see LLSD
 */
public final class LLSDViewerUtils {
    
    private LLSDViewerUtils() {
        // Utility class - no instances
    }
    
    /**
     * Convert a 32-bit unsigned integer to LLSD.
     * Equivalent to C++ ll_sd_from_U32()
     * 
     * @param value the unsigned integer value (as long to handle unsigned range)
     * @return LLSD containing the value
     */
    public static Object fromU32(long value) {
        if (value < 0 || value > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("Value must be in unsigned 32-bit range: 0 to 4294967295");
        }
        return (int) value;
    }
    
    /**
     * Extract a 32-bit unsigned integer from LLSD.
     * Equivalent to C++ ll_U32_from_sd()
     * 
     * @param sd the LLSD value
     * @return the unsigned integer as long
     */
    public static long toU32(Object sd) {
        if (sd instanceof Integer) {
            int value = (Integer) sd;
            return value & 0xFFFFFFFFL; // Convert to unsigned
        } else if (sd instanceof Long) {
            long value = (Long) sd;
            if (value < 0 || value > 0xFFFFFFFFL) {
                throw new IllegalArgumentException("Value out of unsigned 32-bit range");
            }
            return value;
        }
        throw new IllegalArgumentException("Cannot convert to U32: " + sd);
    }
    
    /**
     * Convert a 64-bit unsigned integer to LLSD.
     * Equivalent to C++ ll_sd_from_U64()
     * 
     * @param value the unsigned integer value
     * @return LLSD containing the value
     */
    public static Object fromU64(long value) {
        return value;
    }
    
    /**
     * Extract a 64-bit unsigned integer from LLSD.
     * Equivalent to C++ ll_U64_from_sd()
     * 
     * @param sd the LLSD value
     * @return the unsigned integer
     */
    public static long toU64(Object sd) {
        if (sd instanceof Integer) {
            return ((Integer) sd).longValue();
        } else if (sd instanceof Long) {
            return (Long) sd;
        }
        throw new IllegalArgumentException("Cannot convert to U64: " + sd);
    }
    
    /**
     * Convert an IP address to LLSD.
     * Equivalent to C++ ll_sd_from_ipaddr()
     * 
     * @param ipAddress IP address as 32-bit integer
     * @return LLSD containing the IP address
     */
    public static Object fromIPAddress(int ipAddress) {
        return ipAddress;
    }
    
    /**
     * Extract an IP address from LLSD.
     * Equivalent to C++ ll_ipaddr_from_sd()
     * 
     * @param sd the LLSD value
     * @return IP address as integer
     */
    public static int toIPAddress(Object sd) {
        if (sd instanceof Integer) {
            return (Integer) sd;
        }
        throw new IllegalArgumentException("Cannot convert to IP address: " + sd);
    }
    
    /**
     * Convert binary data to a string representation.
     * Equivalent to C++ ll_string_from_binary()
     * 
     * @param sd LLSD containing binary data
     * @return string representation of binary data
     */
    public static String stringFromBinary(Object sd) {
        if (sd instanceof byte[]) {
            return Base64.getEncoder().encodeToString((byte[]) sd);
        }
        throw new IllegalArgumentException("Cannot convert to string from binary: " + sd);
    }
    
    /**
     * Convert a string to binary data.
     * Equivalent to C++ ll_binary_from_string()
     * 
     * @param sd LLSD containing string data
     * @return binary data as byte array
     */
    public static byte[] binaryFromString(Object sd) {
        if (sd instanceof String) {
            // First try direct UTF-8 encoding, then base64 if it looks like base64
            String str = (String) sd;
            if (isBase64String(str)) {
                try {
                    return Base64.getDecoder().decode(str);
                } catch (IllegalArgumentException e) {
                    // If base64 fails, fall back to UTF-8
                    return str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                }
            } else {
                return str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        throw new IllegalArgumentException("Cannot convert to binary from string: " + sd);
    }
    
    /**
     * Check if a string looks like base64 data.
     */
    private static boolean isBase64String(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        // Simple heuristic: contains only base64 characters and has reasonable length
        return str.matches("[A-Za-z0-9+/]+(=*)") && str.length() % 4 == 0;
    }
    
    /**
     * Compare an LLSD structure with a template and return filtered results.
     * Equivalent to C++ compare_llsd_with_template()
     * 
     * @param testLLSD the LLSD to test
     * @param templateLLSD the template to compare against
     * @param resultLLSD the filtered result (modified in place)
     * @return true if comparison succeeded
     */
    public static boolean compareWithTemplate(Object testLLSD, Object templateLLSD, Map<String, Object> resultLLSD) {
        return compareWithTemplateImpl(testLLSD, templateLLSD, resultLLSD, false);
    }
    
    /**
     * Filter an LLSD structure with a template, supporting wildcards.
     * Equivalent to C++ filter_llsd_with_template()
     * 
     * @param testLLSD the LLSD to filter
     * @param templateLLSD the template to filter with
     * @param resultLLSD the filtered result (modified in place)
     * @return true if filtering succeeded
     */
    public static boolean filterWithTemplate(Object testLLSD, Object templateLLSD, Map<String, Object> resultLLSD) {
        return compareWithTemplateImpl(testLLSD, templateLLSD, resultLLSD, true);
    }
    
    private static boolean compareWithTemplateImpl(Object testObj, Object templateObj, Map<String, Object> result, boolean useWildcard) {
        if (templateObj == null || "".equals(templateObj)) {
            // Undefined template matches anything
            if (testObj != null) {
                if (testObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> testMap = (Map<String, Object>) testObj;
                    result.putAll(testMap);
                } else {
                    // For non-map types, we can't directly merge, so return true
                    return true;
                }
            }
            return true;
        }
        
        if (testObj == null) {
            // Use template default
            if (templateObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> templateMap = (Map<String, Object>) templateObj;
                result.putAll(templateMap);
            }
            return true;
        }
        
        // Type checking - allow compatible types
        if (!areCompatibleTypes(testObj, templateObj)) {
            return false;
        }
        
        if (testObj instanceof Map && templateObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> testMap = (Map<String, Object>) testObj;
            @SuppressWarnings("unchecked")
            Map<String, Object> templateMap = (Map<String, Object>) templateObj;
            
            for (Map.Entry<String, Object> templateEntry : templateMap.entrySet()) {
                String key = templateEntry.getKey();
                Object templateValue = templateEntry.getValue();
                
                if (useWildcard && "*".equals(key)) {
                    // Wildcard key - apply to all unmatched keys in test
                    for (Map.Entry<String, Object> testEntry : testMap.entrySet()) {
                        if (!templateMap.containsKey(testEntry.getKey())) {
                            Map<String, Object> childResult = new HashMap<>();
                            if (compareWithTemplateImpl(testEntry.getValue(), templateValue, childResult, useWildcard)) {
                                if (childResult.isEmpty()) {
                                    result.put(testEntry.getKey(), testEntry.getValue());
                                } else {
                                    result.put(testEntry.getKey(), childResult.size() == 1 ? childResult.values().iterator().next() : childResult);
                                }
                            }
                        }
                    }
                    continue;
                }
                
                if (testMap.containsKey(key)) {
                    if (templateValue == null || "".equals(templateValue)) {
                        // Undefined template value means accept anything
                        result.put(key, testMap.get(key));
                    } else {
                        Map<String, Object> childResult = new HashMap<>();
                        if (compareWithTemplateImpl(testMap.get(key), templateValue, childResult, useWildcard)) {
                            if (childResult.isEmpty()) {
                                result.put(key, testMap.get(key));
                            } else {
                                result.put(key, childResult.size() == 1 ? childResult.values().iterator().next() : childResult);
                            }
                        } else {
                            return false;
                        }
                    }
                } else {
                    // Use template default
                    result.put(key, templateValue);
                }
            }
            return true;
        } else if (testObj instanceof List && templateObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> testArray = (List<Object>) testObj;
            @SuppressWarnings("unchecked")
            List<Object> templateArray = (List<Object>) templateObj;
            
            if (useWildcard && templateArray.size() == 1) {
                // Single template element applies to all test elements
                List<Object> resultArray = new ArrayList<>();
                Object templateElement = templateArray.get(0);
                for (Object testElement : testArray) {
                    Map<String, Object> childResult = new HashMap<>();
                    if (compareWithTemplateImpl(testElement, templateElement, childResult, useWildcard)) {
                        resultArray.add(childResult.isEmpty() ? testElement : childResult);
                    } else {
                        return false;
                    }
                }
                result.put("array", resultArray);
            } else {
                // Regular array comparison
                List<Object> resultArray = new ArrayList<>();
                for (int i = 0; i < Math.min(testArray.size(), templateArray.size()); i++) {
                    Object templateElement = templateArray.get(i);
                    if (templateElement == null || "".equals(templateElement)) {
                        resultArray.add(testArray.get(i));
                    } else {
                        Map<String, Object> childResult = new HashMap<>();
                        if (compareWithTemplateImpl(testArray.get(i), templateElement, childResult, useWildcard)) {
                            resultArray.add(childResult.isEmpty() ? testArray.get(i) : childResult);
                        } else {
                            return false;
                        }
                    }
                }
                result.put("array", resultArray);
            }
            return true;
        } else {
            // Scalar comparison - just store the test value
            return true;
        }
    }
    
    /**
     * Check if two objects have compatible types for template comparison.
     */
    private static boolean areCompatibleTypes(Object test, Object template) {
        if (test == null || template == null) {
            return true;
        }
        
        Class<?> testClass = test.getClass();
        Class<?> templateClass = template.getClass();
        
        // Exact match
        if (testClass.equals(templateClass)) {
            return true;
        }
        
        // Numeric compatibility
        if (isNumericType(testClass) && isNumericType(templateClass)) {
            return true;
        }
        
        // String compatibility with other types
        if (templateClass.equals(String.class) && isStringConvertibleType(testClass)) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isNumericType(Class<?> clazz) {
        return clazz.equals(Integer.class) || clazz.equals(Double.class) || 
               clazz.equals(Float.class) || clazz.equals(Long.class) || 
               clazz.equals(Boolean.class);
    }
    
    private static boolean isStringConvertibleType(Class<?> clazz) {
        return clazz.equals(String.class) || clazz.equals(Integer.class) || 
               clazz.equals(Double.class) || clazz.equals(Boolean.class) ||
               clazz.equals(UUID.class) || clazz.equals(Date.class);
    }
    
    /**
     * Check if LLSD structures match a prototype.
     * Equivalent to C++ llsd_matches()
     * 
     * @param prototype the prototype structure
     * @param data the data to check
     * @param prefix prefix for error messages
     * @return empty string if match, error message if not
     */
    public static String llsdMatches(Object prototype, Object data, String prefix) {
        if (prefix == null) prefix = "";
        
        if (prototype == null || "".equals(prototype)) {
            return ""; // undefined prototype matches anything
        }
        
        if (data == null) {
            return prefix + "missing data";
        }
        
        Class<?> prototypeClass = prototype.getClass();
        Class<?> dataClass = data.getClass();
        
        // Type compatibility checks
        if (prototype instanceof String) {
            // String prototype accepts string-convertible types
            if (!(data instanceof String || data instanceof Boolean || 
                  data instanceof Integer || data instanceof Double ||
                  data instanceof UUID || data instanceof Date)) {
                return prefix + "expected string-convertible type, got " + dataClass.getSimpleName();
            }
        } else if (prototype instanceof Boolean || prototype instanceof Integer || prototype instanceof Double) {
            // Numeric types are interconvertible
            if (!(data instanceof Boolean || data instanceof Integer || 
                  data instanceof Double || data instanceof String)) {
                return prefix + "expected numeric type, got " + dataClass.getSimpleName();
            }
        } else if (prototype instanceof UUID) {
            if (!(data instanceof UUID || data instanceof String)) {
                return prefix + "expected UUID or String, got " + dataClass.getSimpleName();
            }
        } else if (prototype instanceof Date) {
            if (!(data instanceof Date || data instanceof String)) {
                return prefix + "expected Date or String, got " + dataClass.getSimpleName();
            }
        } else if (prototype instanceof byte[]) {
            if (!(data instanceof byte[])) {
                return prefix + "expected Binary, got " + dataClass.getSimpleName();
            }
        } else if (prototype instanceof List) {
            if (!(data instanceof List)) {
                return prefix + "expected Array, got " + dataClass.getSimpleName();
            }
            
            List<?> prototypeList = (List<?>) prototype;
            List<?> dataList = (List<?>) data;
            
            if (dataList.size() < prototypeList.size()) {
                return prefix + "array too short: expected at least " + prototypeList.size() + ", got " + dataList.size();
            }
            
            for (int i = 0; i < prototypeList.size(); i++) {
                Object prototypeElement = prototypeList.get(i);
                if (prototypeElement != null && !"".equals(prototypeElement)) {
                    String result = llsdMatches(prototypeElement, dataList.get(i), prefix + "[" + i + "].");
                    if (!result.isEmpty()) {
                        return result;
                    }
                }
            }
        } else if (prototype instanceof Map) {
            if (!(data instanceof Map)) {
                return prefix + "expected Map, got " + dataClass.getSimpleName();
            }
            
            Map<?, ?> prototypeMap = (Map<?, ?>) prototype;
            Map<?, ?> dataMap = (Map<?, ?>) data;
            
            for (Map.Entry<?, ?> entry : prototypeMap.entrySet()) {
                String key = entry.getKey().toString();
                Object prototypeValue = entry.getValue();
                
                if (prototypeValue != null && !"".equals(prototypeValue)) {
                    if (!dataMap.containsKey(key)) {
                        return prefix + "missing required key: " + key;
                    }
                    String result = llsdMatches(prototypeValue, dataMap.get(key), prefix + key + ".");
                    if (!result.isEmpty()) {
                        return result;
                    }
                }
            }
        } else if (!prototypeClass.equals(dataClass)) {
            return prefix + "type mismatch: expected " + prototypeClass.getSimpleName() + ", got " + dataClass.getSimpleName();
        }
        
        return "";
    }
    
    /**
     * Deep equality comparison for LLSD structures.
     * Equivalent to C++ llsd_equals()
     * 
     * @param lhs left-hand side value
     * @param rhs right-hand side value
     * @param bits precision for floating point comparison (-1 for exact)
     * @return true if deeply equal
     */
    public static boolean llsdEquals(Object lhs, Object rhs, int bits) {
        if (lhs == rhs) return true;
        if (lhs == null || rhs == null) return false;
        
        Class<?> lhsClass = lhs.getClass();
        Class<?> rhsClass = rhs.getClass();
        
        if (!lhsClass.equals(rhsClass)) return false;
        
        if (lhs instanceof Double && rhs instanceof Double) {
            if (bits == -1) {
                return Objects.equals(lhs, rhs);
            } else {
                // Approximate equality for floating point
                double diff = Math.abs((Double) lhs - (Double) rhs);
                double epsilon = Math.pow(2, -bits);
                return diff <= epsilon;
            }
        } else if (lhs instanceof List && rhs instanceof List) {
            List<?> lhsList = (List<?>) lhs;
            List<?> rhsList = (List<?>) rhs;
            
            if (lhsList.size() != rhsList.size()) return false;
            
            for (int i = 0; i < lhsList.size(); i++) {
                if (!llsdEquals(lhsList.get(i), rhsList.get(i), bits)) {
                    return false;
                }
            }
            return true;
        } else if (lhs instanceof Map && rhs instanceof Map) {
            Map<?, ?> lhsMap = (Map<?, ?>) lhs;
            Map<?, ?> rhsMap = (Map<?, ?>) rhs;
            
            if (lhsMap.size() != rhsMap.size()) return false;
            
            for (Map.Entry<?, ?> entry : lhsMap.entrySet()) {
                Object key = entry.getKey();
                if (!rhsMap.containsKey(key)) return false;
                if (!llsdEquals(entry.getValue(), rhsMap.get(key), bits)) {
                    return false;
                }
            }
            return true;
        } else if (lhs instanceof byte[] && rhs instanceof byte[]) {
            return Arrays.equals((byte[]) lhs, (byte[]) rhs);
        } else {
            return Objects.equals(lhs, rhs);
        }
    }
    
    /**
     * Deep equality comparison with default precision.
     * 
     * @param lhs left-hand side value
     * @param rhs right-hand side value
     * @return true if deeply equal
     */
    public static boolean llsdEquals(Object lhs, Object rhs) {
        return llsdEquals(lhs, rhs, -1);
    }
    
    /**
     * Create a deep clone of an LLSD structure.
     * Equivalent to C++ llsd_clone()
     * 
     * @param value the value to clone
     * @param filter optional filter map (null for no filtering)
     * @return deep clone of the value
     */
    public static Object llsdClone(Object value, Map<String, Boolean> filter) {
        if (value == null) return null;
        
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> originalMap = (Map<String, Object>) value;
            Map<String, Object> clonedMap = new HashMap<>();
            
            boolean hasWildcard = filter != null && filter.containsKey("*");
            boolean wildcardValue = hasWildcard ? filter.get("*") : true;
            
            for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
                String key = entry.getKey();
                boolean shouldInclude;
                
                if (filter != null && filter.containsKey(key)) {
                    shouldInclude = filter.get(key);
                } else {
                    shouldInclude = wildcardValue;
                }
                
                if (shouldInclude) {
                    clonedMap.put(key, llsdClone(entry.getValue(), filter));
                }
            }
            return clonedMap;
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> originalList = (List<Object>) value;
            List<Object> clonedList = new ArrayList<>();
            
            for (Object item : originalList) {
                clonedList.add(llsdClone(item, filter));
            }
            return clonedList;
        } else if (value instanceof byte[]) {
            return ((byte[]) value).clone();
        } else {
            // Primitive types are immutable, return as-is
            return value;
        }
    }
    
    /**
     * Create a deep clone with no filtering.
     * 
     * @param value the value to clone
     * @return deep clone of the value
     */
    public static Object llsdClone(Object value) {
        return llsdClone(value, null);
    }
    
    /**
     * Create a shallow copy of a map or array.
     * Equivalent to C++ llsd_shallow()
     * 
     * @param value the value to copy
     * @param filter optional filter map (null for no filtering)
     * @return shallow copy of the value
     */
    public static Object llsdShallow(Object value, Map<String, Boolean> filter) {
        if (value == null) return null;
        
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> originalMap = (Map<String, Object>) value;
            Map<String, Object> copiedMap = new HashMap<>();
            
            boolean hasWildcard = filter != null && filter.containsKey("*");
            boolean wildcardValue = hasWildcard ? filter.get("*") : true;
            
            for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
                String key = entry.getKey();
                boolean shouldInclude;
                
                if (filter != null && filter.containsKey(key)) {
                    shouldInclude = filter.get(key);
                } else {
                    shouldInclude = wildcardValue;
                }
                
                if (shouldInclude) {
                    copiedMap.put(key, entry.getValue());
                }
            }
            return copiedMap;
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> originalList = (List<Object>) value;
            return new ArrayList<>(originalList);
        } else {
            return value;
        }
    }
    
    /**
     * Create a shallow copy with no filtering.
     * 
     * @param value the value to copy
     * @return shallow copy of the value
     */
    public static Object llsdShallow(Object value) {
        return llsdShallow(value, null);
    }
}