package com.lithumc.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.TextComponent;

/**
 * Cloth Config GUI for AnythingButFish.
 * Replaces YACL for MC 1.18.x.
 * 
 * MC 1.18.2 port: uses TranslatableComponent/TextComponent instead of Component.translatable/literal.
 */
public class AbfClothConfig {

    public static Screen createScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new TranslatableComponent("config.anythingbutfish.title"))
                .setSavingRunnable(() -> {
                    AbfConfig.get().save();
                    AbfConfig.reload();
                });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        AbfConfig config = AbfConfig.get();

        // General category
        ConfigCategory general = builder.getOrCreateCategory(new TranslatableComponent("config.anythingbutfish.category.general"));
        
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("config.anythingbutfish.enabled"), config.enabled)
                .setDefaultValue(true)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.enabled.tooltip"))
                .setSaveConsumer(val -> config.enabled = val)
                .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("config.anythingbutfish.debugMode"), config.debugMode)
                .setDefaultValue(false)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.debugMode.tooltip"))
                .setSaveConsumer(val -> config.debugMode = val)
                .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("config.anythingbutfish.infiniteDurability"), config.infiniteDurability)
                .setDefaultValue(false)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.infiniteDurability.tooltip"))
                .setSaveConsumer(val -> config.infiniteDurability = val)
                .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("config.anythingbutfish.replaceLootChestRods"), config.replaceLootChestRods)
                .setDefaultValue(false)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.replaceLootChestRods.tooltip"))
                .setSaveConsumer(val -> config.replaceLootChestRods = val)
                .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("config.anythingbutfish.allowModdedItems"), config.allowModdedItems)
                .setDefaultValue(true)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.allowModdedItems.tooltip"))
                .setSaveConsumer(val -> config.allowModdedItems = val)
                .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("config.anythingbutfish.allowModdedEntities"), config.allowModdedEntities)
                .setDefaultValue(true)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.allowModdedEntities.tooltip"))
                .setSaveConsumer(val -> config.allowModdedEntities = val)
                .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("config.anythingbutfish.allowAdminItems"), config.allowAdminItems)
                .setDefaultValue(false)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.allowAdminItems.tooltip"))
                .setSaveConsumer(val -> config.allowAdminItems = val)
                .build());
        
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("config.anythingbutfish.allowDangerousMobs"), config.allowDangerousMobs)
                .setDefaultValue(false)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.allowDangerousMobs.tooltip"))
                .setSaveConsumer(val -> config.allowDangerousMobs = val)
                .build());

        // Chances category
        ConfigCategory chances = builder.getOrCreateCategory(new TranslatableComponent("config.anythingbutfish.category.chances"));
        
        chances.addEntry(entryBuilder.startIntSlider(new TranslatableComponent("config.anythingbutfish.chanceItem"), config.chanceItem, 0, 100)
                .setDefaultValue(60)
                .setTextGetter(val -> new TextComponent(val + "%"))
                .setTooltip(new TranslatableComponent("config.anythingbutfish.chanceItem.tooltip"))
                .setSaveConsumer(val -> config.chanceItem = val)
                .build());
        
        chances.addEntry(entryBuilder.startIntSlider(new TranslatableComponent("config.anythingbutfish.chanceEntity"), config.chanceEntity, 0, 100)
                .setDefaultValue(25)
                .setTextGetter(val -> new TextComponent(val + "%"))
                .setTooltip(new TranslatableComponent("config.anythingbutfish.chanceEntity.tooltip"))
                .setSaveConsumer(val -> config.chanceEntity = val)
                .build());
        
        chances.addEntry(entryBuilder.startIntSlider(new TranslatableComponent("config.anythingbutfish.chanceXp"), config.chanceXp, 0, 100)
                .setDefaultValue(10)
                .setTextGetter(val -> new TextComponent(val + "%"))
                .setTooltip(new TranslatableComponent("config.anythingbutfish.chanceXp.tooltip"))
                .setSaveConsumer(val -> config.chanceXp = val)
                .build());

        // Item settings category
        ConfigCategory items = builder.getOrCreateCategory(new TranslatableComponent("config.anythingbutfish.category.items"));
        
        items.addEntry(entryBuilder.startIntField(new TranslatableComponent("config.anythingbutfish.itemCountMin"), config.itemCountMin)
                .setDefaultValue(1)
                .setMin(1)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.itemCountMin.tooltip"))
                .setSaveConsumer(val -> config.itemCountMin = val)
                .build());
        
        items.addEntry(entryBuilder.startIntField(new TranslatableComponent("config.anythingbutfish.itemCountMax"), config.itemCountMax)
                .setDefaultValue(1)
                .setMin(1)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.itemCountMax.tooltip"))
                .setSaveConsumer(val -> config.itemCountMax = val)
                .build());

        // XP settings category
        ConfigCategory xp = builder.getOrCreateCategory(new TranslatableComponent("config.anythingbutfish.category.xp"));
        
        xp.addEntry(entryBuilder.startIntField(new TranslatableComponent("config.anythingbutfish.xpMin"), config.xpMin)
                .setDefaultValue(1)
                .setMin(1)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.xpMin.tooltip"))
                .setSaveConsumer(val -> config.xpMin = val)
                .build());
        
        xp.addEntry(entryBuilder.startIntField(new TranslatableComponent("config.anythingbutfish.xpMax"), config.xpMax)
                .setDefaultValue(50)
                .setMin(1)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.xpMax.tooltip"))
                .setSaveConsumer(val -> config.xpMax = val)
                .build());

        // Physics settings category
        ConfigCategory physics = builder.getOrCreateCategory(new TranslatableComponent("config.anythingbutfish.category.physics"));
        
        physics.addEntry(entryBuilder.startDoubleField(new TranslatableComponent("config.anythingbutfish.flingSpeed"), config.flingSpeed)
                .setDefaultValue(0.1)
                .setMin(0.01)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.flingSpeed.tooltip"))
                .setSaveConsumer(val -> config.flingSpeed = val)
                .build());
        
        physics.addEntry(entryBuilder.startDoubleField(new TranslatableComponent("config.anythingbutfish.flingArc"), config.flingArc)
                .setDefaultValue(0.08)
                .setMin(0.0)
                .setTooltip(new TranslatableComponent("config.anythingbutfish.flingArc.tooltip"))
                .setSaveConsumer(val -> config.flingArc = val)
                .build());

        // Info category - explains item/entity pools
        ConfigCategory info = builder.getOrCreateCategory(new TranslatableComponent("config.anythingbutfish.category.info"));
        
        info.addEntry(entryBuilder.startTextDescription(new TranslatableComponent("config.anythingbutfish.info.pools"))
                .build());
        
        info.addEntry(entryBuilder.startTextDescription(new TranslatableComponent("config.anythingbutfish.info.pools.format"))
                .build());

        return builder.build();
    }
}