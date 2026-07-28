package com.lithumc.client;

import com.lithumc.config.AbfYaclBridge;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * ModMenu integration for AnythingButFish.
 *
 * This class is registered as a "modmenu" entrypoint in fabric.mod.json.
 * It is only loaded when ModMenu is present, so it is safe to directly
 * implement ModMenuApi here without risking a NoClassDefFoundError.
 */
@Environment(EnvType.CLIENT)
public class AbfModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // ModMenu 2.x requires a non-null factory (ImmutableMap rejects null values)
        // The factory itself returns null when Cloth Config is absent → hides config button
        return parent -> AbfYaclBridge.isYaclPresent()
                ? AbfYaclBridge.createConfigScreen(parent)
                : null;
    }
}
