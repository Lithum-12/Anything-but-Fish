package com.lithumc.client;

import com.lithumc.config.AbfYaclBridge;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AbfModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AbfYaclBridge.isYaclPresent()
                ? AbfYaclBridge.createConfigScreen(parent)
                : null;
    }
}