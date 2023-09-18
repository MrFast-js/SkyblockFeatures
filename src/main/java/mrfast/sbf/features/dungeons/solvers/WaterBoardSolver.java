package mrfast.sbf.features.dungeons.solvers;

import com.google.common.collect.ImmutableSet;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.RenderUtil;
import net.minecraft.block.BlockColored;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class WaterBoardSolver {
    private final static Minecraft mc = Minecraft.getMinecraft();
    private static final HashMap<WoolColor, ImmutableSet<LeverBlock>> solutions = new HashMap<>();
    private static BlockPos chestLocation = null;
    private static EnumFacing roomFacing = null;
    private static boolean prevInWaterRoom = false;
    private static boolean inWaterRoom = false;
    private static int puzzleVariant = -1;
    private static WoolColor frontWoolType = null;
    private static Thread workerThread = null;
    private static int tickCounter = 0;

    @SubscribeEvent
    public void onGameTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons || mc.theWorld == null || mc.thePlayer == null || !SkyblockFeatures.config.WaterBoardSolver) {
            return;
        }

        EntityPlayerSP player = mc.thePlayer;

        if (tickCounter % 4 == 0) {
            updateFrontWoolType();

            if (puzzleVariant == -1 && (workerThread == null || !workerThread.isAlive() || workerThread.isInterrupted())) {
                workerThread = new Thread(() -> {
                    updateWaterRoomStatus(player);

                    if (foundStickyPiston()) {
                        if (chestLocation == null) {
                            findChestLocation(player);
                        }

                        if (chestLocation != null) {
                            checkWaterPuzzle(player);
                        }
                    }
                });
                workerThread.start();
            }
            tickCounter = 0;
        }
        tickCounter++;
    }

    private void updateFrontWoolType() {
        if (chestLocation == null) {
            return;
        }
        BlockPos outermostWool = findOutermostWool();
        if(outermostWool!=null) {
            frontWoolType = WoolColor.fromBlockState(mc.theWorld.getBlockState(outermostWool));
        } else {
            frontWoolType = null;
        }
    }

    private static BlockPos findOutermostWool() {
        BlockPos outermostWool = null;
        for (BlockPos blockPos : getBlocksInRange(chestLocation, 8, 56)) {
            boolean isMiddleBlock = (blockPos.getX() == chestLocation.getX()) || (blockPos.getZ() == chestLocation.getZ());
            if (mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.wool && isMiddleBlock) {
                if (outermostWool == null || isCloserToChest(blockPos, outermostWool)) {
                    outermostWool = blockPos;
                }
            }
        }

        return outermostWool;
    }

    private static boolean isCloserToChest(BlockPos blockPos1, BlockPos blockPos2) {
        double distance1 = chestLocation.distanceSq(blockPos1);
        double distance2 = chestLocation.distanceSq(blockPos2);
        return distance1 > distance2;
    }

    private void updateWaterRoomStatus(EntityPlayerSP player) {
        prevInWaterRoom = inWaterRoom;
        inWaterRoom = foundStickyPiston() && chestLocation != null;
    }

    private boolean foundStickyPiston() {
        List<BlockPos> positionsInRange = getBlocksInRange(mc.thePlayer.getPosition(), 13, 54);
    
        for (BlockPos pos : positionsInRange) {
            Block block = mc.theWorld.getBlockState(pos).getBlock();
            if (block == Blocks.sticky_piston) {
                return true; // Found a sticky piston
            }
        }
    
        return false; // No sticky pistons found in the range
    }

    private void findChestLocation(EntityPlayerSP player) {
        for (BlockPos potentialChestPos : getBlocksInRange(player.getPosition(), 25, 56)) {
            if (mc.theWorld.getBlockState(potentialChestPos).getBlock() != Blocks.chest) {
                continue;
            }
            if (mc.theWorld.getBlockState(potentialChestPos.down()).getBlock() == Blocks.stone && mc.theWorld.getBlockState(potentialChestPos.up(2)).getBlock() == Blocks.stained_glass) {
                for (EnumFacing direction : EnumFacing.HORIZONTALS) {
                    BlockPos pistonPos = potentialChestPos.offset(direction.getOpposite(), 3).down(2);
                    BlockPos stonePos = potentialChestPos.offset(direction, 2);

                    if (mc.theWorld.getBlockState(pistonPos).getBlock() == Blocks.sticky_piston && mc.theWorld.getBlockState(stonePos).getBlock() == Blocks.stone) {
                        chestLocation = potentialChestPos;
                        roomFacing = direction;
                        break;
                    }
                }
                break;
            }
        }
    }

    private void checkWaterPuzzle(EntityPlayerSP player) {
        for (BlockPos blockPos : getBlocksInRange(player.getPosition(), 25, 82)) {
            if (mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.piston_head) {
                inWaterRoom = true;

                if (!prevInWaterRoom) {
                    boolean foundGoldBlock = false;
                    boolean foundHardenedClay = false;
                    boolean foundEmeraldBlock = false;
                    boolean foundQuartzBlock = false;
                    boolean foundDiamondBlock = false;

                    int posX = blockPos.getX();
                    int posZ = blockPos.getZ();

                    for (BlockPos puzzleBlockPos : BlockPos.getAllInBox(new BlockPos(posX + 1, 78, posZ + 1), new BlockPos(posX - 1, 77, posZ - 1))) {
                        Block currentBlock = mc.theWorld.getBlockState(puzzleBlockPos).getBlock();
                        if (currentBlock == Blocks.gold_block) {
                            foundGoldBlock = true;
                        } else if (currentBlock == Blocks.hardened_clay) {
                            foundHardenedClay = true;
                        } else if (currentBlock == Blocks.emerald_block) {
                            foundEmeraldBlock = true;
                        } else if (currentBlock == Blocks.quartz_block) {
                            foundQuartzBlock = true;
                        } else if (currentBlock == Blocks.diamond_block) {
                            foundDiamondBlock = true;
                        }
                    }

                    updatePuzzleVariant(foundGoldBlock, foundHardenedClay, foundEmeraldBlock, foundQuartzBlock, foundDiamondBlock);
                    if(solutions.size()==0) loadSolutions();
                    break;
                }
            }
        }
    }

    private void updatePuzzleVariant(boolean foundGoldBlock, boolean foundHardenedClay, boolean foundEmeraldBlock, boolean foundQuartzBlock, boolean foundDiamondBlock) {
        if (foundGoldBlock && foundHardenedClay) {
            puzzleVariant = 0;
        } else if (foundEmeraldBlock && foundQuartzBlock) {
            puzzleVariant = 1;
        } else if (foundQuartzBlock && foundDiamondBlock) {
            puzzleVariant = 2;
        } else if (foundGoldBlock && foundQuartzBlock) {
            puzzleVariant = 3;
        } else {
            puzzleVariant = -1;
        }
    }

    private void loadSolutions() {
        switch (puzzleVariant) {
            case 0:
                solutions.put(WoolColor.PURPLE, ImmutableSet.of(LeverBlock.QUARTZ, LeverBlock.GOLD, LeverBlock.DIAMOND, LeverBlock.CLAY));
                solutions.put(WoolColor.ORANGE, ImmutableSet.of(LeverBlock.GOLD, LeverBlock.COAL, LeverBlock.EMERALD));
                solutions.put(WoolColor.BLUE, ImmutableSet.of(LeverBlock.QUARTZ, LeverBlock.GOLD, LeverBlock.EMERALD, LeverBlock.CLAY));
                solutions.put(WoolColor.GREEN, ImmutableSet.of(LeverBlock.EMERALD));
                solutions.put(WoolColor.RED, ImmutableSet.of());
                break;
            case 1:
                solutions.put(WoolColor.PURPLE, ImmutableSet.of(LeverBlock.COAL));
                solutions.put(WoolColor.ORANGE, ImmutableSet.of(LeverBlock.QUARTZ, LeverBlock.GOLD, LeverBlock.EMERALD, LeverBlock.CLAY));
                solutions.put(WoolColor.BLUE, ImmutableSet.of(LeverBlock.QUARTZ, LeverBlock.DIAMOND, LeverBlock.EMERALD));
                solutions.put(WoolColor.GREEN, ImmutableSet.of(LeverBlock.QUARTZ, LeverBlock.EMERALD));
                solutions.put(WoolColor.RED, ImmutableSet.of(LeverBlock.QUARTZ, LeverBlock.COAL, LeverBlock.EMERALD));
                break;
            case 2:
                solutions.put(WoolColor.PURPLE, ImmutableSet.of(LeverBlock.QUARTZ, LeverBlock.GOLD, LeverBlock.DIAMOND));
                solutions.put(WoolColor.ORANGE, ImmutableSet.of(LeverBlock.EMERALD));
                solutions.put(WoolColor.BLUE, ImmutableSet.of(LeverBlock.QUARTZ, LeverBlock.DIAMOND));
                solutions.put(WoolColor.GREEN, ImmutableSet.of());
                solutions.put(WoolColor.RED, ImmutableSet.of(LeverBlock.GOLD, LeverBlock.EMERALD));
                break;
            case 3:
                solutions.put(WoolColor.PURPLE, ImmutableSet.of(LeverBlock.QUARTZ, LeverBlock.GOLD, LeverBlock.EMERALD, LeverBlock.CLAY));
                solutions.put(WoolColor.ORANGE, ImmutableSet.of(LeverBlock.GOLD, LeverBlock.COAL));
                solutions.put(WoolColor.BLUE, ImmutableSet.of(LeverBlock.QUARTZ, LeverBlock.GOLD, LeverBlock.COAL, LeverBlock.EMERALD, LeverBlock.CLAY));
                solutions.put(WoolColor.GREEN, ImmutableSet.of(LeverBlock.GOLD, LeverBlock.EMERALD));
                solutions.put(WoolColor.RED, ImmutableSet.of(LeverBlock.GOLD, LeverBlock.DIAMOND, LeverBlock.EMERALD, LeverBlock.CLAY));
                break;
            default:
                break;
        }
    }


    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (chestLocation == null || roomFacing == null || puzzleVariant == -1 || !SkyblockFeatures.config.WaterBoardSolver || frontWoolType==null) return;

        HashMap<LeverBlock, Boolean> leverStates = new HashMap<>();

        for (LeverBlock lever : LeverBlock.values()) {
            leverStates.put(lever, getLeverToggleState(lever.getLeverPos()));
        }

        // Get color of wool
        Color renderColor = new Color(frontWoolType.dyeColor.getMapColor().colorValue).brighter();

        if (frontWoolType!=null) {
            // Get levers that solve for that color
            ImmutableSet<LeverBlock> solution = solutions.get(frontWoolType);
            if (solution == null) return;
            // Loop through levers that make the right solution
            for (Map.Entry<LeverBlock, Boolean> entry : leverStates.entrySet()) {
                LeverBlock lever = entry.getKey();
                boolean switched = entry.getValue();

                if ((switched && !solution.contains(lever)) || (!switched && solution.contains(lever))) {
                    BlockPos pos = lever.getLeverPos();
                    AxisAlignedBB aabb = new AxisAlignedBB(pos, pos.add(1, 1, 1));
                    RenderUtil.drawOutlinedFilledBoundingBox(aabb, renderColor, event.partialTicks);
                }
            }
            // Toggle water lever
            if (leverStates.entrySet().stream().allMatch(entry -> (entry.getValue() && solution.contains(entry.getKey()) || (!entry.getValue() && !solution.contains(entry.getKey()))))) {
                BlockPos pos = new BlockPos(new Vec3(chestLocation.offset(roomFacing.getOpposite(), 17).up(5)));
                AxisAlignedBB aabb = new AxisAlignedBB(pos, pos.add(1, -1, 1));
                RenderUtil.drawOutlinedFilledBoundingBox(aabb, renderColor, event.partialTicks);
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        puzzleVariant = -1;
        solutions.clear();
        chestLocation = null;
        roomFacing = null;
        prevInWaterRoom = false;
        inWaterRoom = false;
    }

    private boolean getLeverToggleState(BlockPos pos) {
        IBlockState block = mc.theWorld.getBlockState(pos);

        if (block.getBlock() != Blocks.lever) return false;
        return block.getValue(BlockLever.POWERED);
    }

    public enum WoolColor {
        PURPLE(EnumDyeColor.PURPLE),
        ORANGE(EnumDyeColor.ORANGE),
        BLUE(EnumDyeColor.BLUE),
        GREEN(EnumDyeColor.LIME),
        RED(EnumDyeColor.RED);

        public EnumDyeColor dyeColor;

        WoolColor(EnumDyeColor dyeColor) {
            this.dyeColor = dyeColor;
        }

        public static WoolColor fromBlockState(IBlockState state) {
            if(state==null) return null;
            for (WoolColor type : WoolColor.values()) {
                if (type.dyeColor == state.getValue(BlockColored.COLOR)) {
                    return type;
                }
            }
            return null;
        }
    }


    public enum LeverBlock {
        QUARTZ(Blocks.quartz_block),
        GOLD(Blocks.gold_block),
        COAL(Blocks.coal_block),
        DIAMOND(Blocks.diamond_block),
        EMERALD(Blocks.emerald_block),
        CLAY(Blocks.hardened_clay);

        public Block block;

        LeverBlock(Block block) {
            this.block = block;
        }

        public BlockPos getLeverPos() {
            if (chestLocation == null || roomFacing == null) {
                return null;
            }
    
            int shiftBy = (ordinal() % 3) * 5;
            EnumFacing leverSide = ordinal() < 3 ? roomFacing.rotateY() : roomFacing.rotateYCCW();
            return chestLocation.up(5).offset(leverSide.getOpposite(), 6).offset(roomFacing.getOpposite(), 2 + shiftBy).offset(leverSide);
        }
    }

    public static List<BlockPos> getBlocksInRange(BlockPos center, int range, int y) {
        return StreamSupport.stream(
            BlockPos.getAllInBox(center.add(-range, 0, -range).add(0, y - center.getY(), 0),
                center.add(range, 0, range).add(0, y - center.getY(), 0))
                .spliterator(), false)
            .collect(Collectors.toList());
    }
    
}
