package com.lithumc;

import com.lithumc.config.AbfConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AnythingButFish – main mod initializer.
 * Loads config and logs startup info.
 * No loot table modification – loot replacement is handled entirely
 * by FishingHookMixin at the entity level.
 */
public class AnythingButFish implements ModInitializer {

    public static final String MOD_ID = "anythingbutfish";
    public static final Logger LOGGER  = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        AbfConfig cfg = AbfConfig.get();
        LOGGER.info("[AnythingButFish] Mod initialized - good luck fishing!");
        LOGGER.info("[AnythingButFish] Config: item={}% entity={}% xp={}% gotaway={}%",
                cfg.chanceItem, cfg.chanceEntity, cfg.chanceXp,
                100 - cfg.thresholdXp());
    }
}