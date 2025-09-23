/*
 * LLSD Enhanced Type System - Java implementation based on Second Life/Firestorm C++ code
 *
 * Converted from indra/llcommon/llsd.h
 * Copyright (C) 2010, Linden Research, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer;

import java.net.URI;
import java.util.*;
import java.util.UUID;

/**
 * Enhanced LLSD type system converted from the Second Life/Firestorm C++ implementation.
 * 
 * <p>This class provides additional type definitions, constants, and utilities
 * that extend the basic LLSD type system with viewer-specific enhancements.</p>
 * 
 * <p>Key enhancements from the C++ viewer implementation:</p>
 * <ul>
 * <li>Extended type enumeration with viewer-specific types</li>
 * <li>Type conversion utilities with strict validation</li>
 * <li>Helper classes for type-safe LLSD construction</li>
 * <li>Advanced type checking and validation methods</li>
 * <li>Memory-efficient type handling</li>
 * </ul>
 * 
 * @since 1.0
 */
public final class LLSDViewerTypes {
    
    private LLSDViewerTypes() {
        // Utility class - no instances
    }
    
    /**
     * Enhanced LLSD type enumeration based on C++ LLSD::Type.
     * Includes all standard LLSD types plus viewer-specific extensions.
     */
    public enum Type {
        /** Undefined/null value */
        UNDEFINED(0, "Undefined"),
        
        /** Boolean true/false */
        BOOLEAN(1, "Boolean"),
        
        /** 32-bit signed integer */
        INTEGER(2, "Integer"),
        
        /** 64-bit IEEE 754 floating point */
        REAL(3, "Real"),
        
        /** UTF-8 string */
        STRING(4, "String"),
        
        /** 128-bit UUID */
        UUID(5, "UUID"),
        
        /** Date/time value */
        DATE(6, "Date"),
        
        /** URI string */
        URI(7, "URI"),
        
        /** Binary data */
        BINARY(8, "Binary"),
        
        /** Map/dictionary */
        MAP(9, "Map"),
        
        /** Array/list */
        ARRAY(10, "Array"),
        
        /** Viewer extension: 64-bit unsigned integer */
        ULONG(11, "ULong"),
        
        /** Viewer extension: 32-bit unsigned integer */
        UINT(12, "UInt"),
        
        /** Viewer extension: 32-bit float */
        FLOAT(13, "Float"),
        
        /** Marker for end of enum */
        TYPE_END(14, "TypeEnd");
        
        private final int value;
        private final String name;
        
        Type(int value, String name) {
            this.value = value;
            this.name = name;
        }
        
        public int getValue() { return value; }
        public String getName() { return name; }
        
        @Override
        public String toString() { return name; }
        
        /**
         * Get Type enum from integer value.
         * 
         * @param value the integer value
         * @return corresponding Type enum
         * @throws IllegalArgumentException if value is invalid
         */
        public static Type fromValue(int value) {
            for (Type type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid LLSD type value: " + value);
        }
    }
    
    /**
     * Type classification utilities from C++ implementation.
     */
    public static class TypeUtils {
        
        /**
         * Check if a type represents a scalar (non-container) value.
         * 
         * @param type the type to check
         * @return true if scalar type
         */
        public static boolean isScalar(Type type) {
            return type != Type.MAP && type != Type.ARRAY && type != Type.UNDEFINED;
        }
        
        /**
         * Check if a type represents a container (map or array).
         * 
         * @param type the type to check
         * @return true if container type
         */
        public static boolean isContainer(Type type) {
            return type == Type.MAP || type == Type.ARRAY;
        }
        
        /**
         * Check if a type represents a numeric value.
         * 
         * @param type the type to check
         * @return true if numeric type
         */
        public static boolean isNumeric(Type type) {
            return type == Type.INTEGER || type == Type.REAL || 
                   type == Type.BOOLEAN || type == Type.UINT || 
                   type == Type.ULONG || type == Type.FLOAT;
        }
        
        /**
         * Check if a type can be converted to string.
         * 
         * @param type the type to check
         * @return true if string-convertible
         */
        public static boolean isStringConvertible(Type type) {
            return type == Type.STRING || type == Type.BOOLEAN || 
                   type == Type.INTEGER || type == Type.REAL ||
                   type == Type.UUID || type == Type.DATE ||
                   type == Type.URI || type == Type.UINT ||
                   type == Type.ULONG || type == Type.FLOAT;
        }
        
