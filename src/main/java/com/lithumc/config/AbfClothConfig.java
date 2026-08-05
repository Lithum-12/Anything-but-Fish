package com.lithumc.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Cloth Config GUI for AnythingButFish.
 * Replaces YACL for MC 1.18.x.
 *
 * MC 1.18.2 port: uses TranslatableComponent/TextComponent
 *                  instead of Component.translatable/literal.
 *
 * Category layout:
 *   General     – Chances | Item Counts | XP | Physics
 *   Misc        – Feature Switches | Fishing Behavior
 *   Item Pool   – Pool Mode | Registry Filters | Item List
 *   Entity Pool – Pool Mode | Registry Filters | Entity List
 */
public class AbfClothConfig {

    // -----------------------------------------------------------------------
    // Serialization helpers
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
                .setTitle(new TranslatableComponent("config.anythingbutfish.title"))
                .setSavingRunnable(() -> {
                    cfg.save();
                    AbfConfig.reload();
                });

        ConfigEntryBuilder eb = builder.entryBuilder();

        List<String> itemPoolStrings   = new ArrayList<>();
        List<String> entityPoolStrings = new ArrayList<>();
        for (AbfConfig.ItemEntry   e : cfg.itemPool)   itemPoolStrings.add(serializeItem(e));
        for (AbfConfig.EntityEntry e : cfg.entityPool) entityPoolStrings.add(serializeEntity(e));

        // -----------------------------------------------------------------------
        // Category 1: General
        // -----------------------------------------------------------------------
        ConfigCategory general = builder.getOrCreateCategory(
                new TranslatableComponent("config.anythingbutfish.cat.general"));

        general.addEntry(eb.startIntSlider(
                        new TranslatableComponent("config.anythingbutfish.chanceItem"), cfg.chanceItem, 0, 100)
                .setDefaultValue(60).setTextGetter(v -> new TextComponent(v + "%"))
                .setTooltip(new TranslatableComponent("config.anythingbutfish.chanceItem.tooltip"))
                .setSaveConsumer(v -> cfg.chanceItem = v).build());
        general.addEntry(eb.startIntSlider(
                        new TranslatableComponent("config.anythingbutfish.chanceEntity"), cfg.chanceEntity, 0, 100)
                .setDefaultValue(25).setTextGetter(v -> new TextComponent(v + "%"))
                .setTooltip(new TranslatableComponent("config.anythingbutfish.chanceEntity.tooltip"))
                .setSaveConsumer(v -> cfg.chanceEntity = v).build());
        general.addEntry(eb.startIntSlider(
                        new TranslatableComponent("config.anythingbutfish.chanceXp"), cfg.chanceXp, 0, 100)
                .setDefaultValue(10).setTextGetter(v -> new TextComponent(v + "%"))
                .setTooltip(new TranslatableComponent("config.anythingbutfish.chanceXp.tooltip"))
                .setSaveConsumer(v -> cfg.chanceXp = v).build());

        general.addEntry(eb.startIntField(
                        new TranslatableComponent("config.anythingbutfish.itemCountMin"), cfg.itemCountMin)
                .setDefaultValue(1).setMin(1)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.itemCountMin.tooltip"))
                .setSaveConsumer(v -> cfg.itemCountMin = v).build());
        general.addEntry(eb.startIntField(
                        new TranslatableComponent("config.anythingbutfish.itemCountMax"), cfg.itemCountMax)
                .setDefaultValue(1).setMin(1)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.itemCountMax.tooltip"))
                .setSaveConsumer(v -> cfg.itemCountMax = v).build());

        general.addEntry(eb.startIntField(
                        new TranslatableComponent("config.anythingbutfish.xpMin"), cfg.xpMin)
                .setDefaultValue(1).setMin(1)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.xpMin.tooltip"))
                .setSaveConsumer(v -> cfg.xpMin = v).build());
        general.addEntry(eb.startIntField(
                        new TranslatableComponent("config.anythingbutfish.xpMax"), cfg.xpMax)
                .setDefaultValue(50).setMin(1)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.xpMax.tooltip"))
                .setSaveConsumer(v -> cfg.xpMax = v).build());

        general.addEntry(eb.startDoubleField(
                        new TranslatableComponent("config.anythingbutfish.flingSpeed"), cfg.flingSpeed)
                .setDefaultValue(0.1).setMin(0.01)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.flingSpeed.tooltip"))
                .setSaveConsumer(v -> cfg.flingSpeed = v).build());
        general.addEntry(eb.startDoubleField(
                        new TranslatableComponent("config.anythingbutfish.flingArc"), cfg.flingArc)
                .setDefaultValue(0.08).setMin(0.0)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.flingArc.tooltip"))
                .setSaveConsumer(v -> cfg.flingArc = v).build());

