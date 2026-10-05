package br.com.passablebackport.mixin;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import br.com.passablebackport.PassableBackport;

@Mixin(World.class)
public class WorldMixin {

    @WrapWithCondition(
        method = { "getCollidingBoundingBoxes", "func_147461_a" },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;addCollisionBoxesToList(Lnet/minecraft/world/World;IIILnet/minecraft/util/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;)V"),
        require = 2)
    private boolean passablebackport$keepCollision(Block block, World world, int x, int y, int z, AxisAlignedBB mask,
        List<AxisAlignedBB> collisions, Entity entity) {
        return !PassableBackport.isPassableLeaf(block, world, x, y, z);
    }
}
