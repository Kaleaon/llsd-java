/*
 * Second Life Data Stream Processing - Java implementation of general asset streaming
 *
 * Based on Second Life viewer implementation
 * Copyright (C) 2010, Linden Research, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer.secondlife.assets;

import lindenlab.llsd.LLSDException;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.*;

/**
 * Second Life data stream processing and asset management utilities.
 * 
 * <p>This class provides comprehensive data handling for the Second Life
 * ecosystem, including compression, validation, streaming, and caching
 * for various binary asset types.</p>
 * 
 * <p>Supported operations:</p>
 * <ul>
 * <li>Asset compression and decompression</li>
 * <li>Data integrity validation</li>
 * <li>Streaming chunk management</li>
 * <li>Asset caching and retrieval</li>
 * <li>Binary data processing</li>
 * </ul>
 * 
 * @since 1.0
 */
public final class SLDataStreamProcessor {
    
    private static final int MAX_ASSET_SIZE = 100 * 1024 * 1024; // 100MB
    private static final int CHUNK_SIZE = 64 * 1024; // 64KB chunks
    private static final int COMPRESSION_THRESHOLD = 1024; // 1KB minimum for compression
    
    // Cache for processed data streams
    private static final Map<UUID, DataCache> dataCache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRY_MS = 60 * 60 * 1000; // 1 hour
    
    private SLDataStreamProcessor() {
        // Utility class - no instances
    }
    
    /**
     * Compression type enumeration.
     */
    public enum CompressionType {
        NONE(0, "none"),
        GZIP(1, "gzip"),
        DEFLATE(2, "deflate"),
        LZ4(3, "lz4");
        
        private final int value;
        private final String name;
        
        CompressionType(int value, String name) {
            this.value = value;
            this.name = name;
        }
        
        public int getValue() { return value; }
        public String getName() { return name; }
        
        public static CompressionType fromValue(int value) {
            for (CompressionType type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return NONE;
        }
    }
    
    /**
     * Stream chunk information.
     */
    public static class StreamChunk {
        private final int chunkIndex;
        private final byte[] data;
        private final String checksum;
        private final boolean isCompressed;
        
        public StreamChunk(int chunkIndex, byte[] data, String checksum, boolean isCompressed) {
            this.chunkIndex = chunkIndex;
            this.data = data.clone();
            this.checksum = checksum;
            this.isCompressed = isCompressed;
        }
        
        public int getChunkIndex() { return chunkIndex; }
        public byte[] getData() { return data.clone(); }
        public String getChecksum() { return checksum; }
        public boolean isCompressed() { return isCompressed; }
        
        public boolean isValid() {
            return chunkIndex >= 0 && data != null && data.length > 0 &&
                   checksum != null && checksum.equals(calculateChecksum(data));
        }
    }
    
    /**
     * Data stream information container.
     */
    public static class DataStreamInfo {
        private final UUID assetId;
        private final int assetType;
        private final long totalSize;
        private final long compressedSize;
        private final CompressionType compression;
        private final int chunkCount;
        private final String overallChecksum;
        private final Map<String, Object> metadata;
        
        public DataStreamInfo(UUID assetId, int assetType, long totalSize, long compressedSize,
                             CompressionType compression, int chunkCount, String overallChecksum,
                             Map<String, Object> metadata) {
            this.assetId = assetId;
            this.assetType = assetType;
            this.totalSize = totalSize;
            this.compressedSize = compressedSize;
            this.compression = compression;
            this.chunkCount = chunkCount;
            this.overallChecksum = overallChecksum;
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        }
        
        // Getters
        public UUID getAssetId() { return assetId; }
        public int getAssetType() { return assetType; }
        public long getTotalSize() { return totalSize; }
        public long getCompressedSize() { return compressedSize; }
        public CompressionType getCompression() { return compression; }
        public int getChunkCount() { return chunkCount; }
        public String getOverallChecksum() { return overallChecksum; }
        public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
        
        public boolean isValid() {
            return assetId != null && totalSize > 0 && totalSize <= MAX_ASSET_SIZE &&
                   chunkCount > 0 && overallChecksum != null;
        }
        
        public double getCompressionRatio() {
            return compression == CompressionType.NONE ? 1.0 : 
                   (double) compressedSize / totalSize;
        }
    }
    
    /**
     * Data cache entry.
     */
    private static class DataCache {
        private final byte[] data;
        private final DataStreamInfo info;
        private final long timestamp;
        
