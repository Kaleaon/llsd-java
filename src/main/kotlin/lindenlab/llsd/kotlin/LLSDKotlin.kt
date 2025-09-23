/*
 * LLSD Kotlin - Modern Kotlin implementation of LLSD with DSL support
 *
 * Based on Java LLSD implementation with Kotlin-specific features
 * Copyright (C) 2010, Linden Research, Inc.
 * Kotlin conversion Copyright (C) 2024
 */

package lindenlab.llsd.kotlin

import lindenlab.llsd.LLSD
import lindenlab.llsd.LLSDException
import java.net.URI
import java.time.Instant
import java.util.*

/**
 * Modern Kotlin wrapper for LLSD with DSL support and type safety.
 * 
 * <p>This class provides a Kotlin-idiomatic interface to LLSD data structures
 * with DSL builders, extension functions, and modern Kotlin features like
 * sealed classes, data classes, and coroutines support.</p>
 * 
 * @since 1.0
 */
sealed class LLSDValue {
    
    /**
     * Undefined/null value
     */
    object Undefined : LLSDValue()
    
    /**
     * Boolean value
     */
    data class Boolean(val value: kotlin.Boolean) : LLSDValue()
    
    /**
     * Integer value
     */
    data class Integer(val value: Int) : LLSDValue()
    
    /**
     * Real/double value
     */
    data class Real(val value: Double) : LLSDValue()
    
    /**
     * String value
     */
    data class String(val value: kotlin.String) : LLSDValue()
    
    /**
     * UUID value
     */
    data class UUID(val value: java.util.UUID) : LLSDValue()
    
    /**
     * Date value
     */
    data class Date(val value: Instant) : LLSDValue()
    
    /**
     * URI value
     */
    data class URI(val value: java.net.URI) : LLSDValue()
    
    /**
     * Binary data value
     */
    data class Binary(val value: ByteArray) : LLSDValue() {
        override fun equals(other: Any?): kotlin.Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Binary
            return value.contentEquals(other.value)
        }
        
        override fun hashCode(): Int = value.contentHashCode()
    }
    
    /**
     * Array value with type-safe accessors
     */
    data class Array(val values: MutableList<LLSDValue> = mutableListOf()) : LLSDValue() {
        
        constructor(vararg values: LLSDValue) : this(values.toMutableList())
        
        /**
         * Get value at index with optional default
         */
        operator fun get(index: Int, default: LLSDValue = Undefined): LLSDValue {
            return values.getOrElse(index) { default }
        }
        
        /**
         * Set value at index
         */
        operator fun set(index: Int, value: LLSDValue) {
            // Expand array if necessary
            while (values.size <= index) {
                values.add(Undefined)
            }
            values[index] = value
        }
        
        /**
         * Add value to array
         */
        fun add(value: LLSDValue): Array {
            values.add(value)
            return this
        }
        
        /**
         * Add multiple values
         */
        fun addAll(vararg values: LLSDValue): Array {
            this.values.addAll(values)
            return this
        }
        
        /**
         * Size of array
         */
        val size: Int get() = values.size
        
        /**
         * Check if empty
         */
        fun isEmpty(): kotlin.Boolean = values.isEmpty()
        
        /**
         * Iterate over values
         */
        fun forEach(action: (LLSDValue) -> Unit) = values.forEach(action)
        
        /**
         * Map values
         */
        fun <T> map(transform: (LLSDValue) -> T): List<T> = values.map(transform)
        
        /**
         * Filter values
         */
        fun filter(predicate: (LLSDValue) -> kotlin.Boolean): List<LLSDValue> = values.filter(predicate)
    }
    
    /**
     * Map value with type-safe accessors and DSL support
     */
    data class Map(val values: MutableMap<kotlin.String, LLSDValue> = mutableMapOf()) : LLSDValue() {
        
        constructor(vararg pairs: Pair<kotlin.String, LLSDValue>) : this(mutableMapOf(*pairs))
        
        /**
         * Get value by key with optional default
         */
        operator fun get(key: kotlin.String, default: LLSDValue = Undefined): LLSDValue {
            return values[key] ?: default
        }
        
        /**
         * Set value by key
         */
        operator fun set(key: kotlin.String, value: LLSDValue) {
            values[key] = value
        }
        
        /**
         * Check if key exists
         */
        fun containsKey(key: kotlin.String): kotlin.Boolean = values.containsKey(key)
        
        /**
         * Remove key
         */
        fun remove(key: kotlin.String): LLSDValue? = values.remove(key)
        
        /**
         * Size of map
         */
        val size: Int get() = values.size
        
        /**
         * Check if empty
         */
        fun isEmpty(): kotlin.Boolean = values.isEmpty()
        
        /**
         * Get all keys
         */
        val keys: Set<kotlin.String> get() = values.keys
        
        /**
         * Iterate over entries
         */
        fun forEach(action: (kotlin.String, LLSDValue) -> Unit) {
            values.forEach { (key, value) -> action(key, value) }
        }
        
        /**
         * Map entries
         */
        fun <T> mapValues(transform: (LLSDValue) -> T): kotlin.collections.Map<kotlin.String, T> {
            return values.mapValues { transform(it.value) }
        }
        
        /**
         * Filter entries
         */
        fun filter(predicate: (kotlin.String, LLSDValue) -> kotlin.Boolean): kotlin.collections.Map<kotlin.String, LLSDValue> {
            return values.filter { predicate(it.key, it.value) }
        }
    }
}

