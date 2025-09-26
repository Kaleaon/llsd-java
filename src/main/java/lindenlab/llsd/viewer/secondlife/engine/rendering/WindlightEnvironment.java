/*
 * Windlight Environment - Java implementation of Windlight atmospheric rendering system
 *
 * Based on Second Life Windlight and Firestorm environmental rendering
 * Copyright (C) 2010, Linden Research, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer.secondlife.engine.rendering;

import lindenlab.llsd.viewer.secondlife.engine.Vector3;
import lindenlab.llsd.viewer.secondlife.engine.Quaternion;
import java.util.*;
import java.util.UUID;

/**
 * Windlight environmental rendering system.
 * 
 * <p>This class implements the Windlight atmospheric rendering system used in
 * Second Life and Firestorm viewers, providing realistic sky, water, and
 * environmental lighting effects.</p>
 * 
 * @since 1.0
 */
public class WindlightEnvironment {
    
    /** The settings for rendering the sky and atmosphere. */
    private SkySettings skySettings;
    /** The settings for rendering water surfaces. */
    private WaterSettings waterSettings;
    /** The direction vector of the sun. */
    private Vector3 sunDirection;
    /** The direction vector of the moon. */
    private Vector3 moonDirection;
    /** The current time of day, normalized from 0.0 (midnight) to 1.0 (next midnight). */
    private double dayTime;
    
    /** The overall ambient light intensity. */
    private double ambient;
    /** The density of the atmospheric fog. */
    private double fogDensity;
    /** The color of the fog. */
    private Vector3 fogColor;
    /** The distance at which fog starts to appear. */
    private double fogStart;
    /** The distance at which fog becomes fully opaque. */
    private double fogEnd;
    
    /** If true, a gamma correction curve is applied to the final image. */
    private boolean useGammaCurve;
    /** The exposure level for tone mapping. */
    private double exposure;
    /** The brightness of the stars at night. */
    private Vector3 starBrightness;
    /** The amount of cloud coverage in the sky (0.0 to 1.0). */
    private double cloudCoverage;
    /** The density of the clouds. */
    private double cloudDensity;
    /** The color of the clouds. */
    private Vector3 cloudColor;
    /** The scale of the cloud noise texture. */
    private double cloudScale;
    
    /**
     * A container for all settings related to rendering the sky and atmosphere.
     * <p>
     * This includes colors for the horizon and zenith, sun properties, haze,
     * and atmospheric scattering parameters (Rayleigh and Mie).
     */
    public static class SkySettings {
        /** The color of the sky at the horizon. */
        private Vector3 horizonColor;
        /** The color of the sky at its highest point (the zenith). */
        private Vector3 zenithColor;
        /** The color of the sun's light. */
        private Vector3 sunColor;
        /** The base color of the ambient light. */
        private Vector3 ambientColor;
        /** The base color of the clouds. */
        private Vector3 cloudColor;
        
        /** The density of the atmospheric haze. */
        private double hazeDensity;
        /** The height of the haze layer relative to the horizon. */
        private double hazeHorizon;
        /** A multiplier for the overall density of the atmosphere. */
        private double densityMultiplier;
        /** A multiplier for the effect of distance on atmospheric scattering. */
        private double distanceMultiplier;
        
        /** The focus of the sun's glow effect. */
        private double sunGlowFocus;
        /** The size of the sun's glow effect. */
        private double sunGlowSize;
        /** The gamma correction value for the scene. */
        private double sceneGamma;
        
        /** The coefficients for Rayleigh scattering (for small particles like air molecules). */
        private Vector3 rayleighScattering;
        /** The coefficients for Mie scattering (for larger particles like aerosols). */
        private Vector3 mieScattering;
        /** The asymmetry factor for Mie scattering, controlling the direction of scattered light. */
        private double mieAsymmetry;
        
