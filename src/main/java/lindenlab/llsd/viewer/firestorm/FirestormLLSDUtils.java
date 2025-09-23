/*
 * Firestorm LLSD Extensions - Java implementation of Firestorm-specific LLSD functionality
 *
 * Based on Firestorm viewer implementation  
 * Copyright (C) 2010, Linden Research, Inc.
 * Firestorm enhancements Copyright (C) The Phoenix Firestorm Project, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer.firestorm;

import lindenlab.llsd.LLSD;
import lindenlab.llsd.LLSDException;
import lindenlab.llsd.viewer.secondlife.SecondLifeLLSDUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * Firestorm specific LLSD utilities and extensions.
 * 
 * <p>This class provides LLSD functionality that is specific to the Firestorm
 * viewer implementation, building upon the base Second Life functionality with
 * additional features, optimizations, and viewer-specific enhancements.</p>
 * 
 * <p>Key Firestorm specific features:</p>
 * <ul>
 * <li>Enhanced RLV (Restrained Life Viewer) support</li>
 * <li>Advanced radar and contact management</li>
 * <li>Enhanced media and streaming data structures</li>
 * <li>Firestorm-specific UI and preference handling</li>
 * <li>Bridge and LSL communication enhancements</li>
 * <li>Performance monitoring and statistics</li>
 * <li>Advanced scripting and automation support</li>
 * </ul>
 * 
 * @since 1.0
 */
public final class FirestormLLSDUtils {
    
    private FirestormLLSDUtils() {
        // Utility class - no instances
    }
    
    /**
     * Firestorm specific message types extending Second Life types.
     */
    public enum FSMessageType {
        // RLV related
        RLV_COMMAND,
        RLV_RESPONSE,
        RLV_STATUS,
        
        // Radar and contacts
        RADAR_UPDATE,
        CONTACT_UPDATE,
        FRIEND_STATUS,
        
        // Bridge communication
        BRIDGE_REQUEST,
        BRIDGE_RESPONSE,
        BRIDGE_STATUS,
        
        // Firestorm UI
        FS_PREFERENCE,
        FS_NOTIFICATION,
        FS_FLOATER_DATA,
        
        // Performance monitoring
        PERF_STATS,
        MEMORY_STATS,
        RENDER_STATS,
        
        // Media and streaming
        MEDIA_STATUS,
        STREAM_DATA,
        PARCEL_MEDIA
    }
    
    /**
     * RLV (Restrained Life Viewer) command structure.
     * 
     * <p>Represents an RLV command following Second Life RLV standards.
     * All RLV commands must have a command string, and optionally parameters,
     * options, and source identification for security tracking.</p>
     */
    public static class RLVCommand {
        private final String command;
        private final String param;
        private final String option;
        private final UUID sourceId;
        
        /**
         * Constructs a new RLV command.
         * 
         * @param command the RLV command (required, cannot be null or empty)
         * @param param the command parameter (can be null or empty)
         * @param option the command option (can be null or empty)
         * @param sourceId the UUID of the command source (required for security)
         * @throws IllegalArgumentException if command is null/empty or sourceId is null
         */
        public RLVCommand(String command, String param, String option, UUID sourceId) {
            if (command == null || command.trim().isEmpty()) {
                throw new IllegalArgumentException("RLV command cannot be null or empty");
            }
            if (sourceId == null) {
                throw new IllegalArgumentException("RLV sourceId cannot be null");
            }
            
            this.command = command.trim();
            this.param = param != null ? param.trim() : "";
            this.option = option != null ? option.trim() : "";
            this.sourceId = sourceId;
        }
        
        /**
         * Gets the RLV command string.
         * @return the command string, never null or empty
         */
        public String getCommand() {
            return command;
        }
        
        /**
         * Gets the RLV command parameter.
         * @return the parameter string, never null but may be empty
         */
        public String getParam() {
            return param;
        }
        
        /**
         * Gets the RLV command option.
         * @return the option string, never null but may be empty
         */
        public String getOption() {
            return option;
        }
        
        /**
         * Gets the source UUID for this command.
         * @return the source UUID, never null
         */
        public UUID getSourceId() {
            return sourceId;
        }
        
