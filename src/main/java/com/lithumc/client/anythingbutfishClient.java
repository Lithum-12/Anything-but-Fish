package com.lithumc.client;

import com.lithumc.config.AbfYaclBridge;
import net.fabricmc.api.ClientModInitializer;
import com.lithumc.AnythingButFish;

/**
 * Client-side initializer for AnythingButFish.
 *
 * ModMenu integration has been split into a separate class
 * ({@link ModMenuIntegration}) so that the client entrypoint
 * does not require ModMenu at runtime.
 */
public class anythingbutfishClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AnythingButFish.LOGGER.info("[AnythingButFish] Client initialized.");
        // Log YACL status on client startup
        AbfYaclBridge.isYaclPresent();
    }
}
