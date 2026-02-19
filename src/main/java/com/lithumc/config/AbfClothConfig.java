package com.lithumc.config;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
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
 */
public class AbfClothConfig {

    public static Screen createScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.anythingbutfish.title"))
                .setSavingRunnable(() -> {
                    AbfConfig.get().save();
                    AbfConfig.reload();
                });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        AbfConfig config = AbfConfig.get();

        // General category
        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.anythingbutfish.category.general"));
        
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.anythingbutfish.enabled"), config.enabled)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.enabled = val)
                .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.anythingbutfish.debugMode"), config.debugMode)
                .setDefaultValue(false)
                .setSaveConsumer(val -> config.debugMode = val)
                .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.anythingbutfish.infiniteDurability"), config.infiniteDurability)
                .setDefaultValue(false)
                .setSaveConsumer(val -> config.infiniteDurability = val)
                .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.anythingbutfish.replaceLootChestRods"), config.replaceLootChestRods)
                .setDefaultValue(false)
                .setSaveConsumer(val -> config.replaceLootChestRods = val)
                .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.anythingbutfish.allowModdedItems"), config.allowModdedItems)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.allowModdedItems = val)
                .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.anythingbutfish.allowModdedEntities"), config.allowModdedEntities)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.allowModdedEntities = val)
                .build());

        // Chances category
        ConfigCategory chances = builder.getOrCreateCategory(Component.translatable("config.anythingbutfish.category.chances"));
        
        chances.addEntry(entryBuilder.startIntSlider(Component.translatable("config.anythingbutfish.chanceItem"), config.chanceItem, 0, 100)
                .setDefaultValue(60)
                .setTextGetter(val -> Component.literal(val + "%"))
                .setSaveConsumer(val -> config.chanceItem = val)
                .build());
        
        chances.addEntry(entryBuilder.startIntSlider(Component.translatable("config.anythingbutfish.chanceEntity"), config.chanceEntity, 0, 100)
                .setDefaultValue(25)
                .setTextGetter(val -> Component.literal(val + "%"))
                .setSaveConsumer(val -> config.chanceEntity = val)
                .build());
        
        chances.addEntry(entryBuilder.startIntSlider(Component.translatable("config.anythingbutfish.chanceXp"), config.chanceXp, 0, 100)
                .setDefaultValue(10)
                .setTextGetter(val -> Component.literal(val + "%"))
                .setSaveConsumer(val -> config.chanceXp = val)
                .build());

        // Item settings category
        ConfigCategory items = builder.getOrCreateCategory(Component.translatable("config.anythingbutfish.category.items"));
        
        items.addEntry(entryBuilder.startIntField(Component.translatable("config.anythingbutfish.itemCountMin"), config.itemCountMin)
                .setDefaultValue(1)
                .setMin(1)
                .setSaveConsumer(val -> config.itemCountMin = val)
                .build());
        
        items.addEntry(entryBuilder.startIntField(Component.translatable("config.anythingbutfish.itemCountMax"), config.itemCountMax)
                .setDefaultValue(1)
                .setMin(1)
                .setSaveConsumer(val -> config.itemCountMax = val)
                .build());

        // XP settings category
        ConfigCategory xp = builder.getOrCreateCategory(Component.translatable("config.anythingbutfish.category.xp"));
        
        xp.addEntry(entryBuilder.startIntField(Component.translatable("config.anythingbutfish.xpMin"), config.xpMin)
                .setDefaultValue(1)
                .setMin(1)
                .setSaveConsumer(val -> config.xpMin = val)
                .build());
        
        xp.addEntry(entryBuilder.startIntField(Component.translatable("config.anythingbutfish.xpMax"), config.xpMax)
                .setDefaultValue(50)
                .setMin(1)
                .setSaveConsumer(val -> config.xpMax = val)
                .build());

        // Physics settings category
        ConfigCategory physics = builder.getOrCreateCategory(Component.translatable("config.anythingbutfish.category.physics"));
        
        physics.addEntry(entryBuilder.startDoubleField(Component.translatable("config.anythingbutfish.flingSpeed"), config.flingSpeed)
                .setDefaultValue(0.1)
                .setMin(0.01)
                .setSaveConsumer(val -> config.flingSpeed = val)
                .build());
        
        physics.addEntry(entryBuilder.startDoubleField(Component.translatable("config.anythingbutfish.flingArc"), config.flingArc)
                .setDefaultValue(0.08)
                .setMin(0.0)
                .setSaveConsumer(val -> config.flingArc = val)
                .build());

        return builder.build();
    }
}