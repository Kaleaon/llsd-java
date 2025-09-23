/*
 * Second Life LLSD Extensions - Java implementation of SL-specific LLSD functionality
 *
 * Based on Second Life viewer implementation
 * Copyright (C) 2010, Linden Research, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer.secondlife;

import lindenlab.llsd.LLSD;
import lindenlab.llsd.LLSDException;
import lindenlab.llsd.viewer.LLSDViewerUtils;
import lindenlab.llsd.viewer.secondlife.assets.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Second Life specific LLSD utilities and extensions.
 * 
 * <p>This class provides LLSD functionality that is specific to the Second Life
 * viewer implementation, including specialized data structures, protocols,
 * and formatting used within the SL ecosystem.</p>
 * 
 * <p>Key Second Life specific features:</p>
 * <ul>
 * <li>SL-specific message formats and protocols</li>
 * <li>Avatar and object data structures</li>
 * <li>Land and parcel information handling</li>
 * <li>Asset and inventory data management</li>
 * <li>Chat and communication structures</li>
 * <li>Physics and simulation data formats</li>
 * </ul>
 * 
 * @since 1.0
 */
public final class SecondLifeLLSDUtils {
    
    private SecondLifeLLSDUtils() {
        // Utility class - no instances
    }
    
    /**
     * Second Life specific message types.
     */
    public enum SLMessageType {
        AGENT_UPDATE,
        CHAT_MESSAGE,
        OBJECT_UPDATE,
        PARCEL_INFO,
        INVENTORY_ITEM,
        ASSET_DATA,
        TELEPORT_REQUEST,
        PHYSICS_UPDATE,
        ANIMATION_DATA,
        TEXTURE_REQUEST
    }
    
    /**
     * Create an LLSD structure for Second Life agent data.
     * 
     * @param agentId the agent UUID
     * @param position the agent position (x, y, z)
     * @param rotation the agent rotation (x, y, z, w quaternion)
     * @param velocity the agent velocity (x, y, z)
     * @return LLSD map containing agent data
     */
    public static Map<String, Object> createAgentData(UUID agentId, 
                                                      double[] position, 
                                                      double[] rotation, 
                                                      double[] velocity) {
        if (position.length != 3 || rotation.length != 4 || velocity.length != 3) {
            throw new IllegalArgumentException("Invalid vector dimensions");
        }
        
        Map<String, Object> agentData = new HashMap<>();
        agentData.put("AgentID", agentId);
        agentData.put("Position", Arrays.stream(position).boxed().collect(Collectors.toList()));
        agentData.put("Rotation", Arrays.stream(rotation).boxed().collect(Collectors.toList()));
        agentData.put("Velocity", Arrays.stream(velocity).boxed().collect(Collectors.toList()));
        agentData.put("Timestamp", System.currentTimeMillis() / 1000.0);
        
        return agentData;
    }
    
    /**
     * Create an LLSD structure for Second Life object data.
     * 
     * @param objectId the object UUID
     * @param parentId the parent object UUID (null if root)
     * @param position the object position
     * @param rotation the object rotation
     * @param scale the object scale
     * @param material the object material properties
     * @return LLSD map containing object data
     */
    public static Map<String, Object> createObjectData(UUID objectId,
                                                       UUID parentId,
                                                       double[] position,
                                                       double[] rotation,
                                                       double[] scale,
                                                       Map<String, Object> material) {
        if (position.length != 3 || rotation.length != 4 || scale.length != 3) {
            throw new IllegalArgumentException("Invalid vector dimensions");
        }
        
        Map<String, Object> objectData = new HashMap<>();
        objectData.put("ObjectID", objectId);
        objectData.put("ParentID", parentId);
        objectData.put("Position", Arrays.stream(position).boxed().collect(Collectors.toList()));
        objectData.put("Rotation", Arrays.stream(rotation).boxed().collect(Collectors.toList()));
        objectData.put("Scale", Arrays.stream(scale).boxed().collect(Collectors.toList()));
        
        if (material != null) {
            objectData.put("Material", material);
        }
        
        return objectData;
    }
    
