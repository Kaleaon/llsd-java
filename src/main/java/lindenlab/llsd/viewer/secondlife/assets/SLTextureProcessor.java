/*
 * Second Life Texture Processing - Java implementation of texture stream handling
 *
 * Based on Second Life viewer implementation
 * Copyright (C) 2010, Linden Research, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer.secondlife.assets;

import lindenlab.llsd.LLSDException;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

/**
 * Second Life texture processing and streaming utilities.
 * 
 * <p>This class provides comprehensive texture handling for the Second Life
 * ecosystem, including format conversion, caching, streaming, and validation.</p>
 * 
 * <p>Supported texture formats:</p>
 * <ul>
 * <li>JPEG2000 (J2C) - Primary SL texture format</li>
 * <li>TGA - Targa format</li>
 * <li>JPEG - Standard JPEG</li>
 * <li>PNG - Portable Network Graphics</li>
 * <li>BMP - Windows Bitmap</li>
 * </ul>
 * 
 * @since 1.0
 */
public final class SLTextureProcessor {
    
    private static final int MAX_TEXTURE_SIZE = 1024;
    private static final int MIN_TEXTURE_SIZE = 64;
    private static final String[] SUPPORTED_FORMATS = {"j2c", "tga", "jpeg", "jpg", "png", "bmp"};
    
    // Cache for processed textures
    private static final Map<UUID, TextureCache> textureCache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRY_MS = 30 * 60 * 1000; // 30 minutes
    
    private SLTextureProcessor() {
        // Utility class - no instances
    }
    
    /**
     * Texture format enumeration.
     */
    public enum TextureFormat {
        J2C("j2c", "image/x-j2c"),
        TGA("tga", "image/tga"),
        JPEG("jpg", "image/jpeg"),
        PNG("png", "image/png"),
        BMP("bmp", "image/bmp"),
        UNKNOWN("", "application/octet-stream");
        
        private final String extension;
        private final String mimeType;
        
        TextureFormat(String extension, String mimeType) {
            this.extension = extension;
            this.mimeType = mimeType;
        }
        
        public String getExtension() { return extension; }
        public String getMimeType() { return mimeType; }
        
        public static TextureFormat fromExtension(String ext) {
            if (ext == null) return UNKNOWN;
            String normalized = ext.toLowerCase();
            for (TextureFormat format : values()) {
                if (format.extension.equals(normalized)) {
                    return format;
                }
            }
            return UNKNOWN;
        }
    }
    
    /**
     * Texture metadata container.
     */
    public static class TextureInfo {
        private final UUID textureId;
        private final TextureFormat format;
        private final int width;
        private final int height;
        private final int levels;
        private final long size;
        private final boolean hasAlpha;
        
        public TextureInfo(UUID textureId, TextureFormat format, int width, int height, 
                          int levels, long size, boolean hasAlpha) {
            this.textureId = textureId;
            this.format = format;
            this.width = width;
            this.height = height;
            this.levels = levels;
            this.size = size;
            this.hasAlpha = hasAlpha;
        }
        
        // Getters
        public UUID getTextureId() { return textureId; }
        public TextureFormat getFormat() { return format; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public int getLevels() { return levels; }
        public long getSize() { return size; }
        public boolean hasAlpha() { return hasAlpha; }
        
        public boolean isValid() {
            return textureId != null && format != TextureFormat.UNKNOWN &&
                   width >= MIN_TEXTURE_SIZE && width <= MAX_TEXTURE_SIZE &&
                   height >= MIN_TEXTURE_SIZE && height <= MAX_TEXTURE_SIZE &&
                   isPowerOfTwo(width) && isPowerOfTwo(height);
        }
        
        private boolean isPowerOfTwo(int value) {
            return value > 0 && (value & (value - 1)) == 0;
        }
    }
    
    /**
     * Texture cache entry.
     */
    private static class TextureCache {
        private final byte[] data;
        private final TextureInfo info;
        private final long timestamp;
        
        public TextureCache(byte[] data, TextureInfo info) {
            this.data = data.clone();
            this.info = info;
            this.timestamp = System.currentTimeMillis();
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS;
        }
        
        public byte[] getData() { return data.clone(); }
        public TextureInfo getInfo() { return info; }
    }
    
