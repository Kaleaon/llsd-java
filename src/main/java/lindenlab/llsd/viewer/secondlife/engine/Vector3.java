/*
 * Second Life Vector3 - Java implementation of 3D vector mathematics
 *
 * Based on Second Life viewer implementation
 * Copyright (C) 2010, Linden Research, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer.secondlife.engine;

import java.util.Objects;

/**
 * Three-dimensional vector class with mathematical operations.
 * 
 * <p>This class provides comprehensive 3D vector functionality required
 * for Second Life's 3D rendering and physics calculations.</p>
 * 
 * @since 1.0
 */
public class Vector3 {
    
    public static final Vector3 ZERO = new Vector3(0.0, 0.0, 0.0);
    public static final Vector3 ONE = new Vector3(1.0, 1.0, 1.0);
    public static final Vector3 X_AXIS = new Vector3(1.0, 0.0, 0.0);
    public static final Vector3 Y_AXIS = new Vector3(0.0, 1.0, 0.0);
    public static final Vector3 Z_AXIS = new Vector3(0.0, 0.0, 1.0);
    
    private static final double EPSILON = 1e-6;
    
    public final double x;
    public final double y;
    public final double z;
    
    /**
     * Construct a vector with specified components.
     */
    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Construct a vector from an array.
     */
    public Vector3(double[] components) {
        if (components.length != 3) {
            throw new IllegalArgumentException("Vector3 requires 3 components");
        }
        this.x = components[0];
        this.y = components[1];
        this.z = components[2];
    }
    
    /**
     * Copy constructor.
     */
    public Vector3(Vector3 other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }
    
    /**
     * Add two vectors.
     */
    public Vector3 add(Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }
    
    /**
     * Subtract another vector from this one.
     */
    public Vector3 subtract(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }
    
    /**
     * Multiply by scalar.
     */
    public Vector3 multiply(double scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }
    
    /**
     * Component-wise multiplication.
     */
    public Vector3 multiply(Vector3 other) {
        return new Vector3(x * other.x, y * other.y, z * other.z);
    }
    
    /**
     * Divide by scalar.
     */
    public Vector3 divide(double scalar) {
        if (Math.abs(scalar) < EPSILON) {
            throw new ArithmeticException("Division by zero or near-zero value");
        }
        return new Vector3(x / scalar, y / scalar, z / scalar);
    }
    
    /**
     * Component-wise division.
     */
    public Vector3 divide(Vector3 other) {
        if (Math.abs(other.x) < EPSILON || Math.abs(other.y) < EPSILON || Math.abs(other.z) < EPSILON) {
            throw new ArithmeticException("Division by zero or near-zero component");
        }
        return new Vector3(x / other.x, y / other.y, z / other.z);
    }
    
    /**
     * Negate the vector.
     */
    public Vector3 negate() {
        return new Vector3(-x, -y, -z);
    }
    
    /**
     * Calculate dot product.
     */
    public double dot(Vector3 other) {
        return x * other.x + y * other.y + z * other.z;
    }
    
    /**
     * Calculate cross product.
     */
    public Vector3 cross(Vector3 other) {
        return new Vector3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }
    
    /**
     * Calculate the squared magnitude.
     */
    public double magnitudeSquared() {
        return x * x + y * y + z * z;
    }
    
    /**
     * Calculate the magnitude.
     */
    public double magnitude() {
        return Math.sqrt(magnitudeSquared());
    }
    
    /**
     * Normalize the vector.
     */
    public Vector3 normalize() {
        double mag = magnitude();
        if (mag < EPSILON) {
            return ZERO;
        }
        return divide(mag);
    }
    
    /**
     * Calculate distance to another vector.
     */
    public double distance(Vector3 other) {
        return subtract(other).magnitude();
    }
    
    /**
     * Calculate squared distance to another vector.
     */
    public double distanceSquared(Vector3 other) {
        return subtract(other).magnitudeSquared();
    }
    
    /**
     * Linear interpolation between two vectors.
     */
    public Vector3 lerp(Vector3 target, double t) {
        t = Math.max(0.0, Math.min(1.0, t)); // Clamp t to [0,1]
        return add(target.subtract(this).multiply(t));
    }
    
