package com.lithumc.client;

import com.lithumc.config.AbfYaclBridge;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * ModMenu integration for AnythingButFish.
 *
 * This class is registered as a {@code modmenu} entrypoint in
 * {@code fabric.mod.json}. Fabric only attempts to load it when
 * ModMenu is present, so it is safe to reference ModMenu API
 * classes directly here.
 */
public class ModMenuIntegration implements ModMenuApi {

    /**
     * Provide a config screen factory.
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
