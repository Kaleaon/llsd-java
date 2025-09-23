/*
 * Second Life Quaternion - Java implementation of quaternion mathematics for rotations
 *
 * Based on Second Life viewer implementation
 * Copyright (C) 2010, Linden Research, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer.secondlife.engine;

import java.util.Objects;

/**
 * Quaternion class for representing 3D rotations.
 * 
 * <p>This class provides comprehensive quaternion functionality required
 * for Second Life's 3D rotation calculations and animations.</p>
 * 
 * @since 1.0
 */
public class Quaternion {
    
    public static final Quaternion IDENTITY = new Quaternion(0.0, 0.0, 0.0, 1.0);
    
    private static final double EPSILON = 1e-6;
    
    public final double x;
    public final double y;
    public final double z;
    public final double w;
    
    /**
     * Construct a quaternion with specified components.
     */
    public Quaternion(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    
    /**
     * Construct a quaternion from an array [x, y, z, w].
     */
    public Quaternion(double[] components) {
        if (components.length != 4) {
            throw new IllegalArgumentException("Quaternion requires 4 components");
        }
        this.x = components[0];
        this.y = components[1];
        this.z = components[2];
        this.w = components[3];
    }
    
    /**
     * Copy constructor.
     */
    public Quaternion(Quaternion other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.w = other.w;
    }
    
    /**
     * Create quaternion from axis and angle (in radians).
     */
    public static Quaternion fromAxisAngle(Vector3 axis, double angle) {
        Vector3 normalizedAxis = axis.normalize();
        double halfAngle = angle * 0.5;
        double sinHalf = Math.sin(halfAngle);
        
        return new Quaternion(
            normalizedAxis.x * sinHalf,
            normalizedAxis.y * sinHalf,
            normalizedAxis.z * sinHalf,
            Math.cos(halfAngle)
        );
    }
    
    /**
     * Create quaternion from Euler angles (in radians) - ZYX order.
     */
    public static Quaternion fromEulerAngles(double pitch, double yaw, double roll) {
        double cp = Math.cos(pitch * 0.5);
        double sp = Math.sin(pitch * 0.5);
        double cy = Math.cos(yaw * 0.5);
        double sy = Math.sin(yaw * 0.5);
        double cr = Math.cos(roll * 0.5);
        double sr = Math.sin(roll * 0.5);
        
        return new Quaternion(
            sr * cp * cy - cr * sp * sy,
            cr * sp * cy + sr * cp * sy,
            cr * cp * sy - sr * sp * cy,
            cr * cp * cy + sr * sp * sy
        );
    }
    
    /**
     * Create quaternion from rotation matrix (3x3).
     */
    public static Quaternion fromMatrix3(double[][] matrix) {
        if (matrix.length != 3 || matrix[0].length != 3) {
            throw new IllegalArgumentException("Matrix must be 3x3");
        }
        
        double trace = matrix[0][0] + matrix[1][1] + matrix[2][2];
        
        if (trace > 0) {
            double s = Math.sqrt(trace + 1.0) * 2; // s = 4 * qw
            return new Quaternion(
                (matrix[2][1] - matrix[1][2]) / s,
                (matrix[0][2] - matrix[2][0]) / s,
                (matrix[1][0] - matrix[0][1]) / s,
                0.25 * s
            );
        } else if (matrix[0][0] > matrix[1][1] && matrix[0][0] > matrix[2][2]) {
            double s = Math.sqrt(1.0 + matrix[0][0] - matrix[1][1] - matrix[2][2]) * 2; // s = 4 * qx
            return new Quaternion(
                0.25 * s,
                (matrix[0][1] + matrix[1][0]) / s,
                (matrix[0][2] + matrix[2][0]) / s,
                (matrix[2][1] - matrix[1][2]) / s
            );
        } else if (matrix[1][1] > matrix[2][2]) {
            double s = Math.sqrt(1.0 + matrix[1][1] - matrix[0][0] - matrix[2][2]) * 2; // s = 4 * qy
            return new Quaternion(
                (matrix[0][1] + matrix[1][0]) / s,
                0.25 * s,
                (matrix[1][2] + matrix[2][1]) / s,
                (matrix[0][2] - matrix[2][0]) / s
            );
        } else {
            double s = Math.sqrt(1.0 + matrix[2][2] - matrix[0][0] - matrix[1][1]) * 2; // s = 4 * qz
            return new Quaternion(
                (matrix[0][2] + matrix[2][0]) / s,
                (matrix[1][2] + matrix[2][1]) / s,
                0.25 * s,
                (matrix[1][0] - matrix[0][1]) / s
            );
        }
    }
    
    /**
     * Multiply two quaternions.
     */
    public Quaternion multiply(Quaternion other) {
        return new Quaternion(
            w * other.x + x * other.w + y * other.z - z * other.y,
            w * other.y - x * other.z + y * other.w + z * other.x,
            w * other.z + x * other.y - y * other.x + z * other.w,
            w * other.w - x * other.x - y * other.y - z * other.z
        );
    }
    
    /**
     * Add two quaternions.
     */
    public Quaternion add(Quaternion other) {
        return new Quaternion(x + other.x, y + other.y, z + other.z, w + other.w);
    }
    
    /**
     * Subtract another quaternion from this one.
     */
    public Quaternion subtract(Quaternion other) {
        return new Quaternion(x - other.x, y - other.y, z - other.z, w - other.w);
    }
    
    /**
     * Scale quaternion by scalar.
     */
    public Quaternion scale(double scalar) {
        return new Quaternion(x * scalar, y * scalar, z * scalar, w * scalar);
    }
    
    /**
     * Calculate the squared norm.
     */
    public double normSquared() {
        return x * x + y * y + z * z + w * w;
    }
    
    /**
     * Calculate the norm (magnitude).
     */
    public double norm() {
        return Math.sqrt(normSquared());
    }
    
    /**
     * Normalize the quaternion.
     */
    public Quaternion normalize() {
        double n = norm();
        if (n < EPSILON) {
            return IDENTITY;
        }
        return new Quaternion(x / n, y / n, z / n, w / n);
    }
    
    /**
     * Calculate the conjugate.
     */
    public Quaternion conjugate() {
        return new Quaternion(-x, -y, -z, w);
    }
    
    /**
     * Calculate the inverse.
     */
    public Quaternion inverse() {
        double normSq = normSquared();
        if (normSq < EPSILON) {
            return IDENTITY;
        }
        return conjugate().scale(1.0 / normSq);
    }
    
    /**
     * Rotate a vector by this quaternion.
     */
    public Vector3 rotate(Vector3 vector) {
        // Convert vector to quaternion
        Quaternion vecQ = new Quaternion(vector.x, vector.y, vector.z, 0.0);
        
        // Perform rotation: q * v * q*
        Quaternion result = multiply(vecQ).multiply(conjugate());
        
        return new Vector3(result.x, result.y, result.z);
    }
    
    /**
     * Calculate dot product with another quaternion.
     */
    public double dot(Quaternion other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }
    
    /**
     * Spherical linear interpolation between two quaternions.
     */
    public Quaternion slerp(Quaternion target, double t) {
        t = Math.max(0.0, Math.min(1.0, t)); // Clamp t to [0,1]
        
        Quaternion from = normalize();
        Quaternion to = target.normalize();
        
        double dot = from.dot(to);
        
        // If quaternions are very close, use linear interpolation
        if (Math.abs(dot) > 1.0 - EPSILON) {
            return from.add(to.subtract(from).scale(t)).normalize();
        }
        
        // If dot product is negative, slerp won't take the shorter path
        if (dot < 0.0) {
            to = to.scale(-1.0);
            dot = -dot;
        }
        
        double theta = Math.acos(Math.abs(dot));
        double sinTheta = Math.sin(theta);
        
        double a = Math.sin((1.0 - t) * theta) / sinTheta;
        double b = Math.sin(t * theta) / sinTheta;
        
        return from.scale(a).add(to.scale(b));
    }
    
    /**
     * Linear interpolation between two quaternions.
     */
    public Quaternion lerp(Quaternion target, double t) {
        t = Math.max(0.0, Math.min(1.0, t)); // Clamp t to [0,1]
        
        Quaternion to = target;
        
        // Take shorter path
        if (dot(target) < 0.0) {
            to = target.scale(-1.0);
        }
        
        return add(to.subtract(this).scale(t)).normalize();
    }
    
    /**
     * Convert to axis-angle representation.
     */
    public AxisAngle toAxisAngle() {
        Quaternion q = normalize();
        
        double sinHalfAngle = Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z);
        
        if (sinHalfAngle < EPSILON) {
            return new AxisAngle(Vector3.X_AXIS, 0.0);
        }
        
        Vector3 axis = new Vector3(q.x / sinHalfAngle, q.y / sinHalfAngle, q.z / sinHalfAngle);
        double angle = 2.0 * Math.atan2(sinHalfAngle, Math.abs(q.w));
        
        return new AxisAngle(axis, angle);
    }
    