        // -----------------------------------------------------------------------
        // Category 2: Misc
        // -----------------------------------------------------------------------
        ConfigCategory misc = builder.getOrCreateCategory(
                new TranslatableComponent("config.anythingbutfish.cat.misc"));

        misc.addEntry(eb.startBooleanToggle(
                        new TranslatableComponent("config.anythingbutfish.enabled"), cfg.enabled)
                .setDefaultValue(true)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.enabled.tooltip"))
                .setSaveConsumer(v -> cfg.enabled = v).build());
        misc.addEntry(eb.startBooleanToggle(
                        new TranslatableComponent("config.anythingbutfish.debugMode"), cfg.debugMode)
                .setDefaultValue(false)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.debugMode.tooltip"))
                .setSaveConsumer(v -> cfg.debugMode = v).build());
        misc.addEntry(eb.startBooleanToggle(
                        new TranslatableComponent("config.anythingbutfish.infiniteDurability"), cfg.infiniteDurability)
                .setDefaultValue(false)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.infiniteDurability.tooltip"))
                .setSaveConsumer(v -> cfg.infiniteDurability = v).build());
        misc.addEntry(eb.startBooleanToggle(
                        new TranslatableComponent("config.anythingbutfish.waitForBite"), cfg.waitForBite)
                .setDefaultValue(false)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.waitForBite.tooltip"))
                .setSaveConsumer(v -> cfg.waitForBite = v).build());
        misc.addEntry(eb.startBooleanToggle(
                        new TranslatableComponent("config.anythingbutfish.moddedRodCompat"), cfg.moddedRodCompat)
                .setDefaultValue(true)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.moddedRodCompat.tooltip"))
                .setSaveConsumer(v -> cfg.moddedRodCompat = v).build());

        // -----------------------------------------------------------------------
        // Category 3: Item Pool
        // -----------------------------------------------------------------------
        ConfigCategory itemPool = builder.getOrCreateCategory(
                new TranslatableComponent("config.anythingbutfish.cat.itemPool"));

        itemPool.addEntry(eb.startBooleanToggle(
                        new TranslatableComponent("config.anythingbutfish.itemPoolIsWhitelist"), cfg.itemPoolIsWhitelist)
                .setDefaultValue(false)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.itemPoolIsWhitelist.tooltip"))
                .setSaveConsumer(v -> cfg.itemPoolIsWhitelist = v).build());
        itemPool.addEntry(eb.startBooleanToggle(
                        new TranslatableComponent("config.anythingbutfish.allowModdedItems"), cfg.allowModdedItems)
                .setDefaultValue(true)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.allowModdedItems.tooltip"))
                .setSaveConsumer(v -> cfg.allowModdedItems = v).build());
        itemPool.addEntry(eb.startBooleanToggle(
                        new TranslatableComponent("config.anythingbutfish.allowAdminItems"), cfg.allowAdminItems)
                .setDefaultValue(false)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.allowAdminItems.tooltip"))
                .setSaveConsumer(v -> cfg.allowAdminItems = v).build());

        itemPool.addEntry(eb.startStrList(
                        new TranslatableComponent("config.anythingbutfish.itemPool"), itemPoolStrings)
                .setDefaultValue(List.of())
                .setTooltip(new TranslatableComponent("config.anythingbutfish.itemPool.tooltip"))
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
                new TranslatableComponent("config.anythingbutfish.cat.entityPool"));

        entityPool.addEntry(eb.startBooleanToggle(
                        new TranslatableComponent("config.anythingbutfish.entityPoolIsWhitelist"), cfg.entityPoolIsWhitelist)
                .setDefaultValue(false)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.entityPoolIsWhitelist.tooltip"))
                .setSaveConsumer(v -> cfg.entityPoolIsWhitelist = v).build());
        entityPool.addEntry(eb.startBooleanToggle(
                        new TranslatableComponent("config.anythingbutfish.allowModdedEntities"), cfg.allowModdedEntities)
                .setDefaultValue(true)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.allowModdedEntities.tooltip"))
                .setSaveConsumer(v -> cfg.allowModdedEntities = v).build());
        entityPool.addEntry(eb.startBooleanToggle(
                        new TranslatableComponent("config.anythingbutfish.allowDangerousMobs"), cfg.allowDangerousMobs)
                .setDefaultValue(false)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.allowDangerousMobs.tooltip"))
                .setSaveConsumer(v -> cfg.allowDangerousMobs = v).build());

        entityPool.addEntry(eb.startStrList(
                        new TranslatableComponent("config.anythingbutfish.entityPool"), entityPoolStrings)
                .setDefaultValue(List.of())
                .setTooltip(new TranslatableComponent("config.anythingbutfish.entityPool.tooltip"))
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