        public DataCache(byte[] data, DataStreamInfo info) {
            this.data = data.clone();
            this.info = info;
            this.timestamp = System.currentTimeMillis();
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS;
        }
        
        public byte[] getData() { return data.clone(); }
        public DataStreamInfo getInfo() { return info; }
    }
    
    /**
     * Process data stream for Second Life asset system.
     * 
     * @param assetId the asset UUID
     * @param assetType the asset type
     * @param data the raw asset data
     * @param enableCompression whether to apply compression
     * @return processed data stream information
     * @throws LLSDException if processing fails
     */
    public static DataStreamInfo processDataStream(UUID assetId, int assetType, byte[] data, 
                                                  boolean enableCompression) throws LLSDException {
        if (assetId == null || data == null || data.length == 0) {
            throw new LLSDException("Invalid data stream parameters");
        }
        
        if (data.length > MAX_ASSET_SIZE) {
            throw new LLSDException("Asset too large: " + data.length + " bytes");
        }
        
        // Check cache first
        DataCache cached = dataCache.get(assetId);
        if (cached != null && !cached.isExpired()) {
            return cached.getInfo();
        }
        
        try {
            // Determine compression
            CompressionType compression = CompressionType.NONE;
            byte[] processedData = data;
            
            if (enableCompression && data.length >= COMPRESSION_THRESHOLD) {
                byte[] compressed = compressData(data, CompressionType.GZIP);
                if (compressed.length < data.length * 0.9) { // Only if significant compression
                    processedData = compressed;
                    compression = CompressionType.GZIP;
                }
            }
            
            // Calculate checksums
            String overallChecksum = calculateChecksum(data);
            
            // Calculate chunks
            int chunkCount = (int) Math.ceil((double) processedData.length / CHUNK_SIZE);
            
            // Create metadata
            Map<String, Object> metadata = createAssetMetadata(assetType, data);
            
            // Create stream info
            DataStreamInfo info = new DataStreamInfo(
                assetId, assetType, data.length, processedData.length,
                compression, chunkCount, overallChecksum, metadata
            );
            
            // Cache the processed data
            dataCache.put(assetId, new DataCache(processedData, info));
            
            // Clean expired entries
            cleanExpiredCache();
            
            return info;
            
        } catch (Exception e) {
            throw new LLSDException("Data stream processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Split data into stream chunks.
     * 
     * @param data the data to split
     * @param compression the compression type
     * @return list of stream chunks
     * @throws IOException if chunking fails
     */
    public static List<StreamChunk> createStreamChunks(byte[] data, CompressionType compression) 
            throws IOException {
        List<StreamChunk> chunks = new ArrayList<>();
        
        for (int i = 0; i < data.length; i += CHUNK_SIZE) {
            int chunkSize = Math.min(CHUNK_SIZE, data.length - i);
            byte[] chunkData = Arrays.copyOfRange(data, i, i + chunkSize);
            
            // Compress chunk if needed
            boolean isCompressed = false;
            if (compression != CompressionType.NONE) {
                byte[] compressed = compressData(chunkData, compression);
                if (compressed.length < chunkData.length) {
                    chunkData = compressed;
                    isCompressed = true;
                }
            }
            
            String checksum = calculateChecksum(chunkData);
            chunks.add(new StreamChunk(i / CHUNK_SIZE, chunkData, checksum, isCompressed));
        }
        
        return chunks;
    }
    
    /**
     * Reconstruct data from stream chunks.
     * 
     * @param chunks the stream chunks
     * @param compression the compression type
     * @return reconstructed data
     * @throws IOException if reconstruction fails
     */
    public static byte[] reconstructFromChunks(List<StreamChunk> chunks, CompressionType compression) 
            throws IOException {
        // Sort chunks by index
        chunks.sort(Comparator.comparingInt(StreamChunk::getChunkIndex));
        
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        
        for (StreamChunk chunk : chunks) {
            if (!chunk.isValid()) {
                throw new IOException("Invalid chunk: " + chunk.getChunkIndex());
            }
            
            byte[] chunkData = chunk.getData();
            
            // Decompress chunk if needed
            if (chunk.isCompressed() && compression != CompressionType.NONE) {
                chunkData = decompressData(chunkData, compression);
            }
            
            result.write(chunkData);
        }
        
        return result.toByteArray();
    }
    
    /**
     * Compress data using specified algorithm.
     */
    public static byte[] compressData(byte[] data, CompressionType compression) throws IOException {
        switch (compression) {
            case GZIP:
                return compressGzip(data);
            case DEFLATE:
                return compressDeflate(data);
            default:
                return data.clone();
        }
    }
    
    /**
     * Decompress data using specified algorithm.
     */
    public static byte[] decompressData(byte[] data, CompressionType compression) throws IOException {
        switch (compression) {
            case GZIP:
                return decompressGzip(data);
            case DEFLATE:
                return decompressDeflate(data);
            default:
                return data.clone();
        }
    }
    
    /**
     * Compress data using GZIP.
     */
    private static byte[] compressGzip(byte[] data) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPOutputStream gzos = new GZIPOutputStream(bos)) {
            gzos.write(data);
            gzos.finish();
            return bos.toByteArray();
        }
    }
    
    /**
     * Decompress GZIP data.
     */
    private static byte[] decompressGzip(byte[] data) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             GZIPInputStream gzis = new GZIPInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[8192];
            int len;
            while ((len = gzis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        }
    }
    
    /**
     * Compress data using Deflate.
     */
    private static byte[] compressDeflate(byte[] data) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DeflaterOutputStream dos = new DeflaterOutputStream(bos)) {
            dos.write(data);
            dos.finish();
            return bos.toByteArray();
        }
    }
    
