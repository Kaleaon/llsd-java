/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews
 * Updated 2024 based on Second Life viewer and LibreMetaverse implementations
 */

package lindenlab.llsd;

/**
 * Two-dimensional vector
 */
class Vector2 {
    public static final Vector2 ZERO = new Vector2(0.0f, 0.0f);
    
    public final float x;
    public final float y;
    
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
 * Three-dimensional vector  
 */
class Vector3 {
    public static final Vector3 ZERO = new Vector3(0.0f, 0.0f, 0.0f);
    
    public final float x;
    public final float y;
    public final float z;
    
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
 * Four-dimensional vector
 */
class Vector4 {
    public static final Vector4 ZERO = new Vector4(0.0f, 0.0f, 0.0f, 0.0f);
    
    public final float x;
    public final float y;
    public final float z;
    public final float w;
    
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
 * Quaternion for 3D rotation
 */
class Quaternion {
    public static final Quaternion IDENTITY = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
    
    public final float x;
    public final float y;
    public final float z;
    public final float w;
    
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
 * RGBA Color
 */
class Color4 {
    public static final Color4 BLACK = new Color4(0.0f, 0.0f, 0.0f, 1.0f);
    public static final Color4 WHITE = new Color4(1.0f, 1.0f, 1.0f, 1.0f);
    
    public final float r;
    public final float g;
    public final float b;
    public final float a;
    
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