package com.lithumc.config;

import net.minecraft.client.gui.screens.Screen;

/**
 * Bridge to open config screen.
 * For MC 1.19.x, uses Cloth Config API instead of YACL.
 */
public class AbfYaclBridge {
    
    /**
     * Create the config screen using Cloth Config.
     * @param parent The parent screen
     * @return The config screen
     */
    public static Screen createConfigScreen(Screen parent) {
        return AbfClothConfig.createScreen(parent);
    }
}