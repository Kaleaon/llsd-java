/*
 * Texture Transform - Java implementation of texture coordinate transformation
 *
 * Based on Second Life texture transform and glTF standards
 * Copyright (C) 2010, Linden Research, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer.secondlife.engine.rendering;

import lindenlab.llsd.viewer.secondlife.engine.Vector3;
import java.util.*;

/**
 * Texture coordinate transformation for materials.
 * 
 * <p>This class handles UV coordinate transformations including offset,
 * scale, and rotation operations compatible with Second Life and glTF standards.</p>
 * 
 * @since 1.0
 */
public class TextureTransform {
    
    /** The texture coordinate offset (u, v). */
    private Vector3 offset;
    /** The texture coordinate scale (u, v). */
    private Vector3 scale;
    /** The rotation of the texture coordinates in radians. */
    private double rotation;
    /** The index of the texture coordinate set to be transformed. */
    private int texCoord;
    
    /**
     * Constructs a new {@code TextureTransform} with default identity values
     * (no offset, scale of 1, no rotation).
     */
    public TextureTransform() {
        this.offset = Vector3.ZERO;
        this.scale = new Vector3(1.0, 1.0, 1.0);
        this.rotation = 0.0;
        this.texCoord = 0;
    }
    
    /**
     * Constructs a new {@code TextureTransform} with the specified offset and scale.
     *
     * @param offset The texture coordinate offset.
     * @param scale The texture coordinate scale.
     */
    public TextureTransform(Vector3 offset, Vector3 scale) {
        this.offset = offset != null ? offset : Vector3.ZERO;
        this.scale = scale != null ? scale : new Vector3(1.0, 1.0, 1.0);
        this.rotation = 0.0;
        this.texCoord = 0;
    }
    
    /**
     * Constructs a new {@code TextureTransform} with all parameters.
     *
     * @param offset The texture coordinate offset.
     * @param scale The texture coordinate scale.
     * @param rotation The rotation in radians.
     * @param texCoord The texture coordinate set index.
     */
    public TextureTransform(Vector3 offset, Vector3 scale, double rotation, int texCoord) {
        this.offset = offset != null ? offset : Vector3.ZERO;
        this.scale = scale != null ? scale : new Vector3(1.0, 1.0, 1.0);
        this.rotation = rotation;
        this.texCoord = Math.max(0, texCoord);
    }
    
    /**
     * Constructs a new {@code TextureTransform} from 2D components.
     *
     * @param offsetU The U-component of the offset.
     * @param offsetV The V-component of the offset.
     * @param scaleU The U-component of the scale.
     * @param scaleV The V-component of the scale.
     */
    public TextureTransform(double offsetU, double offsetV, double scaleU, double scaleV) {
        this.offset = new Vector3(offsetU, offsetV, 0.0);
        this.scale = new Vector3(scaleU, scaleV, 1.0);
        this.rotation = 0.0;
        this.texCoord = 0;
    }
    
    /**
     * Constructs a new {@code TextureTransform} from 2D components with rotation.
     *
     * @param offsetU The U-component of the offset.
     * @param offsetV The V-component of the offset.
     * @param scaleU The U-component of the scale.
     * @param scaleV The V-component of the scale.
     * @param rotation The rotation in radians.
     */
    public TextureTransform(double offsetU, double offsetV, double scaleU, double scaleV, double rotation) {
        this.offset = new Vector3(offsetU, offsetV, 0.0);
        this.scale = new Vector3(scaleU, scaleV, 1.0);
        this.rotation = rotation;
        this.texCoord = 0;
    }
    
    /** @return The texture coordinate offset. */
    public Vector3 getOffset() { return offset; }
    /** @return The texture coordinate scale. */
    public Vector3 getScale() { return scale; }
    /** @return The rotation in radians. */
    public double getRotation() { return rotation; }
    /** @return The texture coordinate set index. */
    public int getTexCoord() { return texCoord; }
    
    /** @return The U-component of the offset. */
    public double getOffsetU() { return offset.x; }
    /** @return The V-component of the offset. */
    public double getOffsetV() { return offset.y; }
    /** @return The U-component of the scale. */
    public double getScaleU() { return scale.x; }
    /** @return The V-component of the scale. */
    public double getScaleV() { return scale.y; }
    
    /** @param offset The new texture coordinate offset. */
    public void setOffset(Vector3 offset) {
        this.offset = offset != null ? offset : Vector3.ZERO;
    }
    
    /** @param scale The new texture coordinate scale. */
    public void setScale(Vector3 scale) {
        this.scale = scale != null ? scale : new Vector3(1.0, 1.0, 1.0);
    }
    
