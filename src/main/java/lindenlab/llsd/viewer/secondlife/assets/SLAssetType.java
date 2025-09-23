/*
 * Second Life Asset Types - Java implementation of SL asset type constants
 *
 * Based on Second Life viewer implementation
 * Copyright (C) 2010, Linden Research, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer.secondlife.assets;

/**
 * Second Life asset type constants and utilities.
 * 
 * <p>These constants define the standard asset types used throughout the
 * Second Life ecosystem, matching the official viewer implementation.</p>
 * 
 * @since 1.0
 */
public final class SLAssetType {
    
    // Standard Second Life asset types
    public static final int TEXTURE = 0;
    public static final int SOUND = 1;
    public static final int CALLINGCARD = 2;
    public static final int LANDMARK = 3;
    public static final int SCRIPT = 4;
    public static final int CLOTHING = 5;
    public static final int OBJECT = 6;
    public static final int NOTECARD = 7;
    public static final int CATEGORY = 8;
    public static final int ROOT_CATEGORY = 9;
    public static final int LSL_TEXT = 10;
    public static final int LSL_BYTECODE = 11;
    public static final int TEXTURE_TGA = 12;
    public static final int BODYPART = 13;
    public static final int TRASH = 14;
    public static final int SNAPSHOT_CATEGORY = 15;
    public static final int LOST_AND_FOUND = 16;
    public static final int SOUND_WAV = 17;
    public static final int IMAGE_TGA = 18;
    public static final int IMAGE_JPEG = 19;
    public static final int ANIMATION = 20;
    public static final int GESTURE = 21;
    public static final int SIMSTATE = 22;
    public static final int FAVORITE = 23;
    public static final int LINK = 24;
    public static final int LINK_FOLDER = 25;
    public static final int MARKETPLACE_FOLDER = 26;
    public static final int MESH = 49;
    
    // Extended asset types for streaming
    public static final int AUDIO_STREAM = 100;
    public static final int VIDEO_STREAM = 101;
    public static final int TEXTURE_STREAM = 102;
    public static final int MODEL_STREAM = 103;
    
    private SLAssetType() {
        // Utility class - no instances
    }
    
    /**
     * Check if an asset type is a texture type.
     * 
     * @param assetType the asset type to check
     * @return true if the asset type represents a texture
     */
    public static boolean isTextureType(int assetType) {
        return assetType == TEXTURE || 
               assetType == TEXTURE_TGA || 
               assetType == IMAGE_TGA || 
               assetType == IMAGE_JPEG ||
               assetType == TEXTURE_STREAM;
    }
    
    /**
     * Check if an asset type is a sound type.
     * 
     * @param assetType the asset type to check
     * @return true if the asset type represents audio
     */
    public static boolean isSoundType(int assetType) {
        return assetType == SOUND || 
               assetType == SOUND_WAV ||
               assetType == AUDIO_STREAM;
    }
    
    /**
     * Check if an asset type is a streaming type.
     * 
     * @param assetType the asset type to check
     * @return true if the asset type represents streaming data
     */
    public static boolean isStreamType(int assetType) {
        return assetType >= AUDIO_STREAM && assetType <= MODEL_STREAM;
    }
    
    /**
     * Get a human-readable name for an asset type.
     * 
     * @param assetType the asset type
     * @return the name of the asset type
     */
    public static String getTypeName(int assetType) {
        switch (assetType) {
            case TEXTURE: return "Texture";
            case SOUND: return "Sound";
            case CALLINGCARD: return "Calling Card";
            case LANDMARK: return "Landmark";
            case SCRIPT: return "Script";
            case CLOTHING: return "Clothing";
            case OBJECT: return "Object";
            case NOTECARD: return "Notecard";
            case CATEGORY: return "Category";
            case ROOT_CATEGORY: return "Root Category";
            case LSL_TEXT: return "LSL Text";
            case LSL_BYTECODE: return "LSL Bytecode";
            case TEXTURE_TGA: return "Texture TGA";
            case BODYPART: return "Body Part";
            case TRASH: return "Trash";
            case SNAPSHOT_CATEGORY: return "Snapshot Category";
            case LOST_AND_FOUND: return "Lost and Found";
            case SOUND_WAV: return "Sound WAV";
            case IMAGE_TGA: return "Image TGA";
            case IMAGE_JPEG: return "Image JPEG";
            case ANIMATION: return "Animation";
            case GESTURE: return "Gesture";
            case SIMSTATE: return "Sim State";
            case FAVORITE: return "Favorite";
            case LINK: return "Link";
            case LINK_FOLDER: return "Link Folder";
            case MARKETPLACE_FOLDER: return "Marketplace Folder";
            case MESH: return "Mesh";
            case AUDIO_STREAM: return "Audio Stream";
            case VIDEO_STREAM: return "Video Stream";
            case TEXTURE_STREAM: return "Texture Stream";
            case MODEL_STREAM: return "Model Stream";
            default: return "Unknown (" + assetType + ")";
        }
    }
    
    /**
     * Get the MIME type for an asset type.
     * 
     * @param assetType the asset type
     * @return the MIME type for the asset
     */
    public static String getMimeType(int assetType) {
        switch (assetType) {
            case TEXTURE:
            case TEXTURE_STREAM:
                return "image/x-j2c";
            case TEXTURE_TGA:
            case IMAGE_TGA:
                return "image/tga";
            case IMAGE_JPEG:
                return "image/jpeg";
            case SOUND:
            case SOUND_WAV:
            case AUDIO_STREAM:
                return "audio/wav";
            case NOTECARD:
                return "text/plain";
            case LSL_TEXT:
                return "text/lsl";
            case SCRIPT:
            case LSL_BYTECODE:
                return "application/octet-stream";
            case ANIMATION:
                return "application/x-anim";
            case GESTURE:
                return "application/x-gesture";
            case MESH:
            case MODEL_STREAM:
                return "application/vnd.ll.mesh";
            default:
                return "application/octet-stream";
        }
    }
}