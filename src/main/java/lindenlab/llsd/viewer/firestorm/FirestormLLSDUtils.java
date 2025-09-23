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
     */
    public static class RLVCommand {
        public final String command;
        public final String param;
        public final String option;
        public final UUID sourceId;
        
        public RLVCommand(String command, String param, String option, UUID sourceId) {
            this.command = command;
            this.param = param;
            this.option = option;
            this.sourceId = sourceId;
        }
        
        /**
         * Convert to LLSD representation.
         * 
         * @return LLSD map containing RLV command data
         */
        public Map<String, Object> toLLSD() {
            Map<String, Object> rlvData = new HashMap<>();
            rlvData.put("Command", command != null ? command : "");
            rlvData.put("Parameter", param != null ? param : "");
            rlvData.put("Option", option != null ? option : "");
            rlvData.put("SourceID", sourceId);
            rlvData.put("Timestamp", System.currentTimeMillis() / 1000.0);
            return rlvData;
        }
        
        /**
         * Parse from LLSD representation.
         * 
         * @param llsdData the LLSD data
         * @return parsed RLV command
         * @throws LLSDException if parsing fails
         */
        public static RLVCommand fromLLSD(Object llsdData) throws LLSDException {
            if (!(llsdData instanceof Map)) {
                throw new LLSDException("RLV command must be a map");
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) llsdData;
            
            String command = data.get("Command").toString();
            String param = data.containsKey("Parameter") ? data.get("Parameter").toString() : "";
            String option = data.containsKey("Option") ? data.get("Option").toString() : "";
            UUID sourceId = (UUID) data.get("SourceID");
            
            return new RLVCommand(command, param, option, sourceId);
        }
    }
    
    /**
     * Create an LLSD structure for Firestorm radar data.
     * 
     * @param agentId the agent UUID
     * @param name the agent display name
     * @param userName the agent username
     * @param position the agent position
     * @param distance the distance from viewer
     * @param typing whether the agent is typing
     * @param attachments list of agent attachments
     * @return LLSD map containing radar data
     */
    public static Map<String, Object> createRadarData(UUID agentId,
                                                      String name,
                                                      String userName,
                                                      double[] position,
                                                      double distance,
                                                      boolean typing,
                                                      List<Map<String, Object>> attachments) {
        Map<String, Object> radarData = new HashMap<>();
        radarData.put("AgentID", agentId);
        radarData.put("DisplayName", name != null ? name : "");
        radarData.put("UserName", userName != null ? userName : "");
        radarData.put("Distance", distance);
        radarData.put("IsTyping", typing);
        radarData.put("LastSeen", System.currentTimeMillis() / 1000.0);
        
        if (position != null && position.length == 3) {
            radarData.put("Position", Arrays.stream(position).boxed().toList());
        }
        
        if (attachments != null && !attachments.isEmpty()) {
            radarData.put("Attachments", attachments);
        }
        
        return radarData;
    }
    
    /**
     * Create an LLSD structure for Firestorm bridge communication.
     * 
     * @param command the bridge command
     * @param parameters the command parameters
     * @param requestId the request identifier
     * @param priority the message priority (0-3)
     * @return LLSD map containing bridge data
     */
    public static Map<String, Object> createBridgeMessage(String command,
                                                          Map<String, Object> parameters,
                                                          String requestId,
                                                          int priority) {
        Map<String, Object> bridgeData = new HashMap<>();
        bridgeData.put("Command", command != null ? command : "");
        bridgeData.put("RequestID", requestId != null ? requestId : UUID.randomUUID().toString());
        bridgeData.put("Priority", Math.max(0, Math.min(3, priority)));
        bridgeData.put("Timestamp", System.currentTimeMillis() / 1000.0);
        
        if (parameters != null && !parameters.isEmpty()) {
            bridgeData.put("Parameters", parameters);
        }
        
        return bridgeData;
    }
    
    /**
     * Create an LLSD structure for Firestorm performance statistics.
     * 
     * @param fps the current FPS
     * @param bandwidth the current bandwidth usage
     * @param memoryUsed the memory usage in MB
     * @param renderTime the render time in milliseconds
     * @param scriptTime the script processing time
     * @param triangles the number of triangles rendered
     * @return LLSD map containing performance data
     */
    public static Map<String, Object> createPerformanceStats(double fps,
                                                             double bandwidth,
                                                             double memoryUsed,
                                                             double renderTime,
                                                             double scriptTime,
                                                             int triangles) {
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
     * @param mediaUrl the media URL
     * @param mediaType the media type (audio/video)
     * @param autoPlay whether to auto-play
     * @param autoScale whether to auto-scale
     * @param looping whether to loop playback
     * @param volume the playback volume (0.0-1.0)
     * @return LLSD map containing media data
     */
    public static Map<String, Object> createMediaData(String mediaUrl,
                                                      String mediaType,
                                                      boolean autoPlay,
                                                      boolean autoScale,
                                                      boolean looping,
                                                      double volume) {
        Map<String, Object> mediaData = new HashMap<>();
        mediaData.put("URL", mediaUrl != null ? mediaUrl : "");
        mediaData.put("Type", mediaType != null ? mediaType : "");
        mediaData.put("AutoPlay", autoPlay);
        mediaData.put("AutoScale", autoScale);
        mediaData.put("Looping", looping);
        mediaData.put("Volume", Math.max(0.0, Math.min(1.0, volume)));
        
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
     * Check if a Firestorm version is compatible.
     * 
     * @param version the version to check
     * @param minVersion the minimum required version
     * @return true if compatible
     */
    private static boolean isCompatibleFSVersion(String version, String minVersion) {
        if (version == null || minVersion == null) {
            return false;
        }
        
        // Simple version comparison (assumes format like "6.6.17.66677")
        String[] versionParts = version.split("\\.");
        String[] minVersionParts = minVersion.split("\\.");
        
        for (int i = 0; i < Math.min(versionParts.length, minVersionParts.length); i++) {
            try {
                int v = Integer.parseInt(versionParts[i]);
                int min = Integer.parseInt(minVersionParts[i]);
                
                if (v > min) return true;
                if (v < min) return false;
            } catch (NumberFormatException e) {
                // Fallback to string comparison
                int comparison = versionParts[i].compareTo(minVersionParts[i]);
                if (comparison > 0) return true;
                if (comparison < 0) return false;
            }
        }
        
        return versionParts.length >= minVersionParts.length;
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