    /**
     * Create an LLSD structure for Second Life parcel data.
     * 
     * @param parcelId the parcel UUID
     * @param name the parcel name
     * @param description the parcel description
     * @param area the parcel area in square meters
     * @param ownerId the owner UUID
     * @param groupId the group UUID (null if none)
     * @param flags the parcel flags
     * @return LLSD map containing parcel data
     */
    public static Map<String, Object> createParcelData(UUID parcelId,
                                                       String name,
                                                       String description,
                                                       int area,
                                                       UUID ownerId,
                                                       UUID groupId,
                                                       int flags) {
        Map<String, Object> parcelData = new HashMap<>();
        parcelData.put("ParcelID", parcelId);
        parcelData.put("Name", name != null ? name : "");
        parcelData.put("Description", description != null ? description : "");
        parcelData.put("Area", area);
        parcelData.put("OwnerID", ownerId);
        parcelData.put("GroupID", groupId);
        parcelData.put("Flags", flags);
        
        return parcelData;
    }
    
    /**
     * Create an LLSD structure for Second Life inventory item.
     * 
     * @param itemId the item UUID
     * @param parentId the parent folder UUID
     * @param name the item name
     * @param description the item description
     * @param type the item type
     * @param assetId the associated asset UUID
     * @param permissions the item permissions
     * @return LLSD map containing inventory item data
     */
    public static Map<String, Object> createInventoryItem(UUID itemId,
                                                          UUID parentId,
                                                          String name,
                                                          String description,
                                                          int type,
                                                          UUID assetId,
                                                          Map<String, Object> permissions) {
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("ItemID", itemId);
        itemData.put("ParentID", parentId);
        itemData.put("Name", name != null ? name : "");
        itemData.put("Description", description != null ? description : "");
        itemData.put("Type", type);
        itemData.put("AssetID", assetId);
        
        if (permissions != null) {
            itemData.put("Permissions", permissions);
        }
        
        itemData.put("CreationDate", new Date());
        
        return itemData;
    }
    
    /**
     * Create an LLSD structure for Second Life chat message.
     * 
     * @param fromId the sender UUID
     * @param fromName the sender name
     * @param message the message text
     * @param channel the chat channel
     * @param type the chat type (0=say, 1=shout, 2=whisper)
     * @param position the sender position
     * @return LLSD map containing chat message data
     */
    public static Map<String, Object> createChatMessage(UUID fromId,
                                                        String fromName,
                                                        String message,
                                                        int channel,
                                                        int type,
                                                        double[] position) {
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("FromID", fromId);
        chatData.put("FromName", fromName != null ? fromName : "");
        chatData.put("Message", message != null ? message : "");
        chatData.put("Channel", channel);
        chatData.put("Type", type);
        chatData.put("Timestamp", System.currentTimeMillis() / 1000.0);
        
        if (position != null && position.length == 3) {
            chatData.put("Position", Arrays.stream(position).boxed().collect(Collectors.toList()));
        }
        
        return chatData;
    }
    
    /**
     * Create an LLSD structure for Second Life asset data.
     * 
     * @param assetId the asset UUID
     * @param type the asset type
     * @param name the asset name
     * @param description the asset description
     * @param data the asset binary data
     * @param temporary whether the asset is temporary
     * @return LLSD map containing asset data
     */
    public static Map<String, Object> createAssetData(UUID assetId,
                                                      int type,
                                                      String name,
                                                      String description,
                                                      byte[] data,
                                                      boolean temporary) {
        Map<String, Object> assetData = new HashMap<>();
        assetData.put("AssetID", assetId);
        assetData.put("Type", type);
        assetData.put("Name", name != null ? name : "");
        assetData.put("Description", description != null ? description : "");
        assetData.put("Data", data != null ? data : new byte[0]);
        assetData.put("Temporary", temporary);
        assetData.put("CreationDate", new Date());
        
        return assetData;
    }
    
    /**
     * Parse Second Life specific message format.
     * 
     * @param llsdData the LLSD data to parse
     * @param expectedType the expected message type
     * @return parsed message data
     * @throws LLSDException if parsing fails
     */
    public static Map<String, Object> parseSLMessage(Object llsdData, SLMessageType expectedType) 
            throws LLSDException {
        if (!(llsdData instanceof Map)) {
            throw new LLSDException("SL message must be a map");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> messageMap = (Map<String, Object>) llsdData;
        
        // Validate common SL message structure
        if (!messageMap.containsKey("MessageType")) {
            throw new LLSDException("SL message missing MessageType");
        }
        
        String messageType = messageMap.get("MessageType").toString();
        if (!messageType.equals(expectedType.name())) {
            throw new LLSDException("Expected " + expectedType + " but got " + messageType);
        }
        
        // Validate timestamp if present
        if (messageMap.containsKey("Timestamp")) {
            Object timestamp = messageMap.get("Timestamp");
            if (!(timestamp instanceof Number)) {
                throw new LLSDException("Invalid timestamp format");
            }
        }
        
        return messageMap;
    }
    
    /**
     * Validate Second Life UUID format.
     * 
     * @param uuid the UUID to validate
     * @return true if valid SL UUID
     */
    public static boolean isValidSLUUID(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        
        // SL UUIDs should not be null UUID
        return !uuid.equals(new UUID(0L, 0L));
    }
    
    /**
     * Create a Second Life compatible LLSD response structure.
     * 
     * @param success whether the operation succeeded
     * @param message the response message
     * @param data optional response data
     * @return LLSD response map
     */
    public static Map<String, Object> createSLResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("Success", success);
        response.put("Message", message != null ? message : "");
        response.put("Timestamp", System.currentTimeMillis() / 1000.0);
        
        if (data != null) {
            response.put("Data", data);
        }
        
        return response;
    }
    