        /**
         * Constructs a new {@code SkySettings} object with default Second Life sky values.
         */
        public SkySettings() {
            // Default Second Life sky settings
            this.horizonColor = new Vector3(0.25, 0.25, 0.32);
            this.zenithColor = new Vector3(0.24, 0.26, 0.30);
            this.sunColor = new Vector3(0.74, 0.65, 0.39);
            this.ambientColor = new Vector3(0.22, 0.23, 0.24);
            this.cloudColor = new Vector3(0.41, 0.41, 0.41);
            
            this.hazeDensity = 0.7;
            this.hazeHorizon = 0.19;
            this.densityMultiplier = 0.18;
            this.distanceMultiplier = 0.8;
            
            this.sunGlowFocus = 0.05;
            this.sunGlowSize = 1.75;
            this.sceneGamma = 1.0;
            
            this.rayleighScattering = new Vector3(0.13, 0.31, 0.75);
            this.mieScattering = new Vector3(0.04, 0.04, 0.04);
            this.mieAsymmetry = 0.8;
        }
        
        /** @return The color of the sky at the horizon. */
        public Vector3 getHorizonColor() { return horizonColor; }
        /** @param horizonColor The new horizon color. */
        public void setHorizonColor(Vector3 horizonColor) { this.horizonColor = clampColor(horizonColor); }
        
        /** @return The color of the sky at the zenith. */
        public Vector3 getZenithColor() { return zenithColor; }
        /** @param zenithColor The new zenith color. */
        public void setZenithColor(Vector3 zenithColor) { this.zenithColor = clampColor(zenithColor); }
        
        /** @return The color of the sun's light. */
        public Vector3 getSunColor() { return sunColor; }
        /** @param sunColor The new sun color. */
        public void setSunColor(Vector3 sunColor) { this.sunColor = clampColor(sunColor); }
        
        /** @return The base color of the ambient light. */
        public Vector3 getAmbientColor() { return ambientColor; }
        /** @param ambientColor The new ambient color. */
        public void setAmbientColor(Vector3 ambientColor) { this.ambientColor = clampColor(ambientColor); }
        
        /** @return The base color of the clouds. */
        public Vector3 getCloudColor() { return cloudColor; }
        /** @param cloudColor The new cloud color. */
        public void setCloudColor(Vector3 cloudColor) { this.cloudColor = clampColor(cloudColor); }
        
        /** @return The density of the atmospheric haze. */
        public double getHazeDensity() { return hazeDensity; }
        /** @param hazeDensity The new haze density. */
        public void setHazeDensity(double hazeDensity) { this.hazeDensity = Math.max(0.0, hazeDensity); }
        
        /** @return The height of the haze layer. */
        public double getHazeHorizon() { return hazeHorizon; }
        /** @param hazeHorizon The new haze horizon. */
        public void setHazeHorizon(double hazeHorizon) { this.hazeHorizon = Math.max(0.0, Math.min(2.0, hazeHorizon)); }
        
        /** @return The multiplier for atmospheric density. */
        public double getDensityMultiplier() { return densityMultiplier; }
        /** @param densityMultiplier The new density multiplier. */
        public void setDensityMultiplier(double densityMultiplier) { this.densityMultiplier = Math.max(0.0, densityMultiplier); }
        
        /** @return The multiplier for the effect of distance on scattering. */
        public double getDistanceMultiplier() { return distanceMultiplier; }
        /** @param distanceMultiplier The new distance multiplier. */
        public void setDistanceMultiplier(double distanceMultiplier) { this.distanceMultiplier = Math.max(0.0, distanceMultiplier); }
        
        /** @return The focus of the sun's glow. */
        public double getSunGlowFocus() { return sunGlowFocus; }
        /** @param sunGlowFocus The new sun glow focus. */
        public void setSunGlowFocus(double sunGlowFocus) { this.sunGlowFocus = Math.max(0.0, sunGlowFocus); }
        
