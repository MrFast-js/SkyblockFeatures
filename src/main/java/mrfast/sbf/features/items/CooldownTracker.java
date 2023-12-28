package mrfast.sbf.features.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.events.SkyblockMobEvent;
import mrfast.sbf.events.UseItemAbilityEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

public class CooldownTracker {
    public static HashMap<String,CooldownItem> itemCooldowns = new HashMap<>();
    public static HashMap<String,CooldownItem> activeCooldowns = new HashMap<>();
    public static HashMap<String,ItemAbility> endedCooldowns = new HashMap<>();

    public static class CooldownItem {
        String itemId;
        ItemAbility sneakRightClick;
        ItemAbility sneakLeftClick;
        ItemAbility rightClick;
        ItemAbility leftClick;

        public CooldownItem(String id) {
            this.itemId = id;
        }
    }

    public static class ItemAbility {
        public int cooldownSeconds;
        public int currentCount;
        public boolean counting = false;
        public long usedAt;
        public String itemId;
        public String abilityName;
        public String type;
        public ItemAbility(String id) {
            this.itemId=id;
            this.usedAt=System.currentTimeMillis();
        }
        public void reset() {
            if(this.cooldownSeconds-this.currentCount==0) {
                this.currentCount=0;
                MinecraftForge.EVENT_BUS.post(new UseItemAbilityEvent(this));
            }
            this.counting = true;
            this.usedAt=System.currentTimeMillis();
        }
    }
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(!Utils.inSkyblock || Utils.GetMC().theWorld==null) return;

