package com.lithumc.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * YACL-based GUI configuration screen for AnythingButFish.
 *
 * Tab order: 常规 | 杂项 | 物品池 | 实体池
 */
@Environment(EnvType.CLIENT)
public class AbfYaclConfig {

    // -----------------------------------------------------------------------
    // Pool serialization helpers
    // -----------------------------------------------------------------------

    private static String serializeItem(AbfConfig.ItemEntry e) {
        if (e.minCount > 0 && e.maxCount > 0)
            return e.id + ":" + e.weight + ":" + e.minCount + ":" + e.maxCount;
        return e.id + ":" + e.weight;
    }

    private static AbfConfig.ItemEntry parseItem(String s) {
        if (s == null || s.isBlank()) return null;
        String[] parts = s.trim().split(":");
        if (parts.length < 3) return null;
        try {
            String id = parts[0] + ":" + parts[1];
            int weight = Math.max(1, Integer.parseInt(parts[2]));
            AbfConfig.ItemEntry entry = new AbfConfig.ItemEntry(id, weight);
            if (parts.length >= 5) {
                entry.minCount = Integer.parseInt(parts[3]);
                entry.maxCount = Integer.parseInt(parts[4]);
            }
            return entry;
        } catch (NumberFormatException e) { return null; }
    }

    private static String serializeEntity(AbfConfig.EntityEntry e) {
        return e.id + ":" + e.weight;
    }

    private static AbfConfig.EntityEntry parseEntity(String s) {
        if (s == null || s.isBlank()) return null;
        String[] parts = s.trim().split(":");
        if (parts.length < 3) return null;
        try {
            String id = parts[0] + ":" + parts[1];
            int weight = Math.max(1, Integer.parseInt(parts[2]));
            return new AbfConfig.EntityEntry(id, weight);
        } catch (NumberFormatException e) { return null; }
    }

    // -----------------------------------------------------------------------
    // Screen builder
    // -----------------------------------------------------------------------