        /** @return The size of the sun's glow. */
        public double getSunGlowSize() { return sunGlowSize; }
        /** @param sunGlowSize The new sun glow size. */
        public void setSunGlowSize(double sunGlowSize) { this.sunGlowSize = Math.max(0.0, sunGlowSize); }
        
        /** @return The gamma correction value for the scene. */
        public double getSceneGamma() { return sceneGamma; }
        /** @param sceneGamma The new scene gamma value. */
        public void setSceneGamma(double sceneGamma) { this.sceneGamma = Math.max(0.1, Math.min(3.0, sceneGamma)); }
        
        /** @return The Rayleigh scattering coefficients. */
        public Vector3 getRayleighScattering() { return rayleighScattering; }
        /** @param rayleighScattering The new Rayleigh scattering coefficients. */
        public void setRayleighScattering(Vector3 rayleighScattering) { this.rayleighScattering = clampScattering(rayleighScattering); }
        
        /** @return The Mie scattering coefficients. */
        public Vector3 getMieScattering() { return mieScattering; }
        /** @param mieScattering The new Mie scattering coefficients. */
        public void setMieScattering(Vector3 mieScattering) { this.mieScattering = clampScattering(mieScattering); }
        
        /** @return The asymmetry factor for Mie scattering. */
        public double getMieAsymmetry() { return mieAsymmetry; }
        /** @param mieAsymmetry The new Mie asymmetry factor. */
        public void setMieAsymmetry(double mieAsymmetry) { this.mieAsymmetry = Math.max(-1.0, Math.min(1.0, mieAsymmetry)); }
        
        /**
         * Converts these sky settings into an LLSD map representation.
         *
         * @return A {@link Map} suitable for serialization.
         */
        public Map<String, Object> toLLSD() {
            Map<String, Object> data = new HashMap<>();
            data.put("HorizonColor", Arrays.asList(horizonColor.x, horizonColor.y, horizonColor.z));
            data.put("ZenithColor", Arrays.asList(zenithColor.x, zenithColor.y, zenithColor.z));
            data.put("SunColor", Arrays.asList(sunColor.x, sunColor.y, sunColor.z));
            data.put("AmbientColor", Arrays.asList(ambientColor.x, ambientColor.y, ambientColor.z));
            data.put("CloudColor", Arrays.asList(cloudColor.x, cloudColor.y, cloudColor.z));
            data.put("HazeDensity", hazeDensity);
            data.put("HazeHorizon", hazeHorizon);
            data.put("DensityMultiplier", densityMultiplier);
            data.put("DistanceMultiplier", distanceMultiplier);
            data.put("SunGlowFocus", sunGlowFocus);
            data.put("SunGlowSize", sunGlowSize);
            data.put("SceneGamma", sceneGamma);
            data.put("RayleighScattering", Arrays.asList(rayleighScattering.x, rayleighScattering.y, rayleighScattering.z));
            data.put("MieScattering", Arrays.asList(mieScattering.x, mieScattering.y, mieScattering.z));
            data.put("MieAsymmetry", mieAsymmetry);
            return data;
        }
    }
    
    /**
     * A container for all settings related to rendering water surfaces.
     * <p>
     * This includes colors, fog, wave normals, and Fresnel reflection properties.
     */
    public static class WaterSettings {
        /** The base color of the water. */
        private Vector3 waterColor;
        /** The color of the fog effect when underwater. */
        private Vector3 waterFogColor;
        /** The density of the underwater fog. */
        private double waterFogDensity;
        /** A modifier for the fog density when the camera is underwater. */
        private double underwaterFogModifier;
        
        /** The scale of the water's normal map. */
        private Vector3 normalScale;
        /** The UUID of the normal map texture for water waves. */
        private UUID normalMapTexture;
        /** The scale of the Fresnel reflection effect. */
        private double fresnelScale;
        /** The offset for the Fresnel reflection effect. */
        private double fresnelOffset;
        /** The scale of the water texture when viewed from above. */
        private double scaleAbove;
        /** The scale of the water texture when viewed from below. */
        private double scaleBelow;
        