    /**
     * Process texture data from Second Life format.
     * 
     * @param textureId the texture UUID
     * @param data the raw texture data
     * @param format the expected texture format
     * @return processed texture information
     * @throws LLSDException if processing fails
     */
    public static TextureInfo processTexture(UUID textureId, byte[] data, TextureFormat format) 
            throws LLSDException {
        if (textureId == null || data == null || data.length == 0) {
            throw new LLSDException("Invalid texture data");
        }
        
        // Check cache first
        TextureCache cached = textureCache.get(textureId);
        if (cached != null && !cached.isExpired()) {
            return cached.getInfo();
        }
        
        TextureInfo info;
        try {
            switch (format) {
                case J2C:
                    info = processJ2CTexture(textureId, data);
                    break;
                case TGA:
                    info = processTGATexture(textureId, data);
                    break;
                case JPEG:
                    info = processJPEGTexture(textureId, data);
                    break;
                case PNG:
                    info = processPNGTexture(textureId, data);
                    break;
                case BMP:
                    info = processBMPTexture(textureId, data);
                    break;
                default:
                    info = detectAndProcessTexture(textureId, data);
                    break;
            }
            
            // Cache the processed texture
            textureCache.put(textureId, new TextureCache(data, info));
            
            // Clean expired entries
            cleanExpiredCache();
            
            return info;
        } catch (Exception e) {
            throw new LLSDException("Texture processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process JPEG2000 texture data.
     */
    private static TextureInfo processJ2CTexture(UUID textureId, byte[] data) throws IOException {
        // Basic J2C header validation
        if (data.length < 12) {
            throw new IOException("Invalid J2C data - too short");
        }
        
        // Check for J2C signature
        if (data[0] != (byte)0xFF || data[1] != (byte)0x4F) {
            throw new IOException("Invalid J2C signature");
        }
        
        // Extract basic dimensions (simplified - full J2C parsing would be more complex)
        int width = extractJ2CWidth(data);
        int height = extractJ2CHeight(data);
        int levels = calculateMipLevels(width, height);
        
        return new TextureInfo(textureId, TextureFormat.J2C, width, height, 
                              levels, data.length, true);
    }
    
    /**
     * Process TGA texture data.
     */
    private static TextureInfo processTGATexture(UUID textureId, byte[] data) throws IOException {
        if (data.length < 18) {
            throw new IOException("Invalid TGA data - too short");
        }
        
        // Extract TGA header information
        int width = (data[12] & 0xFF) | ((data[13] & 0xFF) << 8);
        int height = (data[14] & 0xFF) | ((data[15] & 0xFF) << 8);
        int bpp = data[16] & 0xFF;
        boolean hasAlpha = bpp == 32;
        
        if (width <= 0 || height <= 0) {
            throw new IOException("Invalid TGA dimensions");
        }
        
        int levels = calculateMipLevels(width, height);
        return new TextureInfo(textureId, TextureFormat.TGA, width, height, 
                              levels, data.length, hasAlpha);
    }
    
    /**
     * Process standard image formats using Java ImageIO.
     */
    private static TextureInfo processJPEGTexture(UUID textureId, byte[] data) throws IOException {
        return processStandardImage(textureId, data, TextureFormat.JPEG);
    }
    
    private static TextureInfo processPNGTexture(UUID textureId, byte[] data) throws IOException {
        return processStandardImage(textureId, data, TextureFormat.PNG);
    }
    
    private static TextureInfo processBMPTexture(UUID textureId, byte[] data) throws IOException {
        return processStandardImage(textureId, data, TextureFormat.BMP);
    }
    
    private static TextureInfo processStandardImage(UUID textureId, byte[] data, TextureFormat format) 
            throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
            BufferedImage image = ImageIO.read(bis);
            if (image == null) {
                throw new IOException("Failed to decode " + format.name() + " image");
            }
            
            int width = image.getWidth();
            int height = image.getHeight();
            boolean hasAlpha = image.getColorModel().hasAlpha();
            int levels = calculateMipLevels(width, height);
            
            return new TextureInfo(textureId, format, width, height, 
                                  levels, data.length, hasAlpha);
        }
    }
    
    /**
     * Auto-detect texture format and process.
     */
    private static TextureInfo detectAndProcessTexture(UUID textureId, byte[] data) throws IOException {
        TextureFormat format = detectTextureFormat(data);
        if (format == TextureFormat.UNKNOWN) {
            throw new IOException("Unsupported texture format");
        }
        
        switch (format) {
            case J2C:
                return processJ2CTexture(textureId, data);
            case TGA:
                return processTGATexture(textureId, data);
            default:
                return processStandardImage(textureId, data, format);
        }
    }
    
    /**
     * Detect texture format from data.
     */
    public static TextureFormat detectTextureFormat(byte[] data) {
        if (data == null || data.length < 4) {
            return TextureFormat.UNKNOWN;
        }
        
        // Check JPEG2000
        if (data[0] == (byte)0xFF && data[1] == (byte)0x4F) {
            return TextureFormat.J2C;
        }
        
        // Check JPEG
        if (data[0] == (byte)0xFF && data[1] == (byte)0xD8) {
            return TextureFormat.JPEG;
        }
        
        // Check PNG
        if (data[0] == (byte)0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            return TextureFormat.PNG;
        }
        
        // Check BMP
        if (data[0] == 'B' && data[1] == 'M') {
            return TextureFormat.BMP;
        }
        
        // Check TGA (more complex detection needed)
        if (data.length >= 18) {
            int imageType = data[2] & 0xFF;
            if (imageType == 2 || imageType == 10) { // Uncompressed or RLE RGB
                return TextureFormat.TGA;
            }
        }
        
        return TextureFormat.UNKNOWN;
    }
    
    /**
     * Convert texture to target format.
     */
    public static byte[] convertTexture(byte[] sourceData, TextureFormat sourceFormat, 
                                       TextureFormat targetFormat) throws IOException {
        if (sourceFormat == targetFormat) {
            return sourceData.clone();
        }
        
        // Load source image
        BufferedImage image;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(sourceData)) {
            image = ImageIO.read(bis);
            if (image == null) {
                throw new IOException("Failed to load source image");
            }
        }
        
        // Convert to target format
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            String formatName = getImageIOFormat(targetFormat);
            if (formatName == null) {
                throw new IOException("Unsupported target format: " + targetFormat);
            }
            
            ImageIO.write(image, formatName, bos);
            return bos.toByteArray();
        }
    }
    
