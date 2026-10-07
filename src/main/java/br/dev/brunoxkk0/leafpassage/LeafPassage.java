package br.dev.brunoxkk0.leafpassage;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import cpw.mods.fml.common.Mod;

@Mod(
    modid = "leafpassage",
    name = "Leaf Passage",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:unimixins@[0.3.1,)")
public class LeafPassage {

    public static boolean isPassableLeaf(Block block, World world, int x, int y, int z) {
        return block.getMaterial() == Material.leaves || block.isLeaves(world, x, y, z);
    }

    public static Block findLeaf(World world, AxisAlignedBB box) {
        for (int x = MathHelper.floor_double(box.minX + .001); x <= MathHelper.floor_double(box.maxX - .001); x++) {
            for (int y = Math.max(0, MathHelper.floor_double(box.minY + .001)); y
                <= Math.min(255, MathHelper.floor_double(box.maxY - .001)); y++) {
                for (int z = MathHelper.floor_double(box.minZ + .001); z
                    <= MathHelper.floor_double(box.maxZ - .001); z++) {
                    if (!world.blockExists(x, y, z)) continue;
                    Block block = world.getBlock(x, y, z);
                    if (isPassableLeaf(block, world, x, y, z)) return block;
                }
            }
        }
        return null;
    }

    public static boolean isFullyHiddenInLeaves(World world, AxisAlignedBB box) {
        for (int x = MathHelper.floor_double(box.minX + .001); x <= MathHelper.floor_double(box.maxX - .001); x++) {
            for (int y = MathHelper.floor_double(box.minY + .001); y <= MathHelper.floor_double(box.maxY - .001); y++) {
                for (int z = MathHelper.floor_double(box.minZ + .001); z
                    <= MathHelper.floor_double(box.maxZ - .001); z++) {
                    if (!world.blockExists(x, y, z) || !isPassableLeaf(world.getBlock(x, y, z), world, x, y, z)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
