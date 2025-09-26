/*
 * PBR Material - Java implementation of Physically Based Rendering materials
 *
 * Based on Second Life PBR implementation and modern rendering standards
 * Copyright (C) 2010, Linden Research, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer.secondlife.engine.rendering;

import lindenlab.llsd.viewer.secondlife.engine.Vector3;
import java.util.*;
import java.util.UUID;

/**
 * Physically Based Rendering (PBR) material system.
 * 
 * <p>This class implements a modern PBR material system compatible with Second Life
 * and modern rendering standards. It supports metallic-roughness workflow with
 * additional features for virtual world rendering.</p>
 * 
 * @since 1.0
 */
public class PBRMaterial {
    
    /** The base color of the material. */
    private Vector3 baseColor;
    /** The metallicness of the material (0.0 for non-metallic, 1.0 for metallic). */
    private double metallic;
    /** The roughness of the material surface (0.0 for smooth, 1.0 for rough). */
    private double roughness;
    /** The emissive color of the material. */
    private Vector3 emissive;
    /** The strength of the emissive light. */
    private double emissiveStrength;
    /** The strength of the normal map effect. */
    private double normalStrength;
    /** The strength of the ambient occlusion effect. */
    private double occlusionStrength;
    /** The alpha cutoff value for MASK blend mode. */
    private double alphaCutoff;
    /** The blend mode of the material (e.g., OPAQUE, BLEND). */
    private BlendMode blendMode;
    /** The face culling mode for this material. */
    private CullMode cullMode;
    
    /** The UUID of the base color (albedo) texture. */
    private UUID baseColorTexture;
    /** The UUID of the metallic-roughness texture. */
    private UUID metallicRoughnessTexture;
    /** The UUID of the normal map texture. */
    private UUID normalTexture;
    /** The UUID of the emissive map texture. */
    private UUID emissiveTexture;
    /** The UUID of the ambient occlusion texture. */
    private UUID occlusionTexture;
    /** The UUID of the height map texture (for parallax effects). */
    private UUID heightTexture;
    
    /** The texture transform for the base color texture. */
    private TextureTransform baseColorTransform;
    /** The texture transform for the metallic-roughness texture. */
    private TextureTransform metallicRoughnessTransform;
    /** The texture transform for the normal map texture. */
    private TextureTransform normalTransform;
    /** The texture transform for the emissive map texture. */
    private TextureTransform emissiveTransform;
    /** The texture transform for the ambient occlusion texture. */
    private TextureTransform occlusionTransform;
    /** The texture transform for the height map texture. */
    private TextureTransform heightTransform;
    
    /** The specular factor for the KHR_materials_specular extension. */
    private double specularFactor;
    /** The specular color factor for the KHR_materials_specular extension. */
    private Vector3 specularColorFactor;
    /** The UUID of the specular texture. */
    private UUID specularTexture;
    /** The UUID of the specular color texture. */
    private UUID specularColorTexture;
    /** The texture transform for the specular texture. */
    private TextureTransform specularTransform;
    /** The texture transform for the specular color texture. */
    private TextureTransform specularColorTransform;
    
    /** The strength of the clearcoat layer. */
    private double clearcoatFactor;
    /** The roughness of the clearcoat layer. */
    private double clearcoatRoughnessFactor;
    /** The scale of the clearcoat normal map. */
    private Vector3 clearcoatNormalScale;
    /** The UUID of the clearcoat texture. */
    private UUID clearcoatTexture;
    /** The UUID of the clearcoat roughness texture. */
    private UUID clearcoatRoughnessTexture;
    /** The UUID of the clearcoat normal map texture. */
    private UUID clearcoatNormalTexture;
    
    /** If true, the material is rendered at full brightness, ignoring lighting. */
    private boolean fullbright;
    /** The glow effect intensity (a Second Life specific feature). */
    private double glow;
    /** The alpha blending mode (a Second Life specific feature). */
    private double alphaMode;
    /** If true, both sides of the geometry are rendered. */
    private boolean doubleSided;
    