        /** A multiplier for the blur effect applied to reflections and refractions. */
        private double blurMultiplier;
        /** The direction of the small waves. */
        private Vector3 littleWaveDirection;
        /** The direction of the large waves. */
        private Vector3 bigWaveDirection;
        
        /**
         * Constructs a new {@code WaterSettings} object with default Second Life water values.
         */
        public WaterSettings() {
            // Default Second Life water settings
            this.waterColor = new Vector3(0.04, 0.15, 0.20);
            this.waterFogColor = new Vector3(0.04, 0.15, 0.20);
            this.waterFogDensity = 10.0;
            this.underwaterFogModifier = 0.25;
            
            this.normalScale = new Vector3(1.0, 1.0, 1.0);
            this.fresnelScale = 0.40;
            this.fresnelOffset = 0.50;
            this.scaleAbove = 0.025;
            this.scaleBelow = 0.2;
            
            this.blurMultiplier = 0.04;
            this.littleWaveDirection = new Vector3(1.0, 1.0, 0.0);
            this.bigWaveDirection = new Vector3(1.0, 0.09, 0.0);
        }
        
        /** @return The base color of the water. */
        public Vector3 getWaterColor() { return waterColor; }
        /** @param waterColor The new water color. */
        public void setWaterColor(Vector3 waterColor) { this.waterColor = clampColor(waterColor); }
        
        /** @return The color of the underwater fog. */
        public Vector3 getWaterFogColor() { return waterFogColor; }
        /** @param waterFogColor The new underwater fog color. */
        public void setWaterFogColor(Vector3 waterFogColor) { this.waterFogColor = clampColor(waterFogColor); }
        
        /** @return The density of the underwater fog. */
        public double getWaterFogDensity() { return waterFogDensity; }
        /** @param waterFogDensity The new underwater fog density. */
        public void setWaterFogDensity(double waterFogDensity) { this.waterFogDensity = Math.max(0.0, waterFogDensity); }
        
        /** @return The modifier for underwater fog. */
        public double getUnderwaterFogModifier() { return underwaterFogModifier; }
        /** @param underwaterFogModifier The new underwater fog modifier. */
        public void setUnderwaterFogModifier(double underwaterFogModifier) { this.underwaterFogModifier = Math.max(0.0, underwaterFogModifier); }
        
        /** @return The scale of the water's normal map. */
        public Vector3 getNormalScale() { return normalScale; }
        /** @param normalScale The new normal map scale. */
        public void setNormalScale(Vector3 normalScale) { this.normalScale = normalScale; }
        
        /** @return The UUID of the normal map texture for waves. */
        public UUID getNormalMapTexture() { return normalMapTexture; }
        /** @param normalMapTexture The new normal map texture UUID. */
        public void setNormalMapTexture(UUID normalMapTexture) { this.normalMapTexture = normalMapTexture; }
        
        /** @return The scale of the Fresnel reflection effect. */
        public double getFresnelScale() { return fresnelScale; }
        /** @param fresnelScale The new Fresnel scale. */
        public void setFresnelScale(double fresnelScale) { this.fresnelScale = Math.max(0.0, Math.min(1.0, fresnelScale)); }
        
        /** @return The offset for the Fresnel reflection effect. */
        public double getFresnelOffset() { return fresnelOffset; }
        /** @param fresnelOffset The new Fresnel offset. */
        public void setFresnelOffset(double fresnelOffset) { this.fresnelOffset = Math.max(0.0, Math.min(1.0, fresnelOffset)); }
        
        /** @return The scale of the water texture when viewed from above. */
        public double getScaleAbove() { return scaleAbove; }
        /** @param scaleAbove The new scale for above-water viewing. */
        public void setScaleAbove(double scaleAbove) { this.scaleAbove = Math.max(0.0, scaleAbove); }
        
