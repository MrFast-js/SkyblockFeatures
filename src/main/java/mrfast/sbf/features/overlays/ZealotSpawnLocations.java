package mrfast.sbf.features.overlays;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.core.SkyblockMobDetector;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ZealotSpawnLocations {
    List<BlockPos> zealotSpawns = new ArrayList<>(
        Arrays.asList(
        new BlockPos(-646,5,-274),
        new BlockPos(-633,5,-277),
        new BlockPos(-639,7,-305),
        new BlockPos(-631,5,-327),
        new BlockPos(-619,6,-313),
        new BlockPos(-665,10,-313),
        new BlockPos(-632,5,-260),
        new BlockPos(-630,7,-229),
        new BlockPos(-647,5,-221),
        new BlockPos(-684,5,-261),
        new BlockPos(-699,6,-263),
        new BlockPos(-683,5,-292),
        new BlockPos(-698,5,-319),
        new BlockPos(-714,5,-289),
        new BlockPos(-732,5,-295),
        new BlockPos(-731,6,-275)));
    List<BlockPos> bruiserSpawns = new ArrayList<>(
        Arrays.asList(
            new BlockPos(-595,80,-190),
            new BlockPos(-575, 72, -201),
            new BlockPos(-560, 64, -220),
            new BlockPos(-554, 56, -237),
            new BlockPos(-571, 50, -240),
            new BlockPos(-585, 52, -232),
            new BlockPos(-96, 55, -216),
            new BlockPos(-578, 53, -214),
            new BlockPos(-598, 54, -201),
            new BlockPos(-532, 37, -223),
            new BlockPos(-520, 37, -235),
            new BlockPos(-530, 37, -246),
            new BlockPos(-515, 38, -250),
            new BlockPos(-516, 38, -264),
            new BlockPos(-513, 37, -279),
            new BlockPos(-524, 44, -268),
            new BlockPos(-536, 48, -252),
            new BlockPos(-526, 37, -294),
            new BlockPos(-514, 38, -304),
            new BlockPos(-526, 38, -317)
        )
    );

    static String loc = "";
    static Boolean inNest = false;
    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if(!SkyblockFeatures.config.showZealotSpawnAreas) return;

        loc = SkyblockInfo.localLocation;
        inNest = loc.contains("Dragons Nest");

        if(inNest) {
            for(BlockPos pos:zealotSpawns) {
                if(pos != null) {
                    Color color = canSpawnZealots? new Color(0x55FF55):new Color(0xFF5555);
                    highlightBlock(color, pos.getX(),pos.getY(), pos.getZ(), 5.0D,event.partialTicks);
                }
            }
        }
        if(loc.contains("Zealot Bruiser Hideout")) {
            for(BlockPos pos:bruiserSpawns) {
                if(pos != null) {
                    Color color = canSpawnBruisers? new Color(0x55FF55):new Color(0xFF5555);
                    highlightBlock(color, pos.getX(),pos.getY()+1, pos.getZ(), 5.0D,event.partialTicks);
                }
            }
        }
    }

    static boolean canSpawnZealots = false;
    static boolean canSpawnBruisers = false;

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if(Utils.GetMC().theWorld == null || Utils.GetMC().thePlayer == null) return;

        if(SkyblockFeatures.config.showZealotSpawnAreas && Utils.inSkyblock && SkyblockInfo.map.equals("The End")) {
            // Need to do this because skyblock doesnt spawn armor stand with zealot name
            Utils.setTimeout(()-> {
                if (event.entity instanceof EntityArmorStand && inNest) {
                    if (event.entity.getCustomNameTag().contains("Zealot")) {
                        startTimer = true;
                        secondsUntilSpawn = 10;
                    }
                }
                if (event.entity instanceof EntityArmorStand && loc.contains("Zealot Bruiser Hideout")) {
                    if (event.entity.getCustomNameTag().contains("Bruiser")) {
                        startTimer = true;
                        secondsUntilSpawn = 10;
                    }
                }
            },100);
        }
    }

    public static void highlightBlock(Color c, double d, double d1, double d2, double size,float ticks) {
        RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(d-size, d1+0.1, d2-size, d+size, d1-3, d2+size),c,ticks);
    }

    public static boolean startTimer = false;
    public static  String activeDisplay = EnumChatFormatting.LIGHT_PURPLE + "Zealot Spawn: "+ChatFormatting.DARK_PURPLE+ "10s";
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static int secondsUntilSpawn = 0;
    @SubscribeEvent
    public void onSeconds(SecondPassedEvent event) {
        if (!Utils.inSkyblock || !SkyblockFeatures.config.showZealotSpawnAreas) return;

        if(!startTimer) {
            activeDisplay="";
            return;
        }
        if(secondsUntilSpawn>0) {
            secondsUntilSpawn--;
        }
        
        String secondsRemaining = secondsUntilSpawn>0?secondsUntilSpawn+"s":ChatFormatting.GREEN+"Ready";
        String type = inNest?"Zealot":"Bruiser";
        activeDisplay = EnumChatFormatting.LIGHT_PURPLE + type+" Spawn: " + ChatFormatting.DARK_PURPLE + secondsRemaining;
    }
    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        startTimer = false;
        secondsUntilSpawn = 10;
    }

    static {
        new ZealotSpawnTimer();
    }   
    public static class ZealotSpawnTimer extends UIElement {
        public ZealotSpawnTimer() {
            super("Zealot Timer", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }
        
        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                GuiUtils.drawText(activeDisplay,0,0, GuiUtils.TextStyle.BLACK_OUTLINE);
            }
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            GuiUtils.drawText(EnumChatFormatting.LIGHT_PURPLE + "Zealot Spawn: "+ChatFormatting.DARK_PURPLE+ "10s",0,0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.showZealotSpawnAreas && SkyblockInfo.map.equals("The End");
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("Zealot Spawn: Ready ");
        }
    }
}