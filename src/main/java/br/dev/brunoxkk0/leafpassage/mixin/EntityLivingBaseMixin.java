package br.dev.brunoxkk0.leafpassage.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import br.dev.brunoxkk0.leafpassage.LeafPassage;

@Mixin(EntityLivingBase.class)
public class EntityLivingBaseMixin {

    @Inject(method = "canEntityBeSeen", at = @At("RETURN"), cancellable = true, require = 1)
    private void leafpassage$hidePlayerInLeaves(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && this instanceof IMob
            && target instanceof EntityPlayer
            && LeafPassage.isFullyHiddenInLeaves(target.worldObj, target.boundingBox)) {
            cir.setReturnValue(false);
        }
    }
}