    /**
     * Convert to Euler angles (in radians) - ZYX order.
     */
    public EulerAngles toEulerAngles() {
        Quaternion q = normalize();
        
        // Roll (x-axis rotation)
        double sinr_cosp = 2 * (q.w * q.x + q.y * q.z);
        double cosr_cosp = 1 - 2 * (q.x * q.x + q.y * q.y);
        double roll = Math.atan2(sinr_cosp, cosr_cosp);
        
        // Pitch (y-axis rotation)
        double sinp = 2 * (q.w * q.y - q.z * q.x);
        double pitch;
        if (Math.abs(sinp) >= 1) {
            pitch = Math.copySign(Math.PI / 2, sinp); // use 90 degrees if out of range
        } else {
            pitch = Math.asin(sinp);
        }
        
        // Yaw (z-axis rotation)
        double siny_cosp = 2 * (q.w * q.z + q.x * q.y);
        double cosy_cosp = 1 - 2 * (q.y * q.y + q.z * q.z);
        double yaw = Math.atan2(siny_cosp, cosy_cosp);
        
        return new EulerAngles(pitch, yaw, roll);
    }
    
    /**
     * Check if quaternion is normalized (unit quaternion).
     */
    public boolean isNormalized() {
        return Math.abs(norm() - 1.0) < EPSILON;
    }
    
