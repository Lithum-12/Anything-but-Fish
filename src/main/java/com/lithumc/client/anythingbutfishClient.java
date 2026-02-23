package com.lithumc.client;

import com.lithumc.AnythingButFish;
import com.lithumc.config.AbfYaclBridge;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Client-side initializer for AnythingButFish.
 * 
 * Configuration is handled via Cloth Config API which is bundled with the mod.
 * No ModMenu dependency required - works standalone.
 */
public class anythingbutfishClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AnythingButFish.LOGGER.info("[AnythingButFish] Client initialized.");
        // Log Cloth Config status on client startup
        AbfYaclBridge.isYaclPresent();

        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            AnythingButFish.LOGGER.info("[AnythingButFish] ModMenu detected.");
        }
    }
}