        /**
         * Get the natural ordering priority for types (for comparison).
         * 
         * @param type the type to get priority for
         * @return priority value (lower = higher priority)
         */
        public static int getTypePriority(Type type) {
            switch (type) {
                case UNDEFINED: return 0;
                case BOOLEAN: return 1;
                case INTEGER: return 2;
                case UINT: return 3;
                case ULONG: return 4;
                case FLOAT: return 5;
                case REAL: return 6;
                case STRING: return 7;
                case UUID: return 8;
                case DATE: return 9;
                case URI: return 10;
                case BINARY: return 11;
                case ARRAY: return 12;
                case MAP: return 13;
                default: return Integer.MAX_VALUE;
            }
        }
    }
    
    /**
     * Type detection utilities for Java objects.
     */
    public static class TypeDetection {
        
        /**
         * Detect the LLSD type of a Java object.
         * 
         * @param obj the object to analyze
         * @return detected LLSD type
         */
        public static Type detectType(Object obj) {
            if (obj == null) {
                return Type.UNDEFINED;
            } else if (obj instanceof Boolean) {
                return Type.BOOLEAN;
            } else if (obj instanceof Integer) {
                return Type.INTEGER;
            } else if (obj instanceof Long) {
                return Type.ULONG;  // Assume long values are unsigned in viewer context
            } else if (obj instanceof Float) {
                return Type.FLOAT;
            } else if (obj instanceof Double) {
                return Type.REAL;
            } else if (obj instanceof String) {
                return Type.STRING;
            } else if (obj instanceof java.util.UUID) {
                return Type.UUID;
            } else if (obj instanceof Date) {
                return Type.DATE;
            } else if (obj instanceof URI) {
                return Type.URI;
            } else if (obj instanceof byte[]) {
                return Type.BINARY;
            } else if (obj instanceof List) {
                return Type.ARRAY;
            } else if (obj instanceof Map) {
                return Type.MAP;
            } else {
                // Default to string for unknown types
                return Type.STRING;
            }
        }
        
        /**
         * Check if an object matches a specific LLSD type.
         * 
         * @param obj the object to check
         * @param expectedType the expected type
         * @return true if object matches type
         */
        public static boolean matchesType(Object obj, Type expectedType) {
            return detectType(obj) == expectedType;
        }
        
        /**
         * Validate that an object can be safely converted to a type.
         * 
         * @param obj the object to validate
         * @param targetType the target type
         * @return true if conversion is safe
         */
        public static boolean canConvertTo(Object obj, Type targetType) {
            Type sourceType = detectType(obj);
            
            if (sourceType == targetType) {
                return true;
            }
            
            // Check conversion rules from C++ implementation
            switch (targetType) {
                case STRING:
                    return TypeUtils.isStringConvertible(sourceType);
                    
                case BOOLEAN:
                case INTEGER:
                case REAL:
                case FLOAT:
                case UINT:
                case ULONG:
                    return TypeUtils.isNumeric(sourceType) || sourceType == Type.STRING;
                    
                case UUID:
                    return sourceType == Type.STRING;
                    
                case DATE:
                    return sourceType == Type.STRING;
                    
                case URI:
                    return sourceType == Type.STRING;
                    
                case BINARY:
                    return false; // Binary only converts to/from Binary
                    
                default:
                    return false;
            }
        }
    }
    
    /**
     * Type-safe LLSD array builder, equivalent to C++ llsd::array().
     */
    public static class ArrayBuilder {
        private final List<Object> items = new ArrayList<>();
        
        /**
         * Add an item to the array.
         * 
         * @param item the item to add
         * @return this builder for chaining
         */
        public ArrayBuilder add(Object item) {
            items.add(item);
            return this;
        }
        
        /**
         * Add multiple items to the array.
         * 
         * @param items the items to add
         * @return this builder for chaining
         */
        public ArrayBuilder addAll(Object... items) {
            Collections.addAll(this.items, items);
            return this;
        }
        
        /**
         * Build the final array.
         * 
         * @return immutable list representing the LLSD array
         */
        public List<Object> build() {
            return Collections.unmodifiableList(new ArrayList<>(items));
        }
        
        /**
         * Get the current size of the array.
         * 
         * @return number of items
         */
        public int size() {
            return items.size();
        }
    }
    
    /**
     * Type-safe LLSD map builder, equivalent to C++ LLSDMap.
     */
    public static class MapBuilder {
        private final Map<String, Object> entries = new HashMap<>();
        
