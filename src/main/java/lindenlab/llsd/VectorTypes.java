/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews
 * Updated 2024 based on Second Life viewer and LibreMetaverse implementations
 */

package lindenlab.llsd;

/**
 * Represents an immutable two-dimensional vector with single-precision components.
 * <p>
 * This class is a simple value object for holding 2D vector data, often used
 * for texture coordinates or 2D positions.
 */
public final class Vector2 {
    /** A vector with all components set to zero. */
    public static final Vector2 ZERO = new Vector2(0.0f, 0.0f);
    
    /** The x-component of the vector. */
    public final float x;
    /** The y-component of the vector. */
    public final float y;
    
    /**
     * Constructs a new Vector2 with the specified components.
     * @param x The x-component.
     * @param y The y-component.
     */
    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString() {
        return String.format("<%f, %f>", x, y);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector2 vector2 = (Vector2) obj;
        return Float.compare(vector2.x, x) == 0 && Float.compare(vector2.y, y) == 0;
    }
    
    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }
}

/**
 * Represents an immutable three-dimensional vector with single-precision components.
 * <p>
 * This class is used for 3D positions, directions, and other spatial calculations.
 */
public final class Vector3 {
    /** A vector with all components set to zero. */
    public static final Vector3 ZERO = new Vector3(0.0f, 0.0f, 0.0f);
    
    /** The x-component of the vector. */
    public final float x;
    /** The y-component of the vector. */
    public final float y;
    /** The z-component of the vector. */
    public final float z;

    /**
     * Constructs a new Vector3 with the specified components.
     * @param x The x-component.
     * @param y The y-component.
     * @param z The z-component.
     */
    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    @Override
    public String toString() {
        return String.format("<%f, %f, %f>", x, y, z);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector3 vector3 = (Vector3) obj;
        return Float.compare(vector3.x, x) == 0 && 
               Float.compare(vector3.y, y) == 0 && 
               Float.compare(vector3.z, z) == 0;
    }
    
    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
        return result;
    }
}

/**
 * Represents an immutable four-dimensional vector with single-precision components.
 */
public final class Vector4 {
    /** A vector with all components set to zero. */
    public static final Vector4 ZERO = new Vector4(0.0f, 0.0f, 0.0f, 0.0f);
    
    /** The x-component of the vector. */
    public final float x;
    /** The y-component of the vector. */
    public final float y;
    /** The z-component of the vector. */
    public final float z;
    /** The w-component of the vector. */
    public final float w;

    /**
     * Constructs a new Vector4 with the specified components.
     * @param x The x-component.
     * @param y The y-component.
     * @param z The z-component.
     * @param w The w-component.
     */
    public Vector4(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    
    @Override
    public String toString() {
        return String.format("<%f, %f, %f, %f>", x, y, z, w);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector4 vector4 = (Vector4) obj;
        return Float.compare(vector4.x, x) == 0 && 
               Float.compare(vector4.y, y) == 0 && 
               Float.compare(vector4.z, z) == 0 && 
               Float.compare(vector4.w, w) == 0;
    }
    
    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
        result = 31 * result + (w != +0.0f ? Float.floatToIntBits(w) : 0);
        return result;
    }
}

/**
 * Represents an immutable quaternion used for 3D rotations.
 */
public final class Quaternion {
    /** The identity quaternion, representing no rotation. */
    public static final Quaternion IDENTITY = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
    
    /** The x-component of the quaternion. */
    public final float x;
    /** The y-component of the quaternion. */
    public final float y;
    /** The z-component of the quaternion. */
    public final float z;
    /** The w-component (scalar part) of the quaternion. */
    public final float w;

    /**
     * Constructs a new Quaternion with the specified components.
     * @param x The x-component.
     * @param y The y-component.
     * @param z The z-component.
     * @param w The w-component.
     */
    public Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    
    @Override
    public String toString() {
        return String.format("<%f, %f, %f, %f>", x, y, z, w);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Quaternion quaternion = (Quaternion) obj;
        return Float.compare(quaternion.x, x) == 0 && 
               Float.compare(quaternion.y, y) == 0 && 
               Float.compare(quaternion.z, z) == 0 && 
               Float.compare(quaternion.w, w) == 0;
    }
    
    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
        result = 31 * result + (w != +0.0f ? Float.floatToIntBits(w) : 0);
        return result;
    }
}

/**
 * Represents an immutable RGBA color with single-precision components.
 * <p>
 * Components are typically in the range [0.0, 1.0].
 */
public final class Color4 {
    /** The color black (R=0, G=0, B=0, A=1). */
    public static final Color4 BLACK = new Color4(0.0f, 0.0f, 0.0f, 1.0f);
    /** The color white (R=1, G=1, B=1, A=1). */
    public static final Color4 WHITE = new Color4(1.0f, 1.0f, 1.0f, 1.0f);
    
    /** The red component of the color. */
    public final float r;
    /** The green component of the color. */
    public final float g;
    /** The blue component of the color. */
    public final float b;
    /** The alpha (transparency) component of the color. */
    public final float a;

    /**
     * Constructs a new Color4 with the specified components.
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     * @param a The alpha component.
     */
    public Color4(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    
    @Override
    public String toString() {
        return String.format("<%f, %f, %f, %f>", r, g, b, a);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Color4 color4 = (Color4) obj;
        return Float.compare(color4.r, r) == 0 && 
               Float.compare(color4.g, g) == 0 && 
               Float.compare(color4.b, b) == 0 && 
               Float.compare(color4.a, a) == 0;
    }
    
    @Override
    public int hashCode() {
        int result = (r != +0.0f ? Float.floatToIntBits(r) : 0);
        result = 31 * result + (g != +0.0f ? Float.floatToIntBits(g) : 0);
        result = 31 * result + (b != +0.0f ? Float.floatToIntBits(b) : 0);
        result = 31 * result + (a != +0.0f ? Float.floatToIntBits(a) : 0);
        return result;
    }
}