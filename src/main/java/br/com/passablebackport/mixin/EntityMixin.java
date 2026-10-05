package br.com.passablebackport.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import br.com.passablebackport.PassableBackport;

@Mixin(Entity.class)
public class EntityMixin {

    @Unique
    private AxisAlignedBB passablebackport$movementStart;

    @Unique
    private int passablebackport$nextRustleTick;

    @Inject(method = "moveEntity", at = @At("HEAD"), require = 1)
    private void passablebackport$recordMovement(double x, double y, double z, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        passablebackport$movementStart = entity instanceof EntityPlayer && !entity.noClip ? entity.boundingBox.copy()
            : null;
    }

    @Inject(method = "moveEntity", at = @At("RETURN"), require = 1)
    private void passablebackport$rustleAndBrake(double x, double y, double z, CallbackInfo ci) {
        AxisAlignedBB start = passablebackport$movementStart;
        passablebackport$movementStart = null;
        if (start == null) return;
        EntityPlayer player = (EntityPlayer) (Object) this;
        double dx = player.boundingBox.minX - start.minX;
        double dy = player.boundingBox.minY - start.minY;
        double dz = player.boundingBox.minZ - start.minZ;
        if (dx * dx + dy * dy + dz * dz < .000001) return;

        // Vanilla resolves movement in Y, X, Z order; scan the traveled path, including fast falls.
        Block leaf = PassableBackport.findLeaf(player.worldObj, start.addCoord(0, dy, 0));
        if (leaf == null) leaf = PassableBackport.findLeaf(
            player.worldObj,
            start.getOffsetBoundingBox(0, dy, 0)
                .addCoord(dx, 0, 0));
        if (leaf == null) leaf = PassableBackport.findLeaf(
            player.worldObj,
            start.getOffsetBoundingBox(dx, dy, 0)
                .addCoord(0, 0, dz));
        if (leaf == null) return;

        if (dy < 0 && player.motionY < 0 && !player.capabilities.isFlying) player.motionY *= .5;
        if (!player.worldObj.isRemote && player.ticksExisted >= passablebackport$nextRustleTick) {
            Block.SoundType sound = leaf.stepSound;
            player.worldObj
                .playSoundAtEntity(player, sound.getStepResourcePath(), sound.getVolume() * .25F, sound.getPitch());
            passablebackport$nextRustleTick = player.ticksExisted + 5;
        }
    }

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
