package mrfast.skyblockfeatures.features.trackers;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.core.PricingData;
import mrfast.skyblockfeatures.gui.components.UIElement;
import mrfast.skyblockfeatures.events.SecondPassedEvent;

import mrfast.skyblockfeatures.utils.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import mrfast.skyblockfeatures.gui.components.Point;

public class AutomatonTracker {
    private static final Minecraft mc = Minecraft.getMinecraft();

    static int total = 0;
    static int Control = 0;
    static int FTX = 0;
    static int Electron = 0;
    static int Robotron = 0;
    static int Superlite = 0;
    static int Synthetic = 0;

    static double ControlPrice = 0;
    static double FTXPrice = 0;
    static double ElectronPrice = 0;
    static double RobotronPrice = 0;
    static double SuperlitePrice = 0;
    static double SyntheticPrice = 0;

    static boolean hidden = true;
    static int kills = 0;
    static int oldKills = 0;
    static int seconds = 0;
    static int totalSeconds = 0;
    @SubscribeEvent
    public void onload(WorldEvent.Load event) {
        try {
            seconds = 0;
            kills = 0;
            oldKills = 0;
            hidden = true;
            totalSeconds = 0;
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @SubscribeEvent
    public void onSecond(SecondPassedEvent event) {
        if(Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null && SkyblockFeatures.config.AutomatonTracker) {
            if(oldKills == 0) {
                oldKills = kills;
            }
            if(!hidden) {
                totalSeconds++;
            }
            if(seconds >= 60) {
                if(oldKills == kills) {
                    hidden = true;
                    totalSeconds=0;
                }
                oldKills = kills;
                seconds = 0;
            }
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        Entity entity = event.entity;
        if(entity instanceof EntityIronGolem && SkyblockFeatures.config.AutomatonTracker) {
            for(Entity thing:Utils.GetMC().theWorld.loadedEntityList) {
                if(thing instanceof EntityArmorStand) {
                    boolean automaton = entity.getDistance(thing.posX,entity.posY,thing.posZ)<1 && thing.getCustomNameTag().contains("Automaton");
                    if(automaton && Utils.GetMC().thePlayer.getDistanceToEntity(entity) < 10) {
                        hidden = false;
                        kills++;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onDrawSlot(SecondPassedEvent event) {
        if(Utils.GetMC().thePlayer == null || !Utils.inSkyblock || !SkyblockFeatures.config.AutomatonTracker) return;
        for(int i=0;i<Utils.GetMC().thePlayer.inventory.mainInventory.length;i++) {
            if(Utils.GetMC().thePlayer.inventory.mainInventory[i] != null) {
                if(i == 0) {
                    total = 0;
                    Control = 0;
                    FTX = 0;
                    Electron = 0;
                    Robotron = 0;
                    Superlite = 0;
                    Synthetic = 0;
                }
                try {
                    ItemStack stack = Utils.GetMC().thePlayer.inventory.mainInventory[i];
                    String name = Utils.cleanColor(stack.getDisplayName());
        
                    if(PricingData.getIdentifier(stack) != null) {
                        double value = Math.floor(PricingData.lowestBINs.get(PricingData.getIdentifier(stack)));
                        if(value != 0) {
                            if(name.contains("Control")) {
                                Control+=stack.stackSize;
                                ControlPrice=value*stack.stackSize;
                            }
                            if(name.contains("FTX")) {
                                FTX+=stack.stackSize;
                                FTXPrice=value*stack.stackSize;
                            }
                            if(name.contains("Electron")) {
                                Electron+=stack.stackSize;
                                ElectronPrice=value*stack.stackSize;
                            }
                            if(name.contains("Robotron")) {
                                Robotron+=stack.stackSize;
                                RobotronPrice=value*stack.stackSize;
                            }
                            if(name.contains("Superlite")) {
                                Superlite+=stack.stackSize;
                                SuperlitePrice=value*stack.stackSize;
                            }
                            if(name.contains("Synthetic")) {
                                Synthetic+=stack.stackSize;
                                SyntheticPrice=value*stack.stackSize;
                            }
                            
                            if(name.contains("Control") || name.contains("FTX") || name.contains("Electron") || name.contains("Robotron") || name.contains("Superlite") || name.contains("Synthetic")) {
                                total += value*stack.stackSize;
                            }
                        }
                    }
                } catch (Exception e) {
                    //TODO: handle exception
                }
            }
        }
    }
    static {
        new AutomatonTrackerGUI();
    }

    static String display = "";
    public static class AutomatonTrackerGUI extends UIElement {
        public AutomatonTrackerGUI() {
            super("Automaton Tracker", new Point(0.45052084f, 0.86944443f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null && !hidden) {
                String[] lines = {
                    ChatFormatting.GREEN+"Time Elapsed: §r"+Utils.secondsToTime(totalSeconds),
                    ChatFormatting.GREEN+"Automatons Killed: §r"+Utils.nf.format(kills),
                    ChatFormatting.BLUE+"Control Switch: §r"+Control+" §7("+Utils.nf.format(ControlPrice)+")",
                    ChatFormatting.BLUE+"FTX 3070: §r"+FTX+" §7("+Utils.nf.format(FTXPrice)+")",
                    ChatFormatting.BLUE+"Electron Transmitter: §r"+Electron+" §7("+Utils.nf.format(ElectronPrice)+")",
                    ChatFormatting.BLUE+"Robotron Reflector: §r"+Robotron+" §7("+Utils.nf.format(RobotronPrice)+")",
                    ChatFormatting.BLUE+"Superlite Motor: §r"+Superlite+" §7("+Utils.nf.format(SuperlitePrice)+")",
                    ChatFormatting.BLUE+"Synthetic Heart: §r"+Synthetic+" §7("+Utils.nf.format(SyntheticPrice)+")",
                    ChatFormatting.WHITE+"Total Value: §6"+Utils.nf.format(total)
                };
                int lineCount = 0;
                for(String line:lines) {
                    Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 0, lineCount*(mc.fontRendererObj.FONT_HEIGHT),0xFFFFFF);
                    lineCount++;
                }
            }
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            String[] lines = {
                ChatFormatting.BLUE+"Control Switch: §r"+4,
                ChatFormatting.BLUE+"FTX 3070: §r"+2,
                ChatFormatting.BLUE+"Electron Transmitter: §r"+5,
                ChatFormatting.BLUE+"Robotron Reflector: §r"+1,
                ChatFormatting.BLUE+"Superlite Motor: §r"+3,
                ChatFormatting.BLUE+"Synthetic Heart: §r"+6,
                ChatFormatting.WHITE+"Total Value: §6"+Utils.formatNumber(1231934)
            };
            int lineCount = 0;
            for(String line:lines) {
                Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 0, lineCount*(mc.fontRendererObj.FONT_HEIGHT),0xFFFFFF);
                lineCount++;
            }
        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.AutomatonTracker;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*7;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("Electron Transmitter: 10");
        }
    }
}