    /**
     * Spherical linear interpolation between two vectors.
     */
    public Vector3 slerp(Vector3 target, double t) {
        t = Math.max(0.0, Math.min(1.0, t)); // Clamp t to [0,1]
        
        Vector3 from = normalize();
        Vector3 to = target.normalize();
        
        double dot = from.dot(to);
        
        // If vectors are very close, use linear interpolation
        if (Math.abs(dot) > 1.0 - EPSILON) {
            return from.lerp(to, t);
        }
        
        double theta = Math.acos(Math.abs(dot));
        double sinTheta = Math.sin(theta);
        
        double a = Math.sin((1.0 - t) * theta) / sinTheta;
        double b = Math.sin(t * theta) / sinTheta;
        
        if (dot < 0.0) {
            to = to.negate();
        }
        
        return from.multiply(a).add(to.multiply(b));
    }
    
    /**
     * Check if vector is zero (within epsilon).
     */
    public boolean isZero() {
        return Math.abs(x) < EPSILON && Math.abs(y) < EPSILON && Math.abs(z) < EPSILON;
    }
    
    /**
     * Check if vector is normalized (unit length).
     */
    public boolean isNormalized() {
        return Math.abs(magnitude() - 1.0) < EPSILON;
    }
    
    /**
     * Project this vector onto another vector.
     */
    public Vector3 project(Vector3 onto) {
        double ontoMagSq = onto.magnitudeSquared();
        if (ontoMagSq < EPSILON) {
            return ZERO;
        }
        return onto.multiply(dot(onto) / ontoMagSq);
    }
    
    /**
     * Reflect this vector off a surface with given normal.
     */
    public Vector3 reflect(Vector3 normal) {
        return subtract(normal.multiply(2.0 * dot(normal)));
    }
    
    /**
     * Get the angle between this vector and another (in radians).
     */
    public double angleTo(Vector3 other) {
        Vector3 a = normalize();
        Vector3 b = other.normalize();
        return Math.acos(Math.max(-1.0, Math.min(1.0, a.dot(b))));
    }
    
    /**
     * Convert to array representation.
     */
    public double[] toArray() {
        return new double[]{x, y, z};
    }
    
    /**
     * Get component by index (0=x, 1=y, 2=z).
     */
    public double getComponent(int index) {
        switch (index) {
            case 0: return x;
            case 1: return y;
            case 2: return z;
            default: throw new IndexOutOfBoundsException("Vector3 index must be 0, 1, or 2");
        }
    }
    
    /**
     * Create vector with component replaced.
     */
    public Vector3 withX(double newX) {
        return new Vector3(newX, y, z);
    }
    
    public Vector3 withY(double newY) {
        return new Vector3(x, newY, z);
    }
    
    public Vector3 withZ(double newZ) {
        return new Vector3(x, y, newZ);
    }
    
    /**
     * Calculate minimum components.
     */
    public static Vector3 min(Vector3 a, Vector3 b) {
        return new Vector3(
            Math.min(a.x, b.x),
            Math.min(a.y, b.y),
            Math.min(a.z, b.z)
        );
    }
    
    /**
     * Calculate maximum components.
     */
    public static Vector3 max(Vector3 a, Vector3 b) {
        return new Vector3(
            Math.max(a.x, b.x),
            Math.max(a.y, b.y),
            Math.max(a.z, b.z)
        );
    }
    
    /**
     * Parse vector from string representation "x,y,z".
     */
    public static Vector3 parse(String str) {
        if (str == null || str.trim().isEmpty()) {
            return ZERO;
        }
        
        String[] parts = str.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Vector3 string must have 3 components: " + str);
        }
        
        try {
            return new Vector3(
                Double.parseDouble(parts[0].trim()),
                Double.parseDouble(parts[1].trim()),
                Double.parseDouble(parts[2].trim())
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Vector3 string: " + str, e);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector3 vector3 = (Vector3) obj;
        return Math.abs(vector3.x - x) < EPSILON &&
               Math.abs(vector3.y - y) < EPSILON &&
               Math.abs(vector3.z - z) < EPSILON;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(
            Math.round(x / EPSILON),
            Math.round(y / EPSILON),
            Math.round(z / EPSILON)
        );
    }
    
    @Override
    public String toString() {
        return String.format("Vector3(%.6f, %.6f, %.6f)", x, y, z);
    }
    
    /**
     * Short string representation for debugging.
     */
    public String toShortString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}