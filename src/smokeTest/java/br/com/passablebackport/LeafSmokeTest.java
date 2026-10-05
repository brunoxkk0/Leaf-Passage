package br.com.passablebackport;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "passable_smoketest", name = "Passable smoke test", version = "1")
public class LeafSmokeTest extends CommandBase {

    private static final Logger LOG = LogManager.getLogger("LeafSmokeTest");
    private static final Block FORGE_LEAF = new Block(Material.wood) {

        @Override
        public boolean isLeaves(IBlockAccess world, int x, int y, int z) {
            return true;
        }

        @Override
        public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask,
            List<AxisAlignedBB> boxes, Entity entity) {
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1);
            if (box.intersectsWith(mask)) boxes.add(box);
        }
    };
    private static final Block MATERIAL_LEAF = new Block(Material.leaves) {

        @Override
        public boolean isLeaves(IBlockAccess world, int x, int y, int z) {
            return false;
        }
    };
    private int clientTicks;
    private boolean launched;
    private volatile Boolean clientResult;
    private volatile Boolean serverResult;
    private int serverTicks;
    private int exitTicks;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerBlock(FORGE_LEAF, "forge_leaf");
        GameRegistry.registerBlock(MATERIAL_LEAF, "material_leaf");
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(this);
    }

    @Override
    public String getCommandName() {
        return "passabletest";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/passabletest";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        check(getCommandSenderAsPlayer(sender));
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (!launched && mc.currentScreen instanceof GuiMainMenu) {
            launched = true;
            mc.gameSettings.pauseOnLostFocus = false;
            WorldSettings settings = new WorldSettings(
                42,
                WorldSettings.GameType.CREATIVE,
                false,
                false,
                WorldType.FLAT).enableCommands();
            mc.launchIntegratedServer("leaf-smoke-" + System.currentTimeMillis(), "Passable Backport test", settings);
        }
        if (mc.thePlayer != null && mc.currentScreen == null
            && clientResult == null
            && mc.theWorld.blockExists((int) Math.floor(mc.thePlayer.posX), 64, (int) Math.floor(mc.thePlayer.posZ))
            && ++clientTicks > 100) {
            clientResult = check(mc.thePlayer);
        }
        if (clientResult != null && serverResult != null && ++exitTicks > 60) {
            LOG.info("SMOKE RESULT client={} server={}", clientResult, serverResult);
            mc.displayGuiScreen(new GuiMainMenu());
            mc.loadWorld(null);
            mc.shutdown();
        }
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        MinecraftServer server = MinecraftServer.getServer();
        if (event.phase == TickEvent.Phase.END && serverResult == null
            && server != null
            && !server.getConfigurationManager().playerEntityList.isEmpty()
            && ++serverTicks > 100) {
            serverResult = check((EntityPlayer) server.getConfigurationManager().playerEntityList.get(0));
        }
    }

    private static boolean check(EntityPlayer player) {
        World world = player.worldObj;
        String side = world.isRemote ? "CLIENT" : "SERVER";
        double px = player.posX;
        double py = player.posY;
        double pz = player.posZ;
        float step = player.stepHeight;
        float fall = player.fallDistance;
        int x = (((int) Math.floor(px)) >> 4) * 16 + 8;
        int y = world.isRemote ? 220 : 200;
        int z = (((int) Math.floor(pz)) >> 4) * 16 + 8;
        List<Block> originals = new ArrayList<>();
        List<Integer> metadata = new ArrayList<>();
        for (int dx = -2; dx <= 3; dx++) {
            for (int dy = -1; dy <= 5; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    originals.add(world.getBlock(x + dx, y + dy, z + dz));
                    metadata.add(world.getBlockMetadata(x + dx, y + dy, z + dz));
                    world.setBlock(x + dx, y + dy, z + dz, Blocks.air, 0, 2);
                }
            }
        }
        try {
            require(!player.noClip, "player must have collision enabled");
            player.stepHeight = 0;
            int variants = 0;
            int bop = 0;
            int forestry = 0;
            for (Object entry : Block.blockRegistry) {
                Block block = (Block) entry;
                if (block.getMaterial() != Material.leaves && block != FORGE_LEAF) continue;
                String name = Block.blockRegistry.getNameForObject(block);
                for (int meta = 0; meta < 16; meta++) {
                    world.setBlock(x, y, z, block, meta, 2);
                    world.setBlock(x, y + 1, z, block, meta, 2);
                    require(world.getBlock(x, y, z) == block, name + ":" + meta + " fixture placed");
                    AxisAlignedBB inside = AxisAlignedBB
                        .getBoundingBox(x + .1, y + .1, z + .1, x + .9, y + 1.9, z + .9);
                    require(
                        world.getCollidingBoundingBoxes(player, inside)
                            .isEmpty(),
                        name + ":" + meta + " entity query");
                    require(
                        world.func_147461_a(inside)
                            .isEmpty(),
                        name + ":" + meta + " block query");
                    player.setPosition(x + .5, y + .5 - player.getEyeHeight(), z + .5);
                    player.motionX = player.motionY = player.motionZ = 0;
                    Method pushOut = Entity.class
                        .getDeclaredMethod("func_145771_j", double.class, double.class, double.class);
                    pushOut.setAccessible(true);
                    pushOut.invoke(player, x + .5, (double) y, z + .5);
                    require(
                        player.motionX == 0 && player.motionY == 0 && player.motionZ == 0,
                        name + ":" + meta + " push-out");
                    require(!player.isEntityInsideOpaqueBlock(), name + ":" + meta + " suffocation");
                    player.setPosition(x - .5, y, z + .5);
                    player.onGround = false;
                    player.moveEntity(2, 0, 0);
                    require(Math.abs(player.posX - (x + 1.5)) < .001, name + ":" + meta + " horizontal movement");
                    player.setPosition(x + .5, y + 3.1, z + .5);
                    player.moveEntity(0, -3, 0);
                    require(Math.abs(player.posY - (y + .1)) < .001, name + ":" + meta + " falling");
                    require(world.getBlock(x, y, z) == block, name + " preserved");
                    variants++;
                    if (name.toLowerCase()
                        .startsWith("biomesoplenty:")) bop++;
                    if (name.toLowerCase()
                        .startsWith("forestry:")) forestry++;
                }
                LOG.info("{} PASS {} (16 metadata values)", side, name);
            }
            world.setBlock(x, y, z, Blocks.stone, 0, 2);
            world.setBlock(x, y + 1, z, Blocks.stone, 0, 2);
            player.setPosition(x - .5, y, z + .5);
            player.moveEntity(2, 0, 0);
            require(player.posX < x, "stone must stop player");
            player.setPosition(x + .5, y + .5 - player.getEyeHeight(), z + .5);
            require(player.isEntityInsideOpaqueBlock(), "stone must remain opaque to entity checks");
            require(
                !world.func_147461_a(AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 2, z + 1))
                    .isEmpty(),
                "stone query");
            require(variants >= 64, "vanilla and synthetic leaves tested");
            require(bop > 0 && forestry > 0, "BOP and Forestry must be loaded");
            LOG.info("{} PASS variants={} bop={} forestry={} stone=solid", side, variants, bop, forestry);
            player.addChatMessage(
                new ChatComponentText(
                    side + " PASS: " + variants + " leaf variants; BOP=" + bop + "; Forestry=" + forestry));
            return true;
        } catch (AssertionError | ReflectiveOperationException error) {
            LOG.error("{} FAIL {}", side, error.getMessage());
            player.addChatMessage(new ChatComponentText(side + " FAIL: " + error.getMessage()));
            return false;
        } finally {
            int index = 0;
            for (int dx = -2; dx <= 3; dx++) {
                for (int dy = -1; dy <= 5; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        world.setBlock(x + dx, y + dy, z + dz, originals.get(index), metadata.get(index), 2);
                        index++;
                    }
                }
            }
            player.stepHeight = step;
            player.fallDistance = fall;
            player.setPosition(px, py, pz);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