    /**
     * An enumeration of the blend modes available for material rendering.
     */
    public enum BlendMode {
        /** The material is fully opaque, and alpha values are ignored. */
        OPAQUE,
        /** The material is either fully opaque or fully transparent, based on an alpha cutoff. */
        MASK,
        /** The material is blended with the background based on its alpha value. */
        BLEND
    }
    
    /**
     * An enumeration of the face culling modes.
     */
    public enum CullMode {
        /** No face culling is performed; both sides of the geometry are rendered. */
        NONE,
        /** Front-facing polygons are culled. */
        FRONT,
        /** Back-facing polygons are culled (this is the default for most rendering). */
        BACK
    }
    
    /**
     * Constructs a new {@code PBRMaterial} with default values that represent a
     * non-metallic, fully rough, white surface.
     */
    public PBRMaterial() {
        this.baseColor = new Vector3(1.0, 1.0, 1.0);
        this.metallic = 0.0;
        this.roughness = 1.0;
        this.emissive = Vector3.ZERO;
        this.emissiveStrength = 1.0;
        this.normalStrength = 1.0;
        this.occlusionStrength = 1.0;
        this.alphaCutoff = 0.5;
        this.blendMode = BlendMode.OPAQUE;
        this.cullMode = CullMode.BACK;
        
        this.baseColorTransform = new TextureTransform();
        this.metallicRoughnessTransform = new TextureTransform();
        this.normalTransform = new TextureTransform();
        this.emissiveTransform = new TextureTransform();
        this.occlusionTransform = new TextureTransform();
        this.heightTransform = new TextureTransform();
        
        this.specularFactor = 1.0;
        this.specularColorFactor = new Vector3(1.0, 1.0, 1.0);
        this.specularTransform = new TextureTransform();
        this.specularColorTransform = new TextureTransform();
        
        this.clearcoatFactor = 0.0;
        this.clearcoatRoughnessFactor = 0.0;
        this.clearcoatNormalScale = new Vector3(1.0, 1.0, 1.0);
        
        this.fullbright = false;
        this.glow = 0.0;
        this.alphaMode = 1.0;
        this.doubleSided = false;
    }
    
    /** @return The base color of the material. */
    public Vector3 getBaseColor() { return baseColor; }
    /** @return The metallicness of the material. */
    public double getMetallic() { return metallic; }
    /** @return The roughness of the material. */
    public double getRoughness() { return roughness; }
    /** @return The emissive color of the material. */
    public Vector3 getEmissive() { return emissive; }
    /** @return The strength of the emissive light. */
    public double getEmissiveStrength() { return emissiveStrength; }
    /** @return The strength of the normal map effect. */
    public double getNormalStrength() { return normalStrength; }
    /** @return The strength of the ambient occlusion effect. */
    public double getOcclusionStrength() { return occlusionStrength; }
    /** @return The alpha cutoff value for MASK blend mode. */
    public double getAlphaCutoff() { return alphaCutoff; }
    /** @return The blend mode of the material. */
    public BlendMode getBlendMode() { return blendMode; }
    /** @return The face culling mode for this material. */
    public CullMode getCullMode() { return cullMode; }
    
    /** @return The UUID of the base color texture, or null if not set. */
    public UUID getBaseColorTexture() { return baseColorTexture; }
    /** @return The UUID of the metallic-roughness texture, or null if not set. */
    public UUID getMetallicRoughnessTexture() { return metallicRoughnessTexture; }
    /** @return The UUID of the normal map texture, or null if not set. */
    public UUID getNormalTexture() { return normalTexture; }
    /** @return The UUID of the emissive map texture, or null if not set. */
    public UUID getEmissiveTexture() { return emissiveTexture; }
    /** @return The UUID of the ambient occlusion texture, or null if not set. */
    public UUID getOcclusionTexture() { return occlusionTexture; }
    /** @return The UUID of the height map texture, or null if not set. */
    public UUID getHeightTexture() { return heightTexture; }
    