        activeCooldowns.clear();
        for (int i = 0; i < 8; i++) {
            if(Utils.GetMC().thePlayer.inventory.mainInventory[i]==null) continue;
            ItemStack stack = Utils.GetMC().thePlayer.inventory.mainInventory[i];
            setStackCooldown(stack);
            String skyblockId = ItemUtils.getSkyBlockItemID(stack);
            if(skyblockId!=null && itemCooldowns.get(skyblockId)!=null) {
                activeCooldowns.put(skyblockId, itemCooldowns.get(skyblockId));
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        activeCooldowns.clear();
        itemCooldowns.clear();
    }

    public void setStackCooldown(ItemStack item) {
        if(!Utils.inSkyblock || Utils.GetMC().theWorld==null) return;

        String skyblockId = ItemUtils.getSkyBlockItemID(item);
        if(skyblockId==null) return;

        if (itemCooldowns.containsKey(skyblockId)) {
            return;
        }

        CooldownItem cdItem = new CooldownItem(skyblockId);
        boolean nextLineRight = false;
        boolean nextLineLeft = false;
        boolean nextLineSneakRight = false;
        boolean nextLineSneakLeft = false;
        String nextAbilityName = "";

        for(String line: ItemUtils.getItemLore(item)) {
            line = Utils.cleanColor(line);
            if(line.contains("Ability: ")) {
                nextAbilityName = line.split(": ")[1].split("  ")[0];
                if(line.endsWith("RIGHT CLICK")) nextLineRight = true;
                if(line.endsWith("LEFT CLICK")) nextLineLeft = true;
                if(line.endsWith("SNEAK RIGHT CLICK")) nextLineSneakRight = true;
                if(line.endsWith("SNEAK LEFT CLICK")) nextLineSneakLeft = true;
            }

            if(line.contains("Cooldown: ") && (nextLineRight||nextLineLeft||nextLineSneakRight||nextLineSneakLeft)) {
                int seconds = 0;
                try {seconds = Integer.parseInt(line.replaceAll("[^0-9]", ""));} catch (Exception ignored) {}

                ItemAbility ability = new ItemAbility(skyblockId);
                ability.counting=true;
                ability.abilityName=nextAbilityName;
                if(nextLineRight) {
                    nextLineRight = false;
                    ability.type="RIGHT";
                    cdItem.rightClick = ability;
                    cdItem.rightClick.cooldownSeconds=seconds;
                }
                if(nextLineLeft) {
                    nextLineLeft = false;
                    ability.type="LEFT";
                    cdItem.leftClick = ability;
                    cdItem.leftClick.cooldownSeconds=seconds;
                }
                if(nextLineSneakRight) {
                    nextLineSneakRight = false;
                    ability.type="SNEAK RIGHT";
                    cdItem.sneakLeftClick = ability;
                    cdItem.sneakRightClick.cooldownSeconds=seconds;
                }
                if(nextLineSneakLeft) {
                    nextLineSneakLeft = false;
                    ability.type="SNEAK LEFT";
                    cdItem.sneakRightClick = ability;
                    cdItem.sneakLeftClick.cooldownSeconds=seconds;
                }
            }
        }
        if(cdItem.rightClick !=null || cdItem.leftClick!=null || cdItem.sneakRightClick!=null || cdItem.sneakLeftClick!=null) {
            itemCooldowns.put(skyblockId,cdItem);
        }
    }

    static ItemAbility justUsedAbility;

    @SubscribeEvent
    public void onMouseClick(MouseEvent event) {
        if(!Utils.inSkyblock || Utils.GetMC().theWorld==null) return;

        ItemStack heldItem = Utils.GetMC().thePlayer.getHeldItem();
        if(heldItem==null) return;
        String skyblockId = ItemUtils.getSkyBlockItemID(heldItem);
        if(skyblockId==null || !itemCooldowns.containsKey(skyblockId)) return;
        CooldownItem cdItem = itemCooldowns.get(skyblockId);

        boolean sneaking = Utils.GetMC().thePlayer.isSneaking();
        if (event.button == 0 && event.buttonstate) {
            // Left mouse button pressed
            if(cdItem.leftClick!=null && (cdItem.sneakLeftClick == null || !sneaking)) {
                justUsedAbility = cdItem.leftClick;
                cdItem.leftClick.reset();
                activeCooldowns.put(skyblockId,cdItem);
            }
            if(cdItem.sneakLeftClick!=null && sneaking) {
                justUsedAbility = cdItem.sneakLeftClick;
                cdItem.sneakLeftClick.reset();
                activeCooldowns.put(skyblockId,cdItem);
            }
        } else if (event.button == 1 && event.buttonstate) {
            // Right mouse button pressed
            if(cdItem.rightClick!=null && (!sneaking || cdItem.sneakRightClick==null)) {
                justUsedAbility = cdItem.rightClick;
                cdItem.rightClick.reset();
                activeCooldowns.put(skyblockId,cdItem);
            }
            if(cdItem.sneakRightClick!=null && sneaking) {
                justUsedAbility = cdItem.sneakRightClick;
                cdItem.sneakRightClick.reset();
                activeCooldowns.put(skyblockId,cdItem);
            }
        }
    }
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String clean = Utils.cleanColor(event.message.getUnformattedText());

        if(clean.startsWith("Used")) {
            justUsedAbility = new ItemAbility("Dungeon_Ability");
        }
        if(justUsedAbility!=null) {
            ItemStack heldItem = Utils.GetMC().thePlayer.getHeldItem();
            if(heldItem==null) return;
            String skyblockId = ItemUtils.getSkyBlockItemID(heldItem);
            if(!justUsedAbility.itemId.equals(skyblockId)) return;

            if(clean.startsWith("This ability is on cooldown for")) {
                if(System.currentTimeMillis()-justUsedAbility.usedAt>300) {
                    return;
                }
                int currentCooldown = Integer.parseInt(clean.replaceAll("[^0-9]",""));
                justUsedAbility.currentCount= justUsedAbility.cooldownSeconds-currentCooldown;
                CooldownItem item = activeCooldowns.get(justUsedAbility.itemId);

                if(justUsedAbility.type.equals("RIGHT")) item.rightClick=justUsedAbility;
                if(justUsedAbility.type.equals("SNEAK RIGHT")) item.sneakRightClick=justUsedAbility;
                if(justUsedAbility.type.equals("LEFT")) item.leftClick=justUsedAbility;
                if(justUsedAbility.type.equals("SNEAK LEFT")) item.sneakLeftClick=justUsedAbility;

                // update cooldown
                activeCooldowns.put(justUsedAbility.itemId,item);
            }
        }
    }
    @SubscribeEvent
    public void onSecond(SecondPassedEvent event) {
        if(!Utils.inSkyblock || Utils.GetMC().theWorld==null) return;

        for (CooldownItem cdItem:activeCooldowns.values()) {
            updateCooldown(cdItem.rightClick);
            updateCooldown(cdItem.leftClick);
            updateCooldown(cdItem.sneakRightClick);
            updateCooldown(cdItem.sneakLeftClick);
        }
        for(ItemAbility ability:endedCooldowns.values()) {
            CooldownItem item = activeCooldowns.get(ability.itemId);

            if(ability.type.equals("RIGHT")) {
                item.rightClick.currentCount=0;
                item.rightClick.counting=false;
            }
            if(ability.type.equals("SNEAK RIGHT")) {
                item.sneakRightClick.currentCount=0;
                item.rightClick.counting=false;

            }
            if(ability.type.equals("LEFT")) {
                item.leftClick.currentCount=0;
                item.rightClick.counting=false;

            }
            if(ability.type.equals("SNEAK LEFT")) {
                item.sneakLeftClick.currentCount=0;
                item.rightClick.counting=false;
            }
        }
        endedCooldowns.clear();
    }

