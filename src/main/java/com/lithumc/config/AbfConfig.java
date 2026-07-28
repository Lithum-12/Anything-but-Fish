package com.lithumc.config;

import com.google.gson.*;
import com.lithumc.AnythingButFish;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple JSON-based configuration for AnythingButFish.
 * Config file: .minecraft/config/anythingbutfish.json5
 *
 * Pool modes:
 *   itemPoolIsWhitelist / entityPoolIsWhitelist
 *     true  (WHITELIST) – only items/entities in the list can appear.
 *                         Empty list = skip this loot type entirely.
 *     false (BLACKLIST) – full registry minus items/entities in the list.
 *                         allowModded/allowAdmin/allowDangerous filters apply.
 */
public class AbfConfig {

    // -----------------------------------------------------------------------
    // Entry types (no weight – pool mode determines selection)
    // -----------------------------------------------------------------------
    public static class ItemEntry {
        public String id = "minecraft:cod";
        public int minCount = -1;
        public int maxCount = -1;

        public ItemEntry() {}
        public ItemEntry(String id) { this.id = id; }
        public ItemEntry(String id, int minCount, int maxCount) {
            this.id = id;
            this.minCount = minCount;
            this.maxCount = maxCount;
        }
    }

    public static class EntityEntry {
        public String id = "minecraft:pig";

        public EntityEntry() {}
        public EntityEntry(String id) { this.id = id; }
    }

    // -----------------------------------------------------------------------
    // Pool mode switches
    // -----------------------------------------------------------------------
    /** true = whitelist (only listed items), false = blacklist (all except listed) */
    public boolean itemPoolIsWhitelist = false;
    /** true = whitelist (only listed entities), false = blacklist (all except listed) */
    public boolean entityPoolIsWhitelist = false;

    // -----------------------------------------------------------------------
    // Misc toggles
    // -----------------------------------------------------------------------
    /** Master switch. If false, vanilla fishing behavior is restored. */
    public boolean enabled = true;
    /** Debug mode: print caught loot to chat and log. */
    public boolean debugMode = false;
    /** Make all fishing rods have infinite durability (no damage on use). */
    public boolean infiniteDurability = false;
    /**
     * Compatibility mode for modded fishing rods.
     * Informational – the mod already hooks into all FishingHook entities.
     */
    public boolean moddedRodCompat = true;
    /**
     * Wait for a fish to bite before giving random loot.
     * false = loot every reel-in; true = loot only on actual bite.
     */
    public boolean waitForBite = false;

    // -----------------------------------------------------------------------
    // Registry filters (apply in BLACKLIST mode only)
    // -----------------------------------------------------------------------
    /** Allow modded (non-minecraft namespace) items in blacklist mode. */
    public boolean allowModdedItems = true;
    /** Allow modded (non-minecraft namespace) entities in blacklist mode. */
    public boolean allowModdedEntities = true;
    /** Allow admin items (command blocks, barriers, etc.) in blacklist mode. */
    public boolean allowAdminItems = false;
    /** Allow dangerous mobs (ender dragon, wither) in blacklist mode. */
    public boolean allowDangerousMobs = false;

    // -----------------------------------------------------------------------
    // Chance settings
    // -----------------------------------------------------------------------
    public int chanceItem = 60;
    public int chanceEntity = 25;
    public int chanceXp = 10;

    // -----------------------------------------------------------------------
    // Item settings
    // -----------------------------------------------------------------------
    public int itemCountMin = 1;
    public int itemCountMax = 1;

    /**
     * Item pool. Behaviour depends on itemPoolIsWhitelist:
     *   WHITELIST – only these items can appear (empty = skip item drops).
     *   BLACKLIST – these items are excluded from the full registry.
     */
    public List<ItemEntry> itemPool = new ArrayList<>();

    // -----------------------------------------------------------------------
    // Entity settings
    // -----------------------------------------------------------------------
    /**
     * Entity pool. Behaviour depends on entityPoolIsWhitelist:
     *   WHITELIST – only these entities can appear (empty = skip entity spawns).
     *   BLACKLIST – these entities are excluded from the full registry.
     */
    public List<EntityEntry> entityPool = new ArrayList<>();

