package br.dev.brunoxkk0.leafpassage.mixin;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import br.dev.brunoxkk0.leafpassage.LeafPassage;

@Mixin(PathFinder.class)
public class PathFinderMixin {

    @WrapOperation(
        method = "func_82565_a",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;"),
        require = 4)
    private static Block leafpassage$excludeLeafFromPath(World world, int x, int y, int z, Operation<Block> original) {
        Block block = original.call(world, x, y, z);
        return LeafPassage.isPassableLeaf(block, world, x, y, z) ? Blocks.air : block;
    }
}