/**
 * DSL builder for LLSD arrays
 */
@LLSDDslMarker
class LLSDArrayBuilder {
    private val values = mutableListOf<LLSDValue>()
    
    /**
     * Add value to array
     */
    fun add(value: LLSDValue) {
        values.add(value)
    }
    
    /**
     * Add primitive values with automatic conversion
     */
    fun add(value: kotlin.Boolean) = add(LLSDValue.Boolean(value))
    fun add(value: Int) = add(LLSDValue.Integer(value))
    fun add(value: Double) = add(LLSDValue.Real(value))
    fun add(value: kotlin.String) = add(LLSDValue.String(value))
    fun add(value: java.util.UUID) = add(LLSDValue.UUID(value))
    fun add(value: Instant) = add(LLSDValue.Date(value))
    fun add(value: java.net.URI) = add(LLSDValue.URI(value))
    fun add(value: ByteArray) = add(LLSDValue.Binary(value))
    
    /**
     * Add nested array
     */
    fun array(init: LLSDArrayBuilder.() -> Unit) {
        val builder = LLSDArrayBuilder()
        builder.init()
        add(builder.build())
    }
    
    /**
     * Add nested map
     */
    fun map(init: LLSDMapBuilder.() -> Unit) {
        val builder = LLSDMapBuilder()
        builder.init()
        add(builder.build())
    }
    
    /**
     * Operator overload for adding values
     */
    operator fun LLSDValue.unaryPlus() {
        add(this)
    }
    
    operator fun kotlin.Boolean.unaryPlus() = add(this)
    operator fun Int.unaryPlus() = add(this)
    operator fun Double.unaryPlus() = add(this)
    operator fun kotlin.String.unaryPlus() = add(this)
    operator fun java.util.UUID.unaryPlus() = add(this)
    operator fun Instant.unaryPlus() = add(this)
    operator fun java.net.URI.unaryPlus() = add(this)
    operator fun ByteArray.unaryPlus() = add(this)
    
    fun build(): LLSDValue.Array = LLSDValue.Array(values)
}

/**
 * DSL builder for LLSD maps
 */
@LLSDDslMarker
class LLSDMapBuilder {
    private val values = mutableMapOf<kotlin.String, LLSDValue>()
    
    /**
     * Set key-value pair
     */
    infix fun kotlin.String.to(value: LLSDValue) {
        values[this] = value
    }
    
