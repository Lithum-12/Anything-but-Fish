package com.lithumc.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Cloth Config GUI for AnythingButFish.
 * Replaces YACL for MC 1.19.x and below.
 *
 * Category layout:
 *   General     – Chances | Item Counts | XP | Physics
 *   Misc        – Feature Switches | Fishing Behavior
 *   Item Pool   – Pool Mode | Registry Filters | Item List
 *   Entity Pool – Pool Mode | Registry Filters | Entity List
 */
public class AbfClothConfig {

    // -----------------------------------------------------------------------
    // Serialization helpers (same format as YACL version)
    // -----------------------------------------------------------------------
    private static String serializeItem(AbfConfig.ItemEntry e) {
        if (e.minCount > 0 && e.maxCount > 0)
            return e.id + ":" + e.minCount + ":" + e.maxCount;
        return e.id;
    }

    private static AbfConfig.ItemEntry parseItem(String s) {
        if (s == null || s.isBlank()) return null;
        String[] parts = s.trim().split(":");
        if (parts.length < 2) return null;
        String id = parts[0] + ":" + parts[1];
        AbfConfig.ItemEntry entry = new AbfConfig.ItemEntry(id);
        if (parts.length >= 4) {
            try {
                entry.minCount = Integer.parseInt(parts[2]);
                entry.maxCount = Integer.parseInt(parts[3]);
            } catch (NumberFormatException ignored) {}
        }
        return entry;
    }

    private static String serializeEntity(AbfConfig.EntityEntry e) {
        return e.id;
    }

    private static AbfConfig.EntityEntry parseEntity(String s) {
        if (s == null || s.isBlank()) return null;
        String[] parts = s.trim().split(":");
        if (parts.length < 2) return null;
        return new AbfConfig.EntityEntry(parts[0] + ":" + parts[1]);
    }