        /** @return The scale of the water texture when viewed from below. */
        public double getScaleBelow() { return scaleBelow; }
        /** @param scaleBelow The new scale for below-water viewing. */
        public void setScaleBelow(double scaleBelow) { this.scaleBelow = Math.max(0.0, scaleBelow); }
        
        /** @return The multiplier for reflection/refraction blur. */
        public double getBlurMultiplier() { return blurMultiplier; }
        /** @param blurMultiplier The new blur multiplier. */
        public void setBlurMultiplier(double blurMultiplier) { this.blurMultiplier = Math.max(0.0, blurMultiplier); }
        
        /** @return The direction of the small waves. */
        public Vector3 getLittleWaveDirection() { return littleWaveDirection; }
        /** @param littleWaveDirection The new direction for small waves. */
        public void setLittleWaveDirection(Vector3 littleWaveDirection) { this.littleWaveDirection = littleWaveDirection; }
        
        /** @return The direction of the large waves. */
        public Vector3 getBigWaveDirection() { return bigWaveDirection; }
        /** @param bigWaveDirection The new direction for large waves. */
        public void setBigWaveDirection(Vector3 bigWaveDirection) { this.bigWaveDirection = bigWaveDirection; }
        
        /**
         * Converts these water settings into an LLSD map representation.
         *
         * @return A {@link Map} suitable for serialization.
         */
        public Map<String, Object> toLLSD() {
            Map<String, Object> data = new HashMap<>();
            data.put("WaterColor", Arrays.asList(waterColor.x, waterColor.y, waterColor.z));
            data.put("WaterFogColor", Arrays.asList(waterFogColor.x, waterFogColor.y, waterFogColor.z));
            data.put("WaterFogDensity", waterFogDensity);
            data.put("UnderwaterFogModifier", underwaterFogModifier);
            data.put("NormalScale", Arrays.asList(normalScale.x, normalScale.y, normalScale.z));
            if (normalMapTexture != null) data.put("NormalMapTexture", normalMapTexture);
            data.put("FresnelScale", fresnelScale);
            data.put("FresnelOffset", fresnelOffset);
            data.put("ScaleAbove", scaleAbove);
            data.put("ScaleBelow", scaleBelow);
            data.put("BlurMultiplier", blurMultiplier);
            data.put("LittleWaveDirection", Arrays.asList(littleWaveDirection.x, littleWaveDirection.y));
            data.put("BigWaveDirection", Arrays.asList(bigWaveDirection.x, bigWaveDirection.y));
            return data;
        }
    }
    
    /**
     * Constructs a new {@code WindlightEnvironment} with default settings that
     * represent a standard Second Life day-cycle environment.
     */
    public WindlightEnvironment() {
        this.skySettings = new SkySettings();
        this.waterSettings = new WaterSettings();
        this.sunDirection = new Vector3(0.0, 0.707, 0.707); // 45 degree sun
        this.moonDirection = new Vector3(0.0, -0.707, 0.707); // Opposite to sun
        this.dayTime = 0.5; // Noon
        
        this.ambient = 0.25;
        this.fogDensity = 0.5;
        this.fogColor = new Vector3(0.5, 0.6, 0.7);
        this.fogStart = 20.0;
        this.fogEnd = 500.0;
        
        this.useGammaCurve = true;
        this.exposure = 1.0;
        this.starBrightness = new Vector3(2.0, 2.0, 2.0);
        this.cloudCoverage = 0.4;
        this.cloudDensity = 1.0;
        this.cloudColor = new Vector3(1.0, 1.0, 1.0);
        this.cloudScale = 0.42;
    }
    