    /**
     * Automatic conversion for primitive types
     */
    infix fun kotlin.String.to(value: kotlin.Boolean) = this to LLSDValue.Boolean(value)
    infix fun kotlin.String.to(value: Int) = this to LLSDValue.Integer(value)
    infix fun kotlin.String.to(value: Double) = this to LLSDValue.Real(value)
    infix fun kotlin.String.to(value: kotlin.String) = this to LLSDValue.String(value)
    infix fun kotlin.String.to(value: java.util.UUID) = this to LLSDValue.UUID(value)
    infix fun kotlin.String.to(value: Instant) = this to LLSDValue.Date(value)
    infix fun kotlin.String.to(value: java.net.URI) = this to LLSDValue.URI(value)
    infix fun kotlin.String.to(value: ByteArray) = this to LLSDValue.Binary(value)
    
    /**
     * Set value directly
     */
    operator fun set(key: kotlin.String, value: LLSDValue) {
        values[key] = value
    }
    
    /**
     * Nested array builder
     */
    fun kotlin.String.array(init: LLSDArrayBuilder.() -> Unit) {
        val builder = LLSDArrayBuilder()
        builder.init()
        values[this] = builder.build()
    }
    
    /**
     * Nested map builder
     */
    fun kotlin.String.map(init: LLSDMapBuilder.() -> Unit) {
        val builder = LLSDMapBuilder()
        builder.init()
        values[this] = builder.build()
    }
    
    fun build(): LLSDValue.Map = LLSDValue.Map(values)
}

/**
 * DSL marker annotation to prevent scope pollution
 */
@DslMarker
annotation class LLSDDslMarker

/**
 * Top-level DSL functions for creating LLSD structures
 */

/**
 * Create LLSD array using DSL
 */
fun llsdArray(init: LLSDArrayBuilder.() -> Unit): LLSDValue.Array {
    val builder = LLSDArrayBuilder()
    builder.init()
    return builder.build()
}

/**
 * Create LLSD map using DSL
 */
fun llsdMap(init: LLSDMapBuilder.() -> Unit): LLSDValue.Map {
    val builder = LLSDMapBuilder()
    builder.init()
    return builder.build()
}

/**
 * Create LLSD value from any type
 */
fun llsdOf(value: Any?): LLSDValue = when (value) {
    null -> LLSDValue.Undefined
    is kotlin.Boolean -> LLSDValue.Boolean(value)
    is Int -> LLSDValue.Integer(value)
    is Long -> LLSDValue.Integer(value.toInt())
    is Float -> LLSDValue.Real(value.toDouble())
    is Double -> LLSDValue.Real(value)
    is kotlin.String -> LLSDValue.String(value)
    is java.util.UUID -> LLSDValue.UUID(value)
    is Instant -> LLSDValue.Date(value)
    is java.util.Date -> LLSDValue.Date(value.toInstant())
    is java.net.URI -> LLSDValue.URI(value)
    is ByteArray -> LLSDValue.Binary(value)
    is List<*> -> {
        val array = LLSDValue.Array()
        value.forEach { item ->
            array.add(llsdOf(item))
        }
        array
    }
    is kotlin.collections.Map<*, *> -> {
        val map = LLSDValue.Map()
        value.forEach { (key, v) ->
            if (key is kotlin.String) {
                map[key] = llsdOf(v)
            }
        }
        map
    }
    is LLSDValue -> value
    else -> LLSDValue.String(value.toString())
}

/**
 * Extension functions for type-safe value extraction
 */

/**
 * Extract value as Boolean or return default
 */
fun LLSDValue.asBoolean(default: kotlin.Boolean = false): kotlin.Boolean = when (this) {
    is LLSDValue.Boolean -> value
    is LLSDValue.Integer -> value != 0
    is LLSDValue.String -> value.toBooleanStrictOrNull() ?: default
    else -> default
}

/**
 * Extract value as Int or return default
 */
