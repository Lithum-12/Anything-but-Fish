package com.lithumc.config;

import net.minecraft.client.gui.screens.Screen;

/**
 * Bridge to open config screen.
 * For MC 1.19.x, uses Cloth Config API instead of YACL.
 */
public class AbfYaclBridge {
    
    /**
     * Check if the config library is present.
     * For Cloth Config, always returns true since it's bundled.
     * @return true
     */
    public static boolean isYaclPresent() {
        return true; // Cloth Config is bundled with the mod
    }
    
    /**
     * Create the config screen using Cloth Config.
     * @param parent The parent screen
     * @return The config screen
     */
    public static Screen createConfigScreen(Screen parent) {
        return AbfClothConfig.createScreen(parent);
    }
}