package mrfast.sbf.gui;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
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
import gg.essential.elementa.constraints.animation.AnimatingConstraints;
import gg.essential.elementa.constraints.animation.Animations;
import gg.essential.elementa.utils.Vector2f;
import mrfast.sbf.gui.ProfileViewerGui.SkillInfo;
import mrfast.sbf.gui.ProfileViewerGui.hotmUpgrade;
import mrfast.sbf.gui.components.ItemStackComponent;
import mrfast.sbf.utils.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;

public class ProfileViewerUtils {
    static Map<String, Map<Integer, Integer>> SLAYER_XP = new HashMap<>();
    static NumberFormat nf = NumberFormat.getInstance();

    public static SkillInfo getSkillInfo(Double xp, String skillName) {
        SkillInfo output = new SkillInfo(0, 0, 0, new ArrayList<>());

        output.level = convertXpToSkillLevel(xp, skillName);
        output.currentXp = (int) getLeftoverXP(xp, skillName);
        output.totalXp = nextLevelXP(output.level, skillName);

        double percent = Math.round((output.currentXp.doubleValue() / output.totalXp.doubleValue()) * 10000d) / 100d;
        if (output.totalXp.doubleValue() != 0) {
            output.hover = new ArrayList<>(Arrays.asList(
                    ChatFormatting.GREEN + skillName,
                    ChatFormatting.GRAY + "Progress: " + ChatFormatting.YELLOW + nf.format(output.currentXp) + ChatFormatting.GOLD + "/" + ChatFormatting.YELLOW + Utils.shortenNumber(output.totalXp) + " " + ChatFormatting.GRAY + "(" + percent + "%)",
                    ChatFormatting.GRAY + "Total XP: " + ChatFormatting.YELLOW + nf.format(xp)
            ));
        } else {
            output.hover = new ArrayList<>(Arrays.asList(
                    ChatFormatting.GREEN + skillName,
                    ChatFormatting.GRAY + "Progress: " + ChatFormatting.GOLD + "MAXED",
                    ChatFormatting.GRAY + "Total XP: " + ChatFormatting.YELLOW + nf.format(xp)
            ));
        }
        return output;
    }

    public static int getCurrentSlayerLevel(int xp, String slayerType) {
        Map<Integer, Integer> slayerXP = SLAYER_XP.get(slayerType);
        if (slayerXP == null) {
            throw new IllegalArgumentException("Invalid Slayer type tried to get " + slayerType);
        }

        int level = 0;
        try {
            while (level < 9 && xp >= slayerXP.get(level + 1)) {
                level++;
            }
        } catch (Exception ignored) {

        }

        return level;
    }

    public static int getNextSlayerLevelXP(int xp, String slayerType) {
        Map<Integer, Integer> slayerXP = SLAYER_XP.get(slayerType);
        int lvl = getCurrentSlayerLevel(xp, slayerType);
        if (slayerXP == null) {
            throw new IllegalArgumentException("Invalid Slayer type tried to get " + slayerType);
        }

        if (slayerXP.get(lvl + 1) != null) {
            return slayerXP.get(lvl + 1);
        } else {
            return slayerXP.get(lvl);
        }
    }

    public ProfileViewerUtils() {
        // Zombie Slayer XP
        Map<Integer, Integer> zombieXP = new HashMap<>();
        zombieXP.put(1, 5);
        zombieXP.put(2, 15);
        zombieXP.put(3, 200);
        zombieXP.put(4, 1000);
        zombieXP.put(5, 5000);
        zombieXP.put(6, 20000);
        zombieXP.put(7, 100000);
        zombieXP.put(8, 400000);
        zombieXP.put(9, 1000000);
        SLAYER_XP.put("zombie", zombieXP);

        // Spider Slayer XP
        Map<Integer, Integer> spiderXP = new HashMap<>();
        spiderXP.put(1, 5);
        spiderXP.put(2, 25);
        spiderXP.put(3, 200);
        spiderXP.put(4, 1000);
        spiderXP.put(5, 5000);
        spiderXP.put(6, 20000);
        spiderXP.put(7, 100000);
        spiderXP.put(8, 400000);
        spiderXP.put(9, 1000000);
        SLAYER_XP.put("spider", spiderXP);

        // Wolf Slayer XP
        Map<Integer, Integer> wolfXP = new HashMap<>();
        wolfXP.put(1, 10);
        wolfXP.put(2, 30);
        wolfXP.put(3, 250);
        wolfXP.put(4, 1500);
        wolfXP.put(5, 5000);
        wolfXP.put(6, 20000);
        wolfXP.put(7, 100000);
        wolfXP.put(8, 400000);
        wolfXP.put(9, 1000000);
        SLAYER_XP.put("wolf", wolfXP);

        // Enderman Slayer XP
        Map<Integer, Integer> endermanXP = new HashMap<>();
        endermanXP.put(1, 10);
        endermanXP.put(2, 30);
        endermanXP.put(3, 250);
        endermanXP.put(4, 1500);
        endermanXP.put(5, 5000);
        endermanXP.put(6, 20000);
        endermanXP.put(7, 100000);
        endermanXP.put(8, 400000);
        endermanXP.put(9, 1000000);
        SLAYER_XP.put("enderman", endermanXP);

        // Blaze Slayer XP
        Map<Integer, Integer> blazeXP = new HashMap<>();
        blazeXP.put(1, 10);
        blazeXP.put(2, 30);
        blazeXP.put(3, 250);
        blazeXP.put(4, 1500);
        blazeXP.put(5, 5000);
        blazeXP.put(6, 20000);
        blazeXP.put(7, 100000);
        blazeXP.put(8, 400000);
        blazeXP.put(9, 1000000);
        SLAYER_XP.put("blaze", blazeXP);

        // Vampire Slayer XP
        Map<Integer, Integer> vampireXP = new HashMap<>();
        vampireXP.put(1, 20);
        vampireXP.put(2, 75);
        vampireXP.put(3, 240);
        vampireXP.put(4, 840);
        vampireXP.put(5, 2400);
        SLAYER_XP.put("vampire", vampireXP);
    }