fun LLSDValue.asInt(default: Int = 0): Int = when (this) {
    is LLSDValue.Integer -> value
    is LLSDValue.Real -> value.toInt()
    is LLSDValue.Boolean -> if (value) 1 else 0
    is LLSDValue.String -> value.toIntOrNull() ?: default
    else -> default
}

/**
 * Extract value as Double or return default
 */
fun LLSDValue.asDouble(default: Double = 0.0): Double = when (this) {
    is LLSDValue.Real -> value
    is LLSDValue.Integer -> value.toDouble()
    is LLSDValue.String -> value.toDoubleOrNull() ?: default
    else -> default
}

/**
 * Extract value as String or return default
 */
fun LLSDValue.asString(default: kotlin.String = ""): kotlin.String = when (this) {
    is LLSDValue.String -> value
    is LLSDValue.Integer -> value.toString()
    is LLSDValue.Real -> value.toString()
    is LLSDValue.Boolean -> value.toString()
    is LLSDValue.UUID -> value.toString()
    is LLSDValue.Date -> value.toString()
    is LLSDValue.URI -> value.toString()
    LLSDValue.Undefined -> default
    else -> default
}

/**
 * Extract value as UUID or return default
 */
fun LLSDValue.asUUID(default: java.util.UUID? = null): java.util.UUID? = when (this) {
    is LLSDValue.UUID -> value
    is LLSDValue.String -> try { java.util.UUID.fromString(value) } catch (e: Exception) { default }
    else -> default
}

/**
 * Extract value as Date or return default
 */
fun LLSDValue.asDate(default: Instant? = null): Instant? = when (this) {
    is LLSDValue.Date -> value
    is LLSDValue.String -> try { Instant.parse(value) } catch (e: Exception) { default }
    else -> default
}

/**
 * Extract value as URI or return default
 */
fun LLSDValue.asURI(default: java.net.URI? = null): java.net.URI? = when (this) {
    is LLSDValue.URI -> value
    is LLSDValue.String -> try { java.net.URI(value) } catch (e: Exception) { default }
    else -> default
}

/**
 * Extract value as ByteArray or return default
 */
fun LLSDValue.asByteArray(default: ByteArray = byteArrayOf()): ByteArray = when (this) {
    is LLSDValue.Binary -> value
    is LLSDValue.String -> value.toByteArray()
    else -> default
}

/**
 * Extract value as Array or return default
 */
fun LLSDValue.asArray(default: LLSDValue.Array = LLSDValue.Array()): LLSDValue.Array = when (this) {
    is LLSDValue.Array -> this
    else -> default
}

/**
 * Extract value as Map or return default
 */
fun LLSDValue.asMap(default: LLSDValue.Map = LLSDValue.Map()): LLSDValue.Map = when (this) {
    is LLSDValue.Map -> this
    else -> default
}

/**
 * Safe array access with path notation
 */
operator fun LLSDValue.Array.get(vararg indices: Int): LLSDValue {
    var current: LLSDValue = this
    for (index in indices) {
        current = when (current) {
            is LLSDValue.Array -> current[index]
            else -> LLSDValue.Undefined
        }
        if (current == LLSDValue.Undefined) break
    }
    return current
}

/**
 * Safe map access with path notation
 */
operator fun LLSDValue.Map.get(vararg keys: kotlin.String): LLSDValue {
    var current: LLSDValue = this
    for (key in keys) {
        current = when (current) {
            is LLSDValue.Map -> current[key]
            else -> LLSDValue.Undefined
        }
        if (current == LLSDValue.Undefined) break
    }
    return current
}

/**
 * Path-based access for mixed array/map navigation
 */
fun LLSDValue.path(vararg elements: Any): LLSDValue {
    var current: LLSDValue = this
    for (element in elements) {
        current = when {
            current is LLSDValue.Array && element is Int -> current[element]
            current is LLSDValue.Map && element is kotlin.String -> current[element]
            else -> LLSDValue.Undefined
        }
        if (current == LLSDValue.Undefined) break
    }
    return current
}

