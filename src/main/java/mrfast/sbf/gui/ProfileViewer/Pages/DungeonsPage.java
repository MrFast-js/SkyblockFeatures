package mrfast.sbf.gui.ProfileViewer.Pages;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.components.UIBlock;
import gg.essential.elementa.components.UIRoundedRectangle;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.constraints.CenterConstraint;
import gg.essential.elementa.constraints.PixelConstraint;
import gg.essential.elementa.constraints.RelativeConstraint;
import gg.essential.elementa.constraints.SiblingConstraint;
import mrfast.sbf.gui.ProfileViewer.ProfileViewerGui;
import mrfast.sbf.gui.ProfileViewer.ProfileViewerUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static mrfast.sbf.gui.ProfileViewer.ProfileViewerGui.*;

public class DungeonsPage extends ProfileViewerGui.ProfileViewerPage {
    @Override
    public void loadPage() {
        UIComponent loadingText = new UIText(ChatFormatting.RED + "Loading")
                .setTextScale(new PixelConstraint(2f))
                .setChildOf(this.mainComponent)
                .setX(new CenterConstraint())
                .setY(new CenterConstraint());
        JsonObject ProfilePlayerResponse = ProfileViewerGui.ProfilePlayerResponse;

        new Thread(() -> {
            // Wait for soopy before displaying the page
            double loadingIndex = 0;
            while (soopyProfiles.entrySet().isEmpty()) {
                if (!ProfileViewerGui.selectedCategory.equals("Dungeons")) break;

                try {
                    ((UIText) loadingText).setText(ProfileViewerUtils.loadingStages[(int) Math.floor(loadingIndex)]);
                    loadingIndex += 0.5;
                    loadingIndex %= 3;
                    Thread.sleep(100);
                } catch (Exception ignored) {
                }
            }

            try {
                String id = ProfileResponse.get("profile_id").toString().replaceAll("\"", "").replace("-", "");
                JsonObject profile = soopyProfiles.get(id).getAsJsonObject();
                JsonObject members = profile.get("members").getAsJsonObject();
                JsonObject player = members.get(playerUuid).getAsJsonObject();
                UIComponent statsAreaLeft = new UIBlock(clear)
                        .setX(new PixelConstraint(0f))
                        .setY(new RelativeConstraint(0f))
                        .setChildOf(this.mainComponent)
                        .setWidth(new RelativeConstraint(0.95f * 0.50f))
                        .setHeight(new RelativeConstraint(0.4f));

                UIComponent statsAreaRight = new UIBlock(clear)
                        .setX(new RelativeConstraint(0.50f))
                        .setY(new RelativeConstraint(0f))
                        .setChildOf(this.mainComponent)
                        .setWidth(new RelativeConstraint(0.95f * 0.5f))
                        .setHeight(new RelativeConstraint((0.4f)));

                JsonObject dungeons = player.get("dungeons").getAsJsonObject();
                JsonObject floorStats = dungeons.get("floorStats").getAsJsonObject();

                Integer secrets = 0;
                try {
                    secrets = HypixelPlayerResponse.get("achievements").getAsJsonObject().get("skyblock_treasure_hunter").getAsInt();
                } catch (Exception ignored) {
                }

                String selectedClass = ProfileViewerUtils.formatTitle(dungeons.get("selected_class").getAsString());

                JsonObject classes = dungeons.get("class_levels").getAsJsonObject();
                JsonObject archerClass = classes.get("archer").getAsJsonObject();
                JsonObject healerClass = classes.get("healer").getAsJsonObject();
                JsonObject mageClass = classes.get("mage").getAsJsonObject();
                JsonObject berserkClass = classes.get("berserk").getAsJsonObject();
                JsonObject tankClass = classes.get("tank").getAsJsonObject();
                double catacombsLevel = dungeons.get("catacombs_level").getAsDouble();
                double catacombsXp = dungeons.get("catacombs_xp").getAsDouble();


                double curCataXp = ProfileViewerUtils.getLeftoverCataXP((int) catacombsXp, (int) catacombsLevel);
                double curBersXp = ProfileViewerUtils.getLeftoverCataXP(berserkClass.get("xp").getAsInt(), berserkClass.get("level").getAsInt());
                double curArchXp = ProfileViewerUtils.getLeftoverCataXP(archerClass.get("xp").getAsInt(), archerClass.get("level").getAsInt());
                double curHealXp = ProfileViewerUtils.getLeftoverCataXP(healerClass.get("xp").getAsInt(), healerClass.get("level").getAsInt());
                double curMageXp = ProfileViewerUtils.getLeftoverCataXP(mageClass.get("xp").getAsInt(), mageClass.get("level").getAsInt());
                double curTankXp = ProfileViewerUtils.getLeftoverCataXP(tankClass.get("xp").getAsInt(), tankClass.get("level").getAsInt());

                int nextCataXp = ProfileViewerUtils.getNextCataLevelXP((int) catacombsLevel);
                int nextBersXp = ProfileViewerUtils.getNextCataLevelXP(berserkClass.get("level").getAsInt());
                int nextArchXp = ProfileViewerUtils.getNextCataLevelXP(archerClass.get("level").getAsInt());
                int nextHealXp = ProfileViewerUtils.getNextCataLevelXP(healerClass.get("level").getAsInt());
                int nextMageXp = ProfileViewerUtils.getNextCataLevelXP(mageClass.get("level").getAsInt());
                int nextTankXp = ProfileViewerUtils.getNextCataLevelXP(tankClass.get("level").getAsInt());

                this.mainComponent.removeChild(loadingText);
                drawProgressbar((int) curCataXp, nextCataXp, statsAreaLeft, "Catacombs " + (int) catacombsLevel, new ItemStack(Items.skull), null, false);
                drawProgressbar((int) curArchXp, nextArchXp, statsAreaLeft, "Archer " + archerClass.get("level").getAsInt(), new ItemStack(Items.bow), null, false);
                drawProgressbar((int) curHealXp, nextHealXp, statsAreaLeft, "Healer " + healerClass.get("level").getAsInt(), new ItemStack(Items.potionitem), null, false);
                drawProgressbar((int) curMageXp, nextMageXp, statsAreaRight, "Mage " + mageClass.get("level").getAsInt(), new ItemStack(Items.blaze_rod), null, false);
                drawProgressbar((int) curBersXp, nextBersXp, statsAreaRight, "Berserk " + berserkClass.get("level").getAsInt(), new ItemStack(Items.iron_sword), null, false);
                drawProgressbar((int) curTankXp, nextTankXp, statsAreaRight, "Tank " + tankClass.get("level").getAsInt(), new ItemStack(Items.leather_chestplate), null, false);

                UIComponent container = new UIBlock(new Color(0, 0, 0, 0))
                        .setWidth(new RelativeConstraint(1f))
                        .setY(new RelativeConstraint(0.26f))
                        .setChildOf(this.mainComponent)
                        .setHeight(new RelativeConstraint(0.3f));

                UIComponent left = new UIBlock(new Color(0, 0, 0, 0))
                        .setWidth(new RelativeConstraint(0.47f))
                        .setChildOf(container)
                        .setHeight(new RelativeConstraint(0.3f));
                new UIText("§7Selected Class: §r§l" + selectedClass).setX(new SiblingConstraint(3f)).setChildOf(left);
                new UIText("§7Secrets Found: §r§l" + Utils.nf.format(secrets)).setY(new SiblingConstraint(3f)).setChildOf(left);

                UIComponent right = new UIBlock(clear)
                        .setWidth(new RelativeConstraint(0.47f))
                        .setX(new SiblingConstraint(10f))
                        .setChildOf(container)
                        .setHeight(new RelativeConstraint(0.3f));

                new UIText("§7Wither Essence: §r§l" + Utils.nf.format(Utils.safeGetInt(ProfilePlayerResponse, "essence_wither"))).setX(new SiblingConstraint(3f)).setChildOf(right);
                new UIText(ChatFormatting.GREEN + "Spider Essence: §r§l" + Utils.nf.format(Utils.safeGetInt(ProfilePlayerResponse, "essence_spider"))).setY(new SiblingConstraint(3f)).setChildOf(right);
                new UIText(ChatFormatting.AQUA + "Ice Essence: §r§l" + Utils.nf.format(Utils.safeGetInt(ProfilePlayerResponse, "essence_ice"))).setY(new SiblingConstraint(3f)).setChildOf(right);
                new UIText(ChatFormatting.RED + "Dragon Essence: §r§l" + Utils.nf.format(Utils.safeGetInt(ProfilePlayerResponse, "essence_dragon"))).setY(new SiblingConstraint(3f)).setChildOf(right);
                new UIText(ChatFormatting.YELLOW + "Undead Essence: §r§l" + Utils.nf.format(Utils.safeGetInt(ProfilePlayerResponse, "essence_undead"))).setY(new SiblingConstraint(3f)).setChildOf(right);
                new UIText(ChatFormatting.DARK_AQUA + "Diamond Essence: §r§l" + Utils.nf.format(Utils.safeGetInt(ProfilePlayerResponse, "essence_diamond"))).setY(new SiblingConstraint(3f)).setChildOf(right);
                new UIText(ChatFormatting.GOLD + "Gold Essence: §r§l" + Utils.nf.format(Utils.safeGetInt(ProfilePlayerResponse, "essence_gold"))).setY(new SiblingConstraint(3f)).setChildOf(right);

                totalDungeonRuns = 1;

                addDungeonFloors(floorStats, false);
                addDungeonFloors(floorStats, true);

                Double averageSecrets = Math.round((secrets.doubleValue() / totalDungeonRuns.doubleValue()) * 100.0) / 100.0;
                UIComponent avgSecretComponent = new UIText("§7Average Secrets Per Run: §r§l" + Utils.nf.format(averageSecrets)).setY(new SiblingConstraint(3f)).setChildOf(left);

                this.hoverables.put(avgSecretComponent, new ArrayList<>(Arrays.asList(ChatFormatting.RED + "This is an estimate because", ChatFormatting.RED + "the secret count is combined", ChatFormatting.RED + "across profiles")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    Integer totalDungeonRuns = 0;

    public void addDungeonFloors(JsonObject floors, boolean masterMode) {
        int index = 0;
        Color c = new Color(100, 100, 100, 100);

        // Create container for the mode
        UIComponent modeContainer = new UIBlock(clear)
                .setChildOf(this.mainComponent)
                .setWidth(new RelativeConstraint(1f))
                .setHeight(new RelativeConstraint(0.7f))
                .setY(new SiblingConstraint(10f));

        // Create title for the mode container
        new UIText(masterMode ? ChatFormatting.RED + "Master Mode" : ChatFormatting.YELLOW + "Catacombs")
                .setChildOf(modeContainer)
                .setTextScale(new PixelConstraint((float) (2.5f)))
                .setX(new CenterConstraint());

        UIComponent container = null;
        for (Map.Entry<String, JsonElement> f : floors.entrySet()) {
            JsonObject floorStats = f.getValue().getAsJsonObject();
            String floorName = ProfileViewerUtils.convertDungeonFloor(f.getKey(), masterMode);
            if (floorName.equals("error")) continue;

            // Create container for the box
            if (index % 4 == 0) {
                container = new UIBlock(clear)
                        .setChildOf(modeContainer)
                        .setWidth(new RelativeConstraint(1f))
                        .setHeight(new RelativeConstraint(0.4f))
                        .setY(new SiblingConstraint(6f));
            }

            // Create box
            UIComponent box = new UIRoundedRectangle(10f)
                    .setChildOf(container)
                    .setY(new RelativeConstraint(0f))
                    .setX(new SiblingConstraint(10f))
                    .setHeight(new RelativeConstraint(0.9f))
                    .setColor(c)
                    .setWidth(new RelativeConstraint(0.23f));

            UIComponent topPart = new UIBlock(clear)
                    .setHeight(new RelativeConstraint(0.2f))
                    .setWidth(new RelativeConstraint(1f))
                    .setChildOf(box);

            UIComponent topUnderline = new UIBlock(masterMode ? new Color(255, 85, 85) : new Color(85, 255, 85))
                    .setY(new SiblingConstraint(0))
                    .setChildOf(box)
                    .setHeight(new PixelConstraint(1f))
                    .setWidth(new RelativeConstraint(1f));

            new UIText(floorName)
                    .setX(new CenterConstraint())
                    .setY(new CenterConstraint())
                    .setTextScale(new PixelConstraint(1.5f))
                    .setChildOf(topPart);

            try {
                int timesCompleted = floorStats.get("completions").getAsInt();
                totalDungeonRuns += timesCompleted;
                new UIText("§7  Completions: §r§l" + Utils.shortenNumber(timesCompleted))
                        .setY(new SiblingConstraint(8f))
                        .setChildOf(box);
            } catch (Exception e) {
                new UIText("§7  Completions: §r§l0")
                        .setY(new SiblingConstraint(8f))
                        .setChildOf(box);
            }
            try {
                String time = floorStats.get("fastest_time").getAsJsonObject().get("renderAble").getAsString();
                if (time.equals("Not completed!")) time = "N/A";

                new UIText("§7  Fastest: §r§l" + time)
                        .setY(new SiblingConstraint(8f))
                        .setChildOf(box);
            } catch (Exception e) {
                // TODO: handle exception
            }
            try {
                String time = floorStats.get("fastest_time_s").getAsJsonObject().get("renderAble").getAsString();
                if (time.equals("Not completed!")) time = "N/A";

                new UIText("§7  Fastest S: §r§l" + time)
                        .setY(new SiblingConstraint(2f))
                        .setChildOf(box);
            } catch (Exception e) {
                // TODO: handle exception
            }
            try {
                String time = floorStats.get("fastest_time_s_plus").getAsJsonObject().get("renderAble").getAsString();
                if (time.equals("Not completed!")) time = "N/A";

                new UIText("§7  Fastest S+: §r§l" + time)
                        .setY(new SiblingConstraint(2f))
                        .setChildOf(box);
            } catch (Exception e) {
                // TODO: handle exception
            }
            index++;
        }
    }

    public DungeonsPage(UIComponent main) {
        super(main);
    }
}
