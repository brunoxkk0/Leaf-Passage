package br.dev.brunoxkk0.passablebackport.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntitySenses;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import br.dev.brunoxkk0.passablebackport.PassableBackport;

@Mixin(EntityAIAttackOnCollide.class)
public class EntityAIAttackOnCollideMixin {

    @Shadow
    EntityCreature attacker;

    @WrapOperation(
        method = "updateTask",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/EntitySenses;canSee(Lnet/minecraft/entity/Entity;)Z"),
        require = 1)
    private boolean passablebackport$keepChasingHiddenTarget(EntitySenses senses, Entity target,
        Operation<Boolean> original) {
        return original.call(senses, target) || attacker instanceof IMob && target instanceof EntityPlayer
            && attacker.getAttackTarget() == target
            && PassableBackport.findLeaf(target.worldObj, target.boundingBox) != null;
    }
}