/**
 * Convert LLSDValue to Java LLSD for interoperability
 */
fun LLSDValue.toJavaLLSD(): LLSD {
    return LLSD(toJavaObject())
}

private fun LLSDValue.toJavaObject(): Any? = when (this) {
    LLSDValue.Undefined -> null
    is LLSDValue.Boolean -> value
    is LLSDValue.Integer -> value
    is LLSDValue.Real -> value
    is LLSDValue.String -> value
    is LLSDValue.UUID -> value
    is LLSDValue.Date -> java.util.Date.from(value)
    is LLSDValue.URI -> value
    is LLSDValue.Binary -> value
    is LLSDValue.Array -> values.map { it.toJavaObject() }.toList()
    is LLSDValue.Map -> values.mapValues { it.value.toJavaObject() }.toMap()
}

/**
 * Convert Java LLSD to Kotlin LLSDValue
 */
fun LLSD.toKotlinLLSD(): LLSDValue {
    return llsdOf(this.getContent())
}

/**
 * Pretty print LLSD in Kotlin style
 */
fun LLSDValue.toPrettyString(indent: Int = 0): kotlin.String {
    val indentStr = "  ".repeat(indent)
    return when (this) {
        LLSDValue.Undefined -> "undefined"
        is LLSDValue.Boolean -> value.toString()
        is LLSDValue.Integer -> value.toString()
        is LLSDValue.Real -> value.toString()
        is LLSDValue.String -> "\"$value\""
        is LLSDValue.UUID -> "UUID(\"$value\")"
        is LLSDValue.Date -> "Date(\"$value\")"
        is LLSDValue.URI -> "URI(\"$value\")"
        is LLSDValue.Binary -> "Binary(${value.size} bytes)"
        is LLSDValue.Array -> {
            if (values.isEmpty()) {
                "[]"
            } else {
                val items = values.joinToString(",\n") { "${indentStr}  ${it.toPrettyString(indent + 1)}" }
                "[\n$items\n$indentStr]"
            }
        }
        is LLSDValue.Map -> {
            if (values.isEmpty()) {
                "{}"
            } else {
                val items = values.entries.joinToString(",\n") { 
                    "${indentStr}  \"${it.key}\": ${it.value.toPrettyString(indent + 1)}" 
                }
                "{\n$items\n$indentStr}"
            }
        }
    }
}

/**
 * Kotlin-style LLSD equality check
 */
fun LLSDValue.deepEquals(other: LLSDValue): kotlin.Boolean = when {
    this === other -> true
    this::class != other::class -> false
    this is LLSDValue.Array && other is LLSDValue.Array -> {
        values.size == other.values.size && values.zip(other.values).all { (a, b) -> a.deepEquals(b) }
    }
    this is LLSDValue.Map && other is LLSDValue.Map -> {
        values.size == other.values.size && 
        values.all { (key, value) -> other.values[key]?.let { value.deepEquals(it) } ?: false }
    }
    else -> this == other
}

/**
 * Create a deep copy of LLSD value
 */
fun LLSDValue.deepCopy(): LLSDValue = when (this) {
    LLSDValue.Undefined -> LLSDValue.Undefined
    is LLSDValue.Boolean -> LLSDValue.Boolean(value)
    is LLSDValue.Integer -> LLSDValue.Integer(value)
    is LLSDValue.Real -> LLSDValue.Real(value)
    is LLSDValue.String -> LLSDValue.String(value)
    is LLSDValue.UUID -> LLSDValue.UUID(value)
    is LLSDValue.Date -> LLSDValue.Date(value)
    is LLSDValue.URI -> LLSDValue.URI(value)
    is LLSDValue.Binary -> LLSDValue.Binary(value.copyOf())
    is LLSDValue.Array -> LLSDValue.Array(values.map { it.deepCopy() }.toMutableList())
    is LLSDValue.Map -> LLSDValue.Map(values.mapValues { it.value.deepCopy() }.toMutableMap())
}