    // -----------------------------------------------------------------------
    // Screen builder
    // -----------------------------------------------------------------------
    public static Screen createScreen(Screen parent) {
        AbfConfig cfg = AbfConfig.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.anythingbutfish.title"))
                .setSavingRunnable(() -> {
                    cfg.save();
                    AbfConfig.reload();
                });

        ConfigEntryBuilder eb = builder.entryBuilder();

        // Mutable string mirrors for list options
        List<String> itemPoolStrings   = new ArrayList<>();
        List<String> entityPoolStrings = new ArrayList<>();
        for (AbfConfig.ItemEntry   e : cfg.itemPool)   itemPoolStrings.add(serializeItem(e));
        for (AbfConfig.EntityEntry e : cfg.entityPool) entityPoolStrings.add(serializeEntity(e));

        // -----------------------------------------------------------------------
        // Category 1: General
        // -----------------------------------------------------------------------
        ConfigCategory general = builder.getOrCreateCategory(
                Component.translatable("config.anythingbutfish.cat.general"));

        // Chances
        general.addEntry(eb.startIntSlider(
                        Component.translatable("config.anythingbutfish.chanceItem"), cfg.chanceItem, 0, 100)
                .setDefaultValue(60).setTextGetter(v -> Component.literal(v + "%"))
                .setTooltip(Component.translatable("config.anythingbutfish.chanceItem.tooltip"))
                .setSaveConsumer(v -> cfg.chanceItem = v).build());
        general.addEntry(eb.startIntSlider(
                        Component.translatable("config.anythingbutfish.chanceEntity"), cfg.chanceEntity, 0, 100)
                .setDefaultValue(25).setTextGetter(v -> Component.literal(v + "%"))
                .setTooltip(Component.translatable("config.anythingbutfish.chanceEntity.tooltip"))
                .setSaveConsumer(v -> cfg.chanceEntity = v).build());
        general.addEntry(eb.startIntSlider(
                        Component.translatable("config.anythingbutfish.chanceXp"), cfg.chanceXp, 0, 100)
                .setDefaultValue(10).setTextGetter(v -> Component.literal(v + "%"))
                .setTooltip(Component.translatable("config.anythingbutfish.chanceXp.tooltip"))
                .setSaveConsumer(v -> cfg.chanceXp = v).build());

        // Item counts
        general.addEntry(eb.startIntField(
                        Component.translatable("config.anythingbutfish.itemCountMin"), cfg.itemCountMin)
                .setDefaultValue(1).setMin(1)
                .setTooltip(Component.translatable("config.anythingbutfish.itemCountMin.tooltip"))
                .setSaveConsumer(v -> cfg.itemCountMin = v).build());
        general.addEntry(eb.startIntField(
                        Component.translatable("config.anythingbutfish.itemCountMax"), cfg.itemCountMax)
                .setDefaultValue(1).setMin(1)
                .setTooltip(Component.translatable("config.anythingbutfish.itemCountMax.tooltip"))
                .setSaveConsumer(v -> cfg.itemCountMax = v).build());

        // XP
        general.addEntry(eb.startIntField(
                        Component.translatable("config.anythingbutfish.xpMin"), cfg.xpMin)
                .setDefaultValue(1).setMin(1)
                .setTooltip(Component.translatable("config.anythingbutfish.xpMin.tooltip"))
                .setSaveConsumer(v -> cfg.xpMin = v).build());
        general.addEntry(eb.startIntField(
                        Component.translatable("config.anythingbutfish.xpMax"), cfg.xpMax)
                .setDefaultValue(50).setMin(1)
                .setTooltip(Component.translatable("config.anythingbutfish.xpMax.tooltip"))
                .setSaveConsumer(v -> cfg.xpMax = v).build());

        // Physics
        general.addEntry(eb.startDoubleField(
                        Component.translatable("config.anythingbutfish.flingSpeed"), cfg.flingSpeed)
                .setDefaultValue(0.1).setMin(0.01)
                .setTooltip(Component.translatable("config.anythingbutfish.flingSpeed.tooltip"))
                .setSaveConsumer(v -> cfg.flingSpeed = v).build());
        general.addEntry(eb.startDoubleField(
                        Component.translatable("config.anythingbutfish.flingArc"), cfg.flingArc)
                .setDefaultValue(0.08).setMin(0.0)
                .setTooltip(Component.translatable("config.anythingbutfish.flingArc.tooltip"))
                .setSaveConsumer(v -> cfg.flingArc = v).build());

        // -----------------------------------------------------------------------
        // Category 2: Misc
        // -----------------------------------------------------------------------
        ConfigCategory misc = builder.getOrCreateCategory(
                Component.translatable("config.anythingbutfish.cat.misc"));

        // Feature switches
        misc.addEntry(eb.startBooleanToggle(
                        Component.translatable("config.anythingbutfish.enabled"), cfg.enabled)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.anythingbutfish.enabled.tooltip"))
                .setSaveConsumer(v -> cfg.enabled = v).build());
        misc.addEntry(eb.startBooleanToggle(
                        Component.translatable("config.anythingbutfish.debugMode"), cfg.debugMode)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.anythingbutfish.debugMode.tooltip"))
                .setSaveConsumer(v -> cfg.debugMode = v).build());
        misc.addEntry(eb.startBooleanToggle(
                        Component.translatable("config.anythingbutfish.infiniteDurability"), cfg.infiniteDurability)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.anythingbutfish.infiniteDurability.tooltip"))
                .setSaveConsumer(v -> cfg.infiniteDurability = v).build());

        // Fishing behavior
        misc.addEntry(eb.startBooleanToggle(
                        Component.translatable("config.anythingbutfish.waitForBite"), cfg.waitForBite)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.anythingbutfish.waitForBite.tooltip"))
                .setSaveConsumer(v -> cfg.waitForBite = v).build());
        misc.addEntry(eb.startBooleanToggle(
                        Component.translatable("config.anythingbutfish.moddedRodCompat"), cfg.moddedRodCompat)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.anythingbutfish.moddedRodCompat.tooltip"))
                .setSaveConsumer(v -> cfg.moddedRodCompat = v).build());

        // -----------------------------------------------------------------------
        // Category 3: Item Pool
        // -----------------------------------------------------------------------
        ConfigCategory itemPool = builder.getOrCreateCategory(
                Component.translatable("config.anythingbutfish.cat.itemPool"));

        // Pool mode
        itemPool.addEntry(eb.startBooleanToggle(
                        Component.translatable("config.anythingbutfish.itemPoolIsWhitelist"), cfg.itemPoolIsWhitelist)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.anythingbutfish.itemPoolIsWhitelist.tooltip"))
                .setSaveConsumer(v -> cfg.itemPoolIsWhitelist = v).build());

        // Registry filters
        itemPool.addEntry(eb.startBooleanToggle(
                        Component.translatable("config.anythingbutfish.allowModdedItems"), cfg.allowModdedItems)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.anythingbutfish.allowModdedItems.tooltip"))
                .setSaveConsumer(v -> cfg.allowModdedItems = v).build());
        itemPool.addEntry(eb.startBooleanToggle(
                        Component.translatable("config.anythingbutfish.allowAdminItems"), cfg.allowAdminItems)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.anythingbutfish.allowAdminItems.tooltip"))
                .setSaveConsumer(v -> cfg.allowAdminItems = v).build());

        // Item list
        itemPool.addEntry(eb.startStrList(
                        Component.translatable("config.anythingbutfish.itemPool"), itemPoolStrings)
                .setDefaultValue(List.of())                                          // Default: empty item list
                .setTooltip(Component.translatable("config.anythingbutfish.itemPool.tooltip"))
                .setSaveConsumer(v -> {
                    cfg.itemPool.clear();
                    for (String s : v) {
                        AbfConfig.ItemEntry e = parseItem(s);
                        if (e != null) cfg.itemPool.add(e);
                    }
                }).build());

        // -----------------------------------------------------------------------
        // Category 4: Entity Pool
        // -----------------------------------------------------------------------
        ConfigCategory entityPool = builder.getOrCreateCategory(
                Component.translatable("config.anythingbutfish.cat.entityPool"));

        // Pool mode
        entityPool.addEntry(eb.startBooleanToggle(
                        Component.translatable("config.anythingbutfish.entityPoolIsWhitelist"), cfg.entityPoolIsWhitelist)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.anythingbutfish.entityPoolIsWhitelist.tooltip"))
                .setSaveConsumer(v -> cfg.entityPoolIsWhitelist = v).build());

        // Registry filters
        entityPool.addEntry(eb.startBooleanToggle(
                        Component.translatable("config.anythingbutfish.allowModdedEntities"), cfg.allowModdedEntities)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.anythingbutfish.allowModdedEntities.tooltip"))
                .setSaveConsumer(v -> cfg.allowModdedEntities = v).build());
        entityPool.addEntry(eb.startBooleanToggle(
                        Component.translatable("config.anythingbutfish.allowDangerousMobs"), cfg.allowDangerousMobs)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.anythingbutfish.allowDangerousMobs.tooltip"))
                .setSaveConsumer(v -> cfg.allowDangerousMobs = v).build());

        // Entity list
        entityPool.addEntry(eb.startStrList(
                        Component.translatable("config.anythingbutfish.entityPool"), entityPoolStrings)
                .setDefaultValue(List.of())                                          // Default: empty entity list
                .setTooltip(Component.translatable("config.anythingbutfish.entityPool.tooltip"))
                .setSaveConsumer(v -> {
                    cfg.entityPool.clear();
                    for (String s : v) {
                        AbfConfig.EntityEntry e = parseEntity(s);
                        if (e != null) cfg.entityPool.add(e);
                    }
                }).build());

        return builder.build();
    }
}