    /** @return The current sky settings. */
    public SkySettings getSkySettings() { return skySettings; }
    /** @return The current water settings. */
    public WaterSettings getWaterSettings() { return waterSettings; }
    /** @return The direction vector of the sun. */
    public Vector3 getSunDirection() { return sunDirection; }
    /** @return The direction vector of the moon. */
    public Vector3 getMoonDirection() { return moonDirection; }
    /** @return The current time of day (0.0 to 1.0). */
    public double getDayTime() { return dayTime; }
    /** @return The overall ambient light intensity. */
    public double getAmbient() { return ambient; }
    /** @return The density of the atmospheric fog. */
    public double getFogDensity() { return fogDensity; }
    /** @return The color of the fog. */
    public Vector3 getFogColor() { return fogColor; }
    /** @return The distance at which fog starts. */
    public double getFogStart() { return fogStart; }
    /** @return The distance at which fog is fully opaque. */
    public double getFogEnd() { return fogEnd; }
    /** @return True if gamma correction is enabled. */
    public boolean isUseGammaCurve() { return useGammaCurve; }
    /** @return The exposure level for tone mapping. */
    public double getExposure() { return exposure; }
    /** @return The brightness of the stars. */
    public Vector3 getStarBrightness() { return starBrightness; }
    /** @return The amount of cloud coverage. */
    public double getCloudCoverage() { return cloudCoverage; }
    /** @return The density of the clouds. */
    public double getCloudDensity() { return cloudDensity; }
    /** @return The color of the clouds. */
    public Vector3 getCloudColor() { return cloudColor; }
    /** @return The scale of the cloud noise texture. */
    public double getCloudScale() { return cloudScale; }
    
    /** @param skySettings The new sky settings. */
    public void setSkySettings(SkySettings skySettings) {
        this.skySettings = skySettings != null ? skySettings : new SkySettings();
    }
    
    /** @param waterSettings The new water settings. */
    public void setWaterSettings(WaterSettings waterSettings) {
        this.waterSettings = waterSettings != null ? waterSettings : new WaterSettings();
    }
    
    /** @param sunDirection The new direction vector for the sun. */
    public void setSunDirection(Vector3 sunDirection) {
        this.sunDirection = sunDirection != null ? sunDirection.normalize() : new Vector3(0.0, 0.707, 0.707);
    }
    
    /** @param moonDirection The new direction vector for the moon. */
    public void setMoonDirection(Vector3 moonDirection) {
        this.moonDirection = moonDirection != null ? moonDirection.normalize() : new Vector3(0.0, -0.707, 0.707);
    }
    
    /** @param dayTime The new time of day, clamped to the range [0, 1]. */
    public void setDayTime(double dayTime) {
        this.dayTime = Math.max(0.0, Math.min(1.0, dayTime));
        updateSunMoonPositions();
    }
    
    /** @param ambient The new ambient light intensity, clamped to the range [0, 1]. */
    public void setAmbient(double ambient) {
        this.ambient = Math.max(0.0, Math.min(1.0, ambient));
    }
    
    /** @param fogDensity The new fog density. */
    public void setFogDensity(double fogDensity) {
        this.fogDensity = Math.max(0.0, fogDensity);
    }
    
    /** @param fogColor The new fog color. */
    public void setFogColor(Vector3 fogColor) {
        this.fogColor = clampColor(fogColor);
    }
    
    /** @param fogStart The new fog start distance. */
    public void setFogStart(double fogStart) {
        this.fogStart = Math.max(0.0, fogStart);
    }
    
    /** @param fogEnd The new fog end distance. */
    public void setFogEnd(double fogEnd) {
        this.fogEnd = Math.max(fogStart, fogEnd);
    }
    
    /** @param useGammaCurve The new gamma correction state. */
    public void setUseGammaCurve(boolean useGammaCurve) {
        this.useGammaCurve = useGammaCurve;
    }
    
    /** @param exposure The new exposure level. */
    public void setExposure(double exposure) {
        this.exposure = Math.max(0.1, Math.min(10.0, exposure));
    }
    
