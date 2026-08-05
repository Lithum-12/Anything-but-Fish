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
 * YACL config screen for AnythingButFish.
 *
 * Tab layout:
 *   General  – chances | item counts | XP | physics
 *   Misc     – toggles | fishing behavior
 *   ItemPool – pool mode | registry filters | item list
 *   EntityPool – pool mode | registry filters | entity list
 *
 * Pool entry format (no weight):
 *   Items:    "namespace:name"  or  "namespace:name:minCount:maxCount"
 *   Entities: "namespace:name"
 */
@Environment(EnvType.CLIENT)
public class AbfYaclConfig {

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
        // Minimum: "namespace:name" -> 2 colon-separated tokens
        if (parts.length < 2) return null;
        String id = parts[0] + ":" + parts[1];
        AbfConfig.ItemEntry entry = new AbfConfig.ItemEntry(id);
        // Optional: "namespace:name:minCount:maxCount" -> 4 tokens
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

        // Mutable string mirrors for list options
        List<String> itemPoolStrings   = new ArrayList<>();
        List<String> entityPoolStrings = new ArrayList<>();
        for (AbfConfig.ItemEntry   e : cfg.itemPool)   itemPoolStrings.add(serializeItem(e));
        for (AbfConfig.EntityEntry e : cfg.entityPool) entityPoolStrings.add(serializeEntity(e));

        // ListOption: item pool
        ListOption<String> itemPoolList = ListOption.<String>createBuilder()
                .name(Component.translatable("config.anythingbutfish.itemPool"))
                .description(OptionDescription.of(
                        Component.translatable("config.anythingbutfish.itemPool.tooltip")))
                .binding(new ArrayList<>(), () -> new ArrayList<>(itemPoolStrings), v -> {
                    itemPoolStrings.clear();
                    itemPoolStrings.addAll(v);
                    cfg.itemPool.clear();
                    for (String s : v) {
                        AbfConfig.ItemEntry e = parseItem(s);
                        if (e != null) cfg.itemPool.add(e);
                    }
                })
                .controller(StringControllerBuilder::create)
                .initial("minecraft:diamond")
                .build();

        // ListOption: entity pool
        ListOption<String> entityPoolList = ListOption.<String>createBuilder()
                .name(Component.translatable("config.anythingbutfish.entityPool"))
                .description(OptionDescription.of(
                        Component.translatable("config.anythingbutfish.entityPool.tooltip")))
                .binding(new ArrayList<>(), () -> new ArrayList<>(entityPoolStrings), v -> {
                    entityPoolStrings.clear();
                    entityPoolStrings.addAll(v);
                    cfg.entityPool.clear();
                    for (String s : v) {
                        AbfConfig.EntityEntry e = parseEntity(s);
                        if (e != null) cfg.entityPool.add(e);
                    }
                })
                .controller(StringControllerBuilder::create)
                .initial("minecraft:pig")
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.anythingbutfish.title"))

                // -----------------------------------------------------------------------
                // Tab 1: General
                // -----------------------------------------------------------------------
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.anythingbutfish.cat.general"))
                        .tooltip(Component.translatable("config.anythingbutfish.cat.general.tooltip"))

                        // Chances
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

                        // Item counts
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
                                        .binding(1, () -> cfg.itemCountMax, v -> cfg.itemCountMax = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 64).step(1))
                                        .build())
                                .build())

                        // XP
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

                        // Physics
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

                // -----------------------------------------------------------------------
                // Tab 2: Misc
                // -----------------------------------------------------------------------
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.anythingbutfish.cat.misc"))
                        .tooltip(Component.translatable("config.anythingbutfish.cat.misc.tooltip"))

                        // Toggles
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
                                .build())

                        // Fishing behavior
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.anythingbutfish.group.behavior"))
                                .description(OptionDescription.of(Component.translatable("config.anythingbutfish.group.behavior.desc")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.waitForBite"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.waitForBite.tooltip")))
                                        .binding(false, () -> cfg.waitForBite, v -> cfg.waitForBite = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.moddedRodCompat"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.moddedRodCompat.tooltip")))
                                        .binding(true, () -> cfg.moddedRodCompat, v -> cfg.moddedRodCompat = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .build())

                        .build())

                // -----------------------------------------------------------------------
                // Tab 3: Item pool
                // -----------------------------------------------------------------------
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.anythingbutfish.cat.itemPool"))
                        .tooltip(Component.translatable("config.anythingbutfish.cat.itemPool.tooltip"))

                        // Pool mode
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.anythingbutfish.group.itemPoolMode"))
                                .description(OptionDescription.of(Component.translatable("config.anythingbutfish.group.itemPoolMode.desc")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.itemPoolIsWhitelist"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.itemPoolIsWhitelist.tooltip")))
                                        .binding(false, () -> cfg.itemPoolIsWhitelist, v -> cfg.itemPoolIsWhitelist = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .valueFormatter(v -> Component.translatable(
                                                        v ? "config.anythingbutfish.mode.whitelist"
                                                                : "config.anythingbutfish.mode.blacklist")))
                                        .build())
                                .build())

                        // Registry filters (apply in BLACKLIST mode)
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.anythingbutfish.group.itemFilters"))
                                .description(OptionDescription.of(Component.translatable("config.anythingbutfish.group.itemFilters.desc")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.allowModdedItems"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.allowModdedItems.tooltip")))
                                        .binding(true, () -> cfg.allowModdedItems, v -> cfg.allowModdedItems = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.allowAdminItems"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.allowAdminItems.tooltip")))
                                        .binding(false, () -> cfg.allowAdminItems, v -> cfg.allowAdminItems = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .build())

                        // Item list
                        .group(itemPoolList)
                        .build())

                // -----------------------------------------------------------------------
                // Tab 4: Entity pool
                // -----------------------------------------------------------------------
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.anythingbutfish.cat.entityPool"))
                        .tooltip(Component.translatable("config.anythingbutfish.cat.entityPool.tooltip"))

                        // Pool mode
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.anythingbutfish.group.entityPoolMode"))
                                .description(OptionDescription.of(Component.translatable("config.anythingbutfish.group.entityPoolMode.desc")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.entityPoolIsWhitelist"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.entityPoolIsWhitelist.tooltip")))
                                        .binding(false, () -> cfg.entityPoolIsWhitelist, v -> cfg.entityPoolIsWhitelist = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .valueFormatter(v -> Component.translatable(
                                                        v ? "config.anythingbutfish.mode.whitelist"
                                                                : "config.anythingbutfish.mode.blacklist")))
                                        .build())
                                .build())

                        // Registry filters
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.anythingbutfish.group.entityFilters"))
                                .description(OptionDescription.of(Component.translatable("config.anythingbutfish.group.entityFilters.desc")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.allowModdedEntities"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.allowModdedEntities.tooltip")))
                                        .binding(true, () -> cfg.allowModdedEntities, v -> cfg.allowModdedEntities = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.anythingbutfish.allowDangerousMobs"))
                                        .description(OptionDescription.of(Component.translatable("config.anythingbutfish.allowDangerousMobs.tooltip")))
                                        .binding(false, () -> cfg.allowDangerousMobs, v -> cfg.allowDangerousMobs = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .build())
                                .build())

                        // Entity list
                        .group(entityPoolList)
                        .build())

                .save(cfg::save)
                .build()
                .generateScreen(parent);
    }
}