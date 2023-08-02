package mrfast.sbf.features.mining;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.TabListUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;

public class CommisionsTracker {
  private static final Minecraft mc = Minecraft.getMinecraft();

  static {
      new CommisionsTrackerGUI();
  }
  
  public static class CommisionsTrackerGUI extends UIElement {

      public CommisionsTrackerGUI() {
          super("Commissions Tracker", new Point(0.45052084f, 0.86944443f));
          SkyblockFeatures.GUIMANAGER.registerElement(this);
      }

      @Override
      public void drawElement() {
          ArrayList<String> text = new ArrayList<>();
          try {
              if(mc.thePlayer == null || !Utils.inSkyblock || (!SkyblockInfo.getInstance().getMap().equals("Crystal Hollows") && !SkyblockInfo.getInstance().map.equals("Dwarven Mines")) || !SkyblockFeatures.config.CommisionsTracker) return;

              text.add(ChatFormatting.BLUE+"Commissions");
              List<String> commissions = new ArrayList<String>();
              commissions.add(mc.ingameGUI.getTabList().getPlayerName(TabListUtils.getTabEntries().get(50)));
              commissions.add(mc.ingameGUI.getTabList().getPlayerName(TabListUtils.getTabEntries().get(51)));

              if(!Utils.cleanColor(mc.ingameGUI.getTabList().getPlayerName(TabListUtils.getTabEntries().get(52))).isEmpty()) {
                commissions.add(mc.ingameGUI.getTabList().getPlayerName(TabListUtils.getTabEntries().get(52)));
              }
              if(!Utils.cleanColor(mc.ingameGUI.getTabList().getPlayerName(TabListUtils.getTabEntries().get(53))).isEmpty()) {
                commissions.add(mc.ingameGUI.getTabList().getPlayerName(TabListUtils.getTabEntries().get(53)));
              }
              for(String commission : commissions) {
                commission = Utils.cleanColor(commission);
                if(commission.contains("Forges")) continue;
                Pattern regex = Pattern.compile("(\\d+(?:\\.\\d+)?)");
                Matcher matcher = regex.matcher(commission);
                if(commission.contains("2x")) {
                  matcher = regex.matcher(commission.replace("2x", ""));
                }
                
                if(matcher.find()) {
                    if(getTotal(commission)!=-1) {
                      String[] a = commission.split(" ");
                      String amount = Math.round(getTotal(commission) * (Double.valueOf(matcher.group(1)) / 100))+"";
                      String mid = ChatFormatting.LIGHT_PURPLE+"["+
                      ChatFormatting.GREEN+amount+
                      ChatFormatting.GOLD+"/"+
                      ChatFormatting.GREEN+getTotal(commission)+
                      ChatFormatting.LIGHT_PURPLE+"]";
                      commission = commission.replace(a[a.length-1], mid);
                    } else {
                      String[] a = commission.split(" ");
                      String amount = Math.round(getTotal(commission) * (Double.valueOf(matcher.group(1)) / 100))+"";
                      String mid = ChatFormatting.LIGHT_PURPLE+"["+
                      ChatFormatting.GREEN+amount+
                      ChatFormatting.GOLD+"/"+
                      ChatFormatting.GREEN+getTotal(commission)+
                      ChatFormatting.LIGHT_PURPLE+"]";
                      commission = commission.replace(a[a.length-1], mid);
                    }
                } else if(commission.contains("DONE")) {
                  commission = commission.replace("DONE", ChatFormatting.GREEN+"DONE");
                }
                text.add(ChatFormatting.AQUA+commission);
              }
            } catch (Exception e) {
              
          }

          for (int i = 0; i < text.size(); i++) {
            Utils.drawTextWithStyle3(text.get(i), 0, i * 10);
          }
      }

      @Override
      public void drawElementExample() {
          ArrayList<String> text = new ArrayList<>();
          text.add(ChatFormatting.BLUE+"Commissions");
          text.add(" Upper Mines Titanium: "+ChatFormatting.LIGHT_PURPLE+"["+ChatFormatting.GREEN+"7"+ChatFormatting.GOLD+"/"+ChatFormatting.GREEN+"10"+ChatFormatting.LIGHT_PURPLE+"]");
          text.add(" Goblin Raid: "+ChatFormatting.LIGHT_PURPLE+"["+ChatFormatting.GREEN+"0"+ChatFormatting.GOLD+"/"+ChatFormatting.GREEN+"1"+ChatFormatting.LIGHT_PURPLE+"]");

          for (int i = 0; i < text.size(); i++) {
            Utils.drawTextWithStyle3(text.get(i), 0, i * 10);
          }
      }

      @Override
      public boolean getToggled() {
          return Utils.inSkyblock && SkyblockFeatures.config.CommisionsTracker;
      }

      @Override
      public int getHeight() {
          return Utils.GetMC().fontRendererObj.FONT_HEIGHT*3;
      }

      @Override
      public int getWidth() {
          return Utils.GetMC().fontRendererObj.getStringWidth("2x Mithril Powder Collector [350/500] ");
      }
  }


  public static int getTotal(String str) {
    if(str.contains("Ice Walker")) return 50;
    if(str.contains("Golden Goblin Slayer")) return 1;
    if(str.contains("Goblin Slayer")) return 100;
    if(str.contains("Powder Ghast Puncher")) return 5;
    if(str.contains("Star Sentry Puncher")) return 10;
    if(str.contains("2x Mithril Powder Collector")) return 500;

    if(str.contains("Raffle")) {
      if(str.contains("Lucky")) return 20;
      return 1;
    }
    if(str.contains("Goblin Raid")) {
      if(str.contains("Slayer")) return 20;
      return 1;
    }
    if(str.contains("Mithril")) {
      if(str.contains("Miner")) return 500;
      return 350;
    }
    if(str.contains("Titanium")) {
      if(str.contains("Miner")) return 15;
      return 10;
    }

    // Crystal Hollows
    if(str.contains("Hard Stone Miner") || str.contains("Gemstone Collector")) return 1000;
    if(str.contains("Chest Looter")) return 3;
    if(str.contains("Treasurite")) return 13;
    if(str.contains("Sludge")) return 25;
    if(str.contains("Yog") || str.contains("Automaton") || str.contains("Goblin Slayer")) return 13;
    if(str.contains("Thyst")) return 5;
    if(str.contains("Crystal Hunter")) return 1;
    if(str.contains("Corleone")) return 1;

    return -1;
  }

}