    public static Integer[] skillXPPerLevel = {50, 125, 200, 300, 500, 750, 1000, 1500, 2000, 3500, // lvl 10
            5000, 7500, 10000, 15000, 20000, 30000, 50000, 75000, 100000, 200000, // lvl 20
            300000, 400000, 500000, 600000, 700000, 800000, 900000, 1000000, 1100000, 1200000, // lvl 30
            1300000, 1400000, 1500000, 1600000, 1700000, 1800000, 1900000, 2000000, 2100000, 2200000, // lvl 40
            2300000, 2400000, 2500000, 2600000, 2750000, 2900000, 3100000, 3400000, 3700000, 4000000, // lvl 50
            4300000, 4600000, 4900000, 5200000, 5500000, 5800000, 6100000, 6400000, 6700000, 7000000}; // lvl 60

    public static Integer[] socialXpPerLevel = {0, 50, 100, 150, 250, 500, 750, 1000, 1250, 1500, 2000,// lvl 10
            2500, 3000, 3750, 4500, 6000, 8000, 10000, 12500, 15000, 20000,// lvl 20
            25000, 30000, 35000, 40000, 50000};// lvl 25

    public static Integer[] runecraftingXpPerLevel = {50, 100, 125, 160, 200, 250, 315, 400, 500, 625,// lvl 10
            785, 1000, 1250, 1600, 2000, 2465, 3125, 4000, 5000, 6200,// lvl 20
            7800, 9800, 12200, 15300, 19050};//lvl 25

    public static Integer[] getMaxSkillLevel(String type) {
        List<Integer> skillXpList = Arrays.stream(skillXPPerLevel).collect(Collectors.toList());
        List<String> skillList = Arrays.stream(lvl60Skills).collect(Collectors.toList());

        boolean lvl60Skill = skillList.contains(type);

        if (!lvl60Skill) {
            skillXpList = skillXpList.subList(0, 50);
        }

        if (type.equals("Social")) skillXpList = Arrays.stream(socialXpPerLevel).collect(Collectors.toList());
        if (type.equals("Runecrafting"))
            skillXpList = Arrays.stream(runecraftingXpPerLevel).collect(Collectors.toList());

        return skillXpList.toArray(new Integer[0]);
    }

    public static String[] lvl60Skills = {"Farming", "Mining", "Combat", "Enchanting"};

    public static int convertXpToSkillLevel(double xp, String type) {
        Integer[] xpPerLevel = getMaxSkillLevel(type);

        int levelCount = 0;
        for (int j : xpPerLevel) {
            if (xp >= j) {
                levelCount++;
                xp -= j;
            } else {
                break;
            }
        }
        return levelCount; // Add 1 to the level count since levels start from 1, not 0
    }

    public static double getLeftoverXP(double xp, String type) {
        int currentLevel = convertXpToSkillLevel(xp, type); // 19
        double xpCost = 0;
        for (int i = 1; i <= currentLevel; i++) {
            xpCost += getXpAtLevel(i, type);
        }
        return xp - xpCost;
    }

    public static int nextLevelXP(int level, String type) {
        double xpAtNextLevel = getXpAtLevel(level + 1, type);
        return (int) xpAtNextLevel;
    }

    private static double getXpAtLevel(int level, String type) {
        Integer[] xpPerLevel = getMaxSkillLevel(type);

        if (level >= 0 && level < xpPerLevel.length) {
            return xpPerLevel[level - 1];
        }
        return 0;
    }

    public static void animateX(UIComponent comp, Float x) {
        AnimatingConstraints animation = comp.makeAnimation();
        animation.setXAnimation(Animations.OUT_EXP, 0.5f, new PixelConstraint(x));
        comp.animateTo(animation);
    }

    public static UIComponent createPet(CompletableFuture<BufferedImage> texture, int lvl, Color color) {
        UIComponent background = new UIRoundedRectangle(5f).setColor(color).setWidth(new PixelConstraint(128f / 5)).setHeight(new PixelConstraint(128f / 5));
        new UIImage(texture).setChildOf(background).setX(new CenterConstraint()).setY(new CenterConstraint()).setWidth(new PixelConstraint(128f / 5)).setHeight(new PixelConstraint(120f / 5));
        new UIText("LVL " + lvl).setChildOf(background).setY(new SiblingConstraint(2f)).setX(new CenterConstraint()).setTextScale(new PixelConstraint(0.5f));
        return background;
    }

    public static Color getPetColor(String tier) {
        Color color = new Color(0x929292);
        if (tier.equals("UNCOMMON")) color = new Color(0x40bb40);
        if (tier.equals("RARE")) color = new Color(0x4444f3);
        if (tier.equals("EPIC")) color = new Color(0xa305a3);
        if (tier.equals("LEGENDARY")) color = new Color(0xd88f07);
        if (tier.equals("MYTHIC")) color = new Color(0xFF55FF);
        return color;
    }

    public static int hotmXpToLevel(int xp) {
        Map<Integer, Integer> hotmXp = new HashMap<>();
        hotmXp.put(1, 0);
        hotmXp.put(2, 3000);
        hotmXp.put(3, 9000);
        hotmXp.put(4, 25000);
        hotmXp.put(5, 60000);
        hotmXp.put(6, 100000);
        hotmXp.put(7, 150000);

        return determineLevel(hotmXp, xp);
    }

    private static int determineLevel(Map<Integer, Integer> xpMap, int xp) {
        int level = 0;
        for (Map.Entry<Integer, Integer> entry : xpMap.entrySet()) {
            int currentLevel = entry.getKey();
            int xpRequirement = entry.getValue();
            if (xp >= xpRequirement) {
                level = currentLevel;
            } else {
                break;
            }
        }
        return level;
    }

    public static String formatTitle(String a) {
        return (a.substring(0, 1).toUpperCase() + a.substring(1)).replaceAll("_", " ");
    }

