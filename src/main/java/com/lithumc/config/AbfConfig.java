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
 * Config file: .minecraft/config/anythingbutfish.json
 */
public class AbfConfig {

    // -----------------------------------------------------------------------
    // Weighted entry types
    // -----------------------------------------------------------------------

    public static class ItemEntry {
        public String id = "minecraft:cod";
        public int weight = 10;
        public int minCount = -1;
        public int maxCount = -1;

        public ItemEntry() {}
        public ItemEntry(String id, int weight) { this.id = id; this.weight = weight; }
        public ItemEntry(String id, int weight, int minCount, int maxCount) {
            this.id = id; this.weight = weight;
            this.minCount = minCount; this.maxCount = maxCount;
        }
    }

    public static class EntityEntry {
        public String id = "minecraft:cod";
        public int weight = 10;

        public EntityEntry() {}
        public EntityEntry(String id, int weight) { this.id = id; this.weight = weight; }
    }

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
     * Replace loot chest fishing rod loot with a fishing rod.
     * When true, any loot table entry that would give a fishing rod is replaced
     * with a fresh fishing rod (full durability, no enchantments).
     */
    public boolean replaceLootChestRods = false;

    /**
     * Compatibility mode for modded fishing rods.
     * When true, the mod hooks into ANY FishingHook entity (including those
     * spawned by modded rods that extend or use vanilla FishingHook).
     * This is already the default behavior since we mixin FishingHook directly.
     * This flag is informational / for future per-mod exclusions.
     */
    public boolean moddedRodCompat = true;

    /**
     * Allow modded items in the random item pool.
     * Only applies when itemPool is empty (full-registry random mode).
     * When false, only items from the "minecraft" namespace are eligible.
     * When true, items from all namespaces (including mods) are eligible.
     */
    public boolean allowModdedItems = true;

    /**
     * Allow modded entities in the random entity pool.
     * Only applies when entityPool is empty (full-registry random mode).
     * When false, only entities from the "minecraft" namespace are eligible.
     * When true, entities from all namespaces (including mods) are eligible.
     */
    public boolean allowModdedEntities = true;

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
    public int itemCountMax = 3;

    /**
     * Weighted item pool. Empty = use full registry.
     * Format: {"id": "minecraft:diamond", "weight": 5}
     * Optional: "minCount", "maxCount" per entry.
     */
    public List<ItemEntry> itemPool = new ArrayList<>();

    // -----------------------------------------------------------------------
    // Entity settings
    // -----------------------------------------------------------------------

    /**
     * Weighted entity pool. Empty = use full registry.
     * Format: {"id": "minecraft:creeper", "weight": 3}
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
        return FabricLoader.getInstance().getConfigDir().resolve("anythingbutfish.json");
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
        // Always clamp before saving so the file is always in a valid state,
        // and so the in-memory values are correct for the current session.
        clamp();
        Path path = configPath();
        try {
            path.getParent().toFile().mkdirs();
            try (Writer w = new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8)) {
                w.write("// AnythingButFish Configuration\n");
                w.write("// chanceItem + chanceEntity + chanceXp <= 100; remainder = 'one that got away'\n");
                w.write("// itemPool/entityPool: {\"id\":\"ns:name\",\"weight\":N} — empty = full registry\n");
                w.write("// Item entries also support: \"minCount\": N, \"maxCount\": N (-1 = use global)\n");
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
            chanceItem   = (int)(chanceItem  * scale);
            chanceEntity = (int)(chanceEntity * scale);
            chanceXp     = (int)(chanceXp    * scale);
        }
        itemCountMin = Math.max(1, itemCountMin);
        itemCountMax = Math.max(itemCountMin, itemCountMax);
        xpMin = Math.max(1, xpMin);
        xpMax = Math.max(xpMin, xpMax);
        flingSpeed = Math.max(0.01, flingSpeed);
        flingArc   = Math.max(0.0,  flingArc);

        if (itemPool == null) itemPool = new ArrayList<>();
        itemPool.removeIf(e -> e == null || e.id == null || e.id.isBlank());
        itemPool.forEach(e -> e.weight = Math.max(1, e.weight));

        if (entityPool == null) entityPool = new ArrayList<>();
        entityPool.removeIf(e -> e == null || e.id == null || e.id.isBlank());
        entityPool.forEach(e -> e.weight = Math.max(1, e.weight));
    }

    // -----------------------------------------------------------------------
    // Weighted random helpers
    // -----------------------------------------------------------------------

    public ItemEntry pickRandomItem(java.util.Random rng) {
        return pickWeighted(itemPool, rng);
    }

    public EntityEntry pickRandomEntity(java.util.Random rng) {
        return pickWeighted(entityPool, rng);
    }

    @SuppressWarnings("unchecked")
    private static <T> T pickWeighted(List<?> pool, java.util.Random rng) {
        if (pool == null || pool.isEmpty()) return null;
        int totalWeight = 0;
        for (Object e : pool) {
            if (e instanceof ItemEntry ie) totalWeight += ie.weight;
            else if (e instanceof EntityEntry ee) totalWeight += ee.weight;
        }
        if (totalWeight <= 0) return null;
        int roll = rng.nextInt(totalWeight);
        int cumulative = 0;
        for (Object e : pool) {
            int w = (e instanceof ItemEntry ie) ? ie.weight : ((EntityEntry) e).weight;
            cumulative += w;
            if (roll < cumulative) return (T) e;
        }
        return (T) pool.get(pool.size() - 1);
    }

    // -----------------------------------------------------------------------
    // Threshold helpers
    // -----------------------------------------------------------------------

    public int thresholdItem()   { return chanceItem; }
    public int thresholdEntity() { return chanceItem + chanceEntity; }
    public int thresholdXp()     { return chanceItem + chanceEntity + chanceXp; }
}