    /**
     * Decompress Deflate data.
     */
    private static byte[] decompressDeflate(byte[] data) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             InflaterInputStream iis = new InflaterInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[8192];
            int len;
            while ((len = iis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        }
    }
    
    /**
     * Calculate SHA-256 checksum of data.
     */
    public static String calculateChecksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
    
    /**
     * Validate data integrity using checksum.
     */
    public static boolean validateDataIntegrity(byte[] data, String expectedChecksum) {
        String actualChecksum = calculateChecksum(data);
        return actualChecksum.equals(expectedChecksum);
    }
    
    /**
     * Create asset-specific metadata.
     */
    private static Map<String, Object> createAssetMetadata(int assetType, byte[] data) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("OriginalSize", data.length);
        metadata.put("AssetType", assetType);
        metadata.put("ProcessedAt", System.currentTimeMillis() / 1000.0);
        
        // Add type-specific metadata
        if (SLAssetType.isTextureType(assetType)) {
            metadata.put("Category", "Texture");
        } else if (SLAssetType.isSoundType(assetType)) {
            metadata.put("Category", "Audio");
        } else {
            metadata.put("Category", "Data");
        }
        
        return metadata;
    }
    
    /**
     * Create data stream LLSD structure.
     */
    public static Map<String, Object> createDataStreamLLSD(DataStreamInfo info, List<StreamChunk> chunks) {
        Map<String, Object> streamData = new HashMap<>();
        streamData.put("AssetID", info.getAssetId());
        streamData.put("AssetType", info.getAssetType());
        streamData.put("TotalSize", info.getTotalSize());
        streamData.put("CompressedSize", info.getCompressedSize());
        streamData.put("Compression", info.getCompression().getName());
        streamData.put("ChunkCount", info.getChunkCount());
        streamData.put("Checksum", info.getOverallChecksum());
        streamData.put("Metadata", info.getMetadata());
        
        // Add chunk information
        List<Map<String, Object>> chunkList = new ArrayList<>();
        for (StreamChunk chunk : chunks) {
            Map<String, Object> chunkData = new HashMap<>();
            chunkData.put("Index", chunk.getChunkIndex());
            chunkData.put("Size", chunk.getData().length);
            chunkData.put("Checksum", chunk.getChecksum());
            chunkData.put("Compressed", chunk.isCompressed());
            chunkList.add(chunkData);
        }
        streamData.put("Chunks", chunkList);
        
        streamData.put("Timestamp", System.currentTimeMillis() / 1000.0);
        
        return streamData;
    }
    
    /**
     * Clean expired cache entries.
     */
    private static void cleanExpiredCache() {
        dataCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Clear data cache.
     */
    public static void clearCache() {
        dataCache.clear();
    }
    
    /**
     * Get cache statistics.
     */
    public static Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("CacheSize", dataCache.size());
        
        long totalSize = dataCache.values().stream()
                .mapToLong(cache -> cache.getData().length)
                .sum();
        stats.put("TotalCacheSize", totalSize);
        
        long totalOriginalSize = dataCache.values().stream()
                .mapToLong(cache -> cache.getInfo().getTotalSize())
                .sum();
        stats.put("TotalOriginalSize", totalOriginalSize);
        
        double compressionRatio = totalSize > 0 ? (double) totalSize / totalOriginalSize : 1.0;
        stats.put("OverallCompressionRatio", compressionRatio);
        
        return stats;
    }
}