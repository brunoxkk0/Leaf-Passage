package br.dev.brunoxkk0.passablebackport.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.EntitySenses;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import br.dev.brunoxkk0.passablebackport.PassableBackport;

@Mixin(EntityAITarget.class)
public class EntityAITargetMixin {

    @Shadow
    protected EntityCreature taskOwner;

    @WrapOperation(
        method = "continueExecuting",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/EntitySenses;canSee(Lnet/minecraft/entity/Entity;)Z"),
        require = 1)
    private boolean passablebackport$retainHiddenTarget(EntitySenses senses, Entity target,
        Operation<Boolean> original) {
        return original.call(senses, target) || taskOwner instanceof IMob && target instanceof EntityPlayer
            && taskOwner.getAttackTarget() == target
            && PassableBackport.isFullyHiddenInLeaves(target.worldObj, target.boundingBox);
    }
}