    private static String getImageIOFormat(TextureFormat format) {
        switch (format) {
            case JPEG:
                return "jpg";
            case PNG:
                return "png";
            case BMP:
                return "bmp";
            default:
                return null;
        }
    }
    
    /**
     * Create texture stream LLSD data.
     */
    public static Map<String, Object> createTextureStreamData(UUID textureId, 
                                                              TextureInfo info, 
                                                              byte[] data) {
        Map<String, Object> streamData = new HashMap<>();
        streamData.put("AssetID", textureId);
        streamData.put("AssetType", SLAssetType.TEXTURE_STREAM);
        streamData.put("Format", info.getFormat().name());
        streamData.put("Width", info.getWidth());
        streamData.put("Height", info.getHeight());
        streamData.put("Levels", info.getLevels());
        streamData.put("HasAlpha", info.hasAlpha());
        streamData.put("Size", info.getSize());
        streamData.put("Data", data);
        streamData.put("Timestamp", System.currentTimeMillis() / 1000.0);
        
        return streamData;
    }
    
    /**
     * Validate texture dimensions for Second Life.
     */
    public static boolean isValidSLTextureDimensions(int width, int height) {
        return width >= MIN_TEXTURE_SIZE && width <= MAX_TEXTURE_SIZE &&
               height >= MIN_TEXTURE_SIZE && height <= MAX_TEXTURE_SIZE &&
               isPowerOfTwo(width) && isPowerOfTwo(height);
    }
    
    /**
     * Calculate mip levels for a texture.
     */
    private static int calculateMipLevels(int width, int height) {
        int maxDimension = Math.max(width, height);
        return (int) (Math.log(maxDimension) / Math.log(2)) + 1;
    }
    
    /**
     * Extract width from J2C data (simplified).
     */
    private static int extractJ2CWidth(byte[] data) {
        // Simplified J2C width extraction - in a real implementation,
        // you'd need to parse the full J2C codestream
        return 512; // Default assumption
    }
    
    /**
     * Extract height from J2C data (simplified).
     */
    private static int extractJ2CHeight(byte[] data) {
        // Simplified J2C height extraction - in a real implementation,
        // you'd need to parse the full J2C codestream
        return 512; // Default assumption
    }
    
    private static boolean isPowerOfTwo(int value) {
        return value > 0 && (value & (value - 1)) == 0;
    }
    
    /**
     * Clean expired cache entries.
     */
    private static void cleanExpiredCache() {
        textureCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Clear texture cache.
     */
    public static void clearCache() {
        textureCache.clear();
    }
    
    /**
     * Get cache statistics.
     */
    public static Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("CacheSize", textureCache.size());
        
        long totalSize = textureCache.values().stream()
                .mapToLong(cache -> cache.getData().length)
                .sum();
        stats.put("TotalCacheSize", totalSize);
        
        return stats;
    }
}