    /** @return The texture transform for the base color texture. */
    public TextureTransform getBaseColorTransform() { return baseColorTransform; }
    /** @return The texture transform for the metallic-roughness texture. */
    public TextureTransform getMetallicRoughnessTransform() { return metallicRoughnessTransform; }
    /** @return The texture transform for the normal map texture. */
    public TextureTransform getNormalTransform() { return normalTransform; }
    /** @return The texture transform for the emissive map texture. */
    public TextureTransform getEmissiveTransform() { return emissiveTransform; }
    /** @return The texture transform for the ambient occlusion texture. */
    public TextureTransform getOcclusionTransform() { return occlusionTransform; }
    /** @return The texture transform for the height map texture. */
    public TextureTransform getHeightTransform() { return heightTransform; }
    
    /** @return The specular factor for the KHR_materials_specular extension. */
    public double getSpecularFactor() { return specularFactor; }
    /** @return The specular color factor for the KHR_materials_specular extension. */
    public Vector3 getSpecularColorFactor() { return specularColorFactor; }
    /** @return The UUID of the specular texture, or null if not set. */
    public UUID getSpecularTexture() { return specularTexture; }
    /** @return The UUID of the specular color texture, or null if not set. */
    public UUID getSpecularColorTexture() { return specularColorTexture; }
    
    /** @return The strength of the clearcoat layer. */
    public double getClearcoatFactor() { return clearcoatFactor; }
    /** @return The roughness of the clearcoat layer. */
    public double getClearcoatRoughnessFactor() { return clearcoatRoughnessFactor; }
    /** @return The scale of the clearcoat normal map. */
    public Vector3 getClearcoatNormalScale() { return clearcoatNormalScale; }
    /** @return The UUID of the clearcoat texture, or null if not set. */
    public UUID getClearcoatTexture() { return clearcoatTexture; }
    /** @return The UUID of the clearcoat roughness texture, or null if not set. */
    public UUID getClearcoatRoughnessTexture() { return clearcoatRoughnessTexture; }
    /** @return The UUID of the clearcoat normal map texture, or null if not set. */
    public UUID getClearcoatNormalTexture() { return clearcoatNormalTexture; }
    
    /** @return True if the material is rendered at full brightness. */
    public boolean isFullbright() { return fullbright; }
    /** @return The glow effect intensity. */
    public double getGlow() { return glow; }
    /** @return The alpha blending mode. */
    public double getAlphaMode() { return alphaMode; }
    /** @return True if both sides of the geometry are rendered. */
    public boolean isDoubleSided() { return doubleSided; }
    
    /** @param baseColor The new base color, clamped to the range [0, 1]. */
    public void setBaseColor(Vector3 baseColor) {
        this.baseColor = clampColor(baseColor);
    }
    
    /** @param metallic The new metallicness value, clamped to the range [0, 1]. */
    public void setMetallic(double metallic) {
        this.metallic = Math.max(0.0, Math.min(1.0, metallic));
    }
    
    /** @param roughness The new roughness value, clamped to the range [0, 1]. */
    public void setRoughness(double roughness) {
        this.roughness = Math.max(0.0, Math.min(1.0, roughness));
    }
    
    /** @param emissive The new emissive color, clamped to the range [0, 1]. */
    public void setEmissive(Vector3 emissive) {
        this.emissive = clampColor(emissive);
    }
    
    /** @param emissiveStrength The new emissive strength, clamped to be non-negative. */
    public void setEmissiveStrength(double emissiveStrength) {
        this.emissiveStrength = Math.max(0.0, emissiveStrength);
    }
    
    /** @param normalStrength The new normal map strength. */
    public void setNormalStrength(double normalStrength) {
        this.normalStrength = normalStrength;
    }
    
    /** @param occlusionStrength The new ambient occlusion strength, clamped to the range [0, 1]. */
    public void setOcclusionStrength(double occlusionStrength) {
        this.occlusionStrength = Math.max(0.0, Math.min(1.0, occlusionStrength));
    }
    
    /** @param alphaCutoff The new alpha cutoff value, clamped to the range [0, 1]. */
    public void setAlphaCutoff(double alphaCutoff) {
        this.alphaCutoff = Math.max(0.0, Math.min(1.0, alphaCutoff));
    }
    
