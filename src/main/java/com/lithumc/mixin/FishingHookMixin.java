package com.lithumc.mixin;

import com.lithumc.config.AbfConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mixin targeting FishingBobberEntity to replace vanilla fishing loot.
 * MC 1.16.5 Yarn mappings. Java 8 compatible.
 */
@Mixin(FishingBobberEntity.class)
public abstract class FishingHookMixin {

    private static final Logger LOGGER = LogManager.getLogger("anythingbutfish");
    private static final Random RANDOM = new Random();

    private static final Set<String> ADMIN_ITEMS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "minecraft:command_block", "minecraft:chain_command_block",
            "minecraft:repeating_command_block", "minecraft:command_block_minecart",
            "minecraft:structure_block", "minecraft:structure_void",
            "minecraft:jigsaw_block", "minecraft:barrier",
            "minecraft:debug_stick", "minecraft:written_book",
            "minecraft:knowledge_book", "minecraft:light"
    )));

    private static final Set<String> DANGEROUS_MOBS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "minecraft:ender_dragon", "minecraft:wither"
    )));

    @Unique private boolean abf$wasInWater    = false;
    @Unique private int     abf$retrieveResult = 0;

    // -----------------------------------------------------------------------
    // Registry helpers
    // -----------------------------------------------------------------------

    private static Optional<Item> resolveItem(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();
        return Registry.ITEM.getOrEmpty(new Identifier(id));
    }

    private static Optional<EntityType<?>> resolveEntityType(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();
        return Registry.ENTITY_TYPE.getOrEmpty(new Identifier(id));
    }

    // -----------------------------------------------------------------------
    // HEAD: snapshot in-water state before vanilla runs (Yarn: use(ItemStack))
    // -----------------------------------------------------------------------

    @Inject(method = "use(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"))
    private void abf$beforeRetrieve(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        FishingBobberEntity self = (FishingBobberEntity) (Object) this;
        abf$wasInWater = !self.world.isClient && self.isTouchingWater();
    }

    @Inject(method = "use(Lnet/minecraft/item/ItemStack;)I", at = @At("RETURN"))
    private void abf$captureResult(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        abf$retrieveResult = cir.getReturnValue();
    }

    // -----------------------------------------------------------------------
    // TAIL: replace loot after vanilla has run
    // -----------------------------------------------------------------------

    @Inject(method = "use(Lnet/minecraft/item/ItemStack;)I", at = @At("TAIL"))
    private void abf$afterRetrieve(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        if (!abf$wasInWater) return;
        abf$wasInWater = false;

        AbfConfig cfg = AbfConfig.get();
        if (!cfg.enabled) return;

        if (cfg.waitForBite && abf$retrieveResult <= 0) {
            if (cfg.debugMode) LOGGER.info("[AnythingButFish] No bite – skipping loot.");
            return;
        }

        FishingBobberEntity self = (FishingBobberEntity) (Object) this;
        World world = self.world;
        if (world.isClient) return;

        PlayerEntity owner = self.getPlayerOwner();
        if (!(owner instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) owner;

        ServerWorld serverWorld = (ServerWorld) world;

        double bx = self.getX(), by = self.getY(), bz = self.getZ();

        // Remove vanilla item drops – Yarn: getEntitiesByClass(Class, Box, Predicate)
        serverWorld.getEntitiesByClass(ItemEntity.class,
                self.getBoundingBox().expand(2.0), null).forEach(Entity::remove);

        world.playSound(null, bx, by, bz,
                SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL,
                1.0F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

        if (cfg.debugMode)
            LOGGER.info("[AnythingButFish] {} reeled in – generating random loot!",
                    serverPlayer.getName().getString());

        spawnRandomLoot(self, serverWorld, serverPlayer, cfg);
    }

    // -----------------------------------------------------------------------
    // Loot dispatch
    // -----------------------------------------------------------------------

    private static void spawnRandomLoot(FishingBobberEntity hook, ServerWorld world,
                                        ServerPlayerEntity player, AbfConfig cfg) {
        int total = cfg.thresholdXp();
        if (total <= 0) {
            if (cfg.debugMode) LOGGER.info("[AnythingButFish] All chances are 0 – no loot.");
            return;
        }

        int roll = RANDOM.nextInt(100);

        if      (roll < cfg.thresholdItem())   spawnItem(hook, world, player, cfg);
        else if (roll < cfg.thresholdEntity()) spawnEntity(hook, world, player, cfg);
        else if (roll < cfg.thresholdXp())     spawnXp(hook, world, player, cfg);
        else {
            world.playSound(null, hook.getX(), hook.getY(), hook.getZ(),
                    SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, SoundCategory.NEUTRAL, 0.25F, 1.0F);
            if (cfg.debugMode) {
                LOGGER.info("[AnythingButFish] The one that got away...");
                player.sendMessage(new LiteralText("[ABF] The one that got away..."), false);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Item drop
    // -----------------------------------------------------------------------

    private static void spawnItem(FishingBobberEntity hook, ServerWorld world,
                                  ServerPlayerEntity player, AbfConfig cfg) {
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
            if (!opt.isPresent()) {
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
                        String key = Registry.ITEM.getId(item).toString();
                        String ns  = Registry.ITEM.getId(item).getNamespace();
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

        ItemEntity ie = new ItemEntity(world, x, y, z, stack);
        applyArc(ie, x, y, z, player, cfg);
        world.spawnEntity(ie);

        if (cfg.debugMode) {
            String msg = "[ABF] Item: " + Registry.ITEM.getId(chosen) + " x" + count;
            LOGGER.info("[AnythingButFish] {}", msg);
            player.sendMessage(new LiteralText(msg), false);
        }
    }

    // -----------------------------------------------------------------------
    // Entity spawn
    // -----------------------------------------------------------------------

    private static void spawnEntity(FishingBobberEntity hook, ServerWorld world,
                                    ServerPlayerEntity player, AbfConfig cfg) {
        EntityType<?> chosen;

        if (cfg.entityPoolIsWhitelist) {
            if (cfg.entityPool.isEmpty()) {
                if (cfg.debugMode)
                    LOGGER.info("[AnythingButFish] Entity whitelist is empty – skipping entity spawn.");
                return;
            }
            AbfConfig.EntityEntry entry = cfg.entityPool.get(RANDOM.nextInt(cfg.entityPool.size()));
            Optional<EntityType<?>> opt = resolveEntityType(entry.id);
            if (!opt.isPresent()) {
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
                        String key = Registry.ENTITY_TYPE.getId(et).toString();
                        String ns  = Registry.ENTITY_TYPE.getId(et).getNamespace();
                        if (excluded.contains(key)) return false;
                        if (!cfg.allowModdedEntities && !"minecraft".equals(ns)) return false;
                        if (!cfg.allowDangerousMobs  && DANGEROUS_MOBS.contains(key)) return false;
                        return true;
                    })
                    .collect(Collectors.toList());

            if (pool.isEmpty()) return;
            chosen = pool.get(RANDOM.nextInt(pool.size()));
        }

        trySpawn(chosen, hook, world, player, cfg);
    }

    private static <T extends Entity> void trySpawn(EntityType<T> type, FishingBobberEntity hook,
                                                    ServerWorld world, ServerPlayerEntity player,
                                                    AbfConfig cfg) {
        try {
            T entity = type.create(world);
            if (entity != null) {
                double x = hook.getX(), y = hook.getY(), z = hook.getZ();
                entity.refreshPositionAndAngles(x, y, z, RANDOM.nextFloat() * 360F, 0.0F);
                applyArc(entity, x, y, z, player, cfg);
                world.spawnEntity(entity);

                if (cfg.debugMode) {
                    String msg = "[ABF] Entity: " + Registry.ENTITY_TYPE.getId(type);
                    LOGGER.info("[AnythingButFish] {}", msg);
                    player.sendMessage(new LiteralText(msg), false);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[AnythingButFish] Failed to spawn {}: {}", type, e.getMessage());
            try {
                Entity pig = EntityType.PIG.create(world);
                if (pig != null) {
                    pig.refreshPositionAndAngles(hook.getX(), hook.getY(), hook.getZ(), 0.0F, 0.0F);
                    applyArc(pig, hook.getX(), hook.getY(), hook.getZ(), player, cfg);
                    world.spawnEntity(pig);
                }
            } catch (Exception ignored) {}
        }
    }

    // -----------------------------------------------------------------------
    // XP orbs
    // -----------------------------------------------------------------------

    private static void spawnXp(FishingBobberEntity hook, ServerWorld world,
                                ServerPlayerEntity player, AbfConfig cfg) {
        int range = cfg.xpMax - cfg.xpMin + 1;
        int xp    = cfg.xpMin + (range > 0 ? RANDOM.nextInt(range) : 0);

        double x  = hook.getX(), y = hook.getY(), z = hook.getZ();

        ExperienceOrbEntity orb = new ExperienceOrbEntity(world, x, y, z, xp);
        applyArc(orb, x, y, z, player, cfg);
        world.spawnEntity(orb);

        if (cfg.debugMode) {
            String msg = "[ABF] XP: " + xp;
            LOGGER.info("[AnythingButFish] {}", msg);
            player.sendMessage(new LiteralText(msg), false);
        }
    }

    // -----------------------------------------------------------------------
    // Arc velocity
    // -----------------------------------------------------------------------

    private static void applyArc(Entity entity, double x, double y, double z,
                                 ServerPlayerEntity player, AbfConfig cfg) {
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double dz = player.getZ() - z;

        entity.setVelocity(
                dx * cfg.flingSpeed,
                dy * cfg.flingSpeed + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * cfg.flingArc,
                dz * cfg.flingSpeed
        );
    }
}