    private void updateCooldown(ItemAbility cooldownInfo) {
        if(cooldownInfo == null) return;
        if (cooldownInfo.counting) {
            cooldownInfo.currentCount++;
            if (cooldownInfo.currentCount >= cooldownInfo.cooldownSeconds) {
                // Remove item from list using the iterator
                cooldownInfo.counting = false;
            }
        }
    }

    public static String getCount(int max,int count) {
        if(max-count==0) {
            return ChatFormatting.GREEN+"✔";
        }
        return (max-count)+"s";
    }

    static {
        new cooldownDisplay();
    }

    public static class cooldownDisplay extends UIElement {
        public cooldownDisplay() {
            super("Cooldown Display", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(activeCooldowns.isEmpty()) return;

            List<String> lines = new ArrayList<>(Collections.singletonList(
                    "§3§lCooldowns"
            ));
            for(CooldownItem cdItem:activeCooldowns.values()) {
                if(cdItem.leftClick!=null) {
                    String cooldown = getCount(cdItem.leftClick.cooldownSeconds,cdItem.leftClick.currentCount);
                    lines.add(" "+ChatFormatting.GOLD+cdItem.leftClick.abilityName+ChatFormatting.YELLOW+" "+cooldown);
                }

                if(cdItem.rightClick!=null) {
                    String cooldown = getCount(cdItem.rightClick.cooldownSeconds,cdItem.rightClick.currentCount);
                    lines.add(" "+ChatFormatting.GOLD+cdItem.rightClick.abilityName+ChatFormatting.YELLOW+" "+cooldown);
                }

                if(cdItem.sneakLeftClick!=null) {
                    String cooldown = getCount(cdItem.sneakLeftClick.cooldownSeconds,cdItem.sneakLeftClick.currentCount);
                    lines.add(" "+ChatFormatting.GOLD+cdItem.sneakLeftClick.abilityName+ChatFormatting.YELLOW+" "+cooldown);
                }

                if(cdItem.sneakRightClick!=null) {
                    String cooldown = getCount(cdItem.sneakRightClick.cooldownSeconds,cdItem.sneakRightClick.currentCount);
                    lines.add(" "+ChatFormatting.GOLD+cdItem.sneakRightClick.abilityName+ChatFormatting.YELLOW+" "+cooldown);
                }
            }

            GuiUtils.drawTextLines(lines,0,0, GuiUtils.TextStyle.DROP_SHADOW);
        }
        @Override
        public void drawElementExample() {
            String[] lines = {
                    "§3§lCooldowns",
                    " §6Throw §e4s",
                    " §6Ragnarok §a✔",
                    " §6Mining Speed Boost §a✔",
                    " §6Creeper Veil §e8s",
            };
            GuiUtils.drawTextLines(Arrays.asList(lines),0,0, GuiUtils.TextStyle.DROP_SHADOW);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.cooldownDisplay;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inSkyblock;
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

}
