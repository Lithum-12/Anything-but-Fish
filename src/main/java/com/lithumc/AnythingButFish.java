package com.lithumc;

import com.lithumc.config.AbfConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AnythingButFish – main mod initializer.
 *
 * Registers:
 *  1. Config loading
 *  2. Loot table modifier: adds a fresh fishing rod pool to chest/fishing loot tables
 *     when replaceLootChestRods is enabled. This supplements (not replaces) existing
 *     rod entries, ensuring players always get at least one fresh rod from these tables.
 *
 * MC 1.19.2 port: uses loot-api-v1 (LootTableLoadingCallback).
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

        registerLootModifier();
    }

    /**
     * Registers a loot table modifier that adds a fresh fishing rod to the
     * world spawn bonus chest (minecraft:chests/spawn_bonus_chest) when
     * replaceLootChestRods is enabled.
     *
     * The bonus chest is the optional starter chest generated at world spawn
     * when the player enables "Bonus Chest" during world creation.
     *
     * Uses Fabric API loot-api-v1 LootTableLoadingCallback for MC 1.19.2.
     */
    private void registerLootModifier() {
        LootTableLoadingCallback.EVENT.register((resourceManager, lootManager, id, supplier, setter) -> {
            if (!AbfConfig.get().replaceLootChestRods) return;

            // Only patch the world spawn bonus chest
            if (!"minecraft:chests/spawn_bonus_chest".equals(id.toString())) return;

            // Add a pool that gives a fresh (undamaged, unenchanted) fishing rod
            FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.builder()
                    .rolls(ConstantValue.exactly(1))
                    .with(LootItem.lootTableItem(Items.FISHING_ROD).build());

            supplier.withPool(poolBuilder.build());

            if (AbfConfig.get().debugMode) {
                LOGGER.info("[AnythingButFish] Added fresh rod to spawn_bonus_chest loot table.");
            }
        });

        LOGGER.info("[AnythingButFish] Loot table modifier registered.");
    }
}