    /** @param starBrightness The new star brightness. */
    public void setStarBrightness(Vector3 starBrightness) {
        this.starBrightness = starBrightness != null ? starBrightness : Vector3.ZERO;
    }
    
    /** @param cloudCoverage The new cloud coverage, clamped to the range [0, 1]. */
    public void setCloudCoverage(double cloudCoverage) {
        this.cloudCoverage = Math.max(0.0, Math.min(1.0, cloudCoverage));
    }
    
    /** @param cloudDensity The new cloud density. */
    public void setCloudDensity(double cloudDensity) {
        this.cloudDensity = Math.max(0.0, cloudDensity);
    }
    
    /** @param cloudColor The new cloud color. */
    public void setCloudColor(Vector3 cloudColor) {
        this.cloudColor = clampColor(cloudColor);
    }
    
    /** @param cloudScale The new cloud scale. */
    public void setCloudScale(double cloudScale) {
        this.cloudScale = Math.max(0.0, cloudScale);
    }
    
    /**
     * Updates the sun and moon positions based on the current day time.
     */
    public void updateSunMoonPositions() {
        // Convert day time to radians (0.0 = midnight, 0.5 = noon)
        double angle = (dayTime - 0.25) * 2.0 * Math.PI; // Offset so noon is at top
        
        // Sun position (sinusoidal path across sky)
        double sunElevation = Math.sin(angle);
        double sunAzimuth = Math.cos(angle);
        this.sunDirection = new Vector3(sunAzimuth * 0.707, sunElevation, Math.abs(sunAzimuth) * 0.707).normalize();
        
        // Moon is opposite to sun
        this.moonDirection = sunDirection.multiply(-1.0);
    }
    
    /**
     * Gets the direction of the dominant light source (sun or moon) based on the time of day.
     *
     * @return The direction vector of the current dominant light.
     */
    public Vector3 getCurrentLightDirection() {
        double sunInfluence = Math.max(0.0, sunDirection.y); // Y is up
        if (sunInfluence > 0.1) {
            return sunDirection;
        } else {
            return moonDirection;
        }
    }
    
    /**
     * Gets the color of the dominant light source, adjusted for its elevation.
     *
     * @return The color of the current dominant light.
     */
    public Vector3 getCurrentLightColor() {
        double sunInfluence = Math.max(0.0, sunDirection.y);
        
        if (sunInfluence > 0.1) {
            // Daytime - use sun color
            return skySettings.getSunColor().multiply(sunInfluence);
        } else {
            // Nighttime - use moon color (cooler, dimmer)
            double moonInfluence = Math.max(0.0, moonDirection.y) * 0.1; // Much dimmer than sun
            return new Vector3(0.6, 0.7, 0.9).multiply(moonInfluence); // Cool blue moonlight
        }
    }
    
    /**
     * Gets the current ambient light color, calculated from atmospheric scattering.
     *
     * @return The calculated ambient light color.
     */
    public Vector3 getCurrentAmbientColor() {
        Vector3 ambientBase = skySettings.getAmbientColor();
        double sunInfluence = Math.max(0.0, sunDirection.y);
        
        // Blend between ambient and horizon color based on sun position
        Vector3 horizonInfluence = skySettings.getHorizonColor().multiply(sunInfluence * 0.5);
        return ambientBase.add(horizonInfluence).multiply(ambient);
    }
    
    /**
     * Calculates the color of the atmospheric fog at a given distance.
     *
     * @param distance The distance from the camera.
     * @return The calculated atmospheric color.
     */
    public Vector3 getAtmosphericColor(double distance) {
        double fogFactor = 1.0 - Math.exp(-distance * fogDensity / 1000.0);
        fogFactor = Math.max(0.0, Math.min(1.0, fogFactor));
        
        // Blend fog color with horizon color based on sun position
        Vector3 atmosphericColor = fogColor;
        double sunInfluence = Math.max(0.0, sunDirection.y);
        if (sunInfluence > 0.0) {
            atmosphericColor = fogColor.lerp(skySettings.getHorizonColor(), sunInfluence * 0.3);
        }
        
        return atmosphericColor.multiply(fogFactor);
    }
    
