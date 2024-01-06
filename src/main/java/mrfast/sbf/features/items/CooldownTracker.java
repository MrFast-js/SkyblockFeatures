package mrfast.sbf.features.items;

import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.UseItemAbilityEvent;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.ScoreboardUtil;
import mrfast.sbf.utils.TabListUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;

public class CooldownTracker {
    // Stored values of each item and its default cooldown
    public static HashMap<String,CooldownItem> itemCooldowns = new HashMap<>();
    // currently stored and updated cooldowns
    public static HashMap<String,CooldownItem> activeCooldowns = new HashMap<>();

    public static class CooldownItem {
        String itemId;
        public ItemAbility sneakRightClick;
        public ItemAbility sneakLeftClick;
        public ItemAbility rightClick;
        public ItemAbility leftClick;

        public CooldownItem(String id) {
            this.itemId = id;
        }
    }

    public static class ItemAbility {
        public int cooldownSeconds;
        public double currentCount;
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
            if(this.cooldownSeconds-this.currentCount<=0) {
                this.currentCount=0;
                MinecraftForge.EVENT_BUS.post(new UseItemAbilityEvent(this));
            }
            this.counting = true;
            this.usedAt=System.currentTimeMillis();
        }
    }
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(event.phase != TickEvent.Phase.START || !Utils.inSkyblock || Utils.GetMC().theWorld==null) return;

        {
            for (CooldownItem cdItem:activeCooldowns.values()) {
                updateCooldown(cdItem.rightClick);
                updateCooldown(cdItem.leftClick);
                updateCooldown(cdItem.sneakRightClick);
                updateCooldown(cdItem.sneakLeftClick);
            }
        }

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
        cooldownReduction = -1;
    }
    int cooldownReduction = -1;
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

    private Integer getCooldownReduction() {
        for (String sidebarLine : ScoreboardUtil.getSidebarLines(true)) {
            if(sidebarLine.contains(Utils.GetMC().thePlayer.getName())) {
                int mageLvl = Integer.parseInt(sidebarLine.split(" ")[2].replaceAll("[^0-9]",""));
                return (int) Math.floor((double) mageLvl /2);
            }
        }
        return 0;
    }

    private boolean isMage() {
        if(TabListUtils.getTabEntries()==null) return false;
        for (NetworkPlayerInfo tabEntry : TabListUtils.getTabEntries()) {
            if(tabEntry==null||tabEntry.getDisplayName()==null) continue;
            if(tabEntry.getDisplayName().getUnformattedText().contains(Utils.GetMC().thePlayer.getName())) {
                return true;
            }
        }
        return false;
    }


    private boolean isUniqueDungeonClass() {
        int mages = 0;
        for (NetworkPlayerInfo tabEntry : TabListUtils.getTabEntries()) {
            try {
                String[] args = tabEntry.getDisplayName().getUnformattedText().split(" ");
                if (args[args.length - 1].contains("Mage")) {
                    mages++;
                }
            } catch (Exception e) {}
        }
        return mages<=1;
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

        if(clean.startsWith("Used") && Utils.inDungeons) {
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
                justUsedAbility.currentCount = justUsedAbility.cooldownSeconds-currentCooldown;
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

    private void updateCooldown(ItemAbility cooldownInfo) {
        if(cooldownInfo == null) return;
        if (cooldownInfo.counting) {
            double secondsToAdd = 0.05;
            if(SkyblockInfo.localLocation.contains("The Catacombs") && cooldownReduction==-1 && isMage()) {
                cooldownReduction = getCooldownReduction();
                if(isUniqueDungeonClass()) {
                    cooldownReduction+=25;
                }
                cooldownReduction+=25;
            }
            if(cooldownReduction!=-1) {
                secondsToAdd *= (100d+cooldownReduction)/cooldownReduction;
            }

            cooldownInfo.currentCount+=secondsToAdd;

            if (cooldownInfo.currentCount >= cooldownInfo.cooldownSeconds) {
                // Remove item from list using the iterator
                cooldownInfo.counting = false;
            }
        }
    }
}
