package com.lithumc.client;

import com.lithumc.AnythingButFish;
import com.lithumc.config.AbfYaclBridge;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Client-side initializer for AnythingButFish.
 *
 * ModMenu integration is handled via a separate entrypoint class (AbfModMenuIntegration)
 * registered in fabric.mod.json only when ModMenu is present. This avoids a
 * NoClassDefFoundError crash when ModMenu is not installed.
 */
public class anythingbutfishClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AnythingButFish.LOGGER.info("[AnythingButFish] Client initialized.");
        // Log YACL status on client startup
        AbfYaclBridge.isYaclPresent();

        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            AnythingButFish.LOGGER.info("[AnythingButFish] ModMenu detected.");
        }
    }
}
