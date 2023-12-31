package mrfast.sbf.features.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.PacketEvent;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RiftFeatures {
    List<BlockPos> Barriers = new ArrayList<>();

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        Gui gui = Utils.GetMC().currentScreen;
        if (!(gui instanceof GuiChest) || !SkyblockFeatures.config.riftHackingHelper) return;
        GuiChest inventory = (GuiChest) gui;
        Container containerChest = inventory.inventorySlots;
        if (!(containerChest instanceof ContainerChest)) return;
        
        String displayName = ((ContainerChest) containerChest).getLowerChestInventory().getDisplayName().getUnformattedText().trim();
        if(displayName.contains("Hacking")) {
            event.toolTip.clear();
        }
    }

    @SubscribeEvent
    public void onDrawSlots(GuiContainerEvent.DrawSlotEvent.Pre event) {
        // Hacking solver
        try {
            if (!(event.gui instanceof GuiChest) || !SkyblockFeatures.config.riftHackingHelper) return;
            GuiChest inventory = (GuiChest) event.gui;
            Container containerChest = inventory.inventorySlots;
            if (!(containerChest instanceof ContainerChest)) return;
            String displayName = ((ContainerChest) containerChest).getLowerChestInventory().getDisplayName().getUnformattedText().trim();
            if(displayName.contains("Hacking")) {
                int num1 = containerChest.getSlot(2).getStack().stackSize;
                int num2 = containerChest.getSlot(3).getStack().stackSize;
                int num3 = containerChest.getSlot(4).getStack().stackSize;
                int num4 = containerChest.getSlot(5).getStack().stackSize;
                int num5 = containerChest.getSlot(6).getStack().stackSize;
                int x = event.slot.xDisplayPosition;
                int y = event.slot.yDisplayPosition;

                if(event.slot.getStack().stackSize==num1) Gui.drawRect(x, y, x + 16, y + 16, new Color(255, 85, 85, 255).getRGB());
                if(event.slot.getStack().stackSize==num2) Gui.drawRect(x, y, x + 16, y + 16, new Color(85, 255, 85, 255).getRGB());
                if(event.slot.getStack().stackSize==num3) Gui.drawRect(x, y, x + 16, y + 16, new Color(85, 255, 255, 255).getRGB());
                if(event.slot.getStack().stackSize==num4) Gui.drawRect(x, y, x + 16, y + 16, new Color(255, 200, 85, 255).getRGB());
                if(event.slot.getStack().stackSize==num5) Gui.drawRect(x, y, x + 16, y + 16, new Color(0, 85, 255, 255).getRGB());
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    static BlockPos startingSilkPos;
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if(!SkyblockFeatures.config.larvaSilkDisplay) return;
        String clean = Utils.cleanColor(event.message.getUnformattedText());
        if(clean.startsWith("You cancelled the wire")) {
            startingSilkPos = null;
        }
    }
    @SubscribeEvent
    public void onBlockInteraction(PlayerInteractEvent event) {
        if(!SkyblockFeatures.config.larvaSilkDisplay) return;
        if(event.action.equals(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)) {
            ItemStack held = Utils.GetMC().thePlayer.getHeldItem();
            if(held!=null) {
                String id = ItemUtils.getSkyBlockItemID(held);
                if(id!=null && id.equals("LARVA_SILK")) {
                    if(startingSilkPos==null) {
                        startingSilkPos = event.pos;
                    } else {
                        startingSilkPos = null;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        String location = Utils.cleanColor(SkyblockInfo.localLocation);
        ItemStack held = Utils.GetMC().thePlayer.getHeldItem();
        if(held!=null && SkyblockFeatures.config.larvaSilkDisplay) {
            String id = ItemUtils.getSkyBlockItemID(held);
            if (id!=null && id.equals("LARVA_SILK")) {
                if (startingSilkPos != null) {
                    RenderUtil.drawOutlinedFilledBoundingBox(startingSilkPos, SkyblockFeatures.config.larvaSilkBlockColor, event.partialTicks);
                    MovingObjectPosition lookingBlock = Utils.GetMC().thePlayer.rayTrace(4, event.partialTicks);
                    if (lookingBlock.getBlockPos() != null) {
                        Vec3 starting = new Vec3(startingSilkPos.getX()+0.5, startingSilkPos.getY()+0.5, startingSilkPos.getZ()+0.5);
                        Vec3 finish = new Vec3(lookingBlock.getBlockPos().getX()+0.5, lookingBlock.getBlockPos().getY()+0.5, lookingBlock.getBlockPos().getZ()+0.5);

                        RenderUtil.draw3DLine(starting, finish, 2, SkyblockFeatures.config.larvaSilkLineColor, event.partialTicks);
                        RenderUtil.drawOutlinedFilledBoundingBox(lookingBlock.getBlockPos(), SkyblockFeatures.config.larvaSilkBlockColor, event.partialTicks);
                    }
                }
            }
        }

        if(location.contains("Mirrorverse") && SkyblockFeatures.config.riftMirrorverseHelper) {
            // Laser puzzle
            if(Utils.GetMC().thePlayer.getPosition().getX()<-297) {
                BlockPos pos = new BlockPos(Math.floor(Utils.GetMC().thePlayer.posX), Math.floor(Utils.GetMC().thePlayer.posY-1), Math.floor(Utils.GetMC().thePlayer.posZ));
                if(Utils.GetMC().theWorld.getBlockState(pos).getBlock() == Blocks.barrier && !Barriers.contains(pos)) {
                    Barriers.add(pos);
                }
            }

            for(BlockPos pos:Barriers) {
                RenderUtil.drawOutlinedFilledBoundingBox(pos,SkyblockFeatures.config.riftMirrorverseHelperColor, event.partialTicks);
            }

            // Upside down Parkour
            if(Utils.GetMC().thePlayer.getPosition().getX()<=-116) {
                for(int x=-122;x>=-224;x--) {
                    for(int z=-91;z>=-126;z--) {
                        for(int y=46;y<60;y++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            if(Utils.GetMC().theWorld.getBlockState(pos).getBlock() == Blocks.planks && Utils.GetMC().theWorld.getBlockState(pos.east()).getBlock() != Blocks.planks && Utils.GetMC().theWorld.getBlockState(pos.north()).getBlock() != Blocks.planks) {
                                int y2 = pos.getY()-46;
                                AxisAlignedBB aabb = new AxisAlignedBB(pos.getX()-1, 46-y2+1.1, pos.getZ(), pos.getX()+1, 46-y2, pos.getZ()+2);

                                RenderUtil.drawOutlinedFilledBoundingBox(aabb,SkyblockFeatures.config.riftMirrorverseHelperColor, event.partialTicks);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        Barriers.clear();
        startingSilkPos = null;
    }
}