    /**
     * Check if quaternion represents identity rotation.
     */
    public boolean isIdentity() {
        return Math.abs(x) < EPSILON && Math.abs(y) < EPSILON && 
               Math.abs(z) < EPSILON && Math.abs(Math.abs(w) - 1.0) < EPSILON;
    }
    
    /**
     * Convert to array representation [x, y, z, w].
     */
    public double[] toArray() {
        return new double[]{x, y, z, w};
    }
    
    /**
     * Convert to 3x3 rotation matrix.
     */
    public double[][] toMatrix3() {
        Quaternion q = normalize();
        
        double xx = q.x * q.x;
        double xy = q.x * q.y;
        double xz = q.x * q.z;
        double xw = q.x * q.w;
        double yy = q.y * q.y;
        double yz = q.y * q.z;
        double yw = q.y * q.w;
        double zz = q.z * q.z;
        double zw = q.z * q.w;
        
        return new double[][]{
            {1 - 2 * (yy + zz), 2 * (xy - zw), 2 * (xz + yw)},
            {2 * (xy + zw), 1 - 2 * (xx + zz), 2 * (yz - xw)},
            {2 * (xz - yw), 2 * (yz + xw), 1 - 2 * (xx + yy)}
        };
    }
    
    /**
     * Parse quaternion from string representation "x,y,z,w".
     */
    public static Quaternion parse(String str) {
        if (str == null || str.trim().isEmpty()) {
            return IDENTITY;
        }
        
        String[] parts = str.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Quaternion string must have 4 components: " + str);
        }
        
        try {
            return new Quaternion(
                Double.parseDouble(parts[0].trim()),
                Double.parseDouble(parts[1].trim()),
                Double.parseDouble(parts[2].trim()),
                Double.parseDouble(parts[3].trim())
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Quaternion string: " + str, e);
        }
    }
    
    /**
     * Axis-angle representation.
     */
    public static class AxisAngle {
        public final Vector3 axis;
        public final double angle;
        
        public AxisAngle(Vector3 axis, double angle) {
            this.axis = axis;
            this.angle = angle;
        }
    }
    
    /**
     * Euler angles representation.
     */
    public static class EulerAngles {
        public final double pitch; // X-axis rotation
        public final double yaw;   // Z-axis rotation  
        public final double roll;  // Y-axis rotation
        
        public EulerAngles(double pitch, double yaw, double roll) {
            this.pitch = pitch;
            this.yaw = yaw;
            this.roll = roll;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Quaternion that = (Quaternion) obj;
        return Math.abs(that.x - x) < EPSILON &&
               Math.abs(that.y - y) < EPSILON &&
               Math.abs(that.z - z) < EPSILON &&
               Math.abs(that.w - w) < EPSILON;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(
            Math.round(x / EPSILON),
            Math.round(y / EPSILON),
            Math.round(z / EPSILON),
            Math.round(w / EPSILON)
        );
    }
    
    @Override
    public String toString() {
        return String.format("Quaternion(%.6f, %.6f, %.6f, %.6f)", x, y, z, w);
    }
    
    /**
     * Short string representation for debugging.
     */
    public String toShortString() {
        return String.format("(%.2f, %.2f, %.2f, %.2f)", x, y, z, w);
    }
}