    /**
     * Gets the effective visibility distance based on the current atmospheric conditions.
     *
     * @return The visibility distance in meters.
     */
    public double getVisibilityDistance() {
        // Base visibility affected by fog density and haze
        double baseVisibility = fogEnd;
        double hazeFactor = skySettings.getHazeDensity() * skySettings.getDensityMultiplier();
        return baseVisibility / (1.0 + hazeFactor);
    }
    
    /**
     * Converts this environment's settings into an LLSD map representation.
     *
     * @return A {@link Map} suitable for serialization.
     */
    public Map<String, Object> toLLSD() {
        Map<String, Object> envData = new HashMap<>();
        
        envData.put("SkySettings", skySettings.toLLSD());
        envData.put("WaterSettings", waterSettings.toLLSD());
        
        envData.put("SunDirection", Arrays.asList(sunDirection.x, sunDirection.y, sunDirection.z));
        envData.put("MoonDirection", Arrays.asList(moonDirection.x, moonDirection.y, moonDirection.z));
        envData.put("DayTime", dayTime);
        
        envData.put("Ambient", ambient);
        envData.put("FogDensity", fogDensity);
        envData.put("FogColor", Arrays.asList(fogColor.x, fogColor.y, fogColor.z));
        envData.put("FogStart", fogStart);
        envData.put("FogEnd", fogEnd);
        
        envData.put("UseGammaCurve", useGammaCurve);
        envData.put("Exposure", exposure);
        envData.put("StarBrightness", Arrays.asList(starBrightness.x, starBrightness.y, starBrightness.z));
        
        envData.put("CloudCoverage", cloudCoverage);
        envData.put("CloudDensity", cloudDensity);
        envData.put("CloudColor", Arrays.asList(cloudColor.x, cloudColor.y, cloudColor.z));
        envData.put("CloudScale", cloudScale);
        
        return envData;
    }
    
    /**
     * Creates a {@code WindlightEnvironment} from an LLSD map.
     *
     * @param data The LLSD map containing the environment data.
     * @return A new {@code WindlightEnvironment} instance.
     */
    @SuppressWarnings("unchecked")
    public static WindlightEnvironment fromLLSD(Map<String, Object> data) {
        WindlightEnvironment env = new WindlightEnvironment();
        
        if (data.containsKey("DayTime")) {
            env.setDayTime(((Number) data.get("DayTime")).doubleValue());
        }
        
        if (data.containsKey("SunDirection")) {
            List<Double> sunDir = (List<Double>) data.get("SunDirection");
            env.setSunDirection(new Vector3(sunDir.get(0), sunDir.get(1), sunDir.get(2)));
        }
        
        // Additional property parsing would continue here...
        
        return env;
    }
    
    private static Vector3 clampColor(Vector3 color) {
        if (color == null) return Vector3.ZERO;
        return new Vector3(
            Math.max(0.0, Math.min(5.0, color.x)), // Allow HDR values up to 5.0
            Math.max(0.0, Math.min(5.0, color.y)),
            Math.max(0.0, Math.min(5.0, color.z))
        );
    }
    
    private static Vector3 clampScattering(Vector3 scattering) {
        if (scattering == null) return Vector3.ZERO;
        return new Vector3(
            Math.max(0.0, Math.min(2.0, scattering.x)),
            Math.max(0.0, Math.min(2.0, scattering.y)),
            Math.max(0.0, Math.min(2.0, scattering.z))
        );
    }
    
    @Override
    public String toString() {
        return String.format("WindlightEnvironment[dayTime=%.2f, sunDir=%s, fogDensity=%.2f]",
                           dayTime, sunDirection.toShortString(), fogDensity);
    }
}