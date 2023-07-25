package mrfast.skyblockfeatures.features.overlays;

import java.util.ArrayList;
import java.util.HashMap;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.events.GuiContainerEvent.TitleDrawnEvent;
import mrfast.skyblockfeatures.features.dungeons.Reparty;
import mrfast.skyblockfeatures.utils.ItemUtil;
import mrfast.skyblockfeatures.utils.NumberUtil;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CollectionOverlay {
    @SubscribeEvent
    public void titleDrawnEvent(TitleDrawnEvent event) {
        if(!SkyblockFeatures.config.collecitonsLeaderboard || !event.displayName.contains("Collections") || !event.displayName.contains(" ")) return;
        HashMap<String,Integer> topCollectors = new HashMap<>();

        for (Slot slot : event.container.inventorySlots) {
            if(!slot.getHasStack()) continue;
            ItemStack itemStack = slot.getStack();
            if(itemStack==null) continue;
            boolean nextLine = false;
            for (String line : ItemUtil.getItemLore(itemStack)) {
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
        Utils.drawGraySquareWithBorder(180, 0, 150, (topCollectors.keySet().size()+4)*Utils.GetMC().fontRendererObj.FONT_HEIGHT,3);

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
        
        int lineCount = 0;
        for(String line:lines) {
            Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 190, lineCount*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)+10, -1);
            lineCount++;
        }
    }
}
