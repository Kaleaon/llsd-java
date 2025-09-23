/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews
 * Updated 2024 based on Second Life viewer and LibreMetaverse implementations
 */

package lindenlab.llsd;

/**
 * Enumeration of LLSD data types
 */
public enum LLSDType {
    UNKNOWN,
    BOOLEAN,
    INTEGER,
    REAL,
    STRING,
    UUID,
    DATE,
    URI,
    BINARY,
    MAP,
    ARRAY
}