    /**
     * Second Life specific LLSD validation rules.
     * 
     * @param llsdData the LLSD data to validate
     * @param rules the validation rules to apply
     * @return validation result with any errors
     */
    public static ValidationResult validateSLStructure(Object llsdData, SLValidationRules rules) {
        ValidationResult result = new ValidationResult();
        
        if (rules.requiresMap && !(llsdData instanceof Map)) {
            result.addError("Expected Map but got " + (llsdData != null ? llsdData.getClass().getSimpleName() : "null"));
            return result;
        }
        
        if (rules.requiresArray && !(llsdData instanceof List)) {
            result.addError("Expected Array but got " + (llsdData != null ? llsdData.getClass().getSimpleName() : "null"));
            return result;
        }
        
        if (llsdData instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapData = (Map<String, Object>) llsdData;
            
            // Check required fields
            for (String required : rules.requiredFields) {
                if (!mapData.containsKey(required)) {
                    result.addError("Missing required field: " + required);
                }
            }
            
            // Check field types
            for (Map.Entry<String, Class<?>> typeCheck : rules.fieldTypes.entrySet()) {
                String field = typeCheck.getKey();
                Class<?> expectedType = typeCheck.getValue();
                
                if (mapData.containsKey(field)) {
                    Object value = mapData.get(field);
                    if (value != null && !expectedType.isAssignableFrom(value.getClass())) {
                        result.addError("Field " + field + " expected " + expectedType.getSimpleName() + 
                                       " but got " + value.getClass().getSimpleName());
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Validation rules for Second Life LLSD structures.
     */
    public static class SLValidationRules {
        public boolean requiresMap = false;
        public boolean requiresArray = false;
        public Set<String> requiredFields = new HashSet<>();
        public Map<String, Class<?>> fieldTypes = new HashMap<>();
        
        public SLValidationRules requireMap() {
            requiresMap = true;
            return this;
        }
        
        public SLValidationRules requireArray() {
            requiresArray = true;
            return this;
        }
        
        public SLValidationRules requireField(String field) {
            requiredFields.add(field);
            return this;
        }
        
        public SLValidationRules requireField(String field, Class<?> type) {
            requiredFields.add(field);
            fieldTypes.put(field, type);
            return this;
        }
    }
    
    /**
     * Create texture stream data structure.
     * 
     * @param textureId the texture UUID
     * @param textureData the texture binary data
     * @param format the texture format
     * @return LLSD map containing texture stream data
     * @throws LLSDException if texture processing fails
     */
    public static Map<String, Object> createTextureStream(UUID textureId, 
                                                          byte[] textureData, 
                                                          SLTextureProcessor.TextureFormat format) 
            throws LLSDException {
        SLTextureProcessor.TextureInfo info = SLTextureProcessor.processTexture(textureId, textureData, format);
        return SLTextureProcessor.createTextureStreamData(textureId, info, textureData);
    }
    
    /**
     * Create sound stream data structure.
     * 
     * @param soundId the sound UUID
     * @param soundData the sound binary data
     * @param format the audio format
     * @return LLSD map containing sound stream data
     * @throws LLSDException if sound processing fails
     */
    public static Map<String, Object> createSoundStream(UUID soundId, 
                                                        byte[] soundData, 
                                                        SLSoundProcessor.AudioFormat format) 
            throws LLSDException {
        SLSoundProcessor.AudioInfo info = SLSoundProcessor.processSound(soundId, soundData, format);
        return SLSoundProcessor.createSoundStreamData(soundId, info, soundData);
    }
    
    /**
     * Create general data stream structure.
     * 
     * @param assetId the asset UUID
     * @param assetType the asset type
     * @param assetData the asset binary data
     * @param enableCompression whether to enable compression
     * @return LLSD map containing data stream information
     * @throws LLSDException if data processing fails
     */
    public static Map<String, Object> createDataStream(UUID assetId, 
                                                       int assetType, 
                                                       byte[] assetData, 
                                                       boolean enableCompression) 
            throws LLSDException {
        try {
            SLDataStreamProcessor.DataStreamInfo info = SLDataStreamProcessor.processDataStream(
                assetId, assetType, assetData, enableCompression);
            
            List<SLDataStreamProcessor.StreamChunk> chunks = SLDataStreamProcessor.createStreamChunks(
                assetData, info.getCompression());
            
            return SLDataStreamProcessor.createDataStreamLLSD(info, chunks);
        } catch (Exception e) {
            throw new LLSDException("Failed to create data stream: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process incoming asset stream from Second Life.
     * 
     * @param streamData the LLSD stream data
     * @return processed asset information
     * @throws LLSDException if stream processing fails
     */
    public static Map<String, Object> processIncomingStream(Map<String, Object> streamData) 
            throws LLSDException {
        if (!streamData.containsKey("AssetType")) {
            throw new LLSDException("Missing AssetType in stream data");
        }
        
        int assetType = (Integer) streamData.get("AssetType");
        UUID assetId = UUID.fromString(streamData.get("AssetID").toString());
        byte[] data = (byte[]) streamData.get("Data");
        
        Map<String, Object> result = new HashMap<>();
        result.put("AssetID", assetId);
        result.put("AssetType", assetType);
        result.put("ProcessedAt", System.currentTimeMillis() / 1000.0);
        
        if (SLAssetType.isTextureType(assetType)) {
            SLTextureProcessor.TextureFormat format = SLTextureProcessor.detectTextureFormat(data);
            SLTextureProcessor.TextureInfo info = SLTextureProcessor.processTexture(assetId, data, format);
            result.put("TextureInfo", createTextureInfoMap(info));
            result.put("Valid", info.isValid());
        } else if (SLAssetType.isSoundType(assetType)) {
            SLSoundProcessor.AudioFormat format = SLSoundProcessor.detectAudioFormat(data);
            SLSoundProcessor.AudioInfo info = SLSoundProcessor.processSound(assetId, data, format);
            result.put("AudioInfo", createAudioInfoMap(info));
            result.put("Valid", info.isValid());
        } else {
            SLDataStreamProcessor.DataStreamInfo info = SLDataStreamProcessor.processDataStream(
                assetId, assetType, data, true);
            result.put("StreamInfo", createStreamInfoMap(info));
            result.put("Valid", info.isValid());
        }
        
        return result;
    }
    
    /**
     * Create texture info map for LLSD.
     */
    private static Map<String, Object> createTextureInfoMap(SLTextureProcessor.TextureInfo info) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("Format", info.getFormat().name());
        infoMap.put("Width", info.getWidth());
        infoMap.put("Height", info.getHeight());
        infoMap.put("Levels", info.getLevels());
        infoMap.put("Size", info.getSize());
        infoMap.put("HasAlpha", info.hasAlpha());
        return infoMap;
    }
    
    /**
     * Create audio info map for LLSD.
     */
    private static Map<String, Object> createAudioInfoMap(SLSoundProcessor.AudioInfo info) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("Format", info.getFormat().name());
        infoMap.put("SampleRate", info.getSampleRate());
        infoMap.put("Channels", info.getChannels());
        infoMap.put("BitsPerSample", info.getBitsPerSample());
        infoMap.put("Duration", info.getDuration());
        infoMap.put("Size", info.getSize());
        infoMap.put("Compressed", info.isCompressed());
        return infoMap;
    }
    
    /**
     * Create stream info map for LLSD.
     */
    private static Map<String, Object> createStreamInfoMap(SLDataStreamProcessor.DataStreamInfo info) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("TotalSize", info.getTotalSize());
        infoMap.put("CompressedSize", info.getCompressedSize());
        infoMap.put("Compression", info.getCompression().getName());
        infoMap.put("ChunkCount", info.getChunkCount());
        infoMap.put("CompressionRatio", info.getCompressionRatio());
        infoMap.put("Metadata", info.getMetadata());
        return infoMap;
    }

    /**
     * Result of LLSD validation.
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }
        
        public List<String> getWarnings() {
            return Collections.unmodifiableList(warnings);
        }
    }
}