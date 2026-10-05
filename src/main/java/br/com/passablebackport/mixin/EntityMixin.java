package br.com.passablebackport.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import br.com.passablebackport.PassableBackport;

@Mixin(Entity.class)
public class EntityMixin {

    @WrapOperation(
        method = "isEntityInsideOpaqueBlock",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;"),
        require = 1)
    private Block passablebackport$excludeLeafFromSuffocation(World world, int x, int y, int z,
        Operation<Block> original) {
        Block block = original.call(world, x, y, z);
        return PassableBackport.isPassableLeaf(block, world, x, y, z) ? Blocks.air : block;
    }

    @WrapOperation(
        method = "func_145771_j",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;func_147469_q(III)Z"),
        require = 7)
    private boolean passablebackport$excludeLeafFromPushOut(World world, int x, int y, int z,
        Operation<Boolean> original) {
        return !PassableBackport.isPassableLeaf(world.getBlock(x, y, z), world, x, y, z)
            && original.call(world, x, y, z);
    }
}
