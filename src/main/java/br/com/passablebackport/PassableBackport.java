package br.com.passablebackport;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import cpw.mods.fml.common.Mod;

@Mod(
    modid = "passablebackport",
    name = "Passable Backport",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:unimixins@[0.3.1,)")
public class PassableBackport {

    public static boolean isPassableLeaf(Block block, World world, int x, int y, int z) {
        return block.getMaterial() == Material.leaves || block.isLeaves(world, x, y, z);
    }
}