    // -----------------------------------------------------------------------
    // XP settings
    // -----------------------------------------------------------------------
    public int xpMin = 1;
    public int xpMax = 50;

    // -----------------------------------------------------------------------
    // Physics settings
    // -----------------------------------------------------------------------
    public double flingSpeed = 0.1;
    public double flingArc = 0.08;

    // -----------------------------------------------------------------------
    // Singleton
    // -----------------------------------------------------------------------
    private static AbfConfig INSTANCE = null;

    public static AbfConfig get() {
        if (INSTANCE == null) INSTANCE = load();
        return INSTANCE;
    }

    public static void reload() {
        INSTANCE = load();
    }

    // -----------------------------------------------------------------------
    // Load / Save
    // -----------------------------------------------------------------------
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("anythingbutfish.json5");
    }

    public static AbfConfig load() {
        Path path = configPath();
        AbfConfig cfg = new AbfConfig();
        if (path.toFile().exists()) {
            try (Reader r = new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8)) {
                AbfConfig loaded = GSON.fromJson(r, AbfConfig.class);
                if (loaded != null) { cfg = loaded; cfg.clamp(); }
            } catch (Exception e) {
                AnythingButFish.LOGGER.warn("[AnythingButFish] Failed to load config: {}", e.getMessage());
            }
        }
        cfg.save();
        return cfg;
    }

    public void save() {
        clamp();
        Path path = configPath();
        try {
            path.getParent().toFile().mkdirs();
            try (Writer w = new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8)) {
                w.write("/*\n");
                w.write(" * AnythingButFish Configuration File\n");
                w.write(" *\n");
                w.write(" * Pool modes:\n");
                w.write(" *   itemPoolIsWhitelist / entityPoolIsWhitelist\n");
                w.write(" *     true  (WHITELIST) - only items/entities in the list can appear.\n");
                w.write(" *                         Empty list = skip this loot type entirely.\n");
                w.write(" *     false (BLACKLIST) - full registry minus items/entities in the list.\n");
                w.write(" *                         allowModded/allowAdmin/allowDangerous filters apply.\n");
                w.write(" *\n");
                w.write(" * Probability: chanceItem + chanceEntity + chanceXp <= 100.\n");
                w.write(" *   Remainder is the chance of nothing (the one that got away).\n");
                w.write(" */\n");
                w.write("\n");
                GSON.toJson(this, w);
            }
        } catch (Exception e) {
            AnythingButFish.LOGGER.warn("[AnythingButFish] Failed to save config: {}", e.getMessage());
        }
    }

    private void clamp() {
        chanceItem   = Math.max(0, Math.min(100, chanceItem));
        chanceEntity = Math.max(0, Math.min(100, chanceEntity));
        chanceXp     = Math.max(0, Math.min(100, chanceXp));

        int total = chanceItem + chanceEntity + chanceXp;
        if (total > 100) {
            double scale = 100.0 / total;
            chanceItem   = (int)(chanceItem   * scale);
            chanceEntity = (int)(chanceEntity * scale);
            chanceXp     = (int)(chanceXp     * scale);
        }

        itemCountMin = Math.max(1, itemCountMin);
        itemCountMax = Math.max(itemCountMin, itemCountMax);
        xpMin = Math.max(1, xpMin);
        xpMax = Math.max(xpMin, xpMax);
        flingSpeed = Math.max(0.01, flingSpeed);
        flingArc   = Math.max(0.0,  flingArc);

        if (itemPool == null) itemPool = new ArrayList<>();
        itemPool.removeIf(e -> e == null || e.id == null || e.id.isBlank());

        if (entityPool == null) entityPool = new ArrayList<>();
        entityPool.removeIf(e -> e == null || e.id == null || e.id.isBlank());
    }

    // -----------------------------------------------------------------------
    // Threshold helpers
    // -----------------------------------------------------------------------
    public int thresholdItem()   { return chanceItem; }
    public int thresholdEntity() { return chanceItem + chanceEntity; }
    public int thresholdXp()     { return chanceItem + chanceEntity + chanceXp; }
}