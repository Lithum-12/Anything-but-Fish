package com.lithumc.mixin;

import com.lithumc.config.AbfConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MobSpawnType;
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

/**
 * Mixin targeting FishingHook to replace vanilla fishing loot.
 *
 * Modded rod compatibility: since all fishing hooks (vanilla and modded)
 * use the same net.minecraft.world.entity.projectile.FishingHook class,
 * this mixin automatically covers modded rods that use the vanilla hook entity.
 * The moddedRodCompat config flag is informational.
 *
 * MC 1.21.1 port: uses MobSpawnType instead of EntitySpawnReason.
 */
@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("anythingbutfish");
    private static final Random RANDOM = new Random();

    // Admin/gamemode items that are restricted by default
    private static final java.util.Set<String> ADMIN_ITEMS = java.util.Set.of(
            "minecraft:command_block",
            "minecraft:chain_command_block",
            "minecraft:repeating_command_block",
            "minecraft:command_block_minecart",
            "minecraft:structure_block",
            "minecraft:structure_void",
            "minecraft:jigsaw_block",
            "minecraft:barrier",
            "minecraft:debug_stick",
            "minecraft:written_book",
            "minecraft:knowledge_book",
            "minecraft:light"
    );

    // Dangerous mobs that are restricted by default
    private static final java.util.Set<String> DANGEROUS_MOBS = java.util.Set.of(
            "minecraft:ender_dragon",
            "minecraft:wither"
    );

    @Unique
    private boolean abf$wasInWater = false;

    // -----------------------------------------------------------------------
    // Registry lookup helpers
    // -----------------------------------------------------------------------

    private static Optional<Item> resolveItem(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> BuiltInRegistries.ITEM.getKey(item).toString().equals(id))
                .findFirst();
    }

    private static Optional<EntityType<?>> resolveEntityType(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(et -> BuiltInRegistries.ENTITY_TYPE.getKey(et).toString().equals(id))
                .findFirst();
    }

    // -----------------------------------------------------------------------
    // HEAD: snapshot in-water state before vanilla runs
    // -----------------------------------------------------------------------

    @Inject(method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"))
    private void abf$beforeRetrieve(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        FishingHook self = (FishingHook)(Object) this;
        abf$wasInWater = !self.level().isClientSide() && self.isInWater();
    }

    // -----------------------------------------------------------------------
    // TAIL: replace loot after vanilla has run
    // -----------------------------------------------------------------------

    @Inject(method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I", at = @At("TAIL"))
    private void abf$afterRetrieve(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        if (!abf$wasInWater) return;
        abf$wasInWater = false;

        AbfConfig cfg = AbfConfig.get();
        if (!cfg.enabled) return;   // master switch

        FishingHook self = (FishingHook)(Object) this;
        Level level = self.level();
        if (level.isClientSide()) return;

        Player owner = self.getPlayerOwner();
        if (!(owner instanceof ServerPlayer serverPlayer)) return;

        ServerLevel serverLevel = (ServerLevel) level;

        // Remove vanilla-spawned ItemEntities near the bobber
        double bx = self.getX(), by = self.getY(), bz = self.getZ();
        serverLevel.getEntitiesOfClass(ItemEntity.class,
                self.getBoundingBox().inflate(2.0))
                .forEach(Entity::discard);

        // Play retrieve sound
        level.playSound(null, bx, by, bz,
                SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL,
                1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (cfg.debugMode)
            LOGGER.info("[AnythingButFish] {} reeled in - generating random loot!", serverPlayer.getName().getString());

        spawnRandomLoot(self, serverLevel, serverPlayer, cfg);
    }

    // -----------------------------------------------------------------------
    // Loot dispatch
    // -----------------------------------------------------------------------

    private static void spawnRandomLoot(FishingHook hook, ServerLevel level,
                                         ServerPlayer player, AbfConfig cfg) {
        // cfg.thresholdXp() = chanceItem + chanceEntity + chanceXp (already clamped to 0-100 by AbfConfig.save/load)
        // If all three chances are 0, total = 0 → nothing happens (silent, no sound).
        int total = cfg.thresholdXp();
        if (total <= 0) {
            // All chances are 0: nothing to do, no loot, no sound.
            if (cfg.debugMode) {
                LOGGER.info("[AnythingButFish] All chances are 0 - no loot generated.");
                player.sendSystemMessage(Component.literal("[ABF] All chances are 0 - no loot."));
            }
            return;
        }

        // Roll in range [0, total) so the probability distribution is always correct
        // regardless of whether total < 100 (remainder = "got away") or total == 100 (no got-away).
        int roll = RANDOM.nextInt(total + (100 - total)); // equivalent to nextInt(100), but explicit

        if (roll < cfg.thresholdItem()) {
            spawnItem(hook, level, player, cfg);
        } else if (roll < cfg.thresholdEntity()) {
            spawnEntity(hook, level, player, cfg);
        } else if (roll < cfg.thresholdXp()) {
            spawnExperienceOrbs(hook, level, player, cfg);
        } else {
            // "The one that got away" — roll fell in the [total, 100) gap
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
        AbfConfig.ItemEntry entry = cfg.pickRandomItem(RANDOM);
        Item chosen;
        int minCount, maxCount;

        if (entry != null) {
            Optional<Item> opt = resolveItem(entry.id);
            if (opt.isEmpty()) {
                LOGGER.warn("[AnythingButFish] Unknown item id '{}', skipping", entry.id);
                return;
            }
            chosen = opt.get();
            minCount = entry.minCount >= 1 ? entry.minCount : cfg.itemCountMin;
            maxCount = entry.maxCount >= minCount ? entry.maxCount : cfg.itemCountMax;
        } else {
            // Full-registry random mode: filter by allowModdedItems and allowAdminItems
            List<Item> allItems = BuiltInRegistries.ITEM.stream()
                    .filter(item -> {
                        // First check namespace (modded items)
                        if (!cfg.allowModdedItems && !"minecraft".equals(BuiltInRegistries.ITEM.getKey(item).getNamespace())) {
                            return false;
                        }
                        // Then check admin items (only when allowAdminItems is false)
                        if (!cfg.allowAdminItems) {
                            String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
                            if (ADMIN_ITEMS.contains(itemId)) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .toList();
            if (allItems.isEmpty()) return;
            chosen = allItems.get(RANDOM.nextInt(allItems.size()));
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
            String msg = "[ABF] Item: " + BuiltInRegistries.ITEM.getKey(chosen) + " x" + count;
            LOGGER.info("[AnythingButFish] {}", msg);
            player.sendSystemMessage(Component.literal(msg));
        }
    }

    // -----------------------------------------------------------------------
    // Entity spawn
    // -----------------------------------------------------------------------

    private static void spawnEntity(FishingHook hook, ServerLevel level,
                                     ServerPlayer player, AbfConfig cfg) {
        AbfConfig.EntityEntry entry = cfg.pickRandomEntity(RANDOM);

        if (entry != null) {
            Optional<EntityType<?>> opt = resolveEntityType(entry.id);
            if (opt.isEmpty()) {
                LOGGER.warn("[AnythingButFish] Unknown entity id '{}', skipping", entry.id);
                return;
            }
            trySpawn(opt.get(), hook, level, player, cfg);
        } else {
            // Full-registry random mode: filter by allowModdedEntities and allowDangerousMobs
            List<EntityType<?>> safeTypes = BuiltInRegistries.ENTITY_TYPE.stream()
                    .filter(et -> et != EntityType.PLAYER && et != EntityType.FISHING_BOBBER)
                    .filter(et -> {
                        // First check namespace (modded entities)
                        if (!cfg.allowModdedEntities && !"minecraft".equals(BuiltInRegistries.ENTITY_TYPE.getKey(et).getNamespace())) {
                            return false;
                        }
                        // Then check dangerous mobs (only when allowDangerousMobs is false)
                        if (!cfg.allowDangerousMobs) {
                            String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(et).toString();
                            if (DANGEROUS_MOBS.contains(entityId)) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .toList();
            if (safeTypes.isEmpty()) return;
            trySpawn(safeTypes.get(RANDOM.nextInt(safeTypes.size())), hook, level, player, cfg);
        }
    }

    private static <T extends Entity> void trySpawn(EntityType<T> type, FishingHook hook,
                                                     ServerLevel level, ServerPlayer player,
                                                     AbfConfig cfg) {
        try {
            // MC 1.20.4: EntityType.create requires 7 parameters
            T entity = type.create(level, null, null, hook.blockPosition(), MobSpawnType.COMMAND, false, false);
            if (entity != null) {
                double x = hook.getX(), y = hook.getY(), z = hook.getZ();
                entity.setPos(x, y, z);
                entity.setYRot(RANDOM.nextFloat() * 360F);
                applyArc(entity, x, y, z, player, cfg);
                level.addFreshEntity(entity);
                if (cfg.debugMode) {
                    String msg = "[ABF] Entity: " + BuiltInRegistries.ENTITY_TYPE.getKey(type);
                    LOGGER.info("[AnythingButFish] {}", msg);
                    player.sendSystemMessage(Component.literal(msg));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[AnythingButFish] Failed to spawn {}: {}", type, e.getMessage());
            try {
                var pig = EntityType.PIG.create(level, null, null, hook.blockPosition(), MobSpawnType.COMMAND, false, false);
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

    private static void spawnExperienceOrbs(FishingHook hook, ServerLevel level,
                                             ServerPlayer player, AbfConfig cfg) {
        int range = cfg.xpMax - cfg.xpMin + 1;
        int xp = cfg.xpMin + (range > 0 ? RANDOM.nextInt(range) : 0);
        double x = hook.getX(), y = hook.getY(), z = hook.getZ();
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
    // Arc velocity helper
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
