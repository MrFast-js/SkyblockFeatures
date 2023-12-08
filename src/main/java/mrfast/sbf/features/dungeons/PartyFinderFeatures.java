package mrfast.sbf.features.dungeons;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.SlotClickedEvent;
import mrfast.sbf.features.items.HideGlass;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyFinderFeatures {
    static List<String> neededClasses = new ArrayList<>(Arrays.asList("Mage","Archer","Tank","Healer","Berserk"));
    static HashMap<String,PartyFinderMonkey> partyFinderMonkeys = new HashMap<>();
    String selfPlayerClass = "";

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
                    selfPlayerClass = Utils.cleanColor(line.split(": ")[1]);
                    PartyFinderMonkey monkey = new PartyFinderMonkey(Utils.GetMC().thePlayer.getName(),"",line.split(": ")[1]);
                    partyFinderMonkeys.put(Utils.GetMC().thePlayer.getName(),monkey);
                }
            }
        }
    }

    @SubscribeEvent
    public void onSlotClick(SlotClickedEvent event) {
        if(event.item==null || !SkyblockFeatures.config.dungeonPartyDisplay) return;

        if(event.item.getItem() instanceof ItemSkull && event.item.getDisplayName().contains("'s Party")) {
            String regex = "^(\\w+):\\s(\\w+)\\s\\((\\d+)\\)$";
            Pattern pattern = Pattern.compile(regex);

            for(String line:ItemUtils.getItemLore(event.item)) {
                line = Utils.cleanColor(line.trim());
                // Match the input against the pattern
                Matcher matcher = pattern.matcher(line);
                if(matcher.matches()) {
                    // Extract information using groups
                    String playerName = matcher.group(1);
                    String className = matcher.group(2);
                    String classLvl = matcher.group(3);
                    PartyFinderMonkey monkey = new PartyFinderMonkey(playerName,classLvl,className);
                    partyFinderMonkeys.put(playerName,monkey);
                }
            }
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();
        boolean partyFinderJoin = message.startsWith("Party Finder > ") && message.contains(" joined the dungeon group! (");
        if(partyFinderJoin && SkyblockFeatures.config.partyFinderJoinMessages) {
            String playerName = Utils.cleanColor(message.split(" ")[3]);
            showDungeonPlayerInfo(playerName);
        }
        if(!SkyblockFeatures.config.dungeonPartyDisplay) return;

        boolean fromServer = !message.contains(":");
        // Clear All
        if((message.contains("The party was disbanded") || message.contains(" has disbanded the party!") || message.contains("You left the party.")) && fromServer) {
            partyFinderMonkeys.clear();
        }

        // Remove specific person
        if((message.contains(" has been removed from the party.") || message.contains(" has left the party.")) && fromServer) {
            Pattern pattern = Pattern.compile("\\[\\w+\\+?]? (\\w+)");
            Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
                partyFinderMonkeys.remove(matcher.group(1));
            }
        }
        if(partyFinderJoin) {
            String className = message.split(" ")[8].replace("(","");
            String classLvl = message.split(" ")[10].replace(")","");
            String playerName = message.split(" ")[3];

            PartyFinderMonkey monkey = new PartyFinderMonkey(playerName,classLvl,className);
            partyFinderMonkeys.put(playerName,monkey);
        }
    }

    private void showDungeonPlayerInfo(String name) {
        new Thread(() -> {
            // Get UUID for Hypixel API requests
            String uuid = APIUtils.getUUID(name);

            // Find stats of latest profile
            String latestProfile = APIUtils.getLatestProfileID(uuid);
            if (latestProfile == null) return;

            String profileURL = "https://api.hypixel.net/skyblock/profile?profile=" + latestProfile+"#PartyFinderJoinMsg";
            JsonObject profileResponse = APIUtils.getJSONResponse(profileURL);
            if (!profileResponse.get("success").getAsBoolean()) {
                String reason = profileResponse.get("cause").getAsString();
                Utils.sendMessage(ChatFormatting.RED + "Failed with reason: " + reason);
                return;
            }

            String playerURL = "https://api.hypixel.net/player?uuid=" + uuid+"#PartyFinderJoinMsg";
            System.out.println("Fetching player data...");
            JsonObject playerResponse = APIUtils.getJSONResponse(playerURL);
            JsonObject profilePlayerResponse = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject();

            if(!playerResponse.get("success").getAsBoolean()){
                String reason = profileResponse.get("cause").getAsString();
                Utils.sendMessage(ChatFormatting.RED + "Failed with reason: " + reason);
                return;
            }

            JsonObject dungeonsObject = profilePlayerResponse.getAsJsonObject().get("dungeons").getAsJsonObject();
            JsonObject catacombsObject = dungeonsObject.get("dungeon_types").getAsJsonObject().get("catacombs").getAsJsonObject();
            double catacombs = Utils.xpToDungeonsLevel(catacombsObject.get("experience").getAsDouble());
            int secrets = playerResponse.get("player").getAsJsonObject().get("achievements").getAsJsonObject().get("skyblock_treasure_hunter").getAsInt();

            String armourBase64 = profilePlayerResponse.getAsJsonObject().get("inv_armor").getAsJsonObject().get("data").getAsString();
            InputStream armourStream = new ByteArrayInputStream(Base64.getDecoder().decode(armourBase64));

            try {
                String inventoryBase64 = profilePlayerResponse.getAsJsonObject().get("inv_contents").getAsJsonObject().get("data").getAsString();
                InputStream inventoryStream = new ByteArrayInputStream(Base64.getDecoder().decode(inventoryBase64));

                NBTTagCompound armour = CompressedStreamTools.readCompressed(armourStream);
                NBTTagList armourList = armour.getTagList("i", 10);

                String weapon = ChatFormatting.RED + "None";
                String weaponLore = ChatFormatting.RED + "None";

                if(!profilePlayerResponse.getAsJsonObject().has("inv_contents")) {
                    weapon = ChatFormatting.RED+"This player has there API disabled!";
                    weaponLore = ChatFormatting.RED+"This player has there API disabled!";
                } else {
                    NBTTagCompound inventory = CompressedStreamTools.readCompressed(inventoryStream);
                    NBTTagList inventoryList = inventory.getTagList("i", 10);

                    for (int i = 0; i < inventoryList.tagCount(); i++) {
                        NBTTagCompound item = inventoryList.getCompoundTagAt(i);
                        if (item.hasNoTags()) continue;
                        NBTTagCompound display = item.getCompoundTag("tag").getCompoundTag("display");
                        String itemName = item.getCompoundTag("tag").getCompoundTag("display").getString("Name");
                        String itemLore = "";
                        if (display.hasKey("Lore", ItemUtils.NBT_LIST)) {
                            NBTTagList lore = display.getTagList("Lore", ItemUtils.NBT_STRING);

                            List<String> loreAsList = new ArrayList<>();
                            for (int lineNumber = 0; lineNumber < lore.tagCount(); lineNumber++) {
                                loreAsList.add(lore.getStringTagAt(lineNumber));
                            }

                            itemLore = itemName+"\n"+String.join("\n",Collections.unmodifiableList(loreAsList));
                        }
                        // NBT is served boots -> helmet
                        if (i == 0) {
                            weapon = itemName;
                            weaponLore = itemLore;
                        } else {
                            System.err.println("An error has occurred.");
                        }
                    }
                    inventoryStream.close();
                }

                int magicPower = profilePlayerResponse.get("accessory_bag_storage").getAsJsonObject().get("highest_magical_power").getAsInt();

                String helmet = ChatFormatting.RED + "None";
                String chest = ChatFormatting.RED + "None";
                String legs = ChatFormatting.RED + "None";
                String boots = ChatFormatting.RED + "None";
                String helmetLore = ChatFormatting.RED + "None";
                String chestLore = ChatFormatting.RED + "None";
                String legsLore = ChatFormatting.RED + "None";
                String bootsLore = ChatFormatting.RED + "None";
                // Loop through armour
                for (int i = 0; i < armourList.tagCount(); i++) {
                    NBTTagCompound armourPiece = armourList.getCompoundTagAt(i);
                    if (armourPiece.hasNoTags()) continue;
                    NBTTagCompound display = armourPiece.getCompoundTag("tag").getCompoundTag("display");
                    String armourPieceName = armourPiece.getCompoundTag("tag").getCompoundTag("display").getString("Name");
                    String armourPieceLore = "";
                    if (display.hasKey("Lore", ItemUtils.NBT_LIST)) {
                        NBTTagList lore = display.getTagList("Lore", ItemUtils.NBT_STRING);

                        List<String> loreAsList = new ArrayList<>();
                        for (int lineNumber = 0; lineNumber < lore.tagCount(); lineNumber++) {
                            loreAsList.add(lore.getStringTagAt(lineNumber));
                        }

                        armourPieceLore = armourPieceName+"\n"+String.join("\n",Collections.unmodifiableList(loreAsList));
                    }
                    // NBT is served boots -> helmet
                    switch (i) {
                        case 0:
                            boots = armourPieceName;
                            bootsLore = armourPieceLore;
                            break;
                        case 1:
                            legs = armourPieceName;
                            legsLore = armourPieceLore;
                            break;
                        case 2:
                            chest = armourPieceName;
                            chestLore = armourPieceLore;
                            break;
                        case 3:
                            helmet = armourPieceName;
                            helmetLore = armourPieceLore;
                            break;
                        default:
                            System.err.println("An error has occurred.");
                            break;
                    }
                }
                armourStream.close();

                ChatComponentText nameComponent = new ChatComponentText(ChatFormatting.AQUA+" Data For: " +ChatFormatting.YELLOW+ name + "\n ");
                ChatComponentText kickComponent = new ChatComponentText("\n"+ChatFormatting.GREEN+"Click here to remove "+ChatFormatting.LIGHT_PURPLE+name+ChatFormatting.GREEN+" from the party");
                ChatComponentText weaponComponent = new ChatComponentText(ChatFormatting.DARK_AQUA + weapon + "\n ");
                ChatComponentText helmetComponent = new ChatComponentText(" "+ChatFormatting.DARK_AQUA + helmet + "\n ");
                ChatComponentText chestComponent = new ChatComponentText(ChatFormatting.DARK_AQUA + chest + "\n ");
                ChatComponentText legComponent = new ChatComponentText(ChatFormatting.DARK_AQUA + legs + "\n ");
                ChatComponentText bootComponent = new ChatComponentText(ChatFormatting.DARK_AQUA + boots + "\n ");

                weaponComponent.setChatStyle(weaponComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(weaponLore))));
                helmetComponent.setChatStyle(helmetComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(helmetLore))));
                chestComponent.setChatStyle(chestComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(chestLore))));
                legComponent.setChatStyle(legComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(legsLore))));
                kickComponent.setChatStyle(kickComponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/p kick "+name)));
                kickComponent.setChatStyle(kickComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ChatComponentText(ChatFormatting.YELLOW+"/p kick "+name))));

                bootComponent.setChatStyle(bootComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(bootsLore))));

                StringBuilder completionsHoverString = new StringBuilder();
                int highestFloor = catacombsObject.get("highest_tier_completed").getAsInt();
                JsonObject completionObj = catacombsObject.get("tier_completions").getAsJsonObject();
                int totalRuns = 1;
                for (int i = 0; i <= highestFloor; i++) {
                    completionsHoverString
                            .append(ChatFormatting.AQUA)
                            .append(i == 0 ? "Entrance: " : "Floor " + i + ": ")
                            .append(ChatFormatting.YELLOW)
                            .append(completionObj.get(String.valueOf(i)).getAsInt())
                            .append(i < highestFloor ? "\n": "");

                    totalRuns = totalRuns + completionObj.get(String.valueOf(i)).getAsInt();
                }
                completionsHoverString.append("\n" + ChatFormatting.GOLD + "Total: " + ChatFormatting.RESET).append(totalRuns);

                ChatComponentText completions = new ChatComponentText(ChatFormatting.AQUA + " Floor Completions: "+ChatFormatting.GRAY+"(Hover)");

                completions.setChatStyle(completions.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(completionsHoverString.toString()))));
                String delimiter = ChatFormatting.RED.toString() + ChatFormatting.STRIKETHROUGH + ChatFormatting.BOLD + "---------------------------";

                Utils.sendMessage(
                        new ChatComponentText(delimiter)
                                .appendText("\n")
                                .appendSibling(nameComponent)
                                .appendText(ChatFormatting.GREEN+"☠ Cata Level: "+ChatFormatting.YELLOW+catacombs+"\n")
                                .appendText(ChatFormatting.GREEN+" • Total Secrets Found: "+ChatFormatting.YELLOW+secrets+"\n")
                                .appendText(ChatFormatting.GREEN+" • Average Secrets: "+ChatFormatting.YELLOW+((secrets/totalRuns))+"\n")
                                .appendText(ChatFormatting.GRAY+" • Magic Power: "+ChatFormatting.GOLD+magicPower+"\n\n")
                                .appendSibling(helmetComponent)
                                .appendSibling(chestComponent)
                                .appendSibling(legComponent)
                                .appendSibling(bootComponent)
                                .appendSibling(weaponComponent)
                                .appendText("\n")
                                .appendSibling(completions)
                                .appendText("\n")
                                .appendSibling(new ChatComponentText(delimiter))
                                .appendSibling(kickComponent)
                );
            } catch (IOException ex) {
                Utils.sendMessage(ChatFormatting.RED+"Error! This player may not have there API on.");
            }
        }).start();
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
            List<String> lines = new ArrayList<>(Collections.singletonList(
                    "§9§lDungeon Party"
            ));
            // Sort by when monkey joined
            List<PartyFinderMonkey> sorted = new ArrayList<>(partyFinderMonkeys.values());
            sorted.sort((a, b) -> Math.toIntExact(b.addedAt - a.addedAt));
            for (PartyFinderMonkey monkey : sorted) {
                stillNeededClasses.remove(monkey.selectedClass);
                boolean dupe = partyFinderMonkeys.values().stream().anyMatch((a)->{return !a.name.equals(monkey.name) && Objects.equals(a.selectedClass, monkey.selectedClass);});
                if(SkyblockFeatures.config.dungeonPartyDisplayDupes && dupe) dupe=false;

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
            GuiUtils.drawTextLines(lines,0,0, GuiUtils.TextStyle.DROP_SHADOW);
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
            GuiUtils.drawTextLines(Arrays.asList(lines),0,0, GuiUtils.TextStyle.DROP_SHADOW);
        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.dungeonPartyDisplay;
        }

        @Override
        public int getHeight() {
            return (Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)*6;
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
            Utils.setTimeout(()->{
                canRefresh = true;
            }, 3300);
        }
    }
    ItemStack hoverItemStack = null;

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;
        if(!SkyblockFeatures.config.betterPartyFinder || !SkyblockFeatures.config.betterPartyFinderSideMenu) return;

        GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
        ContainerChest cont = (ContainerChest) chest.inventorySlots;
        String name = cont.getLowerChestInventory().getName();

        if(!HideGlass.isEmptyGlassPane(event.itemStack) && event.itemStack.getItem() instanceof ItemSkull && event.itemStack.getDisplayName().contains("'s Party")) {
            hoverItemStack = event.itemStack;
        }
        if("Party Finder".equals(name) && event.itemStack.getItem() instanceof ItemSkull) {
            if(event.itemStack.hasDisplayName() && event.itemStack.getDisplayName().contains("Your Party")) return;
            event.toolTip.clear();
        }
    }

    @SubscribeEvent
    public void onDrawContainerTitle(GuiContainerEvent.TitleDrawnEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiChest) || event.gui == null || !SkyblockFeatures.config.betterPartyFinder || !SkyblockFeatures.config.betterPartyFinderSideMenu) {
            return;
        }

        GuiChest chest = (GuiChest) mc.currentScreen;
        ContainerChest cont = (ContainerChest) chest.inventorySlots;
        String name = cont.getLowerChestInventory().getName();

        if (!"Party Finder".equals(name) || hoverItemStack == null || HideGlass.isEmptyGlassPane(hoverItemStack)) {
            return;
        }

        List<String> loreList = ItemUtils.getItemLore(hoverItemStack);
        List<String> filtered = new ArrayList<>(loreList);

        filtered.add(0,hoverItemStack.getDisplayName());
        filtered.removeIf(a -> a.contains("Click to join!"));

        GuiUtils.drawSideMenu(filtered, GuiUtils.TextStyle.BLACK_OUTLINE);
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
                if (display.hasNoTags()) continue;
                NBTTagList lore = display.getTagList("Lore", 8);
                int classLvReq = 0;
                int cataLvReq = 0;
                boolean Req = false;
                String note = "";
                List<String> memberStrings = new ArrayList<>();
                for (int n = 0; n < lore.tagCount(); n++) {
                    String str = lore.getStringTagAt(n);
                    if(str.startsWith("§7Dungeon Level Required: §b")) cataLvReq = Integer.parseInt(str.substring(28));
                    if(str.startsWith("§7Class Level Required: §b")) classLvReq = Integer.parseInt(str.substring(26));
                    if(str.startsWith("§7§7Note:")) note = Utils.cleanColor(str.substring(10));
                    if(str.startsWith("§cRequires")) Req = true;
                    if(str.contains("(")&&str.contains(")")&&str.contains(":")) {
                        memberStrings.add(Utils.cleanColor(str.split(" ")[2]));
                    }
                }

                int x = s.xDisplayPosition;
                int y = s.yDisplayPosition;
                boolean blockSlot = false;
                if (Req) {
                    blockSlot = true;
                } else {
                    if(memberStrings.contains(selfPlayerClass) && SkyblockFeatures.config.betterPartyFinderNoDupe) {
                        blockSlot = true;
                    } else if (note.toLowerCase().contains("car")) {
                        if(SkyblockFeatures.config.betterPartyFinderNoCarry) {
                            blockSlot = true;
                        } else {
                            fr.drawStringWithShadow("C", x + 1, y + 1, 0xFFFF0000);
                        }
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
                if(blockSlot) {
                    Gui.drawRect(x, y, x + 16, y + 16, 0x77AA0000);
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
