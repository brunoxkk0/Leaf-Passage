package br.dev.brunoxkk0.passablebackport.mixin;

import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import br.dev.brunoxkk0.passablebackport.PassableBackport;

@Mixin(EntityPlayerSP.class)
public class EntityPlayerSPMixin {

    @WrapOperation(
        method = "isBlockTranslucent",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;"),
        require = 1)
    private Block passablebackport$excludeLeafFromPushOut(World world, int x, int y, int z, Operation<Block> original) {
        Block block = original.call(world, x, y, z);
        return PassableBackport.isPassableLeaf(block, world, x, y, z) ? Blocks.air : block;
    }
}
