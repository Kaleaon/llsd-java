/*
 * Viewer Demo - Demonstrates the complete Second Life viewer functionality
 */

package lindenlab.llsd.viewer.secondlife.demo;

import lindenlab.llsd.viewer.secondlife.app.SecondLifeViewer;
import lindenlab.llsd.viewer.secondlife.cache.CacheManager;
import lindenlab.llsd.viewer.secondlife.cache.CacheStatistics;
import lindenlab.llsd.viewer.secondlife.config.ViewerConfiguration;
import lindenlab.llsd.viewer.secondlife.rendering.AdvancedRenderingSystem;

import java.util.Map;
import java.util.Scanner;

/**
 * Interactive demonstration of the complete Second Life viewer implementation.
 * 
 * Shows all the key features implemented:
 * - Cache management with configurable storage up to 200GB
 * - Advanced rendering with fine-grained controls 
 * - Battery conservation mode with blank background option
 * - Configuration management and persistence
 */
public class ViewerDemo {
    
    private SecondLifeViewer viewer;
    private Scanner scanner;
    
    public ViewerDemo() {
        this.scanner = new Scanner(System.in);
    }
    
    public void run() {
        System.out.println("==========================================");
        System.out.println(" Second Life Viewer - Java Implementation");
        System.out.println("==========================================");
        System.out.println();
        
        try {
            // Initialize viewer with demo configuration
            ViewerConfiguration config = createDemoConfiguration();
            
            System.out.println("Initializing viewer...");
            viewer = new SecondLifeViewer();
            
            boolean initialized = viewer.initialize(config).join();
            if (!initialized) {
                System.err.println("Failed to initialize viewer!");
                return;
            }
            
            System.out.println("✓ Viewer initialized successfully");
            System.out.println();
            
            // Start viewer
            viewer.start();
            System.out.println("✓ Viewer started");
            
            // Wait a moment for systems to stabilize
            Thread.sleep(500);
            
            // Demo the features
            demonstrateFeatures();
            
        } catch (Exception e) {
            System.err.println("Error during demo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    private ViewerConfiguration createDemoConfiguration() {
        ViewerConfiguration config = new ViewerConfiguration();
        
        // Configure cache for demo
        config.setCacheStorageLocation(CacheManager.StorageLocation.SYSTEM_TEMP);
        config.setMaxCacheSize(1L * 1024 * 1024 * 1024); // 1GB for demo
        
        // Configure rendering for demo
        config.setDefaultQualityPreset(ViewerConfiguration.QualityPreset.BALANCED);
        config.setBatteryOptimizationEnabled(false);
        
        System.out.println("Demo Configuration:");
        System.out.println("  Cache: " + config.getCacheStorageLocation().getDisplayName());
        System.out.println("  Cache Size: " + CacheManager.formatBytes(config.getMaxCacheSize()));
        System.out.println("  Quality: " + config.getDefaultQualityPreset());
        System.out.println();
        
        return config;
    }
    
    private void demonstrateFeatures() throws InterruptedException {
        System.out.println("=== FEATURE DEMONSTRATION ===");
        System.out.println();
        
        // 1. Cache Management Demo
        demonstrateCacheManagement();
        
        // 2. Rendering System Demo
        demonstrateRenderingSystem();
        
        // 3. Battery Conservation Demo
        demonstrateBatteryConservation();
        
        // 4. Statistics Demo
        demonstrateStatistics();
        
        // 5. Interactive Menu
        showInteractiveMenu();
    }
    
    private void demonstrateCacheManagement() {
        System.out.println("1. CACHE MANAGEMENT SYSTEM");
        System.out.println("──────────────────────────");
        
        CacheManager cacheManager = viewer.getCacheManager();
        
        // Show initial cache statistics
        CacheStatistics initialStats = cacheManager.getStatistics();
        System.out.println("Initial Cache State:");
        System.out.println("  Storage: " + initialStats.getStorageLocation().getDisplayName());
        System.out.println("  Max Size: " + CacheManager.formatBytes(initialStats.getMaxSize()));
        System.out.println("  Current Size: " + CacheManager.formatBytes(initialStats.getTotalSize()));
        System.out.println("  Available: " + CacheManager.formatBytes(initialStats.getAvailableSpace()));
        System.out.println();
        
        // Demonstrate cache operations
        System.out.println("Adding test data to cache...");
        
        // Add texture data
        byte[] textureData = createSampleTextureData();
        cacheManager.store(CacheManager.CacheType.TEXTURE, "demo_texture_1", textureData).join();
        System.out.println("✓ Stored texture data (" + CacheManager.formatBytes(textureData.length) + ")");
        
        // Add sound data
        byte[] soundData = createSampleSoundData();
        cacheManager.store(CacheManager.CacheType.SOUND, "demo_sound_1", soundData).join();
        System.out.println("✓ Stored sound data (" + CacheManager.formatBytes(soundData.length) + ")");
        
        // Add mesh data
        byte[] meshData = createSampleMeshData();
        cacheManager.store(CacheManager.CacheType.MESH, "demo_mesh_1", meshData).join();
        System.out.println("✓ Stored mesh data (" + CacheManager.formatBytes(meshData.length) + ")");
        
        // Show updated statistics
        CacheStatistics updatedStats = cacheManager.getStatistics();
        System.out.println();
        System.out.println("Updated Cache State:");
        System.out.println("  Current Size: " + CacheManager.formatBytes(updatedStats.getTotalSize()));
        System.out.println("  Hit Ratio: " + String.format("%.1f%%", updatedStats.getHitRatio() * 100));
        System.out.println("  Total Writes: " + updatedStats.getTotalWrites());
        System.out.println();
        
        // Test cache retrieval
        System.out.println("Testing cache retrieval...");
        byte[] retrievedTexture = cacheManager.retrieve(CacheManager.CacheType.TEXTURE, "demo_texture_1").join();
        System.out.println("✓ Retrieved texture: " + (retrievedTexture != null ? "SUCCESS" : "FAILED"));
        
        byte[] nonExistent = cacheManager.retrieve(CacheManager.CacheType.TEXTURE, "nonexistent").join();
        System.out.println("✓ Non-existent item: " + (nonExistent == null ? "CORRECTLY NOT FOUND" : "UNEXPECTED RESULT"));
        
        System.out.println();
    }
    
    private void demonstrateRenderingSystem() {
        System.out.println("2. ADVANCED RENDERING SYSTEM");
        System.out.println("────────────────────────────");
        
        AdvancedRenderingSystem renderingSystem = viewer.getRenderingSystem();
        
        System.out.println("Current rendering state:");
        System.out.println("  Rendering Enabled: " + renderingSystem.isRenderingEnabled());
        System.out.println("  Battery Mode: " + renderingSystem.isBatteryConservationMode());
        System.out.println("  Overall Quality: " + String.format("%.1f", renderingSystem.getQualitySettings().getOverallQuality()));
        System.out.println();
        
        // Demonstrate quality presets
        System.out.println("Testing quality presets...");
        
        renderingSystem.applyUltraLowPreset();
        System.out.println("✓ Ultra Low Quality: " + renderingSystem.getQualitySettings().getOverallQuality());
        
        renderingSystem.applyLowPreset();
        System.out.println("✓ Low Quality: " + renderingSystem.getQualitySettings().getOverallQuality());
        
        renderingSystem.applyBalancedPreset();
        System.out.println("✓ Balanced Quality: " + renderingSystem.getQualitySettings().getOverallQuality());
        
        renderingSystem.applyHighPreset();
        System.out.println("✓ High Quality: " + renderingSystem.getQualitySettings().getOverallQuality());
        
        renderingSystem.applyUltraPreset();
        System.out.println("✓ Ultra Quality: " + renderingSystem.getQualitySettings().getOverallQuality());
        
        System.out.println();
        
        // Show fine-grained settings
        System.out.println("Fine-grained rendering controls available:");
        System.out.println("  ✓ Texture Settings: " + renderingSystem.getTextureSettings().getTextureQuality().name());
        System.out.println("  ✓ Shadow Settings: " + (renderingSystem.getShadowSettings().isShadowsEnabled() ? "ENABLED" : "DISABLED"));
        System.out.println("  ✓ Particle Settings: Max " + renderingSystem.getParticleSettings().getMaxParticles() + " particles");
        System.out.println("  ✓ Avatar Settings: Max " + renderingSystem.getAvatarSettings().getMaxVisibleAvatars() + " avatars");
        System.out.println();
    }
    
    private void demonstrateBatteryConservation() {
        System.out.println("3. BATTERY CONSERVATION MODE");
        System.out.println("────────────────────────────");
        
        AdvancedRenderingSystem renderingSystem = viewer.getRenderingSystem();
        
        System.out.println("Current state:");
        System.out.println("  Rendering: " + (renderingSystem.isRenderingEnabled() ? "ON" : "OFF"));
        System.out.println("  Battery Mode: " + (renderingSystem.isBatteryConservationMode() ? "ON" : "OFF"));
        System.out.println();
        
        // Enable battery conservation mode
        System.out.println("Enabling battery conservation mode...");
        viewer.setBatteryConservationMode(true);
        
        System.out.println("✓ Battery mode enabled");
        System.out.println("  Rendering: " + (renderingSystem.isRenderingEnabled() ? "ON" : "OFF (blank background)"));
        System.out.println("  Battery Mode: " + (renderingSystem.isBatteryConservationMode() ? "ON" : "OFF"));
        System.out.println("  Power Savings: Maximum (15 FPS, minimal effects)");
        System.out.println();
        
        // Test rendering toggle while in battery mode
        System.out.println("Testing rendering toggle...");
        viewer.toggleRendering();
        System.out.println("✓ Toggled rendering: " + (renderingSystem.isRenderingEnabled() ? "ON" : "OFF"));
        
        viewer.toggleRendering();
        System.out.println("✓ Toggled rendering: " + (renderingSystem.isRenderingEnabled() ? "ON" : "OFF"));
        System.out.println();
        
        // Disable battery conservation mode
        System.out.println("Disabling battery conservation mode...");
        viewer.setBatteryConservationMode(false);
        
        System.out.println("✓ Battery mode disabled");
        System.out.println("  Rendering: " + (renderingSystem.isRenderingEnabled() ? "ON (normal display)" : "OFF"));
        System.out.println("  Battery Mode: " + (renderingSystem.isBatteryConservationMode() ? "ON" : "OFF"));
        System.out.println();
    }
    
    private void demonstrateStatistics() throws InterruptedException {
        System.out.println("4. PERFORMANCE STATISTICS");
        System.out.println("─────────────────────────");
        
        // Let the viewer run for a moment to collect statistics
        System.out.println("Collecting performance data...");
        Thread.sleep(2000);
        
        Map<String, Object> stats = viewer.getStatistics();
        
        System.out.println("Application Statistics:");
        System.out.println("  Uptime: " + formatUptime((Long) stats.get("uptime")));
        System.out.println("  Frame Count: " + stats.get("frameCount"));
        System.out.println("  Average FPS: " + String.format("%.1f", (Double) stats.get("averageFPS")));
        System.out.println("  Memory Used: " + CacheManager.formatBytes((Long) stats.get("memoryUsed")));
        System.out.println("  Max Memory: " + CacheManager.formatBytes((Long) stats.get("maxMemory")));
        System.out.println();
        
        // Cache statistics
        CacheStatistics cacheStats = viewer.getCacheManager().getStatistics();
        System.out.println("Cache Statistics:");
        System.out.println("  Total Size: " + CacheManager.formatBytes(cacheStats.getTotalSize()));
        System.out.println("  Utilization: " + String.format("%.1f%%", cacheStats.getUsagePercent()));
        System.out.println("  Hit Ratio: " + String.format("%.1f%%", cacheStats.getHitRatio() * 100));
        System.out.println("  Total Requests: " + cacheStats.getTotalRequests());
        System.out.println();
        
        // Show cache breakdown by type
        System.out.println("Cache Breakdown by Type:");
        for (CacheManager.CacheType type : CacheManager.CacheType.values()) {
            long size = cacheStats.getTypeSize(type);
            if (size > 0) {
                System.out.println("  " + type.getDescription() + ": " + CacheManager.formatBytes(size));
            }
        }
        System.out.println();
    }
    
    private void showInteractiveMenu() {
        System.out.println("5. INTERACTIVE MENU");
        System.out.println("───────────────────");
        System.out.println();
        
        while (true) {
            System.out.println("Available actions:");
            System.out.println("  1. Toggle rendering on/off");
            System.out.println("  2. Toggle battery conservation mode");
            System.out.println("  3. Change quality preset");
            System.out.println("  4. View cache statistics");
            System.out.println("  5. Add test data to cache");
            System.out.println("  6. Clear cache");
            System.out.println("  7. View application configuration");
            System.out.println("  0. Exit demo");
            System.out.println();
            System.out.print("Choose an action (0-7): ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                System.out.println();
                
                switch (choice) {
                    case 0:
                        return;
                    case 1:
                        handleToggleRendering();
                        break;
                    case 2:
                        handleToggleBatteryMode();
                        break;
                    case 3:
                        handleChangeQuality();
                        break;
                    case 4:
                        handleViewCacheStats();
                        break;
                    case 5:
                        handleAddTestData();
                        break;
                    case 6:
                        handleClearCache();
                        break;
                    case 7:
                        handleViewConfiguration();
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
            
            System.out.println();
        }
    }
    
    private void handleToggleRendering() {
        AdvancedRenderingSystem renderingSystem = viewer.getRenderingSystem();
        boolean wasEnabled = renderingSystem.isRenderingEnabled();
        
        viewer.toggleRendering();
        
        System.out.println("Rendering " + (wasEnabled ? "disabled" : "enabled"));
        if (!renderingSystem.isRenderingEnabled()) {
            System.out.println("  → Display shows blank background for battery conservation");
        } else {
            System.out.println("  → Display shows normal 3D world");
        }
    }
    
    private void handleToggleBatteryMode() {
        AdvancedRenderingSystem renderingSystem = viewer.getRenderingSystem();
        boolean wasEnabled = renderingSystem.isBatteryConservationMode();
        
        viewer.setBatteryConservationMode(!wasEnabled);
        
        System.out.println("Battery conservation mode " + (wasEnabled ? "disabled" : "enabled"));
        if (renderingSystem.isBatteryConservationMode()) {
            System.out.println("  → Power-saving settings applied (15 FPS, minimal effects)");
            System.out.println("  → Rendering disabled for maximum battery life");
        } else {
            System.out.println("  → Normal rendering settings restored");
        }
    }
    
    private void handleChangeQuality() {
        System.out.println("Quality presets:");
        System.out.println("  1. Ultra Low");
        System.out.println("  2. Low");
        System.out.println("  3. Balanced"); 
        System.out.println("  4. High");
        System.out.println("  5. Ultra");
        System.out.print("Choose quality (1-5): ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            AdvancedRenderingSystem renderingSystem = viewer.getRenderingSystem();
            
            switch (choice) {
                case 1:
                    renderingSystem.applyUltraLowPreset();
                    System.out.println("Applied Ultra Low quality preset");
                    break;
                case 2:
                    renderingSystem.applyLowPreset();
                    System.out.println("Applied Low quality preset");
                    break;
                case 3:
                    renderingSystem.applyBalancedPreset();
                    System.out.println("Applied Balanced quality preset");
                    break;
                case 4:
                    renderingSystem.applyHighPreset();
                    System.out.println("Applied High quality preset");
                    break;
                case 5:
                    renderingSystem.applyUltraPreset();
                    System.out.println("Applied Ultra quality preset");
                    break;
                default:
                    System.out.println("Invalid choice");
                    return;
            }
            
            System.out.println("  Overall Quality: " + renderingSystem.getQualitySettings().getOverallQuality());
            System.out.println("  Texture Quality: " + renderingSystem.getTextureSettings().getTextureQuality().name());
            System.out.println("  Shadows: " + (renderingSystem.getShadowSettings().isShadowsEnabled() ? "Enabled" : "Disabled"));
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid input");
        }
    }
    
    private void handleViewCacheStats() {
        CacheStatistics stats = viewer.getCacheManager().getStatistics();
        System.out.println(stats.toString());
    }
    
    private void handleAddTestData() {
        CacheManager cacheManager = viewer.getCacheManager();
        
        System.out.println("Adding random test data...");
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        byte[] data = ("Test data created at " + timestamp).getBytes();
        
        cacheManager.store(CacheManager.CacheType.TEXTURE, "test_" + timestamp, data).join();
        System.out.println("✓ Added test texture data (" + CacheManager.formatBytes(data.length) + ")");
        
        CacheStatistics stats = cacheManager.getStatistics();
        System.out.println("  Total cache size: " + CacheManager.formatBytes(stats.getTotalSize()));
        System.out.println("  Total items: " + stats.getTotalWrites());
    }
    
    private void handleClearCache() {
        System.out.print("Are you sure you want to clear all cache? (y/N): ");
        String response = scanner.nextLine().trim().toLowerCase();
        
        if (response.equals("y") || response.equals("yes")) {
            viewer.getCacheManager().clearAllCache().join();
            System.out.println("✓ Cache cleared");
            
            CacheStatistics stats = viewer.getCacheManager().getStatistics();
            System.out.println("  Cache size: " + CacheManager.formatBytes(stats.getTotalSize()));
        } else {
            System.out.println("Cache clear cancelled");
        }
    }
    
    private void handleViewConfiguration() {
        ViewerConfiguration config = viewer.getConfiguration();
        
        System.out.println("Current Configuration:");
        System.out.println("  Cache Location: " + config.getCacheStorageLocation().getDisplayName());
        System.out.println("  Cache Size: " + CacheManager.formatBytes(config.getMaxCacheSize()));
        System.out.println("  Quality Preset: " + config.getDefaultQualityPreset());
        System.out.println("  Battery Optimization: " + config.isBatteryOptimizationEnabled());
        System.out.println("  Show Splash: " + config.isShowSplashScreen());
        System.out.println("  Default Grid: " + config.getDefaultGrid());
        System.out.println("  UI Theme: " + config.getUITheme());
    }
    
    // Helper methods for creating sample data
    
    private byte[] createSampleTextureData() {
        StringBuilder data = new StringBuilder();
        data.append("TEXTURE_DATA:");
        for (int i = 0; i < 1000; i++) {
            data.append("PIXEL").append(i).append(",");
        }
        return data.toString().getBytes();
    }
    
    private byte[] createSampleSoundData() {
        StringBuilder data = new StringBuilder();
        data.append("SOUND_DATA:");
        for (int i = 0; i < 500; i++) {
            data.append("SAMPLE").append(i).append(",");
        }
        return data.toString().getBytes();
    }
    
    private byte[] createSampleMeshData() {
        StringBuilder data = new StringBuilder();
        data.append("MESH_DATA:");
        for (int i = 0; i < 200; i++) {
            data.append("VERTEX").append(i).append(",");
        }
        return data.toString().getBytes();
    }
    
    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }
    
    private void cleanup() {
        if (viewer != null && viewer.isInitialized()) {
            System.out.println();
            System.out.println("Shutting down viewer...");
            viewer.shutdown().join();
            System.out.println("✓ Viewer shutdown complete");
        }
        
        scanner.close();
    }
    
    public static void main(String[] args) {
        ViewerDemo demo = new ViewerDemo();
        demo.run();
        
        System.out.println();
        System.out.println("Demo completed. Thank you for exploring the Second Life Viewer!");
    }
}