package mrfast.skyblockfeatures.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.utils.APIUtil;
import mrfast.skyblockfeatures.utils.ItemUtil;
import mrfast.skyblockfeatures.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

public class ChatEventListener {
    public static Minecraft mc = Minecraft.getMinecraft();

    public static boolean alreadySent = false;
    int barCount = 0;

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        try {
            barCount = 0;
        } catch(Exception e) {

        }
    }
    
    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.isOnHypixel()) return;
        String delimiter = EnumChatFormatting.RED.toString() + EnumChatFormatting.STRIKETHROUGH.toString() + "" + EnumChatFormatting.BOLD + "---------------------------";
        String unformatted = Utils.cleanColor(event.message.getUnformattedText());

        if(event.message.getFormattedText().contains(": ")) {
            if (SkyblockFeatures.config.DisguisePlayersAs == 8 && SkyblockFeatures.config.playerDiguiser && Utils.inSkyblock) {
                String name = event.message.getFormattedText().split(": ")[0];
                String message = event.message.getFormattedText().split(": ")[1];
                String monkiMessage = "";
                for(String word:message.split(" ")) {
                    List<String> words = Arrays.asList("Ooh","ooh","ah","Ee","Hoo","Grrr","uuh");
                    monkiMessage+=words.get((int) Utils.randomNumber(0, 6))+" ";
                }
                event.setCanceled(true);
                Utils.GetMC().thePlayer.addChatMessage(new ChatComponentText(name+": "+monkiMessage));
            }
        }

        if(unformatted.contains("Click here to warp to the dungeon!") && SkyblockFeatures.config.autoJoinDungeon)  {
            List<IChatComponent> chatComponents = event.message.getSiblings();
            for(IChatComponent chatComponent: chatComponents) {
                if(chatComponent.getChatStyle().getChatClickEvent()!=null) {
                    String joinDungeonCommand = chatComponent.getChatStyle().getChatClickEvent().getValue();
                    Utils.GetMC().thePlayer.sendChatMessage(joinDungeonCommand);
                    Utils.setTimeout(()->{
                        Utils.SendMessage(EnumChatFormatting.YELLOW + "Auto Joining Dungeon..");
                        Utils.playSound("random.orb", 1);
                    },10);
                    break;
                }
            }
        }

        if(unformatted.startsWith("You have joined ") && unformatted.contains("party!") && SkyblockFeatures.config.autoPartyChat) {
            Utils.GetMC().thePlayer.sendChatMessage("/chat p");
            Utils.setTimeout(()->{
                Utils.SendMessage(EnumChatFormatting.YELLOW + "Auto Joined Party Chat.");
            },10);
        }

        if (unformatted.startsWith("Your new API key is ")) {
            String apiKey = event.message.getSiblings().get(0).getChatStyle().getChatClickEvent().getValue();
            SkyblockFeatures.config.apiKey = apiKey;
            SkyblockFeatures.config.markDirty();
            SkyblockFeatures.config.writeData();
            Utils.SendMessage(EnumChatFormatting.GREEN + "Updated your Hypixel API key!");
        }

        if (unformatted.startsWith("Party Finder")) {
            String[] args = unformatted.split(" ");
            if(args[3] != null) {
                new Thread(() -> {
                    // Check key
                    String key = SkyblockFeatures.config.apiKey;
                    if (key.equals("")) {
                        Utils.SendMessage(EnumChatFormatting.RED + "API key not set. Use /setkey.");
                    }
                    
                    // Get UUID for Hypixel API requests
                    String username;
                    String uuid;
                    username = args[3];
                    uuid = APIUtil.getUUID(username);
                    
                    // Find stats of latest profile
                    String latestProfile = APIUtil.getLatestProfileID(uuid, key);
                    if (latestProfile == null) return;
        
                    String profileURL = "https://api.hypixel.net/skyblock/profile?profile=" + latestProfile;
                    JsonObject profileResponse = APIUtil.getJSONResponse(profileURL);
                    if (!profileResponse.get("success").getAsBoolean()) {
                        String reason = profileResponse.get("cause").getAsString();
                        Utils.SendMessage(EnumChatFormatting.RED + "Failed with reason: " + reason);
                        return;
                    }
        
                    String playerURL = "https://api.hypixel.net/player?uuid=" + uuid;
                    System.out.println("Fetching player data...");
                    JsonObject playerResponse = APIUtil.getJSONResponse(playerURL);
                    if(!playerResponse.get("success").getAsBoolean()){
                        String reason = profileResponse.get("cause").getAsString();
                        Utils.SendMessage(EnumChatFormatting.RED + "Failed with reason: " + reason);
                    }
                    int secrets = playerResponse.get("player").getAsJsonObject().get("achievements").getAsJsonObject().get("skyblock_treasure_hunter").getAsInt();
        
                    JsonObject dungeonsObject = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().get("dungeons").getAsJsonObject();
                    JsonObject catacombsObject = dungeonsObject.get("dungeon_types").getAsJsonObject().get("catacombs").getAsJsonObject();
                    double catacombs = Utils.xpToDungeonsLevel(catacombsObject.get("experience").getAsDouble());
                    
                    String armourBase64 = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().get("inv_armor").getAsJsonObject().get("data").getAsString();
                    InputStream armourStream = new ByteArrayInputStream(Base64.getDecoder().decode(armourBase64));
                    
                    try {
                        String inventoryBase64 = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().get("inv_contents").getAsJsonObject().get("data").getAsString();
                        InputStream inventoryStream = new ByteArrayInputStream(Base64.getDecoder().decode(inventoryBase64));
                    
                        NBTTagCompound armour = CompressedStreamTools.readCompressed(armourStream);
                        NBTTagList armourList = armour.getTagList("i", 10);
        
                        String weapon = EnumChatFormatting.RED + "None";
                        String weaponLore = EnumChatFormatting.RED + "None";
                        
                        if(!profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().has("inv_contents")) {
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
                                if (display.hasKey("Lore", ItemUtil.NBT_LIST)) {
                                    NBTTagList lore = display.getTagList("Lore", ItemUtil.NBT_STRING);
                    
                                    List<String> loreAsList = new ArrayList<>();
                                    for (int lineNumber = 0; lineNumber < lore.tagCount(); lineNumber++) {
                                        loreAsList.add(lore.getStringTagAt(lineNumber));
                                    }
                    
                                    itemLore = itemName+"\n"+String.join("\n",Collections.unmodifiableList(loreAsList));
                                }
                                // NBT is served boots -> helmet
                                switch (i) {
                                    case 0:
                                        weapon = itemName;
                                        weaponLore = itemLore;
                                        break;
                                    default:
                                        System.err.println("An error has occurred.");
                                        break;
                                }
                            }
                            inventoryStream.close();
                        }

                        String helmet = EnumChatFormatting.RED + "None";
                        String chest = EnumChatFormatting.RED + "None";
                        String legs = EnumChatFormatting.RED + "None";
                        String boots = EnumChatFormatting.RED + "None";
                        String helmetLore = EnumChatFormatting.RED + "None";
                        String chestLore = EnumChatFormatting.RED + "None";
                        String legsLore = EnumChatFormatting.RED + "None";
                        String bootsLore = EnumChatFormatting.RED + "None";
                        // Loop through armour
                        for (int i = 0; i < armourList.tagCount(); i++) {
                            NBTTagCompound armourPiece = armourList.getCompoundTagAt(i);
                            if (armourPiece.hasNoTags()) continue;
                            NBTTagCompound display = armourPiece.getCompoundTag("tag").getCompoundTag("display");
                            String armourPieceName = armourPiece.getCompoundTag("tag").getCompoundTag("display").getString("Name");
                            String armourPieceLore = "";
                            if (display.hasKey("Lore", ItemUtil.NBT_LIST)) {
                                NBTTagList lore = display.getTagList("Lore", ItemUtil.NBT_STRING);
                
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
        
        
                        ChatComponentText nameComponent = new ChatComponentText(EnumChatFormatting.AQUA+" Data For: " +EnumChatFormatting.YELLOW+ username + "\n ");
                        ChatComponentText kickComponent = new ChatComponentText("\n"+EnumChatFormatting.GREEN+"Click here to remove "+EnumChatFormatting.LIGHT_PURPLE+username+EnumChatFormatting.GREEN+" from the party");
                        ChatComponentText weaponComponent = new ChatComponentText(EnumChatFormatting.DARK_AQUA + weapon + "\n ");
                        ChatComponentText helmetComponent = new ChatComponentText(" "+EnumChatFormatting.DARK_AQUA + helmet + "\n ");
                        ChatComponentText chestComponent = new ChatComponentText(EnumChatFormatting.DARK_AQUA + chest + "\n ");
                        ChatComponentText legComponent = new ChatComponentText(EnumChatFormatting.DARK_AQUA + legs + "\n ");
                        ChatComponentText bootComponent = new ChatComponentText(EnumChatFormatting.DARK_AQUA + boots + "\n ");
        
                        weaponComponent.setChatStyle(weaponComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(weaponLore))));
                        helmetComponent.setChatStyle(helmetComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(helmetLore))));
                        chestComponent.setChatStyle(chestComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(chestLore))));
                        legComponent.setChatStyle(legComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(legsLore))));
                        kickComponent.setChatStyle(kickComponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/p kick "+username)));
                        bootComponent.setChatStyle(bootComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(bootsLore))));
                        
                        StringBuilder completionsHoverString = new StringBuilder();
                        int highestFloor = catacombsObject.get("highest_tier_completed").getAsInt();
                        JsonObject completionObj = catacombsObject.get("tier_completions").getAsJsonObject();
                        int totalRuns = 0;
                        for (int i = 0; i <= highestFloor; i++) {
                            completionsHoverString
                                    .append(EnumChatFormatting.GOLD)
                                    .append(i == 0 ? "Entrance: " : "Floor " + i + ": ")
                                    .append(EnumChatFormatting.RESET)
                                    .append(completionObj.get(String.valueOf(i)).getAsInt())
                                    .append(i < highestFloor ? "\n": "");
        
                            totalRuns = totalRuns + completionObj.get(String.valueOf(i)).getAsInt();
                        }
                        completionsHoverString.append("\n"+EnumChatFormatting.GOLD+"Total: "+ChatFormatting.RESET+totalRuns);
                        ChatComponentText completions = new ChatComponentText(EnumChatFormatting.AQUA + " Floor Completions: "+ChatFormatting.GRAY+"(Hover)");
        
                        completions.setChatStyle(completions.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(completionsHoverString.toString()))));
        
                        mc.thePlayer.addChatMessage(
                            new ChatComponentText(delimiter)
                            .appendText("\n")
                            .appendSibling(nameComponent)
                            .appendText(ChatFormatting.GREEN+"☠ Cata Level: "+ChatFormatting.YELLOW+catacombs+"\n")
                            .appendText(ChatFormatting.GREEN+" Total Secrets Found: "+ChatFormatting.YELLOW+secrets+"\n")
                            .appendText(ChatFormatting.GREEN+" Average Secrets: "+ChatFormatting.YELLOW+((secrets/totalRuns))+"\n\n")
                            .appendSibling(helmetComponent)
                            .appendSibling(chestComponent)
                            .appendSibling(legComponent)
                            .appendSibling(bootComponent)
                            .appendSibling(weaponComponent)
                            .appendText("\n")
                            .appendSibling(completions)
                            .appendText("\n")
                            .appendSibling(new ChatComponentText(delimiter))
                            .appendSibling(kickComponent));
                    } catch (IOException ex) {
                        System.out.println(ex);
                        Utils.SendMessage(ChatFormatting.RED+"Error! This player may not have there API on.");
                    }
                }).start();
            }
        }
        
        // Welcome message
        if (SkyblockFeatures.config.firstLaunch && unformatted.equals("Welcome to Hypixel SkyBlock!")) {
            Utils.SendMessage("§bThank You for download Skyblock Features! Do /sbf for config!");

            SkyblockFeatures.config.firstLaunch = false;
            SkyblockFeatures.config.markDirty();
            SkyblockFeatures.config.writeData();
        }
    }

    public int peoplejoined;
}