    /** @param rotation The new rotation in radians. */
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }
    
    /** @param texCoord The new texture coordinate set index. */
    public void setTexCoord(int texCoord) {
        this.texCoord = Math.max(0, texCoord);
    }
    
    /** @param u The new U-component of the offset.
     *  @param v The new V-component of the offset.
     */
    public void setOffset(double u, double v) {
        this.offset = new Vector3(u, v, 0.0);
    }
    
    /** @param u The new U-component of the scale.
     *  @param v The new V-component of the scale.
     */
    public void setScale(double u, double v) {
        this.scale = new Vector3(u, v, 1.0);
    }
    
    /** @param u The new U-component of the offset. */
    public void setOffsetU(double u) {
        this.offset = new Vector3(u, offset.y, offset.z);
    }
    
    /** @param v The new V-component of the offset. */
    public void setOffsetV(double v) {
        this.offset = new Vector3(offset.x, v, offset.z);
    }
    
    /** @param u The new U-component of the scale. */
    public void setScaleU(double u) {
        this.scale = new Vector3(u, scale.y, scale.z);
    }
    
    /** @param v The new V-component of the scale. */
    public void setScaleV(double v) {
        this.scale = new Vector3(scale.x, v, scale.z);
    }
    
    /**
     * Checks if this transform is an identity transform (i.e., it has no effect).
     *
     * @return {@code true} if the transform is an identity transform, {@code false} otherwise.
     */
    public boolean isIdentity() {
        return offset.isZero() &&
               Math.abs(scale.x - 1.0) < 1e-6 &&
               Math.abs(scale.y - 1.0) < 1e-6 &&
               Math.abs(rotation) < 1e-6;
    }
    
    /**
     * Applies this transform to a set of UV coordinates.
     *
     * @param uv The input UV coordinates.
     * @return A new {@link Vector3} representing the transformed UV coordinates.
     */
    public Vector3 transform(Vector3 uv) {
        if (isIdentity()) {
            return uv;
        }
        
        // Apply scale first
        double u = uv.x * scale.x;
        double v = uv.y * scale.y;
        
        // Apply rotation around center (0.5, 0.5)
        if (Math.abs(rotation) > 1e-6) {
            double centerU = u - 0.5;
            double centerV = v - 0.5;
            
            double cos = Math.cos(rotation);
            double sin = Math.sin(rotation);
            
            double rotatedU = centerU * cos - centerV * sin;
            double rotatedV = centerU * sin + centerV * cos;
            
            u = rotatedU + 0.5;
            v = rotatedV + 0.5;
        }
        
        // Apply offset
        u += offset.x;
        v += offset.y;
        
        return new Vector3(u, v, uv.z);
    }
    
    /**
     * Applies the inverse of this transform to a set of UV coordinates.
     *
     * @param uv The input UV coordinates.
     * @return A new {@link Vector3} representing the inversely transformed UV coordinates.
     */
    public Vector3 inverseTransform(Vector3 uv) {
        if (isIdentity()) {
            return uv;
        }
        
        // Remove offset
        double u = uv.x - offset.x;
        double v = uv.y - offset.y;
        
        // Apply inverse rotation
        if (Math.abs(rotation) > 1e-6) {
            double centerU = u - 0.5;
            double centerV = v - 0.5;
            
            double cos = Math.cos(-rotation);
            double sin = Math.sin(-rotation);
            
            double rotatedU = centerU * cos - centerV * sin;
            double rotatedV = centerU * sin + centerV * cos;
            
            u = rotatedU + 0.5;
            v = rotatedV + 0.5;
        }
        
        // Apply inverse scale
        if (Math.abs(scale.x) > 1e-6) u /= scale.x;
        if (Math.abs(scale.y) > 1e-6) v /= scale.y;
        
        return new Vector3(u, v, uv.z);
    }
    
    /**
     * Gets the 3x3 transformation matrix for this transform, suitable for 2D homogeneous coordinates.
     *
     * @return A 3x3 matrix representing the combined scale, rotation, and translation.
     */
    public double[][] getTransformMatrix() {
        double[][] matrix = new double[3][3];
        
        // Identity matrix
        matrix[0][0] = 1.0; matrix[0][1] = 0.0; matrix[0][2] = 0.0;
        matrix[1][0] = 0.0; matrix[1][1] = 1.0; matrix[1][2] = 0.0;
        matrix[2][0] = 0.0; matrix[2][1] = 0.0; matrix[2][2] = 1.0;
        
        if (isIdentity()) {
            return matrix;
        }
        
        // Translation matrix
        double[][] translation = {
            {1.0, 0.0, offset.x},
            {0.0, 1.0, offset.y},
            {0.0, 0.0, 1.0}
        };
        
        // Rotation matrix (around center 0.5, 0.5)
        double cos = Math.cos(rotation);
        double sin = Math.sin(rotation);
        double[][] rotationMatrix = {
            {cos, -sin, 0.5 - 0.5 * cos + 0.5 * sin},
            {sin, cos, 0.5 - 0.5 * sin - 0.5 * cos},
            {0.0, 0.0, 1.0}
        };
        
        // Scale matrix
        double[][] scaleMatrix = {
            {scale.x, 0.0, 0.0},
            {0.0, scale.y, 0.0},
            {0.0, 0.0, 1.0}
        };
        
        // Combine: T * R * S
        matrix = multiplyMatrix3x3(translation, multiplyMatrix3x3(rotationMatrix, scaleMatrix));
        
        return matrix;
    }
    
    /**
     * Combines this transform with another transform.
     *
     * @param other The other transform to combine with this one.
     * @return A new {@code TextureTransform} representing the combined transformation.
     */
    public TextureTransform combine(TextureTransform other) {
        if (other == null || other.isIdentity()) {
            return new TextureTransform(offset, scale, rotation, texCoord);
        }
        
        if (isIdentity()) {
            return new TextureTransform(other.offset, other.scale, other.rotation, other.texCoord);
        }
        
        // For now, simple combination - could be improved with matrix math
        Vector3 combinedOffset = offset.add(other.offset);
        Vector3 combinedScale = new Vector3(scale.x * other.scale.x, scale.y * other.scale.y, scale.z);
        double combinedRotation = rotation + other.rotation;
        
        return new TextureTransform(combinedOffset, combinedScale, combinedRotation, texCoord);
    }
    
    /**
     * Creates the inverse of this transform.
     *
     * @return A new {@code TextureTransform} representing the inverse transformation.
     */
    public TextureTransform inverse() {
        if (isIdentity()) {
            return new TextureTransform();
        }
        
        Vector3 invScale = new Vector3(
            Math.abs(scale.x) > 1e-6 ? 1.0 / scale.x : 1.0,
            Math.abs(scale.y) > 1e-6 ? 1.0 / scale.y : 1.0,
            1.0
        );
        
        Vector3 invOffset = new Vector3(-offset.x, -offset.y, 0.0);
        double invRotation = -rotation;
        
        return new TextureTransform(invOffset, invScale, invRotation, texCoord);
    }
    
    /**
     * Converts this transform's properties into an LLSD map representation.
     *
     * @return A {@link Map} suitable for serialization.
     */
    public Map<String, Object> toLLSD() {
        Map<String, Object> transformData = new HashMap<>();
        
        if (!offset.isZero()) {
            transformData.put("Offset", Arrays.asList(offset.x, offset.y));
        }
        
        if (Math.abs(scale.x - 1.0) > 1e-6 || Math.abs(scale.y - 1.0) > 1e-6) {
            transformData.put("Scale", Arrays.asList(scale.x, scale.y));
        }
        
        if (Math.abs(rotation) > 1e-6) {
            transformData.put("Rotation", rotation);
        }
        
        if (texCoord != 0) {
            transformData.put("TexCoord", texCoord);
        }
        
        return transformData;
    }
    
    /**
     * Creates a {@code TextureTransform} from an LLSD map.
     *
     * @param data The LLSD map containing the transform data.
     * @return A new {@code TextureTransform} instance.
     */
    @SuppressWarnings("unchecked")
    public static TextureTransform fromLLSD(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return new TextureTransform();
        }
        
        Vector3 offset = Vector3.ZERO;
        Vector3 scale = new Vector3(1.0, 1.0, 1.0);
        double rotation = 0.0;
        int texCoord = 0;
        
        if (data.containsKey("Offset")) {
            List<Double> offsetData = (List<Double>) data.get("Offset");
            if (offsetData.size() >= 2) {
                offset = new Vector3(offsetData.get(0), offsetData.get(1), 0.0);
            }
        }
        
        if (data.containsKey("Scale")) {
            List<Double> scaleData = (List<Double>) data.get("Scale");
            if (scaleData.size() >= 2) {
                scale = new Vector3(scaleData.get(0), scaleData.get(1), 1.0);
            }
        }
        
        if (data.containsKey("Rotation")) {
            rotation = ((Number) data.get("Rotation")).doubleValue();
        }
        
        if (data.containsKey("TexCoord")) {
            texCoord = ((Number) data.get("TexCoord")).intValue();
        }
        
        return new TextureTransform(offset, scale, rotation, texCoord);
    }
    
    /**
     * Creates a {@code TextureTransform} from the Second Life texture animation format parameters.
     *
     * @param offsetU The U-component of the offset.
     * @param offsetV The V-component of the offset.
     * @param scaleU The U-component of the scale.
     * @param scaleV The V-component of the scale.
     * @param rotation The rotation in radians.
     * @return A new {@code TextureTransform} instance.
     */
    public static TextureTransform fromSLFormat(double offsetU, double offsetV, 
                                               double scaleU, double scaleV, double rotation) {
        return new TextureTransform(offsetU, offsetV, scaleU, scaleV, rotation);
    }
    
    /**
     * Converts this transform to the Second Life texture animation format.
     *
     * @return A double array containing {@code [offsetU, offsetV, scaleU, scaleV, rotation]}.
     */
    public double[] toSLFormat() {
        return new double[]{offset.x, offset.y, scale.x, scale.y, rotation};
    }
    
    private static double[][] multiplyMatrix3x3(double[][] a, double[][] b) {
        double[][] result = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
            }
        }
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TextureTransform that = (TextureTransform) obj;
        return Double.compare(that.rotation, rotation) == 0 &&
               texCoord == that.texCoord &&
               Objects.equals(offset, that.offset) &&
               Objects.equals(scale, that.scale);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(offset, scale, rotation, texCoord);
    }
    
    @Override
    public String toString() {
        return String.format("TextureTransform[offset=%s, scale=%s, rotation=%.3f, texCoord=%d]",
                           offset.toShortString(), scale.toShortString(), rotation, texCoord);
    }
}