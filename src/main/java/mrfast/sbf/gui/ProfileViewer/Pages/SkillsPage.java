package mrfast.sbf.gui.ProfileViewer.Pages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.components.UIBlock;
import gg.essential.elementa.components.UIImage;
import gg.essential.elementa.components.UIRoundedRectangle;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.constraints.*;
import gg.essential.elementa.utils.Vector2f;
import mrfast.sbf.gui.ProfileViewer.ProfileViewerGui;
import mrfast.sbf.gui.ProfileViewer.ProfileViewerUtils;
import mrfast.sbf.utils.ItemRarity;
import mrfast.sbf.utils.NetworkUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.item.ItemStack;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SkillsPage extends ProfileViewerGui.ProfileViewerPage {
    @Override
    public void loadPage() {
        UIComponent miningContainer = new UIBlock(new Color(0, 0, 0, 0))
                .setWidth(new RelativeConstraint(1f))
                .setChildOf(this.mainComponent)
                .setHeight(new RelativeConstraint(0.6f));

        UIComponent farmingFishingContainer = new UIBlock(new Color(0, 0, 0, 0))
                .setWidth(new RelativeConstraint(1f))
                .setY(new SiblingConstraint(10f))
                .setChildOf(this.mainComponent)
                .setHeight(new RelativeConstraint(0.3f));

        UIComponent farmingContainer = new UIBlock(new Color(0, 0, 0, 0))
                .setWidth(new RelativeConstraint(0.45f))
                .setY(new PixelConstraint(0f))
                .setChildOf(farmingFishingContainer)
                .setHeight(new RelativeConstraint(1f));

        UIComponent fishingContainer = new UIBlock(new Color(0, 0, 0, 0))
                .setWidth(new RelativeConstraint(0.45f))
                .setY(new PixelConstraint(0f))
                .setX(new SiblingConstraint(10f))
                .setChildOf(farmingFishingContainer)
                .setHeight(new RelativeConstraint(1f));

        if (ProfileViewerGui.ProfilePlayerResponse.has("mining_core")) {// Mining
            new UIText(ChatFormatting.YELLOW + "" + ChatFormatting.BOLD + "Mining").setChildOf(miningContainer).setY(new SiblingConstraint(4f)).setX(new CenterConstraint()).setTextScale(new PixelConstraint(2f));

            JsonObject miningCore = ProfileViewerGui.ProfilePlayerResponse.get("mining_core").getAsJsonObject();

            int hotmExperience = Utils.safeGetInt(miningCore, "experience");
            int mithrilPowderAvailable = Utils.safeGetInt(miningCore, "powder_mithril");
            int mithrilPowderSpent = Utils.safeGetInt(miningCore, "powder_spent_mithril");
            int gemstonePowderAvailable = Utils.safeGetInt(miningCore, "powder_gemstone");
            int gemstonePowderSpent = Utils.safeGetInt(miningCore, "powder_spent_gemstone");
            String g = ChatFormatting.GRAY + "";

            JsonArray tutorial = ProfileViewerGui.ProfilePlayerResponse.get("tutorial").getAsJsonArray();
            int commisionsMilestone = 0;

            for (JsonElement element : tutorial) {
                if (element.getAsString().contains("commission_milestone_reward_mining_xp_tier_")) {
                    commisionsMilestone = Integer.parseInt(element.getAsString().substring(43, 44));
                }
            }


            String passStatus = ChatFormatting.RED + "Expired";
            if (miningCore.has("greater_mines_last_access")) {
                passStatus = miningCore.get("greater_mines_last_access").getAsInt() > System.currentTimeMillis() - 5 * 60 * 60 * 1000 ? ChatFormatting.GREEN + "Active" : ChatFormatting.RED + "Expired";
            }
            int nucleusRuns = 0;
            try {
                JsonObject leveling = ProfileViewerGui.ProfilePlayerResponse.get("leveling").getAsJsonObject();
                JsonObject completions = leveling.getAsJsonObject("completions");
                nucleusRuns = completions.getAsJsonPrimitive("NUCLEUS_RUNS").getAsInt();
            } catch (Exception ignored) {
            }

            UIComponent left = new UIBlock(new Color(0, 0, 0, 0))
                    .setWidth(new RelativeConstraint(0.45f))
                    .setY(new SiblingConstraint(4f))
                    .setChildOf(miningContainer)
                    .setHeight(new RelativeConstraint(1f));

            new UIText(ChatFormatting.YELLOW + "" + ChatFormatting.BOLD + "Dwarven Mines and Crystal Hollows").setChildOf(left).setY(new SiblingConstraint(4f)).setX(new CenterConstraint());
            new UIText("§7Commission Milestone: §r§l" + commisionsMilestone).setY(new SiblingConstraint(2f)).setChildOf(left);
            new UIText("§7Crystal Hollows Pass: §r§l" + passStatus).setY(new SiblingConstraint(2f)).setChildOf(left);
            new UIText("§7Crystal Nucleus: §r§l" + "Completed " + nucleusRuns + " times").setY(new SiblingConstraint(2f)).setChildOf(left);

            new UIText(ChatFormatting.YELLOW + "" + ChatFormatting.BOLD + "Heart of the Mountain").setChildOf(left).setY(new SiblingConstraint(10f)).setX(new CenterConstraint());
            int hotmTier = 0;
            int tokensSpent = Utils.safeGetInt(miningCore, "tokens_spent");

            try {
                hotmTier = ProfileViewerUtils.hotmXpToLevel(hotmExperience);
            } catch (Exception ignored) {
            }

            new UIText("§7Tier: §r§l" + hotmTier + "/7").setY(new SiblingConstraint(2f)).setChildOf(left);
            new UIText("§7Token Of The Mountain: §r§l" + (tokensSpent) + "/17").setY(new SiblingConstraint(2f)).setChildOf(left);
            new UIText("§7Peak Of The Mountain: §r§l" + potm + "/7").setY(new SiblingConstraint(2f)).setChildOf(left);
            new UIText("§7Mithril Powder: " + ChatFormatting.GREEN + ChatFormatting.BOLD + Utils.nf.format(mithrilPowderAvailable) + ChatFormatting.DARK_GREEN + ChatFormatting.BOLD + " / " + Utils.nf.format(mithrilPowderAvailable + mithrilPowderSpent)).setY(new SiblingConstraint(2f)).setChildOf(left);
            new UIText("§7Gemstone Powder: " + ChatFormatting.LIGHT_PURPLE + ChatFormatting.BOLD + Utils.nf.format(gemstonePowderAvailable) + ChatFormatting.DARK_PURPLE + ChatFormatting.BOLD + " / " + Utils.nf.format(gemstonePowderAvailable + gemstonePowderSpent)).setY(new SiblingConstraint(2f)).setChildOf(left);

            String pickaxeAbility = "None";
            try {
                pickaxeAbility = Utils.convertToTitleCase(miningCore.get("selected_pickaxe_ability").getAsString());
            } catch (Exception ignored) {
            }
            new UIText("§7Pickaxe Ability: §r§l" + pickaxeAbility).setY(new SiblingConstraint(2f)).setChildOf(left);
            drawHotmGrid(miningContainer);
        }
        if (ProfileViewerGui.ProfilePlayerResponse == null) return;

        {// Farming
            new UIText(ChatFormatting.YELLOW + "" + ChatFormatting.BOLD + "Farming").setChildOf(farmingContainer).setY(new SiblingConstraint(4f)).setX(new CenterConstraint()).setTextScale(new PixelConstraint(2f));
            int pelts = 0;
            if (ProfileViewerGui.ProfilePlayerResponse.has("trapper_quest")) {
                if (ProfileViewerGui.ProfilePlayerResponse.get("trapper_quest").getAsJsonObject().has("pelt_count")) {
                    pelts = ProfileViewerGui.ProfilePlayerResponse.get("trapper_quest").getAsJsonObject().get("pelt_count").getAsInt();
                }
            }

            JsonObject contests = null;
            try {
                contests = ProfileViewerGui.ProfilePlayerResponse.get("jacob2").getAsJsonObject().get("contests").getAsJsonObject();
            } catch (Exception ignored) {
            }
            JsonObject medals = null;
            try {
                medals = ProfileViewerGui.ProfilePlayerResponse.get("jacob2").getAsJsonObject().get("medals_inv").getAsJsonObject();
            } catch (Exception ignored) {
            }
            int contestsAttended = 0;
            try {
                contestsAttended = contests.entrySet().size();
            } catch (Exception ignored) {
            }

            int gold = Utils.safeGetInt(medals, "gold");
            int silver = Utils.safeGetInt(medals, "silver");
            int bronze = Utils.safeGetInt(medals, "bronze");

            new UIText("§7Pelts: §r§l" + pelts).setY(new SiblingConstraint(2f)).setChildOf(farmingContainer);
            new UIText("§7Contests Attended: §r§l" + contestsAttended).setY(new SiblingConstraint(2f)).setChildOf(farmingContainer);

            if (contestsAttended > 0) {
                new UIText("§6§lGold§r" + "§7 Medals: §r§l" + "§6" + gold).setY(new SiblingConstraint(12f)).setChildOf(farmingContainer);
                new UIText("§r§lSilver§r" + "§7 Medals: §r§l" + "§r" + silver).setY(new SiblingConstraint(2f)).setChildOf(farmingContainer);
                new UIText("§c§lBronze§r" + "§7 Medals: §r§l" + "§c" + bronze).setY(new SiblingConstraint(2f)).setChildOf(farmingContainer);
            } else {
                new UIText("§6§lGold§r" + "§7 Medals: §r§l" + "§60").setY(new SiblingConstraint(12f)).setChildOf(farmingContainer);
                new UIText("§r§lSilver§r" + "§7 Medals: §r§l" + "§r0").setY(new SiblingConstraint(2f)).setChildOf(farmingContainer);
                new UIText("§c§lBronze§r" + "§7 Medals: §r§l" + "§c0").setY(new SiblingConstraint(2f)).setChildOf(farmingContainer);
            }
        }
        {// Fishing
            new UIText(ChatFormatting.YELLOW + "" + ChatFormatting.BOLD + "Fishing").setChildOf(fishingContainer).setY(new SiblingConstraint(4f)).setX(new CenterConstraint()).setTextScale(new PixelConstraint(2f));

            int caught = 0;
            try {
                Utils.safeGetInt(ProfileViewerGui.ProfilePlayerResponse.get("trophy_fish").getAsJsonObject(), "total_caught");
            } catch (Exception ignored) {
            }
            int treasure = Utils.safeGetInt(ProfileViewerGui.ProfilePlayerResponse, "fishing_treasure_caught");

            new UIText("§7Trophy Fish Caught: §r§l" + Utils.nf.format(caught)).setY(new SiblingConstraint(2f)).setChildOf(fishingContainer);
            new UIText("§7Treasures Found: §r§l" + Utils.nf.format(treasure)).setY(new SiblingConstraint(2f)).setChildOf(fishingContainer);
        }
    }

    public void drawHotmGrid(UIComponent miningContainer) {
        miningSpeed = 0;
        miningFortune = 0;
        tittyInsane = 0;
        luckofcave = 0;
        dailyPowder = 0;
        effMiner = 0;
        effMinerStat = 0;
        effMinerStat2 = 0;
        potm = 0;
        mole = 0;
        finalOutput = 0;
        moleStat = 0;
        powderBuff = 0;
        seasonMine = 0;
        lonesomeMiner = 0;
        professional = 0;
        miningSpeed2 = 0;
        quickForge = 0;
        fortunate = 0;
        greatExplorer = 0;
        miningFortune2 = 0;
        orbit = 0;
        crystallized = 0;
        skymall = 0;
        miningMadness = 0;
        veinSeeker = 0;
        precision = 0;
        pickoblus = 0;
        maniacMiner = 0;
        starPowder = 0;
        goblinKiller = 0;
        miningSpeedBoost = 0;
        frontLoaded = 0;

        UIComponent container = new UIBlock(new Color(0, 0, 0, 0)).setWidth(new PixelConstraint(140f)).setHeight(new PixelConstraint(140f)).setX(new RelativeConstraint(0.5f)).setY(new CenterConstraint()).setChildOf(miningContainer);
        ProfileViewerUtils.createHotmTree(container);
    }

    public static Integer miningSpeed = 0;
    public static Integer miningFortune = 0;
    public static Integer tittyInsane = 0;
    public static Integer luckofcave = 0;
    public static Integer dailyPowder = 0;
    public static Integer effMiner = 0;
    public static float effMinerStat = 0;
    public static float effMinerStat2 = 0;
    public static Integer potm = 0;
    public static Integer mole = 0;
    public static Integer finalOutput = 0;
    public static float moleStat = 0;
    public static Integer powderBuff = 0;
    public static Integer seasonMine = 0;
    public static Integer lonesomeMiner = 0;
    public static Integer professional = 0;
    public static Integer miningSpeed2 = 0;
    public static Integer quickForge = 0;
    public static Integer fortunate = 0;
    public static Integer greatExplorer = 0;
    public static Integer miningFortune2 = 0;
    public static Integer orbit = 0;
    public static Integer crystallized = 0;
    public static Integer skymall = 0;
    public static Integer miningMadness = 0;
    public static Integer veinSeeker = 0;
    public static Integer precision = 0;
    public static Integer pickoblus = 0;
    public static Integer maniacMiner = 0;
    public static Integer starPowder = 0;
    public static Integer goblinKiller = 0;
    public static Integer miningSpeedBoost = 0;
    public static Integer frontLoaded = 0;

    public static List<hotmUpgrade> hotmUpgradeTooltips = null;

    public static class hotmUpgrade {
        public Vector2f pos;
        public List<String> hover;
        public ItemStack stack;

        public hotmUpgrade(List<String> hover, Vector2f pos) {
            this.hover = hover;
            this.pos = pos;
        }
    }

    public SkillsPage(UIComponent main) {
        super(main);
    }
}
