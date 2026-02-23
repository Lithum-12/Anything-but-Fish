package com.lithumc;

import com.lithumc.config.AbfConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AnythingButFish – main mod initializer.
 *
 * MC 1.17.1 version: loot table modifier removed (not compatible)
 */
public class AnythingButFish implements ModInitializer {

    public static final String MOD_ID = "anythingbutfish";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        AbfConfig cfg = AbfConfig.get();
        LOGGER.info("[AnythingButFish] Mod initialized - good luck fishing!");
        LOGGER.info("[AnythingButFish] Config: item={}% entity={}% xp={}% gotaway={}%",
                cfg.chanceItem, cfg.chanceEntity, cfg.chanceXp,
                100 - cfg.thresholdXp());

        LOGGER.info("[AnythingButFish] MC 1.17.1 - Loot table modifier not available in this version.");
    }
}
