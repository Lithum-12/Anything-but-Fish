package com.lithumc.mixin;

import com.lithumc.config.AbfConfig;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mixin targeting FishingHook to replace vanilla fishing loot.
 *
 * Pool modes:
 *   WHITELIST (isWhitelist=true)  – only listed items/entities can appear.
 *                                   Empty list = skip this loot type entirely.
 *   BLACKLIST (isWhitelist=false) – full registry minus listed items/entities.
 *                                   allowModded, allowAdmin, allowDangerous filters apply.
 *
 * Uses EntityType.create(Level) for maximum version compatibility (1.17+).
 * MC 1.19.2 port: uses Registry API (not BuiltInRegistries),
 *                  field access for level/isClientSide (not methods).
 */
@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("anythingbutfish");
    private static final Random RANDOM = new Random();

    private static final Set<String> ADMIN_ITEMS = Set.of(
            "minecraft:command_block", "minecraft:chain_command_block",
            "minecraft:repeating_command_block", "minecraft:command_block_minecart",
            "minecraft:structure_block", "minecraft:structure_void",
            "minecraft:jigsaw_block", "minecraft:barrier",
            "minecraft:debug_stick", "minecraft:written_book",
            "minecraft:knowledge_book", "minecraft:light"
    );

    private static final Set<String> DANGEROUS_MOBS = Set.of(
            "minecraft:ender_dragon", "minecraft:wither"
    );

    @Unique private boolean abf$wasInWater    = false;
    @Unique private int     abf$retrieveResult = 0;

    // -----------------------------------------------------------------------
    // Registry helpers (MC 1.19.x uses Registry, not BuiltInRegistries)
    // -----------------------------------------------------------------------
    private static Optional<Item> resolveItem(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return Registry.ITEM.getOptional(new ResourceLocation(id));
    }

    private static Optional<EntityType<?>> resolveEntityType(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return Registry.ENTITY_TYPE.getOptional(new ResourceLocation(id));
    }

    // -----------------------------------------------------------------------
    // Inject HEAD: snapshot in-water state
    // -----------------------------------------------------------------------
    @Inject(method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"))
    private void abf$beforeRetrieve(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        FishingHook self = (FishingHook)(Object) this;
        abf$wasInWater = !self.level.isClientSide && self.isInWater();   // field access
    }

    @Inject(method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I", at = @At("RETURN"))
    private void abf$captureResult(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        abf$retrieveResult = cir.getReturnValue();
    }

    // -----------------------------------------------------------------------
    // Inject TAIL: replace vanilla loot
    // -----------------------------------------------------------------------
    @Inject(method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I", at = @At("TAIL"))
    private void abf$afterRetrieve(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        if (!abf$wasInWater) return;
        abf$wasInWater = false;

        AbfConfig cfg = AbfConfig.get();
        if (!cfg.enabled) return;

        if (cfg.waitForBite && abf$retrieveResult <= 0) {
            if (cfg.debugMode) LOGGER.info("[AnythingButFish] No bite – skipping loot.");
            return;
        }

        FishingHook self  = (FishingHook)(Object) this;
        Level level = self.level;                                       // field access
        if (level.isClientSide) return;                                 // field access

        Player owner = self.getPlayerOwner();
        if (!(owner instanceof ServerPlayer serverPlayer)) return;
        ServerLevel serverLevel = (ServerLevel) level;

        double bx = self.getX(), by = self.getY(), bz = self.getZ();
        serverLevel.getEntitiesOfClass(ItemEntity.class,
                self.getBoundingBox().inflate(2.0)).forEach(Entity::discard);

        level.playSound(null, bx, by, bz,
                SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL,
                1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (cfg.debugMode)
            LOGGER.info("[AnythingButFish] {} reeled in – generating random loot!",
                    serverPlayer.getName().getString());

        spawnRandomLoot(self, serverLevel, serverPlayer, cfg);
    }

    // -----------------------------------------------------------------------
    // Loot dispatch
    // -----------------------------------------------------------------------
    private static void spawnRandomLoot(FishingHook hook, ServerLevel level,
                                        ServerPlayer player, AbfConfig cfg) {
        int total = cfg.thresholdXp();
        if (total <= 0) {
            if (cfg.debugMode) LOGGER.info("[AnythingButFish] All chances are 0 – no loot.");
            return;
        }

        int roll = RANDOM.nextInt(100);
        if      (roll < cfg.thresholdItem())   spawnItem(hook, level, player, cfg);
        else if (roll < cfg.thresholdEntity()) spawnEntity(hook, level, player, cfg);
        else if (roll < cfg.thresholdXp())     spawnXp(hook, level, player, cfg);
        else {
            level.playSound(null, hook.getX(), hook.getY(), hook.getZ(),
                    SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.NEUTRAL, 0.25F, 1.0F);
            if (cfg.debugMode) {
                LOGGER.info("[AnythingButFish] The one that got away...");
                player.sendSystemMessage(Component.literal("[ABF] The one that got away..."));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Item drop
    // -----------------------------------------------------------------------
    private static void spawnItem(FishingHook hook, ServerLevel level,
                                  ServerPlayer player, AbfConfig cfg) {
        Item chosen;
        int  minCount, maxCount;

        if (cfg.itemPoolIsWhitelist) {
            if (cfg.itemPool.isEmpty()) {
                if (cfg.debugMode)
                    LOGGER.info("[AnythingButFish] Item whitelist is empty – skipping item drop.");
                return;
            }
            AbfConfig.ItemEntry entry = cfg.itemPool.get(RANDOM.nextInt(cfg.itemPool.size()));
            Optional<Item> opt = resolveItem(entry.id);
            if (opt.isEmpty()) {
                LOGGER.warn("[AnythingButFish] Unknown item id '{}' in whitelist – skipping.", entry.id);
                return;
            }
            chosen   = opt.get();
            minCount = entry.minCount >= 1 ? entry.minCount : cfg.itemCountMin;
            maxCount = entry.maxCount >= minCount ? entry.maxCount : cfg.itemCountMax;
        } else {
            Set<String> excluded = cfg.itemPool.stream()
                    .map(e -> e.id).collect(Collectors.toSet());
            List<Item> pool = Registry.ITEM.stream()
                    .filter(item -> {
                        String key = Registry.ITEM.getKey(item).toString();
                        String ns  = Registry.ITEM.getKey(item).getNamespace();
                        if (excluded.contains(key)) return false;
                        if (!cfg.allowModdedItems && !"minecraft".equals(ns)) return false;
                        if (!cfg.allowAdminItems  && ADMIN_ITEMS.contains(key)) return false;
                        return true;
                    })
                    .collect(Collectors.toList());
            if (pool.isEmpty()) return;
            chosen   = pool.get(RANDOM.nextInt(pool.size()));
            minCount = cfg.itemCountMin;
            maxCount = cfg.itemCountMax;
        }

        int range = maxCount - minCount + 1;
        int count = minCount + (range > 0 ? RANDOM.nextInt(range) : 0);
        ItemStack stack = new ItemStack(chosen, count);

        double x = hook.getX(), y = hook.getY(), z = hook.getZ();
        ItemEntity ie = new ItemEntity(level, x, y, z, stack);
        applyArc(ie, x, y, z, player, cfg);
        level.addFreshEntity(ie);

        if (cfg.debugMode) {
            String msg = "[ABF] Item: " + Registry.ITEM.getKey(chosen) + " x" + count;
            LOGGER.info("[AnythingButFish] {}", msg);
            player.sendSystemMessage(Component.literal(msg));
        }
    }

    // -----------------------------------------------------------------------
    // Entity spawn
    // -----------------------------------------------------------------------
    private static void spawnEntity(FishingHook hook, ServerLevel level,
                                    ServerPlayer player, AbfConfig cfg) {
        EntityType<?> chosen;

        if (cfg.entityPoolIsWhitelist) {
            if (cfg.entityPool.isEmpty()) {
                if (cfg.debugMode)
                    LOGGER.info("[AnythingButFish] Entity whitelist is empty – skipping entity spawn.");
                return;
            }
            AbfConfig.EntityEntry entry = cfg.entityPool.get(RANDOM.nextInt(cfg.entityPool.size()));
            Optional<EntityType<?>> opt = resolveEntityType(entry.id);
            if (opt.isEmpty()) {
                LOGGER.warn("[AnythingButFish] Unknown entity id '{}' in whitelist – skipping.", entry.id);
                return;
            }
            chosen = opt.get();
        } else {
            Set<String> excluded = cfg.entityPool.stream()
                    .map(e -> e.id).collect(Collectors.toSet());
            List<EntityType<?>> pool = Registry.ENTITY_TYPE.stream()
                    .filter(et -> et != EntityType.PLAYER && et != EntityType.FISHING_BOBBER)
                    .filter(et -> {
                        String key = Registry.ENTITY_TYPE.getKey(et).toString();
                        String ns  = Registry.ENTITY_TYPE.getKey(et).getNamespace();
                        if (excluded.contains(key)) return false;
                        if (!cfg.allowModdedEntities && !"minecraft".equals(ns)) return false;
                        if (!cfg.allowDangerousMobs  && DANGEROUS_MOBS.contains(key)) return false;
                        return true;
                    })
                    .collect(Collectors.toList());
            if (pool.isEmpty()) return;
            chosen = pool.get(RANDOM.nextInt(pool.size()));
        }

        trySpawn(chosen, hook, level, player, cfg);
    }

    /**
     * Uses EntityType.create(Level) – available in all target versions (1.17+).
     */
    private static <T extends Entity> void trySpawn(EntityType<T> type, FishingHook hook,
                                                    ServerLevel level, ServerPlayer player,
                                                    AbfConfig cfg) {
        try {
            T entity = type.create(level);
            if (entity != null) {
                double x = hook.getX(), y = hook.getY(), z = hook.getZ();
                entity.setPos(x, y, z);
                entity.setYRot(RANDOM.nextFloat() * 360F);
                applyArc(entity, x, y, z, player, cfg);
                level.addFreshEntity(entity);
                if (cfg.debugMode) {
                    String msg = "[ABF] Entity: " + Registry.ENTITY_TYPE.getKey(type);
                    LOGGER.info("[AnythingButFish] {}", msg);
                    player.sendSystemMessage(Component.literal(msg));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[AnythingButFish] Failed to spawn {}: {}", type, e.getMessage());
            try {
                var pig = EntityType.PIG.create(level);
                if (pig != null) {
                    pig.setPos(hook.getX(), hook.getY(), hook.getZ());
                    applyArc(pig, hook.getX(), hook.getY(), hook.getZ(), player, cfg);
                    level.addFreshEntity(pig);
                }
            } catch (Exception ignored) {}
        }
    }

    // -----------------------------------------------------------------------
    // XP orbs
    // -----------------------------------------------------------------------
    private static void spawnXp(FishingHook hook, ServerLevel level,
                                ServerPlayer player, AbfConfig cfg) {
        int range = cfg.xpMax - cfg.xpMin + 1;
        int xp    = cfg.xpMin + (range > 0 ? RANDOM.nextInt(range) : 0);
        double x  = hook.getX(), y = hook.getY(), z = hook.getZ();

        ExperienceOrb orb = new ExperienceOrb(level, x, y, z, xp);
        applyArc(orb, x, y, z, player, cfg);
        level.addFreshEntity(orb);

        if (cfg.debugMode) {
            String msg = "[ABF] XP: " + xp;
            LOGGER.info("[AnythingButFish] {}", msg);
            player.sendSystemMessage(Component.literal(msg));
        }
    }

    // -----------------------------------------------------------------------
    // Arc velocity
    // -----------------------------------------------------------------------
    private static void applyArc(Entity entity, double x, double y, double z,
                                 ServerPlayer player, AbfConfig cfg) {
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double dz = player.getZ() - z;
        entity.setDeltaMovement(
                dx * cfg.flingSpeed,
                dy * cfg.flingSpeed + Math.sqrt(Math.sqrt(dx*dx + dy*dy + dz*dz)) * cfg.flingArc,
                dz * cfg.flingSpeed
        );
    }
}