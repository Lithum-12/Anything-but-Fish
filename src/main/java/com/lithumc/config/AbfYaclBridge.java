package com.lithumc.config;

import com.lithumc.AnythingButFish;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Method;

/**
 * Bridge that safely checks for YACL at runtime and delegates to
 * {@link AbfYaclConfig} only when YACL is actually loaded.
 *
 * Using reflection means the JVM will never attempt to load AbfYaclConfig
 * (and its YACL imports) unless YACL is present, so the mod works fine
 * without YACL installed.
 */
public class AbfYaclBridge {

    private static final String YACL_MOD_ID = "yet_another_config_lib_v3";
    private static Boolean yaclPresent = null;

    /** @return true if YACL is loaded in the current environment */
    public static boolean isYaclPresent() {
        if (yaclPresent == null) {
            yaclPresent = FabricLoader.getInstance().isModLoaded(YACL_MOD_ID);
            AnythingButFish.LOGGER.info("[AnythingButFish] YACL present: {}", yaclPresent);
        }
        return yaclPresent;
    }

    /**
     * Create the YACL config screen via reflection.
     *
     * @param parent the screen to return to after closing config
     * @return the config Screen, or {@code null} if YACL is not present
     */
    public static Screen createConfigScreen(Screen parent) {
        if (!isYaclPresent()) return null;
        try {
            Class<?> cls = Class.forName("com.lithumc.config.AbfYaclConfig");
            Method m = cls.getMethod("createScreen", Screen.class);
            return (Screen) m.invoke(null, parent);
        } catch (Exception e) {
            AnythingButFish.LOGGER.error("[AnythingButFish] Failed to open YACL config screen", e);
            return null;
        }
    }
}
