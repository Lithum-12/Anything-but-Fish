package com.lithumc;

import com.lithumc.config.AbfConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
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
 * Note: Fabric loot-api-v2 MODIFY event can add new pools but cannot remove/replace
 * existing pool entries. The "replace" behavior is approximated by adding a fresh rod
 * pool alongside the existing one. For a true replacement, a data pack would be needed.
 *
 * MC 1.21.1 port: uses loot-api-v2 (LootTableEvents without registries parameter).
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
     * Registers a loot table modifier that adds a fresh fishing rod pool to
     * relevant loot tables when replaceLootChestRods is enabled.
     *
     * Uses Fabric API loot-api-v2 LootTableEvents.MODIFY for MC 1.21.1.
     */
    private void registerLootModifier() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, key, tableBuilder, source) -> {
            if (!AbfConfig.get().replaceLootChestRods) return;

            String tableId = key.toString();

            // Only patch fishing and chest-type loot tables
            boolean isFishingOrChest =
                    tableId.contains("fishing") ||
                    tableId.contains("chest/") ||
                    tableId.contains("chests/") ||
                    tableId.contains("shipwreck") ||
                    tableId.contains("dungeon") ||
                    tableId.contains("stronghold") ||
                    tableId.contains("village") ||
                    tableId.contains("bastion") ||
                    tableId.contains("ruined_portal") ||
                    tableId.contains("igloo") ||
                    tableId.contains("jungle_temple") ||
                    tableId.contains("desert_pyramid") ||
                    tableId.contains("woodland_mansion") ||
                    tableId.contains("pillager_outpost") ||
                    tableId.contains("ancient_city") ||
                    tableId.contains("trail_ruins");

            if (!isFishingOrChest) return;

            // Add a pool that gives a fresh (undamaged) fishing rod
            // Roll: 1 rod, guaranteed (rolls=1, bonus_rolls=0)
            LootPool freshRodPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.FISHING_ROD))
                    .build();

            tableBuilder.pool(freshRodPool);

            if (AbfConfig.get().debugMode) {
                LOGGER.info("[AnythingButFish] Added fresh rod pool to: {}", tableId);
            }
        });

        LOGGER.info("[AnythingButFish] Loot table modifier registered.");
    }
}
