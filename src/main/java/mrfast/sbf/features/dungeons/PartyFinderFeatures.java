package mrfast.sbf.features.dungeons;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.features.items.HideGlass;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyFinderFeatures {
    static List<String> neededClasses = new ArrayList<>(Arrays.asList("Mage","Archer","Tank","Healer","Berserk"));
    static HashMap<String,PartyFinderMonkey> partyFinderMonkeys = new HashMap<>();

    public static class PartyFinderMonkey {
        String name;
        String level;
        String selectedClass;
        long addedAt;
        public PartyFinderMonkey(String name,String level,String selectedClass) {
            this.name = name;
            this.addedAt = System.currentTimeMillis();
            this.level = level;
            this.selectedClass = selectedClass;
        }
    }


    @SubscribeEvent
    public void onDrawSlot(GuiContainerEvent.DrawSlotEvent event) {
        if(!SkyblockFeatures.config.dungeonPartyDisplay) return;
        if(event.chestName.equals("Catacombs Gate") && event.slot.getSlotIndex()==45) {
            for(String line: ItemUtils.getItemLore(event.slot.getStack())) {
                line = Utils.cleanColor(line);
                if(line.contains("Currently Selected")) {
                    PartyFinderMonkey monkey = new PartyFinderMonkey(Utils.GetMC().thePlayer.getName(),"",line.split(": ")[1]);
                    partyFinderMonkeys.put(Utils.GetMC().thePlayer.getName(),monkey);
                }
            }
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if(!SkyblockFeatures.config.dungeonPartyDisplay) return;

        String message = event.message.getUnformattedText();
        boolean fromServer = !message.contains(":");
        // Clear All
        if((message.contains("The party was disbanded") || message.contains(" has disbanded the party!") || message.contains("You left the party.")) && fromServer) {
            partyFinderMonkeys.clear();
        }

        // Remove specific person
        if((message.contains(" has been removed from the party.") || message.contains(" has left the party.")) && fromServer) {
            Pattern pattern = Pattern.compile("\\[\\w+\\+?] (\\w+)");
            Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
                partyFinderMonkeys.remove(matcher.group(1));
            }
        }
        if(message.startsWith("Party Finder > ") && message.contains(" joined the dungeon group! (")) {
            String className = message.split(" ")[8].replace("(","");
            String classLvl = message.split(" ")[10].replace(")","");
            String playerName = message.split(" ")[3];

            PartyFinderMonkey monkey = new PartyFinderMonkey(playerName,classLvl,className);
            partyFinderMonkeys.put(playerName,monkey);
        }
    }


    static {
        new partyDisplayGUI();
    }

    public static class partyDisplayGUI extends UIElement {
        public partyDisplayGUI() {
            super("Dungeon Party Display", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(Utils.GetMC().thePlayer == null || !Utils.inSkyblock || partyFinderMonkeys.isEmpty()) return;
            List<String> stillNeededClasses = new ArrayList<>(neededClasses);
            List<String> lines = new ArrayList<>(Arrays.asList(
                    "§9§lDungeon Party"
            ));
            // Sort by when monkey joined
            List<PartyFinderMonkey> sorted = new ArrayList<>(partyFinderMonkeys.values());
            sorted.sort((a, b) -> Math.toIntExact(b.addedAt - a.addedAt));
            for (PartyFinderMonkey monkey : sorted) {
                stillNeededClasses.remove(monkey.selectedClass);
                boolean dupe = partyFinderMonkeys.values().stream().anyMatch((a)-> !a.name.equals(monkey.name) && Objects.equals(a.selectedClass, monkey.selectedClass));

                String name = monkey.name;
                if(Objects.equals(name, Utils.GetMC().thePlayer.getName())) name = "§5"+name;

                String formattedString = " §a["+(dupe?"§c":"§6")+monkey.selectedClass.charAt(0)+"§a] "+name;
                if(!monkey.level.isEmpty()) formattedString+=" §7("+monkey.level+")";

                lines.add(formattedString);
            }
            for(int i=0;i<5-partyFinderMonkeys.values().size();i++) {
                try {
                    String formatted = " §a[§6" + stillNeededClasses.get(i).charAt(0) + "§a] §cNone";
                    lines.add(formatted);
                } catch (Exception ignored){}
            }

            int lineCount = 0;
            for(String line:lines) {
                Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 0, lineCount*(Utils.GetMC().fontRendererObj.FONT_HEIGHT),0xFFFFFF);
                lineCount++;
            }
        }
        @Override
        public void drawElementExample() {
            if(Utils.GetMC().thePlayer == null || !Utils.inSkyblock) return;
            String playerName = Utils.GetMC().thePlayer.getName();
            String[] lines = {
                    "§9§lDungeon Party",
                    " §a[§6M§a] §d"+playerName+" §7(50)",
                    " §a[§6A§a] §d"+playerName+" §7(50)",
                    " §a[§6T§a] §d"+playerName+" §7(50)",
                    " §a[§6H§a] §d"+playerName+" §7(50)",
                    " §a[§6B§a] §cNone",
            };
            int lineCount = 0;
            for(String line:lines) {
                Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 0, lineCount*(Utils.GetMC().fontRendererObj.FONT_HEIGHT),0xFFFFFF);
                lineCount++;
            }
        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.dungeonPartyDisplay;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*6;
        }

        @Override
        public int getWidth() {
            String playerName = Utils.GetMC().thePlayer.getName();

            return Utils.GetMC().fontRendererObj.getStringWidth(" §a[§6H§a] §d"+playerName+" §7(50)");
        }
    }

    boolean canRefresh = true;
    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        canRefresh = true;
        partyFinderMonkeys.clear();
    }
    @SubscribeEvent
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent keyboardInputEvent) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof GuiChest && Keyboard.isKeyDown(SkyblockFeatures.reloadPartyFinder.getKeyCode()) && canRefresh) {
            GuiChest chestScreen = (GuiChest) screen;
            ContainerChest chestContainer = (ContainerChest) chestScreen.inventorySlots;
            if (!chestContainer.getLowerChestInventory().getName().contains("Party Finder")) return;
            canRefresh = false;

            Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, 46, 0, 0, Utils.GetMC().thePlayer);
            Utils.setTimeout(()-> canRefresh = true, 3300);
        }
    }
    ItemStack hoverItemStack = null;

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;
        if(!SkyblockFeatures.config.betterPartyFinder) return;

        GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
        ContainerChest cont = (ContainerChest) chest.inventorySlots;
        String name = cont.getLowerChestInventory().getName();

        if(!HideGlass.isEmptyGlassPane(event.itemStack) && event.itemStack.getItem() instanceof ItemSkull && event.itemStack.getDisplayName().contains("'s Party")) {
            hoverItemStack = event.itemStack;
        }
        if("Party Finder".equals(name) && event.itemStack.getItem() instanceof ItemSkull) {
            event.toolTip.clear();
        }
    }

    @SubscribeEvent
    public void onDrawContainerTitle(GuiContainerEvent.TitleDrawnEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiChest) || event.gui == null || !SkyblockFeatures.config.betterPartyFinder) {
            return;
        }

        GuiChest chest = (GuiChest) mc.currentScreen;
        ContainerChest cont = (ContainerChest) chest.inventorySlots;
        String name = cont.getLowerChestInventory().getName();

        if (!"Party Finder".equals(name) || hoverItemStack == null || HideGlass.isEmptyGlassPane(hoverItemStack)) {
            return;
        }

        List<String> loreList = ItemUtils.getItemLore(hoverItemStack);
        int maxLineLength = loreList.stream()
                .map(Utils::cleanColor)
                .mapToInt(String::length)
                .max()
                .orElse(0);

        int lineCount = loreList.size() + 3;
        Utils.drawGraySquareWithBorder(180, 0, maxLineLength * 6, (lineCount + 1) * mc.fontRendererObj.FONT_HEIGHT, 3);

        Utils.drawTextWithStyle3(hoverItemStack.getDisplayName(), 190, 10);
        int temp = 1;
        for (String line : loreList) {
            if (!line.contains("Click to join!")) {
                Utils.drawTextWithStyle3(line, 190, temp * (mc.fontRendererObj.FONT_HEIGHT + 1) + 10);
                temp++;
            }
        }
    }

    @SubscribeEvent
    public void onGuiPostRender(GuiScreenEvent.DrawScreenEvent.Post rendered) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest) || !SkyblockFeatures.config.betterPartyFinder) return;

        GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
        ContainerChest cont = (ContainerChest) chest.inventorySlots;
        String name = cont.getLowerChestInventory().getName();
        if(!name.equals("Party Finder")) return;

        int i = 222;
        int j = i - 108;
        int ySize = j + (((ContainerChest)(((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots)).getLowerChestInventory().getSizeInventory() / 9) * 18;
        int left = (rendered.gui.width - 176) / 2;
        int top = (rendered.gui.height - ySize ) / 2;
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.colorMask(true, true, true, false);
        GlStateManager.translate(left, top, 0);
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        try {
            for (int i1 = 0; i1 < Integer.min(54, cont.inventorySlots.size()); i1++) {
                Slot s = cont.inventorySlots.get(i1);
                if (s.getStack() == null) continue;
                if (s.getStack().getItem() != Items.skull) continue;
                NBTTagCompound nbt = s.getStack().getTagCompound();
                if (nbt == null || nbt.hasNoTags()) continue;
                NBTTagCompound display = nbt.getCompoundTag("display");
                if (display.hasNoTags()) return;
                NBTTagList lore = display.getTagList("Lore", 8);
                int classLvReq = 0;
                int cataLvReq = 0;
                boolean Req = false;
                String note = "";
                for (int n = 0; n < lore.tagCount(); n++) {
                    String str = lore.getStringTagAt(n);
                    if (str.startsWith("§7Dungeon Level Required: §b")) cataLvReq = Integer.parseInt(str.substring(28));
                    if (str.startsWith("§7Class Level Required: §b")) classLvReq = Integer.parseInt(str.substring(26));
                    if (str.startsWith("§7§7Note:")) note = Utils.cleanColor(str.substring(10));
                    if (str.startsWith("§cRequires")) Req = true;
                }

                int x = s.xDisplayPosition;
                int y = s.yDisplayPosition;
                if (Req) {
                    Gui.drawRect(x, y, x + 16, y + 16, 0x77AA0000);
                } else {

                    if (note.toLowerCase().contains("car")) {
                        fr.drawStringWithShadow("C", x + 1, y + 1, 0xFFFF0000);
                    } else if (note.toLowerCase().replace(" ", "").contains("s/s+")) {
                        fr.drawStringWithShadow("S+", x + 1, y + 1, 0xFFFFFF00);
                    } else if (note.toLowerCase().contains("s+")) {
                        fr.drawStringWithShadow("S+", x + 1, y + 1, 0xFF00FF00);
                    } else if (note.toLowerCase().contains(" s") || note.toLowerCase().contains(" s ")) {
                        fr.drawStringWithShadow("S", x + 1, y + 1, 0xFFFFFF00);
                    } else if (note.toLowerCase().contains("rush")) {
                        fr.drawStringWithShadow("R", x + 1, y + 1, 0xFFFF0000);
                    }
                    fr.drawStringWithShadow("§e"+Integer.max(classLvReq, cataLvReq), x + 1, y + fr.FONT_HEIGHT, 0xFFFFFFFF);
                }

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableLighting();
    }
}