    /** @param blendMode The new blend mode. */
    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode != null ? blendMode : BlendMode.OPAQUE;
    }
    
    /** @param cullMode The new face culling mode. */
    public void setCullMode(CullMode cullMode) {
        this.cullMode = cullMode != null ? cullMode : CullMode.BACK;
    }
    
    /** @param texture The UUID of the new base color texture. */
    public void setBaseColorTexture(UUID texture) { this.baseColorTexture = texture; }
    /** @param texture The UUID of the new metallic-roughness texture. */
    public void setMetallicRoughnessTexture(UUID texture) { this.metallicRoughnessTexture = texture; }
    /** @param texture The UUID of the new normal map texture. */
    public void setNormalTexture(UUID texture) { this.normalTexture = texture; }
    /** @param texture The UUID of the new emissive map texture. */
    public void setEmissiveTexture(UUID texture) { this.emissiveTexture = texture; }
    /** @param texture The UUID of the new ambient occlusion texture. */
    public void setOcclusionTexture(UUID texture) { this.occlusionTexture = texture; }
    /** @param texture The UUID of the new height map texture. */
    public void setHeightTexture(UUID texture) { this.heightTexture = texture; }
    
    /** @param transform The new texture transform for the base color texture. */
    public void setBaseColorTransform(TextureTransform transform) {
        this.baseColorTransform = transform != null ? transform : new TextureTransform();
    }
    
    /** @param transform The new texture transform for the metallic-roughness texture. */
    public void setMetallicRoughnessTransform(TextureTransform transform) {
        this.metallicRoughnessTransform = transform != null ? transform : new TextureTransform();
    }
    
    /** @param transform The new texture transform for the normal map texture. */
    public void setNormalTransform(TextureTransform transform) {
        this.normalTransform = transform != null ? transform : new TextureTransform();
    }
    
    /** @param transform The new texture transform for the emissive map texture. */
    public void setEmissiveTransform(TextureTransform transform) {
        this.emissiveTransform = transform != null ? transform : new TextureTransform();
    }
    
    /** @param transform The new texture transform for the ambient occlusion texture. */
    public void setOcclusionTransform(TextureTransform transform) {
        this.occlusionTransform = transform != null ? transform : new TextureTransform();
    }
    
    /** @param transform The new texture transform for the height map texture. */
    public void setHeightTransform(TextureTransform transform) {
        this.heightTransform = transform != null ? transform : new TextureTransform();
    }
    
    /** @param specularFactor The new specular factor, clamped to be non-negative. */
    public void setSpecularFactor(double specularFactor) {
        this.specularFactor = Math.max(0.0, specularFactor);
    }
    
    /** @param specularColorFactor The new specular color factor, clamped to the range [0, 1]. */
    public void setSpecularColorFactor(Vector3 specularColorFactor) {
        this.specularColorFactor = clampColor(specularColorFactor);
    }
    
    /** @param texture The UUID of the new specular texture. */
    public void setSpecularTexture(UUID texture) { this.specularTexture = texture; }
    /** @param texture The UUID of the new specular color texture. */
    public void setSpecularColorTexture(UUID texture) { this.specularColorTexture = texture; }
    
    /** @param clearcoatFactor The new clearcoat factor, clamped to the range [0, 1]. */
    public void setClearcoatFactor(double clearcoatFactor) {
        this.clearcoatFactor = Math.max(0.0, Math.min(1.0, clearcoatFactor));
    }
    
    /** @param clearcoatRoughnessFactor The new clearcoat roughness, clamped to the range [0, 1]. */
    public void setClearcoatRoughnessFactor(double clearcoatRoughnessFactor) {
        this.clearcoatRoughnessFactor = Math.max(0.0, Math.min(1.0, clearcoatRoughnessFactor));
    }
    
    /** @param scale The new scale for the clearcoat normal map. */
    public void setClearcoatNormalScale(Vector3 scale) {
        this.clearcoatNormalScale = scale != null ? scale : new Vector3(1.0, 1.0, 1.0);
    }
    
    /** @param texture The UUID of the new clearcoat texture. */
    public void setClearcoatTexture(UUID texture) { this.clearcoatTexture = texture; }
    /** @param texture The UUID of the new clearcoat roughness texture. */
    public void setClearcoatRoughnessTexture(UUID texture) { this.clearcoatRoughnessTexture = texture; }
    /** @param texture The UUID of the new clearcoat normal map texture. */
    public void setClearcoatNormalTexture(UUID texture) { this.clearcoatNormalTexture = texture; }
    
    /** @param fullbright True to enable full-bright rendering. */
    public void setFullbright(boolean fullbright) { this.fullbright = fullbright; }
    
    /** @param glow The new glow intensity, clamped to the range [0, 1]. */
    public void setGlow(double glow) {
        this.glow = Math.max(0.0, Math.min(1.0, glow));
    }
    
    /** @param alphaMode The new alpha mode, clamped to the range [0, 1]. */
    public void setAlphaMode(double alphaMode) {
        this.alphaMode = Math.max(0.0, Math.min(1.0, alphaMode));
    }
    
    /** @param doubleSided True to enable double-sided rendering. */
    public void setDoubleSided(boolean doubleSided) { this.doubleSided = doubleSided; }
    
    /**
     * Checks if this material has transparency properties.
     *
     * @return {@code true} if the material's blend mode is BLEND, or if it is
     *         MASK with an alpha cutoff less than 1.0.
     */
    public boolean hasTransparency() {
        return blendMode == BlendMode.BLEND || 
               (blendMode == BlendMode.MASK && alphaCutoff < 1.0);
    }
    
    /**
     * Checks if this material emits light.
     *
     * @return {@code true} if the material has a non-zero emissive color and strength.
     */
    public boolean isEmissive() {
        return !emissive.isZero() && emissiveStrength > 0.0;
    }
    
    /**
     * Checks if this material should be treated as metallic.
     *
     * @return {@code true} if the metallic property is greater than 0.5.
     */
    public boolean isMetallic() {
        return metallic > 0.5;
    }
    
    /**
     * Checks if this material uses the clearcoat extension.
     *
     * @return {@code true} if the clearcoat factor is greater than 0.0.
     */
    public boolean hasClearcoat() {
        return clearcoatFactor > 0.0;
    }
    
    /**
     * Gets the effective roughness of the material, considering the clearcoat layer.
     *
     * @return The maximum of the base roughness and the clearcoat roughness if
     *         clearcoat is enabled, otherwise the base roughness.
     */
    public double getEffectiveRoughness() {
        if (hasClearcoat()) {
            return Math.max(roughness, clearcoatRoughnessFactor);
        }
        return roughness;
    }
    
    /**
     * Converts this material's properties into an LLSD map representation.
     *
     * @return A {@link Map} suitable for serialization, representing the material.
     */
    public Map<String, Object> toLLSD() {
        Map<String, Object> materialData = new HashMap<>();
        
        // Basic properties
        materialData.put("BaseColor", Arrays.asList(baseColor.x, baseColor.y, baseColor.z));
        materialData.put("Metallic", metallic);
        materialData.put("Roughness", roughness);
        materialData.put("Emissive", Arrays.asList(emissive.x, emissive.y, emissive.z));
        materialData.put("EmissiveStrength", emissiveStrength);
        materialData.put("NormalStrength", normalStrength);
        materialData.put("OcclusionStrength", occlusionStrength);
        materialData.put("AlphaCutoff", alphaCutoff);
        materialData.put("BlendMode", blendMode.name());
        materialData.put("CullMode", cullMode.name());
        
        // Textures
        Map<String, Object> textures = new HashMap<>();
        if (baseColorTexture != null) textures.put("BaseColor", baseColorTexture);
        if (metallicRoughnessTexture != null) textures.put("MetallicRoughness", metallicRoughnessTexture);
        if (normalTexture != null) textures.put("Normal", normalTexture);
        if (emissiveTexture != null) textures.put("Emissive", emissiveTexture);
        if (occlusionTexture != null) textures.put("Occlusion", occlusionTexture);
        if (heightTexture != null) textures.put("Height", heightTexture);
        materialData.put("Textures", textures);
        
        // Texture transforms
        Map<String, Object> transforms = new HashMap<>();
        transforms.put("BaseColor", baseColorTransform.toLLSD());
        transforms.put("MetallicRoughness", metallicRoughnessTransform.toLLSD());
        transforms.put("Normal", normalTransform.toLLSD());
        transforms.put("Emissive", emissiveTransform.toLLSD());
        transforms.put("Occlusion", occlusionTransform.toLLSD());
        transforms.put("Height", heightTransform.toLLSD());
        materialData.put("TextureTransforms", transforms);
        
        // Extensions
        Map<String, Object> extensions = new HashMap<>();
        
        // Specular extension
        if (specularFactor != 1.0 || specularTexture != null) {
            Map<String, Object> specular = new HashMap<>();
            specular.put("SpecularFactor", specularFactor);
            specular.put("SpecularColorFactor", Arrays.asList(
                specularColorFactor.x, specularColorFactor.y, specularColorFactor.z));
            if (specularTexture != null) specular.put("SpecularTexture", specularTexture);
            if (specularColorTexture != null) specular.put("SpecularColorTexture", specularColorTexture);
            extensions.put("KHR_materials_specular", specular);
        }
        
        // Clearcoat extension
        if (hasClearcoat()) {
            Map<String, Object> clearcoat = new HashMap<>();
            clearcoat.put("ClearcoatFactor", clearcoatFactor);
            clearcoat.put("ClearcoatRoughnessFactor", clearcoatRoughnessFactor);
            clearcoat.put("ClearcoatNormalScale", Arrays.asList(
                clearcoatNormalScale.x, clearcoatNormalScale.y, clearcoatNormalScale.z));
            if (clearcoatTexture != null) clearcoat.put("ClearcoatTexture", clearcoatTexture);
            if (clearcoatRoughnessTexture != null) clearcoat.put("ClearcoatRoughnessTexture", clearcoatRoughnessTexture);
            if (clearcoatNormalTexture != null) clearcoat.put("ClearcoatNormalTexture", clearcoatNormalTexture);
            extensions.put("KHR_materials_clearcoat", clearcoat);
        }
        
        materialData.put("Extensions", extensions);
        
        // Second Life specific
        Map<String, Object> secondLife = new HashMap<>();
        secondLife.put("Fullbright", fullbright);
        secondLife.put("Glow", glow);
        secondLife.put("AlphaMode", alphaMode);
        secondLife.put("DoubleSided", doubleSided);
        materialData.put("SecondLife", secondLife);
        
        return materialData;
    }
    
    /**
     * Create material from LLSD data.
     */
    @SuppressWarnings("unchecked")
    public static PBRMaterial fromLLSD(Map<String, Object> data) {
        PBRMaterial material = new PBRMaterial();
        
        // Basic properties
        if (data.containsKey("BaseColor")) {
            List<Double> color = (List<Double>) data.get("BaseColor");
            material.baseColor = new Vector3(color.get(0), color.get(1), color.get(2));
        }
        
        if (data.containsKey("Metallic")) {
            material.metallic = ((Number) data.get("Metallic")).doubleValue();
        }
        
        if (data.containsKey("Roughness")) {
            material.roughness = ((Number) data.get("Roughness")).doubleValue();
        }
        
        if (data.containsKey("Emissive")) {
            List<Double> emissive = (List<Double>) data.get("Emissive");
            material.emissive = new Vector3(emissive.get(0), emissive.get(1), emissive.get(2));
        }
        
        // Additional properties...
        // (Implementation would continue for all properties)
        
        return material;
    }
    
    private Vector3 clampColor(Vector3 color) {
        if (color == null) return Vector3.ZERO;
        return new Vector3(
            Math.max(0.0, Math.min(1.0, color.x)),
            Math.max(0.0, Math.min(1.0, color.y)),
            Math.max(0.0, Math.min(1.0, color.z))
        );
    }
    
    @Override
    public String toString() {
        return String.format("PBRMaterial[baseColor=%s, metallic=%.2f, roughness=%.2f]",
                           baseColor.toShortString(), metallic, roughness);
    }
}