    public static List<String> getLoreFromPos(Vector2f pos) {
        for (hotmUpgrade pair : ProfileViewerGui.tooltips) {
            if (pair.pos.getX() == pos.getX() && pair.pos.getY() == pos.getY()) {
                return pair.hover;
            }
        }
        return null;
    }

    public static ItemStack getStackFromPos(Vector2f pos) {
        for (hotmUpgrade pair : ProfileViewerGui.tooltips) {
            if (pair.pos.getX() == pos.getX() && pair.pos.getY() == pos.getY()) {
                return pair.stack;
            }
        }
        return null;
    }

    public static String cleanLoreRow(String loreRow) {
        return loreRow.replaceAll("<span style='color: var\\(--", "")
                .replaceAll("\\);'>", "")
                .replaceAll("</span>", "")
                .replaceAll("<span>", "");
    }

    public static void loadToolTips() {
        ProfileViewerGui.tooltips = new ArrayList<>(Arrays.asList(
                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Mining Speed",
                        "§7Level " + ProfileViewerGui.miningSpeed + "§8/50",
                        "",
                        "§7Grants §a+" + (ProfileViewerGui.miningSpeed * 20) + EnumChatFormatting.GOLD + " ⸕ Mining",
                        EnumChatFormatting.GOLD + "Speed§7."), new Vector2f(3f, 6f), ProfileViewerGui.miningSpeed > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Mining Fortune",
                        "§7Level " + ProfileViewerGui.miningFortune + "§8/50",
                        "",
                        "§7Grants §a+" + (ProfileViewerGui.miningFortune * 5) + EnumChatFormatting.GOLD + " ☘ Mining",
                        EnumChatFormatting.GOLD + "Fortune§7."), new Vector2f(3f, 5f), ProfileViewerGui.miningFortune > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Titanium Insanium",
                        "§7Level " + ProfileViewerGui.tittyInsane + "§8/50",
                        "",
                        "§7When mining Mithril Ore, you",
                        "§7have a §a" + (2d + (ProfileViewerGui.tittyInsane * 0.1)) + "% " + "§7chance to",
                        "§7convert the block into Titanium",
                        "§7Ore."), new Vector2f(2f, 5f), ProfileViewerGui.tittyInsane > 0),

                newHotmUpgrade2(Lists.newArrayList(
                        EnumChatFormatting.RED + "Mining Speed Boost",
                        "",
                        "§7Pickaxe Ability: Mining Speed Boost",
                        "§7Grants §a300% " + EnumChatFormatting.GOLD + "⸕ Mining",
                        EnumChatFormatting.GOLD + "Speed §7for §a" + "20s" + EnumChatFormatting.GRAY,
                        "§8Cooldown: §a120s"), new Vector2f(1f, 5f), ProfileViewerGui.miningSpeedBoost > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Luck of the Cave",
                        "§7Level " + ProfileViewerGui.luckofcave + "§8/45",
                        "",
                        "§7Increases the chance for you to",
                        "§7trigger rare occurrences in",
                        "§2Dwarven Mines §7by §a" + (6 + ProfileViewerGui.luckofcave - 1) + "%§7."), new Vector2f(1f, 4f), ProfileViewerGui.luckofcave > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Daily Powder",
                        "§7Level " + ProfileViewerGui.dailyPowder + "§8/100",
                        "",
                        "§7Gains §a" + (400 + (ProfileViewerGui.dailyPowder - 1) * 36) + " Powder§7 from the",
                        "§7first ore you mine every day.",
                        "§7Works for all Powder types."), new Vector2f(3f, 4f), ProfileViewerGui.dailyPowder > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Efficient Miner",
                        "§7Level " + ProfileViewerGui.effMiner + "§8/100",
                        "",
                        "§7When mining ores, you have a",
                        "§a" + ProfileViewerGui.effMinerStat + "%§7 chance to mine §a" + Math.round(ProfileViewerGui.effMinerStat2), "§7adjacent ores."), new Vector2f(3f, 3f), ProfileViewerGui.effMiner > 0),

                newHotmUpgrade4(Lists.newArrayList("§cPeak of the Mountain",
                        "§7Level " + ProfileViewerGui.potm + "§8/7"), new Vector2f(3f, 2f), ProfileViewerGui.potm > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Mole",
                        "§7Level " + ProfileViewerGui.mole + "§8/190",
                        "",
                        "§7When mining hard stone, you have",
                        "§7a §a" + ProfileViewerGui.finalOutput + "% §7chance to mine §a" + Math.round(ProfileViewerGui.moleStat),
                        "§7adjacent ores."), new Vector2f(3f, 1f), ProfileViewerGui.mole > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Powder Buff",
                        "§7Level " + ProfileViewerGui.powderBuff + "§8/50",
                        "",
                        "§7Gain §a" + ProfileViewerGui.powderBuff + "% " + "§7more Mithril",
                        "§7Powder and Gemstone Powder§7."), new Vector2f(3f, 0f), ProfileViewerGui.powderBuff > 0),

                newHotmUpgrade3(Lists.newArrayList(EnumChatFormatting.RED + "Goblin Killer",
                        "§7Killing a §6Golden Goblin",
                        "§6§7gives §2200 §7extra §2Mithril",
                        "§2Powder§7, while killing other",
                        "§7Goblins gives some based on",
                        "§7their wits."), new Vector2f(1f, 2f), ProfileViewerGui.goblinKiller > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Seasoned Mineman",
                        "§7Level " + ProfileViewerGui.seasonMine + "§8/100",
                        "",
                        "§7Grants §3+" + EnumChatFormatting.DARK_AQUA + (ProfileViewerGui.seasonMine * 0.1 + 5) + "☯ Mining Wisdom§7."), new Vector2f(2f, 3f), ProfileViewerGui.seasonMine > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Lonesome Miner",
                        "§7Level " + ProfileViewerGui.lonesomeMiner + "§8/45",
                        "",
                        "§7Increases §c❁ Strength, §9☣ Crit",
                        "§9Chance, §9☠ Crit Damage, §a❈",
                        "§aDefense, and §c❤ Health",
                        "§c§7statistics gain by §a" + (ProfileViewerGui.lonesomeMiner * .5 + 5) + "%§7",
                        "§7while in the Crystal Hollows."), new Vector2f(1f, 1f), ProfileViewerGui.lonesomeMiner > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Professional",
                        "§7Level " + ProfileViewerGui.professional + "§8/140",
                        "",
                        "§7Gain §a+" + (ProfileViewerGui.professional * 5 + 50) + "§6 ⸕ Mining",
                        "§6Speed§7 when mining Gemstones."), new Vector2f(2f, 1f), ProfileViewerGui.professional > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Mining Speed 2",
                        "§7Level " + ProfileViewerGui.miningSpeed2 + "§8/50",
                        "",
                        "§7Grants §a+" + (ProfileViewerGui.miningSpeed2 * 40) + EnumChatFormatting.GOLD + " ⸕ Mining",
                        "§6Speed§7."), new Vector2f(1f, 0f), ProfileViewerGui.miningSpeed2 > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Quick Forge",
                        "§7Level " + ProfileViewerGui.quickForge + "§8/20",
                        "",
                        "§7Decreases the time it takes to",
                        "§7forge by §a" + (ProfileViewerGui.quickForge * 0.5 + 10) + "%"), new Vector2f(4f, 5f), ProfileViewerGui.quickForge > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Fortunate",
                        "§7Level " + ProfileViewerGui.fortunate + "§8/20",
                        "",
                        "§7Gain §a+" + (ProfileViewerGui.fortunate * 5) + " §6☘ Mining",
                        "§6Fortune§7 when mining Gemstone."), new Vector2f(4f, 1f), ProfileViewerGui.fortunate > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Great Explorer",
                        "§7Level " + ProfileViewerGui.greatExplorer + "§8/20",
                        "",
                        "§7Grants §a+" + (ProfileViewerGui.greatExplorer * 4 + 16) + "% " + EnumChatFormatting.GRAY + "chance to",
                        "§7find treasure."), new Vector2f(5f, 1f), ProfileViewerGui.greatExplorer > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Mining Fortune 2",
                        "§7Level " + ProfileViewerGui.miningFortune2 + "§8/50",
                        "",
                        "§7Grants §a+§a" + (ProfileViewerGui.miningFortune2 * 5) + "§7 §6☘ Mining",
                        "§6Fortune§7."), new Vector2f(5f, 0f), ProfileViewerGui.miningFortune2 > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Orbiter",
                        "§7Level " + ProfileViewerGui.orbit + "§8/80",
                        "",
                        "§7When mining ores, you have a",
                        EnumChatFormatting.GREEN + Utils.round(ProfileViewerGui.orbit * 0.1 + 0.2, 1) + "%" + EnumChatFormatting.GRAY + " chance to get a random",
                        "§7amount of experience orbs."), new Vector2f(4f, 3f), ProfileViewerGui.orbit > 0),

                newHotmUpgrade3(Lists.newArrayList(EnumChatFormatting.RED + "Front Loaded",
                        "§7Grants §a+100 §6⸕ Mining Speed",
                        "§7and §6☘ Mining Fortune §7as",
                        "§7well as §a+2 base powder §7for",
                        "§7the first §e2,500 §7ores you",
                        "§7mine in a day."), new Vector2f(5f, 3f), ProfileViewerGui.frontLoaded > 0),

                newHotmUpgrade3(Lists.newArrayList(EnumChatFormatting.RED + "Precision Mining",
                        "§7When mining ore, a particle",
                        "§7target appears on the block that",
                        "§7increases your §6⸕ Mining Speed",
                        "§7by §a30% §7when aiming at it."), new Vector2f(6f, 3f), ProfileViewerGui.precision > 0),

                newHotmUpgrade3(Lists.newArrayList(EnumChatFormatting.RED + "Crystallized",
                        "§7Level " + ProfileViewerGui.crystallized + "§8/30",
                        "",
                        "§7Grants §a+§a" + (ProfileViewerGui.crystallized * 6 + 14) + "§7 §6⸕ Mining",
                        "§6Speed §7and a §a" + (ProfileViewerGui.crystallized * 6 + 14) + "%§7 §7chance",
                        "§7to deal §a+1 §7extra damage near",
                        "§7§5Fallen Stars§7."), new Vector2f(5f, 4f), ProfileViewerGui.crystallized > 0),

                newHotmUpgrade2(Lists.newArrayList(EnumChatFormatting.RED + "Maniac Miner",
                        "",
                        "§6Pickaxe Ability: Maniac Miner",
                        "§7Spends all your Mana and grants",
                        "§7§a+1 §6⸕ Mining Speed §7for",
                        "§7every §b10 Mana §7spent, for",
                        "§7§a15s§7.",
                        "§8Cooldown: §a59s"), new Vector2f(6f, 1f), ProfileViewerGui.maniacMiner > 0),

                newHotmUpgrade2(Lists.newArrayList(EnumChatFormatting.RED + "Pickobulus",
                        "",
                        "§6Pickaxe Ability: Pickobulus",
                        "§7Throw your pickaxe to create an",
                        "§7explosion on impact, mining all",
                        "§7ores within a §a2 block §7radius",
                        "§7Cooldown: §a110s"), new Vector2f(5f, 5f), ProfileViewerGui.pickoblus > 0),

                newHotmUpgrade(Lists.newArrayList(EnumChatFormatting.RED + "Star Powder",
                        "",
                        "§6Pickaxe Ability: Star Powder",
                        "§7Mining Mithril Ore near §5Fallen",
                        "§5Crystals§7 gives §a3x§7 Mithril",
                        "§7Powder",
                        "§7Cooldown: §a110s"), new Vector2f(5f, 2), ProfileViewerGui.starPowder > 0),

                newHotmUpgrade3(Lists.newArrayList("§cMining Madness", "§7Grants §a+50 §6⸕ Mining Speed", "§7and §6☘ Mining Fortune§7."), new Vector2f(1f, 3f), ProfileViewerGui.miningMadness > 0),

                newHotmUpgrade3(Lists.newArrayList(
                        "§cSky Mall",
                        "§7Every SkyBlock day, you receive",
                        "§7a random buff in the §2Dwarven",
                        "§2Mines§7.",
                        "",
                        "§7Possible Buffs",
                        "§8 ■ §7Gain §a+100 §6⸕ Mining Speed.",
                        "§8 ■ §7Gain §a+50 §6☘ Mining Fortune.",
                        "§8 ■ §7Gain §a+15% §7chance to gain",
                        "    §7extra Powder while mining.",
                        "§8 ■ §7Reduce Pickaxe Ability cooldown",
                        "    §7by §a20%",
                        "§8 ■ §7§a10x §7chance to find Goblins",
                        "    §7while mining.",
                        "§8 ■ §7Gain §a5x §9Titanium §7drops."
                ), new Vector2f(0f, 3f), ProfileViewerGui.skymall > 0),

                newHotmUpgrade2(Lists.newArrayList(EnumChatFormatting.RED + "Vein Seeker",
                        "",
                        "§6Pickaxe Ability: Vein Seeker",
                        "§7Points in the direction of the",
                        "§7nearest vein and grants §a+3",
                        "§7§6Mining Spread§7 for §a14s",
                        "§8Cooldown: §a60s"), new Vector2f(0f, 1f), ProfileViewerGui.veinSeeker > 0)
        ));
    }


    // Repeatable upgrades
    public static hotmUpgrade newHotmUpgrade(List<String> hover, Vector2f pos, Boolean unlocked) {
        hotmUpgrade upgrade = new hotmUpgrade(hover, pos);
        if (unlocked) {
            upgrade.stack = new ItemStack(Items.emerald).setStackDisplayName(hover.get(0));
        } else {
            upgrade.stack = new ItemStack(Items.coal).setStackDisplayName(hover.get(0));
        }
        if (!unlocked) {
            hover.add("");
            hover.add("§7Cost");
            hover.add("§51 token of the mountain");
        }
        setCustomLore(upgrade.stack, hover);
        return upgrade;
    }

    // Pickaxe ability
    public static hotmUpgrade newHotmUpgrade2(List<String> hover, Vector2f pos, Boolean unlocked) {
        hotmUpgrade upgrade = new hotmUpgrade(hover, pos);
        if (unlocked) {
            upgrade.stack = new ItemStack(Blocks.emerald_block).setStackDisplayName(hover.get(0));
        } else {
            upgrade.stack = new ItemStack(Blocks.coal_block).setStackDisplayName(hover.get(0));
        }
        if (!unlocked) {
            hover.add("");
            hover.add("§7Cost");
            hover.add("§51 token of the mountain");
        }
        setCustomLore(upgrade.stack, hover);

        return upgrade;
    }

    // One time upgrades
    public static hotmUpgrade newHotmUpgrade3(List<String> hover, Vector2f pos, Boolean unlocked) {
        hotmUpgrade upgrade = new hotmUpgrade(hover, pos);
        if (unlocked) {
            upgrade.stack = new ItemStack(Items.diamond).setStackDisplayName(hover.get(0));
        } else {
            upgrade.stack = new ItemStack(Items.coal).setStackDisplayName(hover.get(0));
        }
        if (!unlocked) {
            hover.add("");
            hover.add("§7Cost");
            hover.add("§51 token of the mountain");
        }
        setCustomLore(upgrade.stack, hover);

        return upgrade;
    }

    // Peak
    public static hotmUpgrade newHotmUpgrade4(List<String> hover, Vector2f pos, Boolean unlocked) {
        hotmUpgrade upgrade = new hotmUpgrade(hover, pos);
        if (unlocked) upgrade.stack = new ItemStack(Blocks.redstone_block).setStackDisplayName(hover.get(0));
        else upgrade.stack = new ItemStack(Blocks.bedrock).setStackDisplayName(hover.get(0));
        if (!unlocked) {
            hover.add("");
            hover.add("§7Cost");
            hover.add("§51 token of the mountain");
        }
        setCustomLore(upgrade.stack, hover);
        return upgrade;
    }

    public static void setCustomLore(ItemStack itemStack, List<String> loreLines) {
        NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
        if (nbtTagCompound == null) {
            nbtTagCompound = new NBTTagCompound();
        }

        NBTTagList loreList = new NBTTagList();
        for (String loreLine : loreLines) {
            loreList.appendTag(new NBTTagString(loreLine));
        }

        NBTTagCompound displayTag = nbtTagCompound.getCompoundTag("display");
        displayTag.setTag("Lore", loreList);
        nbtTagCompound.setTag("display", displayTag);
        itemStack.setTagCompound(nbtTagCompound);
    }


    public static void createHotmTree(UIComponent container) {
        JsonObject hotmNodes = new JsonObject();
        if (ProfileViewerGui.ProfilePlayerResponse.get("mining_core").getAsJsonObject().has("nodes")) {
            hotmNodes = ProfileViewerGui.ProfilePlayerResponse.get("mining_core").getAsJsonObject().get("nodes").getAsJsonObject();
        }
        if (hotmNodes.has("mining_speed")) ProfileViewerGui.miningSpeed = hotmNodes.get("mining_speed").getAsInt();
        if (hotmNodes.has("mining_fortune"))
            ProfileViewerGui.miningFortune = hotmNodes.get("mining_fortune").getAsInt();
        if (hotmNodes.has("titanium_insanium"))
            ProfileViewerGui.tittyInsane = hotmNodes.get("titanium_insanium").getAsInt();
        if (hotmNodes.has("random_event")) ProfileViewerGui.luckofcave = hotmNodes.get("random_event").getAsInt();
        if (hotmNodes.has("daily_powder")) ProfileViewerGui.dailyPowder = hotmNodes.get("daily_powder").getAsInt();
        if (hotmNodes.has("efficient_miner")) {
            ProfileViewerGui.effMiner = hotmNodes.get("efficient_miner").getAsInt();
            ProfileViewerGui.effMinerStat = (float) (ProfileViewerGui.effMiner * 0.4 + 10.4);
            ProfileViewerGui.effMinerStat2 = (float) (ProfileViewerGui.effMiner * .06 + 0.31);
        }
        if (hotmNodes.has("special_0")) ProfileViewerGui.potm = hotmNodes.get("special_0").getAsInt();
        if (hotmNodes.has("fallen_star_bonus"))
            ProfileViewerGui.crystallized = hotmNodes.get("fallen_star_bonus").getAsInt();
        if (hotmNodes.has("professional")) ProfileViewerGui.professional = hotmNodes.get("professional").getAsInt();
        if (hotmNodes.has("forge_time")) ProfileViewerGui.quickForge = hotmNodes.get("forge_time").getAsInt();
        if (hotmNodes.has("experience_orbs")) ProfileViewerGui.orbit = hotmNodes.get("experience_orbs").getAsInt();
        if (hotmNodes.has("mining_fortune_2"))
            ProfileViewerGui.miningFortune2 = hotmNodes.get("mining_fortune_2").getAsInt();
        if (hotmNodes.has("mining_speed_2")) ProfileViewerGui.miningSpeed2 = hotmNodes.get("mining_speed_2").getAsInt();
        if (hotmNodes.has("lonesome_miner"))
            ProfileViewerGui.lonesomeMiner = hotmNodes.get("lonesome_miner").getAsInt();
        if (hotmNodes.has("fortunate")) ProfileViewerGui.fortunate = hotmNodes.get("fortunate").getAsInt();
        if (hotmNodes.has("great_explorer"))
            ProfileViewerGui.greatExplorer = hotmNodes.get("great_explorer").getAsInt();
        if (hotmNodes.has("mining_experience"))
            ProfileViewerGui.seasonMine = hotmNodes.get("mining_experience").getAsInt();
        if (hotmNodes.has("powder_buff")) ProfileViewerGui.powderBuff = hotmNodes.get("powder_buff").getAsInt();
        if (hotmNodes.has("daily_powder")) ProfileViewerGui.dailyPowder = hotmNodes.get("daily_powder").getAsInt();
        if (hotmNodes.has("daily_effect")) ProfileViewerGui.skymall = hotmNodes.get("daily_effect").getAsInt();
        if (hotmNodes.has("mining_madness"))
            ProfileViewerGui.miningMadness = hotmNodes.get("mining_madness").getAsInt();
        if (hotmNodes.has("vein_seeker")) ProfileViewerGui.veinSeeker = hotmNodes.get("vein_seeker").getAsInt();
        if (hotmNodes.has("precision_mining"))
            ProfileViewerGui.precision = hotmNodes.get("precision_mining").getAsInt();
        if (hotmNodes.has("star_powder")) ProfileViewerGui.starPowder = hotmNodes.get("star_powder").getAsInt();
        if (hotmNodes.has("pickaxe_toss")) ProfileViewerGui.pickoblus = hotmNodes.get("pickaxe_toss").getAsInt();
        if (hotmNodes.has("maniac_miner")) ProfileViewerGui.maniacMiner = hotmNodes.get("maniac_miner").getAsInt();
        if (hotmNodes.has("mining_speed_boost"))
            ProfileViewerGui.miningSpeedBoost = hotmNodes.get("mining_speed_boost").getAsInt();
        if (hotmNodes.has("goblin_killer")) ProfileViewerGui.goblinKiller = hotmNodes.get("goblin_killer").getAsInt();
        if (hotmNodes.has("front_loaded")) ProfileViewerGui.frontLoaded = hotmNodes.get("front_loaded").getAsInt();

        if (hotmNodes.has("mole")) {
            ProfileViewerGui.mole = hotmNodes.get("mole").getAsInt();
            ProfileViewerGui.moleStat = (float) (ProfileViewerGui.mole.doubleValue() * 0.051);
            double moleperkstat = (double) ProfileViewerGui.mole / 20 - 0.55 + 50;
            double moleperkstat2 = (double) Math.round(moleperkstat * 100) / 100;

            ProfileViewerGui.finalOutput = Math.round((float) (moleperkstat2 % 1) * 100);
            if (ProfileViewerGui.finalOutput == 0) {
                ProfileViewerGui.finalOutput = 100;
            }
        }

        ProfileViewerUtils.loadToolTips();
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                float xDelta = container.getWidth() / 7;
                float yDelta = container.getHeight() / 7;
                Vector2f vec = new Vector2f(x, y);

                try {
                    boolean exists = false;
                    for (hotmUpgrade upgrade : ProfileViewerGui.tooltips) {
                        if (upgrade.pos.getX() == vec.getX() && upgrade.pos.getY() == vec.getY()) {
                            exists = true;
                            break;
                        }
                    }
                    if (exists) {
                        UIComponent background = new UIRoundedRectangle(3f).setX(new PixelConstraint(xDelta * x)).setY(new PixelConstraint(yDelta * y)).setColor(new Color(100, 100, 100, 100)).setWidth(new PixelConstraint(20f)).setHeight(new PixelConstraint(20f)).setChildOf(container);
                        new ItemStackComponent(ProfileViewerUtils.getStackFromPos(vec)).setX(new CenterConstraint()).setWidth(new RelativeConstraint(1f)).setHeight(new RelativeConstraint(1f)).setY(new CenterConstraint()).setChildOf(background);
                        ProfileViewerGui.HOTMHoverables.put(background, ProfileViewerUtils.getLoreFromPos(vec));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // TODO: handle exception
                }
            }
        }
    }


    public static void setSlayerSkills(JsonObject userObject) {
        System.out.println("Settings slayer experience");
        JsonObject slayersObject = null;
        if (userObject.has("slayer_bosses")) {
            slayersObject = userObject.get("slayer_bosses").getAsJsonObject();
        }
        try {
            Integer[] tierPrices = {2000, 7500, 20000, 50000, 100000};

            assert slayersObject != null;
            if (slayersObject.has("zombie")) {
                JsonObject obj = slayersObject.get("zombie").getAsJsonObject();
                int xp = 0;
                if (obj.has("xp")) xp = obj.get("xp").getAsInt();
                int level = ProfileViewerUtils.getCurrentSlayerLevel(xp, "zombie");

                int[] tiers = new int[5];
                String[] tierNames = {"Tier I", "Tier II", "Tier III", "Tier IV", "Tier V"};
                int totalCost = 0;
                for (int i = 0; i < tiers.length; i++) {
                    try {
                        tiers[i] = obj.get("boss_kills_tier_" + i).getAsInt();
                        if (tiers[i] > 0) {
                            totalCost += tierPrices[i] * tiers[i];
                        }
                    } catch (Exception e) {
                        // Handle exception
                    }
                }

                List<String> hover = new ArrayList<>();
                hover.add(ChatFormatting.RED + "Revenent Horror");
                for (int i = 0; i < tiers.length; i++) {
                    hover.add(ChatFormatting.GRAY + tierNames[i] + ": " + ChatFormatting.YELLOW + tiers[i]);
                }
                hover.add(ChatFormatting.GOLD + "Coins Spent: " + ChatFormatting.YELLOW + nf.format(totalCost));

                int nextXp = ProfileViewerUtils.getNextSlayerLevelXP(xp, "zombie");
                ProfileViewerGui.zombieSlayer = new SkillInfo(level, nextXp, xp, hover);
            }

            if (slayersObject.has("spider")) {
                String slayerType = "spider";
                JsonObject obj = slayersObject.get(slayerType).getAsJsonObject();
                int xp = 0;
                if (obj.has("xp")) xp = obj.get("xp").getAsInt();
                int level = ProfileViewerUtils.getCurrentSlayerLevel(xp, slayerType);

                String[] tierNames = {"Tier I", "Tier II", "Tier III", "Tier IV"};
                int[] tiers = new int[tierNames.length];
                int totalCost = 0;
                for (int i = 0; i < tiers.length; i++) {
                    try {
                        tiers[i] = obj.get("boss_kills_tier_" + i).getAsInt();
                        if (tiers[i] > 0) {
                            totalCost += tierPrices[i] * tiers[i];
                        }
                    } catch (Exception e) {
                        // Handle exception
                    }
                }

                List<String> hover = new ArrayList<>();
                hover.add(ChatFormatting.RED + "Tarantula Broodfather");
                for (int i = 0; i < tiers.length; i++) {
                    hover.add(ChatFormatting.GRAY + tierNames[i] + ": " + ChatFormatting.YELLOW + tiers[i]);
                }
                hover.add(ChatFormatting.GOLD + "Coins Spent: " + ChatFormatting.YELLOW + nf.format(totalCost));

                int nextXp = ProfileViewerUtils.getNextSlayerLevelXP(xp, slayerType);
                ProfileViewerGui.spiderSlayer = new SkillInfo(level, nextXp, xp, hover);
            }

            if (slayersObject.has("wolf")) {
                String slayerType = "wolf";
                JsonObject obj = slayersObject.get(slayerType).getAsJsonObject();
                int xp = 0;
                if (obj.has("xp")) xp = obj.get("xp").getAsInt();
                int level = ProfileViewerUtils.getCurrentSlayerLevel(xp, slayerType);

                String[] tierNames = {"Tier I", "Tier II", "Tier III", "Tier IV"};
                int[] tiers = new int[tierNames.length];
                int totalCost = 0;
                for (int i = 0; i < tiers.length; i++) {
                    try {
                        tiers[i] = obj.get("boss_kills_tier_" + i).getAsInt();
                        if (tiers[i] > 0) {
                            totalCost += tierPrices[i] * tiers[i];
                        }
                    } catch (Exception e) {
                        // Handle exception
                    }
                }

                List<String> hover = new ArrayList<>();
                hover.add(ChatFormatting.RED + "Sven Packmaster");
                for (int i = 0; i < tiers.length; i++) {
                    hover.add(ChatFormatting.GRAY + tierNames[i] + ": " + ChatFormatting.YELLOW + tiers[i]);
                }
                hover.add(ChatFormatting.GOLD + "Coins Spent: " + ChatFormatting.YELLOW + nf.format(totalCost));

                int nextXp = ProfileViewerUtils.getNextSlayerLevelXP(xp, slayerType);
                ProfileViewerGui.wolfSlayer = new SkillInfo(level, nextXp, xp, hover);
            }

            if (slayersObject.has("enderman")) {
                String slayerType = "enderman";
                JsonObject obj = slayersObject.get(slayerType).getAsJsonObject();
                int xp = 0;
                if (obj.has("xp")) xp = obj.get("xp").getAsInt();
                int level = ProfileViewerUtils.getCurrentSlayerLevel(xp, slayerType);

                String[] tierNames = {"Tier I", "Tier II", "Tier III", "Tier IV"};
                int[] tiers = new int[tierNames.length];
                int totalCost = 0;
                for (int i = 0; i < tiers.length; i++) {
                    try {
                        tiers[i] = obj.get("boss_kills_tier_" + i).getAsInt();
                        if (tiers[i] > 0) {
                            totalCost += tierPrices[i] * tiers[i];
                        }
                    } catch (Exception e) {
                        // Handle exception
                    }
                }

                List<String> hover = new ArrayList<>();
                hover.add(ChatFormatting.RED + "Enderman");
                for (int i = 0; i < tiers.length; i++) {
                    hover.add(ChatFormatting.GRAY + tierNames[i] + ": " + ChatFormatting.YELLOW + tiers[i]);
                }
                hover.add(ChatFormatting.GOLD + "Coins Spent: " + ChatFormatting.YELLOW + nf.format(totalCost));

                int nextXp = ProfileViewerUtils.getNextSlayerLevelXP(xp, slayerType);
                ProfileViewerGui.emanSlayer = new SkillInfo(level, nextXp, xp, hover);
            }

            if (slayersObject.has("blaze")) {
                String slayerType = "blaze";
                JsonObject obj = slayersObject.get(slayerType).getAsJsonObject();
                int xp = 0;
                if (obj.has("xp")) xp = obj.get("xp").getAsInt();

                int level = ProfileViewerUtils.getCurrentSlayerLevel(xp, slayerType);

                String[] tierNames = {"Tier I", "Tier II", "Tier III", "Tier IV"};
                int[] tiers = new int[tierNames.length];
                int totalCost = 0;
                for (int i = 0; i < tiers.length; i++) {
                    try {
                        tiers[i] = obj.get("boss_kills_tier_" + i).getAsInt();
                        if (tiers[i] > 0) {
                            totalCost += tierPrices[i] * tiers[i];
                        }
                    } catch (Exception e) {
                        // Handle exception
                    }

                }

                List<String> hover = new ArrayList<>();
                hover.add(ChatFormatting.RED + "Blaze");
                for (int i = 0; i < tiers.length; i++) {
                    hover.add(ChatFormatting.GRAY + tierNames[i] + ": " + ChatFormatting.YELLOW + tiers[i]);
                }
                hover.add(ChatFormatting.GOLD + "Coins Spent: " + ChatFormatting.YELLOW + nf.format(totalCost));

                int nextXp = ProfileViewerUtils.getNextSlayerLevelXP(xp, slayerType);

                ProfileViewerGui.blazeSlayer = new SkillInfo(level, nextXp, xp, hover);
            }

            if (slayersObject.has("vampire")) {
                String slayerType = "vampire";
                JsonObject obj = slayersObject.get(slayerType).getAsJsonObject();
                int xp = 0;
                if (obj.has("xp")) xp = obj.get("xp").getAsInt();
                int level = ProfileViewerUtils.getCurrentSlayerLevel(xp, slayerType);

                String[] tierNames = {"Tier I", "Tier II", "Tier III", "Tier IV", "Tier V"};
                int[] tiers = new int[tierNames.length];
                int totalCost = 0;
                for (int i = 0; i < tiers.length; i++) {
                    try {
                        tiers[i] = obj.get("boss_kills_tier_" + i).getAsInt();
                        if (tiers[i] > 0) {
                            totalCost += tierPrices[i] * tiers[i];
                        }
                    } catch (Exception e) {
                        // Handle exception
                    }
                }

                List<String> hover = new ArrayList<>();
                hover.add(ChatFormatting.RED + "Vampire");
                for (int i = 0; i < tiers.length; i++) {
                    hover.add(ChatFormatting.GRAY + tierNames[i] + ": " + ChatFormatting.YELLOW + tiers[i]);
                }
                hover.add(ChatFormatting.GOLD + "Coins Spent: " + ChatFormatting.YELLOW + nf.format(totalCost));

                int nextXp = ProfileViewerUtils.getNextSlayerLevelXP(xp, slayerType);
                ProfileViewerGui.vampireSlayer = new SkillInfo(level, nextXp, xp, hover);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        System.out.println("set slayer experience");
    }

    public static UIComponent createTimecharm(ItemStack item, String id, JsonObject timecharmObj, UIComponent parent) {
        boolean unlocked = timecharmObj != null;
        UIComponent border = new UIRoundedRectangle(5f)
                .setColor(unlocked ? Color.WHITE : Color.gray)
                .setChildOf(parent)
                .setX(new SiblingConstraint(5f))
                .setWidth(new PixelConstraint(30f + Utils.GetMC().fontRendererObj.getStringWidth(id)))
                .setHeight(new RelativeConstraint(1f));

        UIComponent inside = new UIRoundedRectangle(5f)
                .setChildOf(border)
                .setColor(new Color(0x191919))
                .setX(new PixelConstraint(1f))
                .setY(new PixelConstraint(1f))
                .setWidth(new PixelConstraint(border.getWidth() - 2))
                .setHeight(new PixelConstraint(border.getHeight() - 2));

        UIComponent left = new UIBlock(new Color(0, 0, 0, 0))
                .setChildOf(inside)
                .setX(new PixelConstraint(0f))
                .setY(new PixelConstraint(0f))
                .setWidth(new PixelConstraint(24f))
                .setHeight(new RelativeConstraint(1f));
        ItemStack itemStack = new ItemStack(item.getItem());
        if (id.equals("Supreme Timecharm")) {
            itemStack = new ItemStack(item.getItem(), 1, 1);
        }
        new ItemStackComponent(itemStack)
                .setChildOf(left)
                .setWidth(new PixelConstraint(20f))
                .setHeight(new PixelConstraint(20f))
                .setX(new CenterConstraint())
                .setY(new CenterConstraint());

        UIComponent right = new UIBlock(new Color(0, 0, 0, 0))
                .setChildOf(inside)
                .setX(new SiblingConstraint(0f))
                .setY(new PixelConstraint(0f))
                .setWidth(new RelativeConstraint(0.7f))
                .setHeight(new RelativeConstraint(1f));

        String unlockedName = unlocked ? ChatFormatting.GOLD + id : ChatFormatting.GRAY + id;
        new UIText(unlockedName)
                .setChildOf(right)
                .setX(new PixelConstraint(0f))
                .setY(new CenterConstraint());
        String pattern = "MMMM d yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        long obtainedDate = 0;
        try {
            obtainedDate = timecharmObj.get("timestamp").getAsLong();
        } catch (Exception ignored) {
        }

        String joinedString = simpleDateFormat.format(new Date(obtainedDate));

        List<String> hover = new ArrayList<>(Arrays.asList(
                unlockedName,
                ChatFormatting.YELLOW + "Unlocked: " + (unlocked ? ChatFormatting.GREEN + "✔" : ChatFormatting.RED + "✘")));
        if (obtainedDate != 0) {
            hover.add(ChatFormatting.GREEN + "Obtained on " + joinedString);
        }

        ProfileViewerGui.riftHoverables.put(border, hover);
        return border;
    }

    public static JsonObject getTimecharm(String id, JsonArray array) {
        if (array == null) return null;
        for (JsonElement jsonElement : array) {
            JsonObject timecharmObj = jsonElement.getAsJsonObject();

            if (timecharmObj.get("type").getAsString().equals(id)) {
                return timecharmObj;
            }
        }
        return null;
    }
}