        /**
         * Add a key-value pair to the map.
         * 
         * @param key the key
         * @param value the value
         * @return this builder for chaining
         */
        public MapBuilder put(String key, Object value) {
            entries.put(key, value);
            return this;
        }
        
        /**
         * Add all entries from another map.
         * 
         * @param map the source map
         * @return this builder for chaining
         */
        public MapBuilder putAll(Map<String, Object> map) {
            entries.putAll(map);
            return this;
        }
        
        /**
         * Remove a key from the map.
         * 
         * @param key the key to remove
         * @return this builder for chaining
         */
        public MapBuilder remove(String key) {
            entries.remove(key);
            return this;
        }
        
        /**
         * Check if the map contains a key.
         * 
         * @param key the key to check
         * @return true if key exists
         */
        public boolean containsKey(String key) {
            return entries.containsKey(key);
        }
        
        /**
         * Build the final map.
         * 
         * @return immutable map representing the LLSD map
         */
        public Map<String, Object> build() {
            return Collections.unmodifiableMap(new HashMap<>(entries));
        }
        
        /**
         * Get the current size of the map.
         * 
         * @return number of entries
         */
        public int size() {
            return entries.size();
        }
    }
    
    /**
     * Convenience factory methods for creating LLSD structures.
     * Equivalent to C++ llsd namespace functions.
     */
    public static class Factory {
        
        /**
         * Create an empty LLSD array.
         * 
         * @return new ArrayBuilder
         */
        public static ArrayBuilder array() {
            return new ArrayBuilder();
        }
        
        /**
         * Create an LLSD array with initial items.
         * 
         * @param items initial items
         * @return new ArrayBuilder with items
         */
        public static ArrayBuilder array(Object... items) {
            return new ArrayBuilder().addAll(items);
        }
        
        /**
         * Create an empty LLSD map.
         * 
         * @return new MapBuilder
         */
        public static MapBuilder map() {
            return new MapBuilder();
        }
        
        /**
         * Create an LLSD map with initial key-value pair.
         * 
         * @param key initial key
         * @param value initial value
         * @return new MapBuilder with entry
         */
        public static MapBuilder map(String key, Object value) {
            return new MapBuilder().put(key, value);
        }
        
        /**
         * Create a deep copy of an LLSD structure.
         * 
         * @param source the source object
         * @return deep copy of the source
         */
        public static Object clone(Object source) {
            return LLSDViewerUtils.llsdClone(source);
        }
        
        /**
         * Create a shallow copy of an LLSD structure.
         * 
         * @param source the source object
         * @return shallow copy of the source
         */
        public static Object shallow(Object source) {
            return LLSDViewerUtils.llsdShallow(source);
        }
    }
    
    /**
     * Constants for commonly used LLSD values from viewer implementation.
     */
    public static class Constants {
        
        /** Empty string constant */
        public static final String EMPTY_STRING = "";
        
        /** Empty array constant */
        public static final List<Object> EMPTY_ARRAY = Collections.emptyList();
        
        /** Empty map constant */
        public static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();
        
        /** Zero integer constant */
        public static final Integer ZERO_INTEGER = 0;
        
        /** Zero real constant */
        public static final Double ZERO_REAL = 0.0;
        
        /** False boolean constant */
        public static final Boolean FALSE_BOOLEAN = Boolean.FALSE;
        
        /** Null UUID constant */
        public static final java.util.UUID NULL_UUID = new java.util.UUID(0L, 0L);
        
        /** Unix epoch date constant */
        public static final Date EPOCH_DATE = new Date(0L);
        
        /** Empty binary constant */
        public static final byte[] EMPTY_BINARY = new byte[0];
        
        /** Maximum safe integer value for LLSD */
        public static final int MAX_SAFE_INTEGER = Integer.MAX_VALUE;
        
        /** Minimum safe integer value for LLSD */
        public static final int MIN_SAFE_INTEGER = Integer.MIN_VALUE;
        
        /** Maximum safe string length (from viewer limits) */
        public static final int MAX_SAFE_STRING_LENGTH = 65535;
        
        /** Maximum safe array size (from viewer limits) */
        public static final int MAX_SAFE_ARRAY_SIZE = 65535;
        
        /** Maximum safe map size (from viewer limits) */
        public static final int MAX_SAFE_MAP_SIZE = 65535;
        
        /** Maximum safe binary size (from viewer limits) */
        public static final int MAX_SAFE_BINARY_SIZE = 16 * 1024 * 1024; // 16MB
    }
}