    public static Screen createScreen(Screen parent) {
        AbfConfig cfg = AbfConfig.get();

        List<String> itemPoolStrings = new ArrayList<>();
        for (AbfConfig.ItemEntry e : cfg.itemPool) itemPoolStrings.add(serializeItem(e));

        List<String> entityPoolStrings = new ArrayList<>();
        for (AbfConfig.EntityEntry e : cfg.entityPool) entityPoolStrings.add(serializeEntity(e));

        // ListOptions must be added as groups directly to ConfigCategory (not inside OptionGroup)
        ListOption<String> itemPoolList = ListOption.<String>createBuilder()
                .name(Component.translatable("config.anythingbutfish.itemPool"))
                .description(OptionDescription.of(Component.translatable("config.anythingbutfish.itemPool.tooltip")))
                .binding(new ArrayList<>(), () -> new ArrayList<>(itemPoolStrings), v -> {
                    itemPoolStrings.clear(); itemPoolStrings.addAll(v);
                    cfg.itemPool.clear();
                    for (String s : v) { AbfConfig.ItemEntry e = parseItem(s); if (e != null) cfg.itemPool.add(e); }
                })
                .controller(StringControllerBuilder::create)
                .initial("minecraft:cod:10")
                .build();

        ListOption<String> entityPoolList = ListOption.<String>createBuilder()
                .name(Component.translatable("config.anythingbutfish.entityPool"))
                .description(OptionDescription.of(Component.translatable("config.anythingbutfish.entityPool.tooltip")))
                .binding(new ArrayList<>(), () -> new ArrayList<>(entityPoolStrings), v -> {
                    entityPoolStrings.clear(); entityPoolStrings.addAll(v);
                    cfg.entityPool.clear();
                    for (String s : v) { AbfConfig.EntityEntry e = parseEntity(s); if (e != null) cfg.entityPool.add(e); }
                })
                .controller(StringControllerBuilder::create)
                .initial("minecraft:pig:10")
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.anythingbutfish.title"))

                // ── Tab 1: 常规 ───────────────────────────────────────────────
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.anythingbutfish.cat.general"))
                        .tooltip(Component.translatable("config.anythingbutfish.cat.general.tooltip"))

                        // Chances group
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.anythingbutfish.group.chances"))
                                .description(OptionDescription.of(Component.translatable("config.anythingbutfish.group.chances.desc")))
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.chanceItem"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.chanceItem.tooltip")))
                                        .binding(60, () -> cfg.chanceItem, v -> cfg.chanceItem = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.chanceEntity"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.chanceEntity.tooltip")))
                                        .binding(25, () -> cfg.chanceEntity, v -> cfg.chanceEntity = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.chanceXp"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.chanceXp.tooltip")))
                                        .binding(10, () -> cfg.chanceXp, v -> cfg.chanceXp = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1))
                                        .build())
                                .build())

                        // Item settings group
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.anythingbutfish.group.items"))
                                .description(OptionDescription.of(Component.translatable("config.anythingbutfish.group.items.desc")))
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.itemCountMin"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.itemCountMin.tooltip")))
                                        .binding(1, () -> cfg.itemCountMin, v -> cfg.itemCountMin = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 64).step(1))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.itemCountMax"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.itemCountMax.tooltip")))
                                        .binding(3, () -> cfg.itemCountMax, v -> cfg.itemCountMax = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 64).step(1))
                                        .build())
                                .build())

                        // XP group
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.anythingbutfish.group.xp"))
                                .description(OptionDescription.of(Component.translatable("config.anythingbutfish.group.xp.desc")))
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.xpMin"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.xpMin.tooltip")))
                                        .binding(1, () -> cfg.xpMin, v -> cfg.xpMin = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 1000).step(1))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.xpMax"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.xpMax.tooltip")))
                                        .binding(50, () -> cfg.xpMax, v -> cfg.xpMax = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 1000).step(1))
                                        .build())
                                .build())

                        // Physics group
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.anythingbutfish.group.physics"))
                                .description(OptionDescription.of(Component.translatable("config.anythingbutfish.group.physics.desc")))
                                .option(Option.<Double>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.flingSpeed"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.flingSpeed.tooltip")))
                                        .binding(0.1, () -> cfg.flingSpeed, v -> cfg.flingSpeed = v)
                                        .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.01, 1.0).step(0.01))
                                        .build())
                                .option(Option.<Double>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.flingArc"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.flingArc.tooltip")))
                                        .binding(0.08, () -> cfg.flingArc, v -> cfg.flingArc = v)
                                        .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 1.0).step(0.01))
                                        .build())
                                .build())

                        .build())

                // ── Tab 2: 杂项 ───────────────────────────────────────────────
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.anythingbutfish.cat.misc"))
                        .tooltip(Component.translatable("config.anythingbutfish.cat.misc.tooltip"))

                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.anythingbutfish.group.switches"))
                                .description(OptionDescription.of(Component.translatable("config.anythingbutfish.group.switches.desc")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.enabled"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.enabled.tooltip")))
                                        .binding(true, () -> cfg.enabled, v -> cfg.enabled = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.debugMode"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.debugMode.tooltip")))
                                        .binding(false, () -> cfg.debugMode, v -> cfg.debugMode = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.infiniteDurability"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.infiniteDurability.tooltip")))
                                        .binding(false, () -> cfg.infiniteDurability, v -> cfg.infiniteDurability = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.replaceLootChestRods"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.replaceLootChestRods.tooltip")))
                                        .binding(false, () -> cfg.replaceLootChestRods, v -> cfg.replaceLootChestRods = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.moddedRodCompat"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.moddedRodCompat.tooltip")))
                                        .binding(true, () -> cfg.moddedRodCompat, v -> cfg.moddedRodCompat = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.allowModdedItems"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.allowModdedItems.tooltip")))
                                        .binding(true, () -> cfg.allowModdedItems, v -> cfg.allowModdedItems = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.allowModdedEntities"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.allowModdedEntities.tooltip")))
                                        .binding(true, () -> cfg.allowModdedEntities, v -> cfg.allowModdedEntities = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .build())

                        .build())

                // ── Tab 3: 物品池 ─────────────────────────────────────────────
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.anythingbutfish.cat.itemPool"))
                        .tooltip(Component.translatable("config.anythingbutfish.cat.itemPool.tooltip"))
                        .group(itemPoolList)
                        .build())

                // ── Tab 4: 实体池 ─────────────────────────────────────────────
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.anythingbutfish.cat.entityPool"))
                        .tooltip(Component.translatable("config.anythingbutfish.cat.entityPool.tooltip"))
                        .group(entityPoolList)
                        .build())

                .save(cfg::save)
                .build()
                .generateScreen(parent);
    }
}