        /**
         * Convert to LLSD representation.
         * 
         * @return LLSD map containing RLV command data, never null
         */
        public Map<String, Object> toLLSD() {
            Map<String, Object> rlvData = new HashMap<>();
            rlvData.put("Command", command);
            rlvData.put("Parameter", param);
            rlvData.put("Option", option);
            rlvData.put("SourceID", sourceId.toString());
            rlvData.put("Timestamp", System.currentTimeMillis() / 1000.0);
            return rlvData;
        }
        
        /**
         * Parse from LLSD representation.
         * 
         * @param llsdData the LLSD data to parse (must be a Map)
         * @return parsed RLV command, never null
         * @throws LLSDException if parsing fails or data is invalid
         */
        public static RLVCommand fromLLSD(Object llsdData) throws LLSDException {
            if (llsdData == null) {
                throw new LLSDException("RLV command data cannot be null");
            }
            if (!(llsdData instanceof Map)) {
                throw new LLSDException("RLV command must be a map, got: " + llsdData.getClass().getSimpleName());
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) llsdData;
            
            // Extract command (required)
            Object commandObj = data.get("Command");
            if (commandObj == null) {
                throw new LLSDException("RLV command missing required 'Command' field");
            }
            String command = commandObj.toString();
            
            // Extract parameter (optional)
            String param = data.containsKey("Parameter") ? 
                          data.get("Parameter").toString() : "";
            
            // Extract option (optional)  
            String option = data.containsKey("Option") ? 
                           data.get("Option").toString() : "";
            
            // Extract sourceId (required)
            Object sourceIdObj = data.get("SourceID");
            UUID sourceId;
            if (sourceIdObj == null) {
                throw new LLSDException("RLV command missing required 'SourceID' field");
            }
            
            try {
                if (sourceIdObj instanceof UUID) {
                    sourceId = (UUID) sourceIdObj;
                } else {
                    sourceId = UUID.fromString(sourceIdObj.toString());
                }
            } catch (IllegalArgumentException e) {
                throw new LLSDException("Invalid SourceID format: " + sourceIdObj, e);
            }
            
            return new RLVCommand(command, param, option, sourceId);
        }
    }
    
    /**
     * Create an LLSD structure for Firestorm radar data.
     * 
     * @param agentId the agent UUID (required)
     * @param name the agent display name (can be null)
     * @param userName the agent username (can be null)
     * @param position the agent position as [x, y, z] array (can be null)
     * @param distance the distance from viewer (must be >= 0)
     * @param typing whether the agent is typing
     * @param attachments list of agent attachments (can be null)
     * @return LLSD map containing radar data, never null
     * @throws IllegalArgumentException if agentId is null or distance is negative
     */
    public static Map<String, Object> createRadarData(UUID agentId,
                                                      String name,
                                                      String userName,
                                                      double[] position,
                                                      double distance,
                                                      boolean typing,
                                                      List<Map<String, Object>> attachments) {
        if (agentId == null) {
            throw new IllegalArgumentException("Agent ID cannot be null");
        }
        if (distance < 0) {
            throw new IllegalArgumentException("Distance cannot be negative: " + distance);
        }
        
        Map<String, Object> radarData = new HashMap<>();
        radarData.put("AgentID", agentId.toString());
        radarData.put("DisplayName", name != null ? name : "");
        radarData.put("UserName", userName != null ? userName : "");
        radarData.put("Distance", distance);
        radarData.put("IsTyping", typing);
        radarData.put("LastSeen", System.currentTimeMillis() / 1000.0);
        
        if (position != null) {
            if (position.length != 3) {
                throw new IllegalArgumentException("Position array must have exactly 3 elements (x, y, z)");
            }
            radarData.put("Position", Arrays.stream(position).boxed().toList());
        }
        
        if (attachments != null && !attachments.isEmpty()) {
            radarData.put("Attachments", new ArrayList<>(attachments)); // Defensive copy
        }
        
        return radarData;
    }
    
