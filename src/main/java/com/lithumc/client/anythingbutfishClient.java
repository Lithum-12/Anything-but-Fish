package com.lithumc.client;

import com.lithumc.config.AbfYaclBridge;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import com.lithumc.AnythingButFish;

/**
 * Client-side initializer for AnythingButFish.
 *
 * Registers a ModMenu config screen factory if both ModMenu and YACL are present.
 * If only ModMenu is present (no YACL), the config button is hidden.
 * If neither is present, nothing happens.
 */
public class anythingbutfishClient implements ClientModInitializer, ModMenuApi {

    @Override
    public void onInitializeClient() {
        AnythingButFish.LOGGER.info("[AnythingButFish] Client initialized.");
        // Log YACL status on client startup
        AbfYaclBridge.isYaclPresent();
    }

    /**
     * ModMenu integration: provide a config screen factory.
     * Returns null factory if YACL is not present (ModMenu will hide the button).
     */
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (AbfYaclBridge.isYaclPresent()) {
            return parent -> AbfYaclBridge.createConfigScreen(parent);
        }
        // No YACL – return null so ModMenu shows no config button
        return null;
    }
}
