package com.lithumc.config;

import com.lithumc.AnythingButFish;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Method;

/**
 * Bridge that safely checks for Cloth Config at runtime and delegates to
 * {@link AbfClothConfig} only when Cloth Config is actually loaded.
 *
 * Using reflection means the JVM will never attempt to load AbfClothConfig
 * (and its Cloth Config imports) unless Cloth Config is present, so the mod
 * works fine without Cloth Config installed.
 *
 * This class is client-only: it references net.minecraft.client.gui.screens.Screen.
 */
@Environment(EnvType.CLIENT)
public class AbfYaclBridge {

    private static final String CLOTH_MOD_ID = "cloth-config";
    private static Boolean clothPresent = null;

    /** @return true if Cloth Config is loaded in the current environment */
    public static boolean isYaclPresent() {
        if (clothPresent == null) {
            clothPresent = FabricLoader.getInstance().isModLoaded(CLOTH_MOD_ID);
            AnythingButFish.LOGGER.info("[AnythingButFish] Cloth Config present: {}", clothPresent);
        }
        return clothPresent;
    }

    /**
     * Create the Cloth Config screen via reflection.
     *
     * @param parent the screen to return to after closing config
     * @return the config Screen, or {@code null} if Cloth Config is not present
     */
    public static Screen createConfigScreen(Screen parent) {
        if (!isYaclPresent()) return null;
        try {
            Class<?> cls = Class.forName("com.lithumc.config.AbfClothConfig");
            Method m = cls.getMethod("createScreen", Screen.class);
            return (Screen) m.invoke(null, parent);
        } catch (Exception e) {
            AnythingButFish.LOGGER.error("[AnythingButFish] Failed to open Cloth Config screen", e);
            return null;
        }
    }
}