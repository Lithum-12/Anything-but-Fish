package com.lithumc.mixin;

import com.lithumc.config.AbfConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC 1.16.5 Yarn: targets Item.use() because FishingRodItem inherits it.
 * Loom 0.10 refmap cannot resolve overridden method names on subclasses.
 */
@Mixin(Item.class)
public class FishingRodDurabilityMixin {

    @Inject(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;", at = @At("RETURN"))
    private void abf$preventDurabilityLoss(World world, PlayerEntity player, Hand hand,
                                           CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        // Only apply to fishing rods
        if (!((Object) this instanceof FishingRodItem)) return;
        if (!AbfConfig.get().infiniteDurability) return;
        if (world.isClient) return;

        ItemStack stack = player.getStackInHand(hand);
        if (stack.isDamageable() && stack.getDamage() > 0) {
            stack.setDamage(0);
        }
    }
}