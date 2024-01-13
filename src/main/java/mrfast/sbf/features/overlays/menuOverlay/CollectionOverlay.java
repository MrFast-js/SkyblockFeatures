package mrfast.sbf.features.overlays.menuOverlay;

import java.util.ArrayList;
import java.util.HashMap;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.GuiContainerEvent.TitleDrawnEvent;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.ItemUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CollectionOverlay {
    @SubscribeEvent
    public void titleDrawnEvent(TitleDrawnEvent event) {
        if(!SkyblockFeatures.config.collectionsLeaderboard || !event.displayName.contains("Collections") || !event.displayName.contains(" ")) return;
        HashMap<String,Integer> topCollectors = new HashMap<>();

        for (Slot slot : event.container.inventorySlots) {
            if(!slot.getHasStack()) continue;
            ItemStack itemStack = slot.getStack();
            if(itemStack==null) continue;
            boolean nextLine = false;
            for (String line : ItemUtils.getItemLore(itemStack)) {
                if(line.contains("Co-op Contributions")) {
                    nextLine = true;
                    continue;
                }
                if(nextLine) {
                    nextLine = false;
                    String player = line.split(":")[0];
                    if(topCollectors.containsKey(player)) topCollectors.put(player, topCollectors.get(player)+1);
                    else topCollectors.put(player, 1);
                }
            }
        }

        ArrayList<String> lines = new ArrayList<>();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableLighting();

        lines.add("§eTop Collectors");
        ArrayList<String> sorted = new ArrayList<>();
        
        for (String name : topCollectors.keySet()) {
            sorted.add(name+":"+topCollectors.get(name));
        }
        sorted.sort((a,b)->{
            int aInt = Integer.parseInt(a.split(":")[1]);
            int bInt = Integer.parseInt(b.split(":")[1]);
            return bInt-aInt; 
        });
        for (String entry : sorted) {
            String name = entry.split(":")[0];
            String value = entry.split(":")[1];
            String line = name+"§f: "+value;
            lines.add(line);
        }

        GuiUtils.drawSideMenu(lines, GuiUtils.TextStyle.DROP_SHADOW);
    }
}