    /**
     * Create an LLSD structure for Firestorm bridge communication.
     * 
     * @param command the bridge command (required)
     * @param parameters the command parameters (can be null)
     * @param requestId the request identifier (auto-generated if null)
     * @param priority the message priority 0-3 (clamped to valid range)
     * @return LLSD map containing bridge data, never null
     * @throws IllegalArgumentException if command is null or empty
     */
    public static Map<String, Object> createBridgeMessage(String command,
                                                          Map<String, Object> parameters,
                                                          String requestId,
                                                          int priority) {
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("Bridge command cannot be null or empty");
        }
        
        Map<String, Object> bridgeData = new HashMap<>();
        bridgeData.put("Command", command.trim());
        bridgeData.put("RequestID", requestId != null ? requestId : UUID.randomUUID().toString());
        bridgeData.put("Priority", Math.max(0, Math.min(3, priority))); // Clamp to 0-3 range
        bridgeData.put("Timestamp", System.currentTimeMillis() / 1000.0);
        
        if (parameters != null && !parameters.isEmpty()) {
            bridgeData.put("Parameters", new HashMap<>(parameters)); // Defensive copy
        }
        
        return bridgeData;
    }
    
    /**
     * Create an LLSD structure for Firestorm performance statistics.
     * 
     * @param fps the current FPS (must be >= 0)
     * @param bandwidth the current bandwidth usage in bytes/sec (must be >= 0)
     * @param memoryUsed the memory usage in MB (must be >= 0)
     * @param renderTime the render time in milliseconds (must be >= 0)
     * @param scriptTime the script processing time in milliseconds (must be >= 0)
     * @param triangles the number of triangles rendered (must be >= 0)
     * @return LLSD map containing performance data, never null
     * @throws IllegalArgumentException if any parameter is negative
     */
    public static Map<String, Object> createPerformanceStats(double fps,
                                                             double bandwidth,
                                                             double memoryUsed,
                                                             double renderTime,
                                                             double scriptTime,
                                                             int triangles) {
        if (fps < 0) {
            throw new IllegalArgumentException("FPS cannot be negative: " + fps);
        }
        if (bandwidth < 0) {
            throw new IllegalArgumentException("Bandwidth cannot be negative: " + bandwidth);
        }
        if (memoryUsed < 0) {
            throw new IllegalArgumentException("Memory usage cannot be negative: " + memoryUsed);
        }
        if (renderTime < 0) {
            throw new IllegalArgumentException("Render time cannot be negative: " + renderTime);
        }
        if (scriptTime < 0) {
            throw new IllegalArgumentException("Script time cannot be negative: " + scriptTime);
        }
        if (triangles < 0) {
            throw new IllegalArgumentException("Triangle count cannot be negative: " + triangles);
        }
        
        Map<String, Object> perfStats = new HashMap<>();
        perfStats.put("FPS", fps);
        perfStats.put("Bandwidth", bandwidth);
        perfStats.put("MemoryUsed", memoryUsed);
        perfStats.put("RenderTime", renderTime);
        perfStats.put("ScriptTime", scriptTime);
        perfStats.put("Triangles", triangles);
        perfStats.put("Timestamp", System.currentTimeMillis() / 1000.0);
        
        return perfStats;
    }
    
    /**
     * Create an LLSD structure for Firestorm media data.
     * 
     * @param mediaUrl the media URL (can be null)
     * @param mediaType the media type (audio/video, can be null)
     * @param autoPlay whether to auto-play
     * @param autoScale whether to auto-scale
     * @param looping whether to loop playback
     * @param volume the playback volume (will be clamped to 0.0-1.0 range)
     * @return LLSD map containing media data, never null
     */
    public static Map<String, Object> createMediaData(String mediaUrl,
                                                      String mediaType,
                                                      boolean autoPlay,
                                                      boolean autoScale,
                                                      boolean looping,
                                                      double volume) {
        Map<String, Object> mediaData = new HashMap<>();
        mediaData.put("URL", mediaUrl != null ? mediaUrl.trim() : "");
        mediaData.put("Type", mediaType != null ? mediaType.trim() : "");
        mediaData.put("AutoPlay", autoPlay);
        mediaData.put("AutoScale", autoScale);
        mediaData.put("Looping", looping);
        mediaData.put("Volume", Math.max(0.0, Math.min(1.0, volume))); // Clamp to 0.0-1.0 range
        mediaData.put("Timestamp", System.currentTimeMillis() / 1000.0);
        
        return mediaData;
    }
    
    /**
     * Firestorm-specific LLSD validation with enhanced rules.
     * 
     * @param llsdData the LLSD data to validate
     * @param rules the Firestorm validation rules
     * @return validation result
     */
    public static FSValidationResult validateFSStructure(Object llsdData, FSValidationRules rules) {
        FSValidationResult result = new FSValidationResult();
        
        // Use base SL validation first
        SecondLifeLLSDUtils.ValidationResult baseResult = 
            SecondLifeLLSDUtils.validateSLStructure(llsdData, rules.getBaseRules());
        
        result.addErrors(baseResult.getErrors());
        result.addWarnings(baseResult.getWarnings());
        
        if (!baseResult.isValid()) {
            return result; // Don't continue if base validation failed
        }
        
        // Firestorm-specific validations
        if (llsdData instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapData = (Map<String, Object>) llsdData;
            
            // Check Firestorm version requirements
            if (rules.requiresFSVersion && mapData.containsKey("ViewerVersion")) {
                String version = mapData.get("ViewerVersion").toString();
                if (!isCompatibleFSVersion(version, rules.minFSVersion)) {
                    result.addError("Incompatible Firestorm version: " + version + 
                                   " (required: " + rules.minFSVersion + "+)");
                }
            }
            
            // Check RLV requirements
            if (rules.requiresRLV && mapData.containsKey("RLVEnabled")) {
                Boolean rlvEnabled = (Boolean) mapData.get("RLVEnabled");
                if (!Boolean.TRUE.equals(rlvEnabled)) {
                    result.addError("RLV support required but not enabled");
                }
            }
            
            // Check bridge requirements
            if (rules.requiresBridge && mapData.containsKey("BridgeConnected")) {
                Boolean bridgeConnected = (Boolean) mapData.get("BridgeConnected");
                if (!Boolean.TRUE.equals(bridgeConnected)) {
                    result.addWarning("Bridge connection recommended for full functionality");
                }
            }
        }
        
        return result;
    }
    
    /**
     * Check if a Firestorm version is compatible with the minimum required version.
     * 
     * <p>Supports semantic versioning format (major.minor.patch.build) and handles
     * various edge cases including missing components and non-numeric parts.</p>
     * 
     * @param version the version to check (can be null)
     * @param minVersion the minimum required version (can be null)
     * @return true if version meets or exceeds minimum requirement
     */
    private static boolean isCompatibleFSVersion(String version, String minVersion) {
        if (version == null || minVersion == null || 
            version.trim().isEmpty() || minVersion.trim().isEmpty()) {
            return false;
        }
        
        // Normalize versions by removing leading/trailing whitespace
        String[] versionParts = version.trim().split("\\.");
        String[] minVersionParts = minVersion.trim().split("\\.");
        
        // Compare version components
        int maxLength = Math.max(versionParts.length, minVersionParts.length);
        for (int i = 0; i < maxLength; i++) {
            int v = parseVersionComponent(versionParts, i);
            int min = parseVersionComponent(minVersionParts, i);
            
            if (v > min) return true;
            if (v < min) return false;
            // If equal, continue to next component
        }
        
        return true; // All components are equal, so it's compatible
    }
    
    /**
     * Parse a version component, handling missing parts and non-numeric values.
     * 
     * @param parts the version parts array
     * @param index the index to parse
     * @return the numeric value, or 0 if missing/invalid
     */
    private static int parseVersionComponent(String[] parts, int index) {
        if (index >= parts.length) {
            return 0; // Missing component defaults to 0
        }
        
        try {
            return Integer.parseInt(parts[index].trim());
        } catch (NumberFormatException e) {
            // For non-numeric parts, use string comparison
            // Return a hash-based number for consistent comparison
            return parts[index].trim().hashCode() & Integer.MAX_VALUE;
        }
    }
    
    /**
     * Firestorm-specific validation rules extending base SL rules.
     */
    public static class FSValidationRules {
        private final SecondLifeLLSDUtils.SLValidationRules baseRules = new SecondLifeLLSDUtils.SLValidationRules();
        
        public boolean requiresFSVersion = false;
        public String minFSVersion = "6.0.0";
        public boolean requiresRLV = false;
        public boolean requiresBridge = false;
        public Set<String> fsRequiredFields = new HashSet<>();
        public Map<String, String> fsFieldValidators = new HashMap<>();
        
        public FSValidationRules requireMap() {
            baseRules.requireMap();
            return this;
        }
        
        public FSValidationRules requireArray() {
            baseRules.requireArray();
            return this;
        }
        
        public FSValidationRules requireField(String field) {
            baseRules.requireField(field);
            return this;
        }
        
        public FSValidationRules requireField(String field, Class<?> type) {
            baseRules.requireField(field, type);
            return this;
        }
        
        public FSValidationRules requireFSVersion(String minVersion) {
            requiresFSVersion = true;
            minFSVersion = minVersion;
            return this;
        }
        
        public FSValidationRules requireRLV() {
            requiresRLV = true;
            return this;
        }
        
        public FSValidationRules requireBridge() {
            requiresBridge = true;
            return this;
        }
        
        public FSValidationRules requireFSField(String field) {
            fsRequiredFields.add(field);
            return this;
        }
        
        public FSValidationRules addFieldValidator(String field, String validatorName) {
            fsFieldValidators.put(field, validatorName);
            return this;
        }
        
        SecondLifeLLSDUtils.SLValidationRules getBaseRules() {
            return baseRules;
        }
    }
    
    /**
     * Enhanced validation result for Firestorm-specific checks.
     */
    public static class FSValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final List<String> info = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public void addInfo(String info) {
            this.info.add(info);
        }
        
        public void addErrors(List<String> errors) {
            this.errors.addAll(errors);
        }
        
        public void addWarnings(List<String> warnings) {
            this.warnings.addAll(warnings);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }
        
        public List<String> getWarnings() {
            return Collections.unmodifiableList(warnings);
        }
        
        public List<String> getInfo() {
            return Collections.unmodifiableList(info);
        }
        
        public String getSummary() {
            StringBuilder summary = new StringBuilder();
            
            if (!errors.isEmpty()) {
                summary.append("Errors: ").append(errors.size()).append("\n");
                errors.forEach(error -> summary.append("  - ").append(error).append("\n"));
            }
            
            if (!warnings.isEmpty()) {
                summary.append("Warnings: ").append(warnings.size()).append("\n");
                warnings.forEach(warning -> summary.append("  - ").append(warning).append("\n"));
            }
            
            if (!info.isEmpty()) {
                summary.append("Info: ").append(info.size()).append("\n");
                info.forEach(infoMsg -> summary.append("  - ").append(infoMsg).append("\n"));
            }
            
            return summary.toString();
        }
    }
    
    /**
     * Thread-safe cache for Firestorm LLSD processing.
     */
    public static class FSLLSDCache {
        private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
        private final long maxAge;
        private final ConcurrentHashMap<String, Long> timestamps = new ConcurrentHashMap<>();
        
        public FSLLSDCache(long maxAgeMillis) {
            this.maxAge = maxAgeMillis;
        }
        
        public void put(String key, Object value) {
            cache.put(key, value);
            timestamps.put(key, System.currentTimeMillis());
        }
        
        public Object get(String key) {
            Long timestamp = timestamps.get(key);
            if (timestamp != null && System.currentTimeMillis() - timestamp > maxAge) {
                // Expired
                cache.remove(key);
                timestamps.remove(key);
                return null;
            }
            return cache.get(key);
        }
        
        public boolean contains(String key) {
            return get(key) != null;
        }
        
        public void clear() {
            cache.clear();
            timestamps.clear();
        }
        
        public int size() {
            // Clean expired entries first
            long now = System.currentTimeMillis();
            timestamps.entrySet().removeIf(entry -> now - entry.getValue() > maxAge);
            cache.keySet().retainAll(timestamps.keySet());
            
            return cache.size();
        }
    }
}