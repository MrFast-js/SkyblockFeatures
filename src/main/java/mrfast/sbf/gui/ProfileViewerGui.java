package mrfast.sbf.gui;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.realmsclient.gui.ChatFormatting;

//import gg.essential.api.EssentialAPI;
//import gg.essential.api.gui.EmulatedPlayerBuilder;
//import gg.essential.api.utils.GuiUtil;
import gg.essential.api.EssentialAPI;
import gg.essential.api.gui.EmulatedPlayerBuilder;
import gg.essential.elementa.ElementaVersion;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.WindowScreen;
import gg.essential.elementa.components.GradientComponent;
import gg.essential.elementa.components.GradientComponent.GradientDirection;
import gg.essential.elementa.components.ScrollComponent;
import gg.essential.elementa.components.UIBlock;
import gg.essential.elementa.components.UICircle;
import gg.essential.elementa.components.UIRoundedRectangle;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.components.UIWrappedText;
import gg.essential.elementa.constraints.CenterConstraint;
import gg.essential.elementa.constraints.ChildBasedSizeConstraint;
import gg.essential.elementa.constraints.PixelConstraint;
import gg.essential.elementa.constraints.RelativeConstraint;
import gg.essential.elementa.constraints.SiblingConstraint;
import gg.essential.elementa.constraints.animation.AnimatingConstraints;
import gg.essential.elementa.constraints.animation.Animations;
import gg.essential.elementa.effects.OutlineEffect;
import gg.essential.elementa.effects.ScissorEffect;
import gg.essential.elementa.utils.Vector2f;
import gg.essential.universal.UMatrixStack;
import gg.essential.vigilance.gui.common.shadow.ShadowIcon;
import gg.essential.vigilance.gui.settings.DropDownComponent;
import gg.essential.vigilance.utils.ResourceImageFactory;
import kotlin.Unit;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.ItemUtils.Inventory;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.gui.components.InventoryComponent;
import mrfast.sbf.gui.components.ItemStackComponent;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.ItemRarity;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.jetbrains.annotations.NotNull;


public class ProfileViewerGui extends WindowScreen {
    //  The player in the specified profile from hypixel
    static JsonObject ProfilePlayerResponse = null;
    //  The specified profile's data
    JsonObject ProfileResponse = null;
    //  The users profiles according to hypixel

    JsonArray hypixelProfilesResponse = null;
    // Hoverables are what is used for the lore popups for skills, etc.
    static HashMap<UIComponent,List<String>> generalHoverables = new HashMap<>();
    static HashMap<UIComponent,List<String>> HOTMHoverables = new HashMap<>();
    static HashMap<UIComponent,List<String>> petHoverables = new HashMap<>();
    static HashMap<UIComponent,List<String>> dungeonHoverables = new HashMap<>();

    String playerLocation = "";
    String selectedProfileUUID = "";
    GameProfile profile;
    public static List<String> renderTooltip = null;
    static boolean quickSwapping = false;

    @Override
    public void onScreenClose() {
        if(quickSwapping) {
            quickSwapping = false;
        } else {
            Utils.GetMC().gameSettings.guiScale = Utils.lastGuiScale;
        }
    }

    @Override
    public void onDrawScreen(UMatrixStack matrixStack,int mouseX, int mouseY, float partialTicks) {
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks);
        getWindow().draw(matrixStack);
        HashMap<UIComponent, List<String>> hoverables = null;

        if(selectedCategory.equals("General")) hoverables = generalHoverables;
        if(selectedCategory.equals("Skills")) hoverables = HOTMHoverables;
        if(selectedCategory.equals("Pets")) hoverables = petHoverables;
        if(selectedCategory.equals("Dungeons")) hoverables = dungeonHoverables;

        try {
            if(hoverables!=null) {
                for(Entry<UIComponent, List<String>> entry:hoverables.entrySet()) {
                    if(entry.getKey().isHovered()) {
                        renderTooltip = entry.getValue();
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        if (renderTooltip != null) {
            int tooltipWidth = Utils.GetMC().fontRendererObj.getStringWidth(renderTooltip.get(0));
            int tooltipHeight = renderTooltip.size() * Utils.GetMC().fontRendererObj.FONT_HEIGHT;

            int adjustedX = mouseX - 3;
            int adjustedY = mouseY;
            int screenWidth = Utils.GetMC().displayWidth;
            int screenHeight = Utils.GetMC().displayHeight/2;

            if (adjustedX < 0) {
                adjustedX = 0;
            } else if (adjustedX + tooltipWidth > screenWidth) {
                adjustedX = screenWidth - tooltipWidth;
            }

            if (adjustedY + tooltipHeight > screenHeight) {
                adjustedY = Math.max(screenHeight - tooltipHeight, mouseY - tooltipHeight - 12);
            }

            GuiUtils.drawHoveringText(renderTooltip, adjustedX, adjustedY, screenWidth, screenHeight, -1, Utils.GetMC().fontRendererObj);
            renderTooltip = null;
        }
    }
    // Skills
    static boolean skillApiDisabled = false;
    static SkillInfo blazeSlayer = new SkillInfo(0,0,0,null);
    static SkillInfo vampireSlayer = new SkillInfo(0,0,0,null);
    static SkillInfo wolfSlayer = new SkillInfo(0,0,0,null);
    static SkillInfo emanSlayer = new SkillInfo(0,0,0,null);
    static SkillInfo spiderSlayer = new SkillInfo(0,0,0,null);
    static SkillInfo zombieSlayer = new SkillInfo(0,0,0,null);
    static SkillInfo farmingLevel = new SkillInfo(0,0,0,null);
    static SkillInfo miningLevel = new SkillInfo(0,0,0,null);
    static SkillInfo combatLevel = new SkillInfo(0,0,0,null);
    static SkillInfo foragingLevel = new SkillInfo(0,0,0,null);
    static SkillInfo fishingLevel = new SkillInfo(0,0,0,null);
    static SkillInfo enchantingLevel = new SkillInfo(0,0,0,null);
    static SkillInfo alchemyLevel = new SkillInfo(0,0,0,null);
    static SkillInfo tamingLevel = new SkillInfo(0,0,0,null);
    static SkillInfo carpentryLevel = new SkillInfo(0,0,0,null);
    static SkillInfo socialLevel = new SkillInfo(0,0,0,null);
    static SkillInfo runecraftingLevel = new SkillInfo(0,0,0,null);

    public static class SkillInfo {
        public Integer level;
        public Integer currentXp;
        public Integer totalXp;
        public List<String> hover;

        public SkillInfo(Integer level, Integer totalXp, Integer currentXp,List<String> hover) {
            this.level = level;
            this.totalXp = totalXp;
            this.currentXp = currentXp;
            this.hover = hover;
        }
    }

    // Text/Lines colors
    Color titleColor = SkyblockFeatures.config.titleColor;//new Color(0x00FFFF);
    Color guiLines = SkyblockFeatures.config.guiLines;
    Color featureBoxOutline = SkyblockFeatures.config.featureBoxOutline;
    NumberFormat nf = NumberFormat.getInstance();
    String bold = ChatFormatting.WHITE+""+ChatFormatting.BOLD;
    String g = ChatFormatting.GRAY+"";
    // Background colors
    Color mainBackground = SkyblockFeatures.config.mainBackground;
    JsonObject achievementsJson = null;

    public static UIComponent box = null;
    UIComponent statsAreaContainer = null;
    static int screenHeight = Utils.GetMC().currentScreen.height;
    static double fontScale = screenHeight/540d;
    String playerUuid = "";
    public static Color clear = new Color(0,0,0,0);
    public ProfileViewerGui(Boolean doAnimation,String username,String profileString) {
        super(ElementaVersion.V2);

        String uuidString = APIUtils.getUUID(username,true);
        UUID uuid = UUID.fromString(uuidString);

        if(doAnimation) Utils.saveGuiScale();

        screenHeight = Utils.GetMC().currentScreen.height;
        fontScale = (screenHeight/540d);

        profile = new GameProfile(uuid, username);
        selectedCategory = "";

        // This sets the skin from the uuid
        Utils.GetMC().getSessionService().fillProfileProperties(profile, true);

        playerUuid = uuidString.replaceAll("-","");
        {
            box = new UIRoundedRectangle(10f)
                    .setX(new CenterConstraint())
                    .setY(new CenterConstraint())
                    .setWidth(new RelativeConstraint(0.70f))
                    .setHeight(new RelativeConstraint(0.70f))
                    .setChildOf(getWindow())
                    .enableEffect(new ScissorEffect())
                    .setColor(mainBackground);
            float guiWidth = box.getWidth();
            float guiHeight = box.getHeight();
            new ShadowIcon(new ResourceImageFactory("/assets/skyblockfeatures/gui/largeOutline.png",false),false).setChildOf(box)
                .setX(new PixelConstraint(0f))
                .setY(new PixelConstraint(0f))
                .setWidth(new RelativeConstraint(1f))
                .setHeight(new RelativeConstraint(1f));

            UIComponent titleArea = new UIBlock().setColor(clear).setChildOf(box)
                .setX(new CenterConstraint())
                .setWidth(new PixelConstraint(guiWidth))
                .setHeight(new PixelConstraint(0.15f*guiHeight))
                .enableEffect(new ScissorEffect());

            // Subtitle
            UIComponent subtitle = new UIText("Skyblock Features Profile Viewer")
                .setColor(Color.gray)
                .setChildOf(titleArea)
                .setX(new CenterConstraint())
                .setY(new PixelConstraint(3f,true))
                .enableEffect(new ScissorEffect())
                .setTextScale(new PixelConstraint(((float)fontScale)));

            // Title text
            UIComponent titleText = new UIText(username)
                .setColor(titleColor)
                .setChildOf(titleArea)
                .setX(new CenterConstraint())
                .setY(new CenterConstraint())
                .enableEffect(new ScissorEffect())
                .setTextScale(new PixelConstraint((float) (doAnimation?1*fontScale:3*fontScale)));

            // Gray horizontal line 1px from bottom of the title area
            new UIBlock().setChildOf(titleArea)
                .setWidth(new PixelConstraint(guiWidth-2))
                .setHeight(new PixelConstraint(1f))
                .setX(new CenterConstraint())
                .setY(new PixelConstraint(titleArea.getHeight()-1))
                .setColor(guiLines);
            new UIBlock()
                .setX(new PixelConstraint(0f))
                .setY(new PixelConstraint(titleArea.getHeight()))
                .setWidth(new PixelConstraint(0.25f*guiWidth))
                .setHeight(new PixelConstraint(0.85f*guiHeight))
                .setChildOf(box)
                .setColor(clear)
                .enableEffect(new ScissorEffect());


            // Seperator to the right side of the sidebar
            UIComponent sidebarSeperator = new UIBlock()
                .setWidth(new PixelConstraint(1f))
                .setHeight(new PixelConstraint((0.85f*guiHeight)-1))
                .setX(new PixelConstraint(0.25f*guiWidth))
                .setY(new PixelConstraint(titleArea.getHeight()))
                .setColor(guiLines);

            box.addChild(titleArea);
            box.addChild(sidebarSeperator);

            if(doAnimation) {
                box.setWidth(new PixelConstraint(0f));

                AnimatingConstraints anim = box.makeAnimation();
                anim.setWidthAnimation(Animations.OUT_EXP, 0.5f, new RelativeConstraint(0.70f));
                box.animateTo(anim);

                AnimatingConstraints animation = titleText.makeAnimation();
                animation.setTextScaleAnimation(Animations.OUT_EXP, 0.5f, new PixelConstraint((float) (3.0*fontScale)));
                titleText.animateTo(animation);
            }
        }

		new Thread(() -> {
			System.out.println("Starting thread ");

            hypixelProfilesResponse = null;
            String latestProfile = APIUtils.getLatestProfileID(playerUuid);
            if (profileString.equals("auto") && latestProfile == null) return;
            String locationURL = "https://api.hypixel.net/status?uuid="+playerUuid+"#GetLocationPV";
            String profileURL = "https://api.hypixel.net/skyblock/profiles?uuid=" + playerUuid+"#GetProfilePV";
            String achievmentsURL = "https://api.hypixel.net/resources/achievements?uuid=" + playerUuid+"#getAchievmentsPV";

            System.out.println("Fetching Hypixel profile...");
            JsonObject profiles = APIUtils.getJSONResponse(profileURL);

            JsonObject locationJson = APIUtils.getJSONResponse(locationURL);
            achievementsJson = APIUtils.getJSONResponse(achievmentsURL).get("achievements").getAsJsonObject();
            Boolean playerOnline = locationJson.get("session").getAsJsonObject().get("online").getAsBoolean();
            if(playerOnline) {
                String location = locationJson.get("session").getAsJsonObject().get("mode").getAsString();
                String formattedLocation = Utils.convertIdToLocation(location);
                playerLocation = ChatFormatting.GREEN+formattedLocation;
            } else {
                playerLocation = ChatFormatting.RED+"OFFLINE";
            }

            if(profiles.has("cause")) {
                System.out.println(profiles.get("cause").getAsString());
                return;
            }
            hypixelProfilesResponse = profiles.get("profiles").getAsJsonArray();

            try {
                System.out.println("Finding Current/specified Profile");
                AtomicBoolean found = new AtomicBoolean(false);
                hypixelProfilesResponse.forEach((profile)-> {
                    String cuteName = profile.getAsJsonObject().get("cute_name").getAsString();
                    if(profileString.equals("auto")) {
                        System.out.println(profile.getAsJsonObject().get("profile_id").getAsString()+" vs "+latestProfile);
                        if(profile.getAsJsonObject().get("profile_id").getAsString().equals(latestProfile)) {
                            System.out.println("Loading Current Profile");
                            loadProfile(cuteName,true);
                            found.set(true);
                        }
                    } else {
                        System.out.println(profile.getAsJsonObject().get("profile_id").getAsString()+" vs "+profileString);
                        if(profile.getAsJsonObject().get("profile_id").getAsString().equals(profileString)) {
                            System.out.println("Loading specified Profile");
                            loadProfile(cuteName,true);
                            found.set(true);
                        }
                    }
                });
                if(!found.get()) {
                    displayError("We couldn't find this player's profile. It's possible they have been removed from the co-op.");
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }).start();
    }

    public void displayError(String error) {
        cleanBox();
        selectedCategory = "error";

        new UIWrappedText(ChatFormatting.RED + error)
            .setTextScale(new PixelConstraint(2f))
            .setChildOf(box)
            .setX(new CenterConstraint())
            .setY(new CenterConstraint());
    }

    public void cleanBox() {
        box.clearChildren();
        box = new UIRoundedRectangle(10f)
            .setX(new CenterConstraint())
            .setY(new CenterConstraint())
            .setWidth(new RelativeConstraint(0.70f))
            .setHeight(new RelativeConstraint(0.70f))
            .setChildOf(getWindow())
            .setColor(clear);
        new ShadowIcon(new ResourceImageFactory("/assets/skyblockfeatures/gui/largeOutline.png",false),false).setChildOf(box)
            .setX(new PixelConstraint(0f))
            .setY(new PixelConstraint(0f))
            .setWidth(new RelativeConstraint(1f))
            .setHeight(new RelativeConstraint(1f));
    }

    UIComponent sideButtonContainer = new UIRoundedRectangle(5f)
            .setColor(clear)
            .setX(new RelativeConstraint(0.03f))
            .setY(new RelativeConstraint(0.3f))
            .setWidth(new RelativeConstraint(0.12f))
            .setHeight(new RelativeConstraint(0.6f))
            .enableEffect(new ScissorEffect())
            .setChildOf(getWindow());
    JsonObject profiles = new JsonObject();
    static JsonObject networthResponse = null;

    public void loadProfile(String cute_name,Boolean initial) {
        System.out.println("Loading Profile "+cute_name+ " initial: "+initial);
        generalHoverables.clear();
        petHoverables.clear();
        HOTMHoverables.clear();
        selectedCategory = "General";

        if(generalButton!=null) {
            ProfileViewerUtils.animateX(lastSelectedButton, 8f);
            lastSelectedButton = generalButton;
            ProfileViewerUtils.animateX(lastSelectedButton, 0f);
        }
        if(initial) {
            drawSideButton(sideButtonContainer,"General",()->{
                loadCategory("General");
            });
            drawSideButton(sideButtonContainer,"Inventories",()->{
                loadCategory("Inventories");
            });
            drawSideButton(sideButtonContainer,"Pets",()->{
                loadCategory("Pets");
            });
            drawSideButton(sideButtonContainer,"Skills",()->{
                loadCategory("Skills");
            });
            drawSideButton(sideButtonContainer,"Dungeons",()->{
                loadCategory("Dungeons");
            });
            drawSideButton(sideButtonContainer,"Collections",()->{
                loadCategory("Collections");
            });
            drawSideButton(sideButtonContainer,"Crimson",()->{
                // loadCategory("Crimson");
                Utils.SendMessage(ChatFormatting.RED+"Currently Disabled");
            });
            drawSideButton(sideButtonContainer,"Misc Stats",()->{
                // loadCategory("Misc Stats");
                Utils.SendMessage(ChatFormatting.RED+"Currently Disabled");
            });
        }

        String profileUUID = null;
        cute_name = Utils.cleanColor(cute_name);
        List<String> profileList = new ArrayList<>();
        List<String> coopMemberList = new ArrayList<>();
        Thread loadUsernamesThread=null;

        for(JsonElement profile : hypixelProfilesResponse) {
            JsonObject profileObject = profile.getAsJsonObject();
            if(profileObject.get("selected").getAsBoolean()) {
                profileList.add(0, ChatFormatting.GREEN+profileObject.get("cute_name").getAsString());
            } else {
                profileList.add(ChatFormatting.GREEN+profileObject.get("cute_name").getAsString());
            }
            if(profileObject.get("cute_name").getAsString().equals(cute_name)) {
                profileUUID = profileObject.get("profile_id").getAsString();
                System.out.println("Adding members..");
                for(Entry<String, JsonElement> entry:profileObject.get("members").getAsJsonObject().entrySet()) {
                    String username = APIUtils.getName(entry.getKey());
                    System.out.println("Adding username: "+username);
                    coopMemberList.add(username);
                }
            }
        }


        if(profileUUID==null) return;
        selectedProfileUUID = profileUUID;

        hypixelProfilesResponse.forEach((profile)->{
            if(profile.getAsJsonObject().get("profile_id").getAsString().equals(selectedProfileUUID)) {
                ProfilePlayerResponse = profile.getAsJsonObject().get("members").getAsJsonObject().get(playerUuid).getAsJsonObject();
                ProfileResponse = profile.getAsJsonObject();
            }
        });

        resetSkillsAndSlayers();
        setSkillsAndSlayers(ProfilePlayerResponse);

        int sbLevelXP = 0;
        if(ProfilePlayerResponse.has("leveling")) {
           sbLevelXP = ProfilePlayerResponse.get("leveling").getAsJsonObject().get("experience").getAsInt();
        }
        profiles = new JsonObject();
        new Thread(()->{
            profiles = APIUtils.getJSONResponse("https://sky.shiiyu.moe/api/v2/profile/"+playerUuid+"#skycryptForPV").get("profiles").getAsJsonObject();
        }).start();

        Integer sbLevelCurrXp = sbLevelXP%100;
        int sbLevel = (int) Math.floor(sbLevelXP / 100);

        cleanBox();
        float guiWidth = box.getWidth();
        float guiHeight = box.getHeight();

        UIComponent titleArea = new UIBlock().setColor(clear).setChildOf(box)
            .setX(new CenterConstraint())
            .setWidth(new PixelConstraint(guiWidth))
            .setHeight(new PixelConstraint(0.15f*guiHeight))
            .enableEffect(new ScissorEffect());

        // Subtitle
        UIComponent subtitle = new UIText("Skyblock Features Profile Viewer")
            .setColor(Color.gray)
            .setChildOf(titleArea)
            .setX(new CenterConstraint())
            .setY(new PixelConstraint(3f,true))
            .enableEffect(new ScissorEffect())
            .setTextScale(new PixelConstraint(((float)fontScale)));

        // Title text
        UIComponent titleText = new UIText(profile.getName())
            .setColor(titleColor)
            .setChildOf(titleArea)
            .setX(new CenterConstraint())
            .setY(new CenterConstraint())
            .enableEffect(new ScissorEffect())
            .setTextScale(new PixelConstraint((float) (3f*fontScale)));

        // Gray horizontal line 1px from bottom of the title area
        new UIBlock().setChildOf(titleArea).setWidth(new PixelConstraint(guiWidth-2)).setHeight(new PixelConstraint(1f)).setX(new CenterConstraint()).setY(new PixelConstraint(titleArea.getHeight()-1)).setColor(guiLines);

        // Area of where the stats are all contained
        statsAreaContainer = new ScrollComponent("", 10f, featureBoxOutline, false, true, false, false, 25f, 1f, null)
            .setX(new PixelConstraint(0.25f*guiWidth))
            .setY(new PixelConstraint(titleArea.getHeight()))
            .setWidth(new PixelConstraint(0.75f*guiWidth))
            .setHeight(new PixelConstraint(((0.85f*guiHeight)-1)));

        UIComponent statsAreaTop = new UIBlock(clear).setX(new PixelConstraint(0f)).setChildOf(statsAreaContainer).setY(new PixelConstraint(0f)).setWidth(new PixelConstraint(0.75f*guiWidth*0.91f)).setHeight(new PixelConstraint((0.36f*guiHeight)-1));
        UIComponent statsAreaLeft = new UIBlock(clear).setX(new PixelConstraint(0f)).setY(new PixelConstraint((0.09f*guiHeight)-1)).setChildOf(statsAreaContainer).setWidth(new PixelConstraint(0.75f*guiWidth*0.30f)).setHeight(new PixelConstraint(((0.35f*guiHeight)-1)));
        UIComponent statsAreaMid = new UIBlock(clear).setX(new PixelConstraint(0.25f*guiWidth)).setY(new PixelConstraint((0.09f*guiHeight)-1)).setChildOf(statsAreaContainer).setWidth(new PixelConstraint(0.75f*guiWidth*0.30f)).setHeight(new PixelConstraint(((0.35f*guiHeight)-1)));
        UIComponent statsAreaRight = new UIBlock(clear).setX(new PixelConstraint(0.50f*guiWidth)).setY(new PixelConstraint((0.09f*guiHeight)-1)).setChildOf(statsAreaContainer).setWidth(new PixelConstraint(0.75f*guiWidth*0.30f)).setHeight(new PixelConstraint(((0.35f*guiHeight)-1)));

        drawProgressbar(sbLevelCurrXp,100,statsAreaTop,"Level "+sbLevel,new ItemStack(Items.diamond),null, false);
        drawProgressbar(tamingLevel.currentXp,tamingLevel.totalXp,statsAreaLeft,"Taming "+tamingLevel.level,new ItemStack(Items.spawn_egg),tamingLevel.hover,true);
        drawProgressbar(miningLevel.currentXp,miningLevel.totalXp,statsAreaLeft,"Mining "+miningLevel.level,new ItemStack(Items.iron_pickaxe),miningLevel.hover,true);
        drawProgressbar(foragingLevel.currentXp,foragingLevel.totalXp,statsAreaLeft,"Foraging "+foragingLevel.level,new ItemStack(Blocks.sapling),foragingLevel.hover, true);
        drawProgressbar(enchantingLevel.currentXp,enchantingLevel.totalXp,statsAreaLeft,"Enchanting "+enchantingLevel.level,new ItemStack(Blocks.enchanting_table),enchantingLevel.hover,true);
        drawProgressbar(carpentryLevel.currentXp,carpentryLevel.totalXp,statsAreaLeft,"Carpentry "+carpentryLevel.level,new ItemStack(Blocks.crafting_table),carpentryLevel.hover,true);
        drawProgressbar(socialLevel.currentXp,socialLevel.totalXp,statsAreaLeft,"Social "+socialLevel.level,new ItemStack(Items.emerald),socialLevel.hover,true);

        drawProgressbar(farmingLevel.currentXp,farmingLevel.totalXp,statsAreaMid,"Farming "+farmingLevel.level,new ItemStack(Items.golden_hoe),farmingLevel.hover,true);
        drawProgressbar(combatLevel.currentXp,combatLevel.totalXp,statsAreaMid,"Combat "+combatLevel.level,new ItemStack(Items.stone_sword),combatLevel.hover,true);
        drawProgressbar(fishingLevel.currentXp,fishingLevel.totalXp,statsAreaMid,"Fishing "+fishingLevel.level,new ItemStack(Items.fishing_rod),fishingLevel.hover,true);
        drawProgressbar(alchemyLevel.currentXp,alchemyLevel.totalXp,statsAreaMid,"Alchemy "+alchemyLevel.level,new ItemStack(Items.potionitem),alchemyLevel.hover,true);
        drawProgressbar(runecraftingLevel.currentXp,runecraftingLevel.totalXp,statsAreaMid,"Runecrafting "+runecraftingLevel.level,new ItemStack(Items.magma_cream),runecraftingLevel.hover,true);

        drawProgressbar(zombieSlayer.currentXp,zombieSlayer.totalXp,statsAreaRight,"Rev "+zombieSlayer.level,new ItemStack(Items.rotten_flesh),zombieSlayer.hover, false);
        drawProgressbar(spiderSlayer.currentXp,spiderSlayer.totalXp,statsAreaRight,"Tara "+spiderSlayer.level,new ItemStack(Items.spider_eye),spiderSlayer.hover, false);
        drawProgressbar(wolfSlayer.currentXp,wolfSlayer.totalXp,statsAreaRight,"Sven "+wolfSlayer.level,new ItemStack(Items.bone),wolfSlayer.hover, false);
        drawProgressbar(emanSlayer.currentXp,emanSlayer.totalXp,statsAreaRight,"Eman "+emanSlayer.level,new ItemStack(Items.ender_pearl),emanSlayer.hover, false);
        drawProgressbar(blazeSlayer.currentXp,blazeSlayer.totalXp,statsAreaRight,"Blaze "+blazeSlayer.level,new ItemStack(Items.blaze_rod),blazeSlayer.hover, false);
        drawProgressbar(vampireSlayer.currentXp,vampireSlayer.totalXp,statsAreaRight,"Vampire "+vampireSlayer.level,new ItemStack(Items.wooden_sword),vampireSlayer.hover, false);

        UIComponent generalInfoContainer = new UIBlock(clear).setY(new SiblingConstraint(20f)).setX(new PixelConstraint(0f)).setWidth(new RelativeConstraint(1f)).setHeight(new RelativeConstraint(0.175f)).setChildOf(statsAreaContainer);
        long Purse = 0;
        long Bank = 0;
        try {Purse = (long) ProfilePlayerResponse.get("coin_purse").getAsDouble();} catch (Exception ignored) {}
        try {Bank = (long) ProfileResponse.get("banking").getAsJsonObject().get("balance").getAsDouble();} catch (Exception ignored) {}

        String networth = ChatFormatting.RED+"Loading";

        String pattern = "MMMM d yyyy";SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        long joinedDate = ProfilePlayerResponse.get("first_join").getAsLong();
        String joinedString = simpleDateFormat.format(new Date(joinedDate));
        String profileType = ChatFormatting.GREEN+bold+"Classic";

        if(ProfileResponse.has("game_mode")) {
            String gamemode = ProfileResponse.get("game_mode").getAsString();

            if(gamemode.equals("ironman")) profileType = ChatFormatting.WHITE+bold+"♲ Ironman";
            if(gamemode.equals("stranded")) profileType = ChatFormatting.GREEN+bold+"☀ Stranded";
        }

        String fairySouls = ProfilePlayerResponse.get("fairy_souls_collected").getAsInt()+" / 240";

        UIComponent topRow = new UIBlock(clear).setWidth(new RelativeConstraint(1f)).setY(new PixelConstraint(0f)).setChildOf(generalInfoContainer).setHeight(new RelativeConstraint(0.15f));
        UIComponent midRow = new UIBlock(clear).setWidth(new RelativeConstraint(1f)).setY(new SiblingConstraint(2f)).setChildOf(generalInfoContainer).setHeight(new RelativeConstraint(0.15f));
        UIComponent lastRow = new UIBlock(clear).setWidth(new RelativeConstraint(1f)).setY(new SiblingConstraint(2f)).setChildOf(generalInfoContainer).setHeight(new RelativeConstraint(0.15f));

        new UIText(g+"Profile Name: "+bold+Utils.convertToTitleCase(cute_name)).setX(new SiblingConstraint(10f)).setChildOf(topRow);
        new UIText(g+"Profile Type: "+profileType).setX(new SiblingConstraint(10f)).setChildOf(topRow);
        new UIText(g+"Joined: "+bold+joinedString).setX(new SiblingConstraint(10f)).setChildOf(topRow);

        new UIText(g+"Current Area: "+bold+playerLocation.trim()).setX(new PixelConstraint(0)).setChildOf(midRow);

        UIComponent networthComponent = new UIText(g+"Networth: "+bold+networth).setX(new SiblingConstraint(10f)).setChildOf(midRow);

        new UIText(g+"Bank: "+bold+Utils.formatNumber(Bank)).setX(new SiblingConstraint(10f)).setChildOf(midRow);
        new UIText(g+"Fairy Souls: "+bold+fairySouls).setX(new SiblingConstraint(10f)).setChildOf(lastRow);

        // Sidebar on the left that holds the categories
        UIComponent sidebarArea = new UIBlock()
            .setX(new PixelConstraint(0f))
            .setY(new PixelConstraint(titleArea.getHeight()))
            .setWidth(new PixelConstraint(0.25f*guiWidth))
            .setHeight(new PixelConstraint(0.85f*guiHeight))
            .setColor(clear)
            .enableEffect(new ScissorEffect());

        EmulatedPlayerBuilder playerBuilder = new EmulatedPlayerBuilder();
        playerBuilder.setProfile(profile);

        EssentialAPI.getEssentialComponentFactory().build(playerBuilder)
            .setChildOf(sidebarArea)
            .setX(new CenterConstraint())
            .setY(new CenterConstraint())
            .setHeight(new RelativeConstraint(0.75f))
            .setWidth(new RelativeConstraint(0.75f));

        System.out.println(profile.getName());
        int coopSelectorIndex = -1;
        int currentCoopSelectorIndex = -1;
        for (String member : coopMemberList) {
            currentCoopSelectorIndex++;
            if (member.equalsIgnoreCase(profile.getName())) {
                coopSelectorIndex = currentCoopSelectorIndex;
                break;
            }
        }

        DropDownComponent coopSelector = (DropDownComponent) new DropDownComponent(coopSelectorIndex, coopMemberList,coopMemberList.size())
            .setChildOf(sidebarArea)
            .setWidth(new RelativeConstraint(0.60f))
            .setX(new CenterConstraint())
            .setY(new RelativeConstraint(0.88f));
        System.out.println("Created Coop Selector");

        DropDownComponent profileSelector = (DropDownComponent) new DropDownComponent(profileList.indexOf(ChatFormatting.GREEN+cute_name), profileList,profileList.size())
            .setChildOf(sidebarArea)
            .setWidth(new RelativeConstraint(0.50f))
            .setX(new CenterConstraint())
            .setY(new RelativeConstraint(0.94f));
        System.out.println("Created Profile Selector");

        profileSelector.getSelectedText().onSetValue((value)->{
            ProfileViewerUtils.animateX(lastSelectedButton, 8f);
            loadProfile(value,false);
            return Unit.INSTANCE;
        });

        coopSelector.getSelectedText().onSetValue((value)->{
            ProfileViewerUtils.animateX(lastSelectedButton, 8f);
            System.out.println("Loading PROFILEEE: "+value+" "+selectedProfileUUID);
            quickSwapping = true;
            Utils.openGui(new ProfileViewerGui(false,value,selectedProfileUUID));
            return Unit.INSTANCE;
        });

        UIComponent armorComponent = new UIBlock(clear)
                .setX(new CenterConstraint())
                .setY(new RelativeConstraint(0.005f))
                .setWidth(new PixelConstraint(4*21f))
                .setHeight(new PixelConstraint(20f));

        if(ProfilePlayerResponse.has("inv_armor")) {
            String inventoryBase64 = ProfilePlayerResponse.get("inv_armor").getAsJsonObject().get("data").getAsString();
            Inventory items = new Inventory(inventoryBase64);
            List<ItemStack> a = ItemUtils.decodeItem(items,true);
            List<ItemStack> b = new ArrayList<>(Arrays.asList(null,null,null,null));

            int index = 0;
            for(ItemStack item: a) {
                index++;
                // Leggings
                if(index==1) b.set(2, item);
                // Chestplate
                if(index==2) b.set(1, item);
                // Helmet
                if(index==3) b.set(0, item);
                // Boots
                if(index==4) b.set(3, item);
            }
            for(ItemStack item: b) {
                UIComponent backgroundSlot = new UIRoundedRectangle(3f)
                    .setHeight(new PixelConstraint(20f))
                    .setWidth(new PixelConstraint(20f))
                    .setX(new SiblingConstraint(1f))
                    .setColor(new Color(100,100,100,200));

                backgroundSlot.addChild(new ItemStackComponent(item)
                    .setHeight(new PixelConstraint(20f))
                    .setWidth(new PixelConstraint(20f))
                    .setX(new CenterConstraint())
                    .setY(new CenterConstraint()));
                armorComponent.addChild(backgroundSlot);
            }
        }

        UIComponent equipmentComponent = new UIBlock(clear)
            .setX(new CenterConstraint())
            .setY(new SiblingConstraint(2f,false))
            .setWidth(new PixelConstraint(4*17f))
            .setHeight(new PixelConstraint(16f));

        if(ProfilePlayerResponse.has("equippment_contents")) {
            String inventoryBase64 = ProfilePlayerResponse.get("equippment_contents").getAsJsonObject().get("data").getAsString();
            Inventory items = new Inventory(inventoryBase64);
            List<ItemStack> a = ItemUtils.decodeItem(items,false);

            for(ItemStack item: a) {
                UIComponent backgroundSlot = new UIRoundedRectangle(3f)
                    .setHeight(new PixelConstraint(16f))
                    .setWidth(new PixelConstraint(16f))
                    .setX(new SiblingConstraint(1f))
                    .setColor(new Color(100,100,100,200));

                backgroundSlot.addChild(new ItemStackComponent(item)
                    .setX(new CenterConstraint())
                    .setY(new CenterConstraint()));
                equipmentComponent.addChild(backgroundSlot);
            }
        }

        sidebarArea.addChild(armorComponent);
        sidebarArea.addChild(equipmentComponent);

        // Separator to the right side of the sidebar
        UIComponent sidebarSeparator = new UIBlock()
            .setWidth(new PixelConstraint(1f))
            .setHeight(new PixelConstraint((0.85f*guiHeight)-1))
            .setX(new PixelConstraint(0.25f*guiWidth))
            .setY(new PixelConstraint(titleArea.getHeight()))
            .setColor(guiLines);

        box.addChild(titleArea);
        box.addChild(sidebarArea);

        box.addChild(sidebarSeparator);
        box.addChild(statsAreaContainer);

        titleText.setTextScale(new PixelConstraint((float) (3.0*fontScale)));
        loadCollectionsCategories();


        loadNetworth(profileUUID,playerUuid,Purse,Bank,networthComponent);
    }

    public void loadNetworth(String profileUUID,String playerUuid,long Purse,long Bank,UIComponent networthComponent) {
        System.out.println("doing networth");

        new Thread(()->{
            JsonObject profiles = APIUtils.getJSONResponse("https://sky.shiiyu.moe/api/v2/profile/"+playerUuid+"#skycryptForPV").get("profiles").getAsJsonObject();

            List<String> networthTooltip = new ArrayList<>(Arrays.asList(ChatFormatting.RED + "Player has API disabled"));
            String networth = ChatFormatting.RED+"Loading";

            while (profiles.entrySet().isEmpty()) {
                try {
                    Thread.sleep(100); // Add a delay of 100 milliseconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (profiles.has(profileUUID) && profiles.get(profileUUID).isJsonObject()) {
                JsonObject profileObject = profiles.getAsJsonObject(profileUUID);
                if (profileObject.has("data") && profileObject.get("data").isJsonObject()) {
                    JsonObject dataObject = profileObject.getAsJsonObject("data");
                    if (dataObject.has("networth") && dataObject.get("networth").isJsonObject()) {
                        networthResponse = dataObject.getAsJsonObject("networth");
                    }
                }
            }

            JsonObject museumResponse = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/museum?profile="+profileUUID);

            JsonObject networthCategories = networthResponse.get("types").getAsJsonObject();

            if(museumResponse.get("success").getAsBoolean()) {
                boolean museumApiEnabled = !museumResponse.get("members").getAsJsonObject().entrySet().isEmpty();
                if(museumApiEnabled) {
                    JsonObject members =  museumResponse.get("members").getAsJsonObject();
                    JsonObject member = members.get(playerUuid).getAsJsonObject();
                    Long value = member.get("value").getAsLong();
                    networthCategories.addProperty("museum", value);
                }
            }
            long Armor = 0;
            long equipment = 0;
            long Wardrobe = 0;
            long Inventory = 0;
            long Museum = 0;
            long accessories = 0;
            long enderchest = 0;
            long Sacks = 0;
            long Storage = 0;
            long pets = 0;
            long total = 0;
            long irl = 0;

            try {Armor = networthCategories.get("armor").getAsJsonObject().get("total").getAsLong();} catch (NullPointerException ignored) {}
            try {equipment = networthCategories.get("equipment").getAsJsonObject().get("total").getAsLong();} catch (NullPointerException ignored) {}
            try {Wardrobe = networthCategories.get("wardrobe").getAsJsonObject().get("total").getAsLong();} catch (NullPointerException ignored) {}
            try {Inventory = networthCategories.get("inventory").getAsJsonObject().get("total").getAsLong();} catch (NullPointerException ignored) {}
            try {accessories = networthCategories.get("accessories").getAsJsonObject().get("total").getAsLong();} catch (NullPointerException ignored) {}
            try {Storage = networthCategories.get("storage").getAsJsonObject().get("total").getAsLong();} catch (NullPointerException ignored) {}
            try {enderchest = networthCategories.get("enderchest").getAsJsonObject().get("total").getAsLong();} catch (NullPointerException ignored) {}
            try {Sacks = networthCategories.get("sacks").getAsJsonObject().get("total").getAsLong();} catch (NullPointerException ignored) {}
            try {pets = networthCategories.get("pets").getAsJsonObject().get("total").getAsLong();} catch (NullPointerException ignored) {}
            try {Museum = networthCategories.get("museum").getAsLong();} catch (NullPointerException ignored) {}
            try {total = networthResponse.get("networth").getAsLong()+Museum;} catch (NullPointerException ignored) {}

            if(PricingData.bazaarPrices.get("BOOSTER_COOKIE")!=null) irl = (int) ((total/PricingData.bazaarPrices.get("BOOSTER_COOKIE"))*2.4);

            networthTooltip = new ArrayList<>(Arrays.asList(
                ChatFormatting.AQUA + "Networth",
                ChatFormatting.ITALIC +""+ ChatFormatting.DARK_GRAY + "Networth calculations by SkyHelper",
                "",
                ChatFormatting.GREEN + "Total Networth: " + ChatFormatting.GOLD + nf.format(total),
                ChatFormatting.GREEN + "IRL Worth: " + ChatFormatting.DARK_GREEN+"$" + nf.format(irl),
                "",
                ChatFormatting.GREEN + "Purse: " + ChatFormatting.GOLD + Utils.formatNumber(Purse) + Utils.percentOf(Purse,total),
                ChatFormatting.GREEN + "Bank: " + ChatFormatting.GOLD + Utils.formatNumber(Bank) + Utils.percentOf(Bank,total),
                ChatFormatting.GREEN + "Sacks: " + ChatFormatting.GOLD + Utils.formatNumber(Sacks) + Utils.percentOf(Sacks,total),
                ChatFormatting.GREEN + "Armor: " + ChatFormatting.GOLD + Utils.formatNumber(Armor) + Utils.percentOf(Armor,total),
                ChatFormatting.GREEN + "Museum: " + ChatFormatting.GOLD + Utils.formatNumber(Museum) + Utils.percentOf(Museum,total),
                ChatFormatting.GREEN + "Storage: " + ChatFormatting.GOLD + Utils.formatNumber(Storage) + Utils.percentOf(Storage,total),
                ChatFormatting.GREEN + "Wardrobe: " + ChatFormatting.GOLD + Utils.formatNumber(Wardrobe) + Utils.percentOf(Wardrobe,total),
                ChatFormatting.GREEN + "Inventory: " + ChatFormatting.GOLD + Utils.formatNumber(Inventory) + Utils.percentOf(Inventory,total),
                ChatFormatting.GREEN + "Equipment: " + ChatFormatting.GOLD + Utils.formatNumber(equipment) + Utils.percentOf(equipment,total),
                ChatFormatting.GREEN + "Enderchest: " + ChatFormatting.GOLD + Utils.formatNumber(enderchest) + Utils.percentOf(enderchest,total),
                ChatFormatting.GREEN + "Accessories: " + ChatFormatting.GOLD + Utils.formatNumber(accessories) + Utils.percentOf(accessories,total),
                ChatFormatting.GREEN + "Pets: " + ChatFormatting.GOLD + Utils.formatNumber(pets)+Utils.percentOf(pets,total)
            ));
            networth = nf.format(networthResponse.get("networth").getAsLong()+Museum);
            ((UIText) networthComponent).setText(g+"Networth: "+bold+networth);
            generalHoverables.put(networthComponent, networthTooltip);
        }).start();
    }


    /**
     * @param v Value
     * @param m Max
     * @param hover hover text
     */
    public static void drawProgressbar(Integer v, Integer m,UIComponent statsArea,String label,ItemStack labelItem,List<String> hover,Boolean skill) {
        if(m==null) m = 0;
        if(v==null) v = 0;

        float value = v.floatValue();
        float max = m.floatValue();

        // System.out.println("Value: "+value+" Max: "+max+" "+label);
        UIComponent container = new UIBlock(clear).setChildOf(statsArea)
            .setWidth(new RelativeConstraint(1f))
            .setHeight(new RelativeConstraint(0.12f))
            .setY(new SiblingConstraint(8f, false))
            .setX(new CenterConstraint());
        generalHoverables.put(container,hover);

        // Title
        UIComponent labelText = new UIText(label).setChildOf(container)
            .setX(new PixelConstraint(20f))
            .setY(new PixelConstraint(0f));

        float percent = Math.min(value/max,1);

        UIComponent progressBarContainer = new UIBlock(clear)
            .setY(new SiblingConstraint(1f))
            .setX(new CenterConstraint())
            .setChildOf(container)
            .setWidth(new RelativeConstraint(1f))
            .setHeight(new RelativeConstraint(0.6f));

        UIComponent progressBarBackground = new UIRoundedRectangle(5f)
            .setColor(new Color(100,100,100))
            .setWidth(new RelativeConstraint(1f))
            .setHeight(new RelativeConstraint(1f))
            .setY(new SiblingConstraint(5f))
            .enableEffect(new ScissorEffect())
            .setX(new CenterConstraint())
            .setChildOf(progressBarContainer);
        boolean maxed = value>=max && value!=0;

        Color color = maxed?new Color(255,191,0):new Color(0x0baa51);

        UIComponent progressBarFillColor = new UIRoundedRectangle(5f)
            .setColor(color)
            .setChildOf(progressBarBackground)
            .setWidth(new RelativeConstraint(percent))
            .setHeight(new RelativeConstraint(1f))
            .setX(new PixelConstraint(0f));

        UIComponent gradient = new GradientComponent(new Color(0,0,0,150), new Color(0,0,0,0),GradientDirection.LEFT_TO_RIGHT)
            .setWidth(new RelativeConstraint(0.1f))
            .setHeight(new RelativeConstraint(1f))
            .setX(new PixelConstraint(5f))
            .setChildOf(progressBarContainer);

        Color colorCircle = maxed?new Color(0xdd980e):new Color(0x0bca51);

        if(skill && skillApiDisabled) {
            colorCircle = new Color(0x5B5351);
        }

        UIComponent greenCircle = new UICircle(10f, colorCircle)
            .setChildOf(progressBarContainer)
            .setX(new PixelConstraint(5f));
        labelItem.setStackDisplayName("");

        new ItemStackComponent(labelItem).setChildOf(greenCircle).setX(new CenterConstraint()).setY(new CenterConstraint());

        String l1 = Utils.formatNumber(v.longValue());
        String l2 = Utils.formatNumber(m.longValue());

        // Percent Values
        if(skill && skillApiDisabled) {
            new UIText(ChatFormatting.RED+"Player has API Disabled").setChildOf(progressBarContainer)
                .setX(new CenterConstraint())
                .setY(new CenterConstraint());
        }

        else if(!maxed) {
            new UIText(l1+" / "+l2+" XP").setChildOf(progressBarContainer)
                .setX(new CenterConstraint())
                .setY(new CenterConstraint());
        } else {
            new UIText(l1).setChildOf(progressBarContainer)
                .setX(new CenterConstraint())
                .setY(new CenterConstraint());
        }
    }

    public void resetSkillsAndSlayers() {
        System.out.println("Resetting Skills & Slayers");
        skillApiDisabled = false;

        // Reset Skills
        tamingLevel = new SkillInfo(0,0,0,null);
        farmingLevel = new SkillInfo(0,0,0,null);
        miningLevel = new SkillInfo(0,0,0,null);
        combatLevel = new SkillInfo(0,0,0,null);
        foragingLevel = new SkillInfo(0,0,0,null);
        fishingLevel = new SkillInfo(0,0,0,null);
        enchantingLevel = new SkillInfo(0,0,0,null);
        alchemyLevel = new SkillInfo(0,0,0,null);
        socialLevel = new SkillInfo(0,0,0,null);
        runecraftingLevel = new SkillInfo(0,0,0,null);
        carpentryLevel = new SkillInfo(0,0,0,null);

        // Reset slayers
        zombieSlayer = new SkillInfo(0,0,0,null);
        spiderSlayer = new SkillInfo(0,0,0,null);
        blazeSlayer = new SkillInfo(0,0,0,null);
        emanSlayer = new SkillInfo(0,0,0,null);
        wolfSlayer = new SkillInfo(0,0,0,null);
        vampireSlayer = new SkillInfo(0,0,0,null);
    }

    public void setSkillsAndSlayers(JsonObject userObject) {
        if(userObject==null) return;

        System.out.println("Updating Skills");
        try {
            if (userObject.has("experience_skill_taming")) {
                double tamingXp = userObject.get("experience_skill_taming").getAsDouble();
                tamingLevel = ProfileViewerUtils.getSkillInfo(tamingXp, "Taming");
            }
            if (userObject.has("experience_skill_farming")) {
                double farmingXp = userObject.get("experience_skill_farming").getAsDouble();
                farmingLevel = ProfileViewerUtils.getSkillInfo(farmingXp, "Farming");
            }
            if (userObject.has("experience_skill_mining")) {
                double miningXp = userObject.get("experience_skill_mining").getAsDouble();
                miningLevel = ProfileViewerUtils.getSkillInfo(miningXp, "Mining");
            }
            if (userObject.has("experience_skill_combat")) {
                double combatXp = userObject.get("experience_skill_combat").getAsDouble();
                combatLevel = ProfileViewerUtils.getSkillInfo(combatXp, "Combat");
            }
            if (userObject.has("experience_skill_foraging")) {
                double foragingXp = userObject.get("experience_skill_foraging").getAsDouble();
                foragingLevel = ProfileViewerUtils.getSkillInfo(foragingXp, "Foraging");
            }
            if (userObject.has("experience_skill_fishing")) {
                double fishingXp = userObject.get("experience_skill_fishing").getAsDouble();
                fishingLevel = ProfileViewerUtils.getSkillInfo(fishingXp, "Fishing");
            }
            if (userObject.has("experience_skill_enchanting")) {
                double enchantingXp = userObject.get("experience_skill_enchanting").getAsDouble();
                enchantingLevel = ProfileViewerUtils.getSkillInfo(enchantingXp, "Enchanting");
            }
            if (userObject.has("experience_skill_alchemy")) {
                double alchemyXp = userObject.get("experience_skill_alchemy").getAsDouble();
                alchemyLevel = ProfileViewerUtils.getSkillInfo(alchemyXp, "Alchemy");
            }
            if (userObject.has("experience_skill_carpentry")) {
                double carpentryXp = userObject.get("experience_skill_carpentry").getAsDouble();
                carpentryLevel = ProfileViewerUtils.getSkillInfo(carpentryXp, "Carpentry");
            }
            if (userObject.has("experience_skill_runecrafting")) {
                double runecraftingXp = userObject.get("experience_skill_runecrafting").getAsDouble();
                runecraftingLevel = ProfileViewerUtils.getSkillInfo(runecraftingXp, "Runecrafting");
            }
            if (userObject.has("experience_skill_social2")) {
                double socialXp = userObject.get("experience_skill_social2").getAsDouble();
                socialLevel = ProfileViewerUtils.getSkillInfo(socialXp, "Social");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }

        if(tamingLevel.currentXp==0 && farmingLevel.currentXp==0 && miningLevel.currentXp==0 && combatLevel.currentXp==0 && foragingLevel.currentXp==0 && fishingLevel.currentXp==0) {
            skillApiDisabled = true;
        }
        System.out.println("set skill experience");

        ProfileViewerUtils.setSlayerSkills(userObject);
    }

    UIComponent lastSelectedButton = null;
    UIComponent generalButton = null;
    public void drawSideButton(UIComponent sideButtonContainer, String buttonText, Runnable runnable) {
        UIComponent container = new UIRoundedRectangle(8f)
            .setColor(Color.white)
            .setX(new PixelConstraint(8f))
            .setY(new SiblingConstraint(2.2f))
            .setWidth(new RelativeConstraint(2f))
            .setHeight(new RelativeConstraint(0.10f))
            .setChildOf(sideButtonContainer);

        new UIRoundedRectangle(8f)
            .setColor(new Color(0x191919))
            .setX(new CenterConstraint())
            .setY(new CenterConstraint())
            .setWidth(new RelativeConstraint(0.99f))
            .setHeight(new RelativeConstraint(0.96f))
            .setChildOf(container);

        UIComponent insideTextContainer = new UIRoundedRectangle(8f)
            .setColor(clear)
            .setX(new PixelConstraint(0f))
            .setY(new CenterConstraint())
            .setWidth(new RelativeConstraint(0.45f))
            .setHeight(new RelativeConstraint(1f))
            .setChildOf(container);
        insideTextContainer.onMouseEnterRunnable(()->{
            if(container==lastSelectedButton) return;
            ProfileViewerUtils.animateX(container,4f);
        });
        insideTextContainer.onMouseLeaveRunnable(()->{
            if(container==lastSelectedButton) return;
            ProfileViewerUtils.animateX(container,8f);
        });
        insideTextContainer.onMouseClickConsumer((event)->{
            if(lastSelectedButton==container) return;
            runnable.run();
            if(lastSelectedButton!=null) {
                ProfileViewerUtils.animateX(lastSelectedButton,8f);
            }
            ProfileViewerUtils.animateX(container,0f);
            lastSelectedButton = container;
        });
        if(buttonText.equals("General")) {
            ProfileViewerUtils.animateX(container,0f);
            lastSelectedButton = container;
            generalButton = container;
        }
        new UIText(buttonText)
            .setY(new CenterConstraint())
            .setX(new CenterConstraint())
            .setTextScale(new PixelConstraint(1.5f))
            .setChildOf(insideTextContainer);
    }

    public static String selectedCategory = "General";

    public void loadCategory(String categoryName) {
        categoryName = Utils.cleanColor(categoryName);
        statsAreaContainer.clearChildren();

        selectedCategory = categoryName;

        if(categoryName.equals("Inventories")) {
            if(ProfilePlayerResponse.has("inv_contents")) {
                { // Inventory, armor, equipment, wardrope
                    InventoryBasic inv = new InventoryBasic("Test#1",true, 36);
                    if(ProfilePlayerResponse.has("inv_contents")) {
                        String inventoryBase64 = ProfilePlayerResponse.get("inv_contents").getAsJsonObject().get("data").getAsString();
                        Inventory items = new Inventory(inventoryBase64);
                        List<ItemStack> a = ItemUtils.decodeItem(items,true);

                        int index = 0;
                        for(ItemStack item: a) {
                            inv.setInventorySlotContents(index, item);
                            index++;
                        }
                    }
                    InventoryBasic wardrobePage1 = new InventoryBasic("Test#1",true, 36);
                    InventoryBasic wardrobePage2 = new InventoryBasic("Test#1",true, 36);

                    if(ProfilePlayerResponse.has("wardrobe_contents")) {
                        String inventoryBase64 = ProfilePlayerResponse.get("wardrobe_contents").getAsJsonObject().get("data").getAsString();
                        Inventory items = new Inventory(inventoryBase64);
                        List<ItemStack> a = ItemUtils.decodeItem(items,false);

                        int index = 0;
                        for(ItemStack item: a) {
                            if(item!=null) {
                                if(index>=wardrobePage1.getSizeInventory()) {
                                    wardrobePage2.setInventorySlotContents(index-36, item);
                                } else {
                                    wardrobePage1.setInventorySlotContents(index, item);
                                }
                            };
                            index++;
                        }
                    }

                    UIComponent container =  new UIBlock(clear)
                        .setWidth(new RelativeConstraint(1f))
                        .setY(new SiblingConstraint(10f))
                        .setChildOf(statsAreaContainer)
                        .setHeight(new RelativeConstraint(0.25f));

                    new InventoryComponent(inv,"Inventory")
                        .setChildOf(container)
                        .setX(new PixelConstraint(0f));
                    new InventoryComponent(wardrobePage1,"Wardrobe Page 1")
                        .setX(new SiblingConstraint(10f))
                        .setChildOf(container);
                    new InventoryComponent(wardrobePage2,"Wardrobe Page 2")
                        .setX(new SiblingConstraint(10f))
                        .setChildOf(container);
                }

                { // Talisman Bag, 3 pages
                    InventoryBasic accessoryBag1 = new InventoryBasic("Test#1",true, 45);
                    InventoryBasic accessoryBag2 = new InventoryBasic("Test#1",true, 45);
                    InventoryBasic accessoryBag3 = new InventoryBasic("Test#1",true, 45);

                    for(int i = 0; i < accessoryBag1.getSizeInventory(); i++) {
                        accessoryBag1.setInventorySlotContents(i, null);
                        accessoryBag2.setInventorySlotContents(i, null);
                        accessoryBag3.setInventorySlotContents(i, null);
                    }
                    if(ProfilePlayerResponse.has("talisman_bag")) {
                        String inventoryBase64 = ProfilePlayerResponse.get("talisman_bag").getAsJsonObject().get("data").getAsString();
                        Inventory items = new Inventory(inventoryBase64);
                        List<ItemStack> a = ItemUtils.decodeItem(items,false);

                        int index = 0;
                        for(ItemStack item: a) {

                            if(index>=accessoryBag1.getSizeInventory()) {
                                if(index-45>=accessoryBag2.getSizeInventory()) {
                                    try {
                                        accessoryBag3.setInventorySlotContents(index-45*2, item);
                                    } catch (Exception ignored) {}
                                } else {
                                    accessoryBag2.setInventorySlotContents(index-45, item);
                                }
                            } else {
                                accessoryBag1.setInventorySlotContents(index, item);
                            }
                            index++;
                        }
                    }
                    UIComponent topContainer =  new UIBlock(clear)
                        .setWidth(new RelativeConstraint(1f))
                        .setY(new SiblingConstraint(15f))
                        .setChildOf(statsAreaContainer)
                        .setHeight(new RelativeConstraint(0.15f));

                    new UIText("Accessories", true)
                            .setChildOf(topContainer)
                            .setTextScale(new PixelConstraint((float) (fontScale * 1.3f)))
                            .setX(new CenterConstraint())
                            .setY(new PixelConstraint(0f));

                    String selectedPower = "None";
                    try {
                        selectedPower = ProfilePlayerResponse.get("accessory_bag_storage").getAsJsonObject().get("selected_power").getAsString();
                    } catch (Exception ignored) {}

                    new UIText(ChatFormatting.GRAY+"Selected Power: "+ChatFormatting.GREEN+Utils.convertToTitleCase(selectedPower), true)
                                .setChildOf(topContainer)
                                .setTextScale(new PixelConstraint((float) (fontScale)))
                                .setX(new CenterConstraint())
                                .setY(new SiblingConstraint(2f));

                    int magicPower = 0;
                    try{
                        magicPower = ProfilePlayerResponse.get("accessory_bag_storage").getAsJsonObject().get("highest_magical_power").getAsInt();
                    } catch (Exception ignored) {}

                    new UIText(ChatFormatting.GRAY+"Magic Power: "+ChatFormatting.GOLD+magicPower, true)
                        .setChildOf(topContainer)
                        .setTextScale(new PixelConstraint((float) (fontScale)))
                        .setX(new CenterConstraint())
                        .setY(new SiblingConstraint(2f));

                    UIComponent container =  new UIBlock(clear)
                        .setWidth(new RelativeConstraint(1f))
                        .setY(new SiblingConstraint(0f))
                        .setChildOf(statsAreaContainer)
                        .setHeight(new RelativeConstraint(0.33f));

                    new InventoryComponent(accessoryBag1,"Accessory Bag Page 1")
                        .setChildOf(container)
                        .setX(new PixelConstraint(0f));

                    new InventoryComponent(accessoryBag2,"Accessory Bag Page 2")
                        .setChildOf(container)
                        .setX(new SiblingConstraint(10f));

                    new InventoryComponent(accessoryBag3,"Accessory Bag Page 3")
                        .setChildOf(container)
                        .setX(new SiblingConstraint(10f));
                }

                { // Enderchest, 3 pages
                    if (ProfilePlayerResponse.has("ender_chest_contents")) {
                        String inventoryBase64 = ProfilePlayerResponse.get("ender_chest_contents").getAsJsonObject().get("data").getAsString();
                        Inventory items = new Inventory(inventoryBase64);
                        List<ItemStack> a = ItemUtils.decodeItem(items, false);
                        int numPages = (int) Math.ceil(a.size() / 45.0);

                        int pageIndex = 0;
                        UIComponent container = null;
                        while (pageIndex < numPages) {
                            InventoryBasic enderchestPage = new InventoryBasic("Test #" + (pageIndex + 1), true, 45);
                            for (int i = 0; i < enderchestPage.getSizeInventory(); i++) {
                                enderchestPage.setInventorySlotContents(i, null);
                            }
                            int startIndex = pageIndex * 45;
                            int endIndex = Math.min((pageIndex + 1) * 45, a.size());
                            for (int i = startIndex; i < endIndex; i++) {
                                enderchestPage.setInventorySlotContents(i - startIndex, a.get(i));
                            }

                            if (pageIndex % 3 == 0) {
                                container = new UIBlock(clear)
                                        .setWidth(new RelativeConstraint(1f))
                                        .setX(new PixelConstraint(0f))
                                        .setY(new SiblingConstraint(5f))
                                        .setChildOf(statsAreaContainer)
                                        .setHeight(new RelativeConstraint(0.33f));
                            }

                            new InventoryComponent(enderchestPage, "Enderchest Page " + (pageIndex + 1))
                                    .setChildOf(container)
                                    .setX(new SiblingConstraint(10f));

                            pageIndex++;
                        }
                    }
                }
                { // Backpacks
                    if (ProfilePlayerResponse.has("backpack_contents")) {
                        JsonObject backpacks = ProfilePlayerResponse.get("backpack_contents").getAsJsonObject();

                        UIComponent backpacksContainer = new UIBlock(clear)
                            .setWidth(new RelativeConstraint(1f))
                            .setX(new PixelConstraint(0f))
                            .setY(new SiblingConstraint(5f))
                            .setChildOf(statsAreaContainer)
                            .setHeight(new RelativeConstraint(0.33f));

                        int currentPage = 1;
                        UIComponent currentContainer = backpacksContainer;
                        int backpackCount = 1;
                        for (Entry<String, JsonElement> a : backpacks.entrySet()) {
                            JsonObject backpack = a.getValue().getAsJsonObject();
                            String inventoryBase64 = backpack.get("data").getAsString();
                            Inventory items = new Inventory(inventoryBase64);
                            List<ItemStack> b = ItemUtils.decodeItem(items, false);
                            InventoryBasic backpackInv = new InventoryBasic("Backpack: " + a.getKey(), true, b.size());

                            for (int i = 0; i < b.size(); i++) {
                                backpackInv.setInventorySlotContents(i, b.get(i));
                            }

                            UIComponent backpackComponent = new InventoryComponent(backpackInv, "Backpack #" + backpackCount)
                                .setChildOf(currentContainer)
                                .setX(new SiblingConstraint(10f));
                            backpackCount++;
                            float height = (Math.round(b.size()/9f)+27f)/100;
                            if (currentPage % 3 == 0) {
                                currentContainer = new UIBlock(clear)
                                    .setWidth(new RelativeConstraint(1f))
                                    .setX(new PixelConstraint(0f))
                                    .setY(new SiblingConstraint(5f))
                                    .setChildOf(statsAreaContainer)
                                    .setHeight(new RelativeConstraint(height));
                            }
                            currentPage++;
                        }
                        if(currentContainer.getChildren().isEmpty()) {
//                            TEST
                            currentContainer.hide();
                        }

                    }
                }
            } else {
                new UIText(ChatFormatting.RED+"Player Has Inventory API Disabled")
                    .setTextScale(new PixelConstraint(2f))
                    .setChildOf(statsAreaContainer)
                    .setX(new CenterConstraint())
                    .setY(new CenterConstraint());
            }
        }

        if(categoryName.equals("General")) {
            loadProfile(ProfileResponse.get("cute_name").getAsString(),false);
        }

        if(categoryName.equals("Dungeons")) {
             UIComponent loadingText = new UIText(ChatFormatting.RED+"Waiting for SkyCrypt..")
                    .setTextScale(new PixelConstraint(2f))
                    .setChildOf(statsAreaContainer)
                    .setX(new CenterConstraint())
                    .setY(new CenterConstraint());
            try {
                String id = ProfileResponse.get("profile_id").toString().replaceAll("\"", "");
                JsonObject obj = profiles.get(id).getAsJsonObject();
                UIComponent statsAreaLeft = new UIBlock(clear).setX(new PixelConstraint(0f)).setY(new RelativeConstraint(0f)).setChildOf(statsAreaContainer).setWidth(new RelativeConstraint(0.95f*0.50f)).setHeight(new RelativeConstraint(0.4f));
                UIComponent statsAreaRight = new UIBlock(clear).setX(new RelativeConstraint(0.50f)).setY(new RelativeConstraint(0f)).setChildOf(statsAreaContainer).setWidth(new RelativeConstraint(0.95f*0.5f)).setHeight(new RelativeConstraint((0.4f)));

                JsonObject dungeons = obj.get("data").getAsJsonObject().get("dungeons").getAsJsonObject();
                JsonObject essences = obj.get("data").getAsJsonObject().get("essence").getAsJsonObject();
                JsonObject catacombs = dungeons.get("catacombs").getAsJsonObject();
                JsonObject mastermode = dungeons.get("master_catacombs").getAsJsonObject();

                Integer secrets = dungeons.get("secrets_found").getAsInt();
                String selectedClass = ProfileViewerUtils.formatTitle(dungeons.get("selected_class").getAsString());
                double classAverage = Math.floor(dungeons.get("class_average").getAsJsonObject().get("avrg_level").getAsDouble()*10)/10;;
                int itemBoost = catacombs.get("bonuses").getAsJsonObject().get("item_boost").getAsInt();
                String highestFloorNormal = ProfileViewerUtils.formatTitle(catacombs.get("highest_floor").getAsString());
                String highestFloorMaster = ProfileViewerUtils.formatTitle(mastermode.get("highest_floor").getAsString());

                JsonObject classes = dungeons.get("classes").getAsJsonObject();
                JsonObject archerClass = classes.get("archer").getAsJsonObject().get("experience").getAsJsonObject();
                JsonObject healerClass = classes.get("healer").getAsJsonObject().get("experience").getAsJsonObject();
                JsonObject mageClass = classes.get("mage").getAsJsonObject().get("experience").getAsJsonObject();
                JsonObject berserkClass = classes.get("berserk").getAsJsonObject().get("experience").getAsJsonObject();
                JsonObject tankClass = classes.get("tank").getAsJsonObject().get("experience").getAsJsonObject();
                JsonObject catacombsLevel = catacombs.get("level").getAsJsonObject();

                int nextCataXp = 0;
                int nextBersXp = 0;
                int nextArchXp = 0;
                int nextHealXp = 0;
                int nextMageXp = 0;
                int nextTankXp = 0;
                try {nextCataXp = catacombsLevel.get("xpForNext").getAsInt();} catch (Exception ignored) {}
                try {nextBersXp = berserkClass.get("xpForNext").getAsInt();} catch (Exception ignored) {}
                try {nextArchXp = archerClass.get("xpForNext").getAsInt();} catch (Exception ignored) {}
                try {nextHealXp = healerClass.get("xpForNext").getAsInt();} catch (Exception ignored) {}
                try {nextMageXp = mageClass.get("xpForNext").getAsInt();} catch (Exception ignored) {}
                try {nextTankXp = tankClass.get("xpForNext").getAsInt();} catch (Exception ignored) {}
                statsAreaContainer.removeChild(loadingText);
                drawProgressbar(catacombsLevel.get("xpCurrent").getAsInt(),nextCataXp,statsAreaLeft,"Catacombs "+catacombsLevel.get("level").getAsInt(),new ItemStack(Items.skull),null,false);
                drawProgressbar(archerClass.get("xpCurrent").getAsInt(),nextArchXp,statsAreaLeft,"Archer "+archerClass.get("level").getAsInt(),new ItemStack(Items.bow),null,false);
                drawProgressbar(healerClass.get("xpCurrent").getAsInt(),nextHealXp,statsAreaLeft,"Healer "+healerClass.get("level").getAsInt(),new ItemStack(Items.potionitem),null,false);
                drawProgressbar(mageClass.get("xpCurrent").getAsInt(),nextMageXp,statsAreaRight,"Mage "+mageClass.get("level").getAsInt(),new ItemStack(Items.blaze_rod),null,false);
                drawProgressbar(berserkClass.get("xpCurrent").getAsInt(),nextBersXp,statsAreaRight,"Berserk "+berserkClass.get("level").getAsInt(),new ItemStack(Items.iron_sword),null,false);
                drawProgressbar(tankClass.get("xpCurrent").getAsInt(),nextTankXp,statsAreaRight,"Tank "+tankClass.get("level").getAsInt(),new ItemStack(Items.leather_chestplate),null,false);

                UIComponent container =  new UIBlock(clear)
                    .setWidth(new RelativeConstraint(1f))
                    .setY(new RelativeConstraint(0.26f))
                    .setChildOf(statsAreaContainer)
                    .setHeight(new RelativeConstraint(0.3f));

                UIComponent left = new UIBlock(clear)
                    .setWidth(new RelativeConstraint(0.47f))
                    .setChildOf(container)
                    .setHeight(new RelativeConstraint(0.3f));
                new UIText(g+"Selected Class: "+bold+selectedClass).setX(new SiblingConstraint(3f)).setChildOf(left);
                new UIText(g+"Class Average: "+bold+classAverage).setY(new SiblingConstraint(3f)).setChildOf(left);
                new UIText(g+"Dungeon Item Boost: "+bold+itemBoost+"%").setY(new SiblingConstraint(3f)).setChildOf(left);
                new UIText(g+"Highest Floor Beated (Normal): "+bold+highestFloorNormal).setY(new SiblingConstraint(3f)).setChildOf(left);
                new UIText(g+"Highest Floor Beated (Master): "+bold+highestFloorMaster).setY(new SiblingConstraint(3f)).setChildOf(left);
                new UIText(g+"Secrets Found: "+bold+nf.format(secrets)).setY(new SiblingConstraint(3f)).setChildOf(left);

                UIComponent right =  new UIBlock(clear)
                    .setWidth(new RelativeConstraint(0.47f))
                    .setX(new SiblingConstraint(10f))
                    .setChildOf(container)
                    .setHeight(new RelativeConstraint(0.3f));

                new UIText(g+"Wither Essence: "+bold+nf.format(essences.get("wither").getAsInt())).setX(new SiblingConstraint(3f)).setChildOf(right);
                new UIText(g+ChatFormatting.GREEN+"Spider Essence: "+bold+nf.format(essences.get("spider").getAsInt())).setY(new SiblingConstraint(3f)).setChildOf(right);
                new UIText(g+ChatFormatting.AQUA+"Ice Essence: "+bold+nf.format(essences.get("ice").getAsInt())).setY(new SiblingConstraint(3f)).setChildOf(right);
                new UIText(g+ChatFormatting.RED+"Dragon Essence: "+bold+nf.format(essences.get("dragon").getAsInt())).setY(new SiblingConstraint(3f)).setChildOf(right);
                new UIText(g+ChatFormatting.YELLOW+"Undead Essence: "+bold+nf.format(essences.get("undead").getAsInt())).setY(new SiblingConstraint(3f)).setChildOf(right);
                new UIText(g+ChatFormatting.DARK_AQUA+"Diamond Essence: "+bold+nf.format(essences.get("diamond").getAsInt())).setY(new SiblingConstraint(3f)).setChildOf(right);
                new UIText(g+ChatFormatting.GOLD+"Gold Essence: "+bold+nf.format(essences.get("gold").getAsInt())).setY(new SiblingConstraint(3f)).setChildOf(right);

                totalDungeonRuns = 1;

                addDungeonFloors(catacombs);
                addDungeonFloors(mastermode);

                Double averageSecrets = Math.round((secrets.doubleValue()/totalDungeonRuns.doubleValue())*100.0)/100.0;
                UIComponent avgSecretComponent = new UIText(g+"Average Secrets Per Run: "+bold+nf.format(averageSecrets)).setY(new SiblingConstraint(3f)).setChildOf(left);

                dungeonHoverables.put(avgSecretComponent, new ArrayList<>(Arrays.asList(ChatFormatting.RED + "This is an estimate because",ChatFormatting.RED+"the secret count is combined",ChatFormatting.RED+"across profiles")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(categoryName.equals("Skills")) {
            UIComponent miningContainer = new UIBlock(clear).setWidth(new RelativeConstraint(1f)).setChildOf(statsAreaContainer).setHeight(new RelativeConstraint(0.6f));
            UIComponent farmingFishingContainer = new UIBlock(clear).setWidth(new RelativeConstraint(1f)).setY(new SiblingConstraint(10f)).setChildOf(statsAreaContainer).setHeight(new RelativeConstraint(0.3f));
            UIComponent farmingContainer = new UIBlock(clear).setWidth(new RelativeConstraint(0.45f)).setY(new PixelConstraint(0f)).setChildOf(farmingFishingContainer).setHeight(new RelativeConstraint(1f));
            UIComponent fishingContainer = new UIBlock(clear).setWidth(new RelativeConstraint(0.45f)).setY(new PixelConstraint(0f)).setX(new SiblingConstraint(10f)).setChildOf(farmingFishingContainer).setHeight(new RelativeConstraint(1f));

            {// Mining
                new UIText(ChatFormatting.YELLOW+""+ChatFormatting.BOLD+"Mining").setChildOf(miningContainer).setY(new SiblingConstraint(4f)).setX(new CenterConstraint()).setTextScale(new PixelConstraint(2f));

                JsonObject miningCore = ProfilePlayerResponse.get("mining_core").getAsJsonObject();
                int hotmExperience = 0;
                int mithrilPowder = 0;
                int gemstonePowder = 0;

                if(miningCore.has("experience")) hotmExperience = miningCore.get("experience").getAsInt();
                if(miningCore.has("powder_mithril")) mithrilPowder = miningCore.get("powder_mithril").getAsInt();
                if(miningCore.has("powder_gemstone")) gemstonePowder = miningCore.get("powder_gemstone").getAsInt();

                // JsonObject skyblockAchievements = achievmentsJson.get("skyblock").getAsJsonObject().get("tiered").getAsJsonObject()
                JsonArray tutorial = ProfilePlayerResponse.get("tutorial").getAsJsonArray();
                int commisionsMilestone = 0;

                for(JsonElement element:tutorial) {
                    if(element.getAsString().contains("commission_milestone_reward_mining_xp_tier_")) {
                        commisionsMilestone = Integer.parseInt(element.getAsString().substring(43, 44));
                    }
                };

                String passStatus = ChatFormatting.RED+"Expired";
                if(miningCore.has("greater_mines_last_access")) {
                    passStatus = miningCore.get("greater_mines_last_access").getAsInt()>System.currentTimeMillis()-5*60*60*1000?ChatFormatting.GREEN+"Active":ChatFormatting.RED+"Expired";
                }
                int nucleusRuns = Optional.ofNullable(ProfilePlayerResponse.get("leveling")).map(JsonElement::getAsJsonObject).map(obj -> obj.get("completions")).map(JsonElement::getAsJsonObject).map(obj -> obj.get("NUCLEUS_RUNS")).map(JsonElement::getAsInt).orElse(0);

                UIComponent left = new UIBlock(clear)
                    .setWidth(new RelativeConstraint(0.45f))
                    .setY(new SiblingConstraint(4f))
                    .setChildOf(miningContainer)
                    .setHeight(new RelativeConstraint(1f));

                new UIText(ChatFormatting.YELLOW+""+ChatFormatting.BOLD+"Dwarven Mines and Crystal Hollows").setChildOf(left).setY(new SiblingConstraint(4f)).setX(new CenterConstraint());
                new UIText(g+"Commission Milestone: "+bold+commisionsMilestone).setY(new SiblingConstraint(2f)).setChildOf(left);
                new UIText(g+"Crystal Hollows Pass: "+bold+passStatus).setY(new SiblingConstraint(2f)).setChildOf(left);
                new UIText(g+"Crystal Nucleus: "+bold+"Completed "+nucleusRuns+" times").setY(new SiblingConstraint(2f)).setChildOf(left);

                new UIText(ChatFormatting.YELLOW+""+ChatFormatting.BOLD+"Heart of the Mountain").setChildOf(left).setY(new SiblingConstraint(10f)).setX(new CenterConstraint());
                int hotmTier = 0;
                int tokensSpent = 0;
                try {tokensSpent = miningCore.get("tokens_spent").getAsInt();} catch (Exception ignored) {}
                try {hotmTier = ProfileViewerUtils.hotmXpToLevel(hotmExperience);} catch (Exception ignored) {}

                new UIText(g+"Tier: "+bold+hotmTier+"/7").setY(new SiblingConstraint(2f)).setChildOf(left);
                new UIText(g+"Token Of The Mountain: "+bold+(tokensSpent)+"/17").setY(new SiblingConstraint(2f)).setChildOf(left);
                new UIText(g+"Peak Of The Mountain: "+bold+potm+"/7").setY(new SiblingConstraint(2f)).setChildOf(left);
                new UIText(g+"Mithril Powder: "+ChatFormatting.DARK_GREEN+ChatFormatting.BOLD+nf.format(mithrilPowder)).setY(new SiblingConstraint(2f)).setChildOf(left);
                new UIText(g+"Gemstone Powder: "+ChatFormatting.LIGHT_PURPLE+ChatFormatting.BOLD+nf.format(gemstonePowder)).setY(new SiblingConstraint(2f)).setChildOf(left);

                String pickaxeAbility = "None";
                try {pickaxeAbility = Utils.convertToTitleCase(miningCore.get("selected_pickaxe_ability").getAsString());} catch (Exception e) {}
                new UIText(g+"Pickaxe Ability: "+bold+pickaxeAbility).setY(new SiblingConstraint(2f)).setChildOf(left);
                drawHotmGrid(miningContainer);
            }
            {// Farming
                new UIText(ChatFormatting.YELLOW+""+ChatFormatting.BOLD+"Farming").setChildOf(farmingContainer).setY(new SiblingConstraint(4f)).setX(new CenterConstraint()).setTextScale(new PixelConstraint(2f));
                int pelts = 0;
                if(ProfilePlayerResponse.has("trapper_quest")) if(ProfilePlayerResponse.get("trapper_quest").getAsJsonObject().has("pelt_count")) pelts = ProfilePlayerResponse.get("trapper_quest").getAsJsonObject().get("pelt_count").getAsInt();
                JsonObject contests = ProfilePlayerResponse.get("jacob2").getAsJsonObject().get("contests").getAsJsonObject();
                JsonObject medals = ProfilePlayerResponse.get("jacob2").getAsJsonObject().get("medals_inv").getAsJsonObject();
                int contestsAttended = contests.entrySet().size();

                int gold = 0;
                int silver = 0;
                int bronze = 0;
                if(medals.has("gold")) gold = medals.get("gold").getAsInt();
                if(medals.has("silver")) silver = medals.get("silver").getAsInt();
                if(medals.has("bronze")) bronze = medals.get("bronze").getAsInt();

                new UIText(g+"Pelts: "+bold+pelts).setY(new SiblingConstraint(2f)).setChildOf(farmingContainer);
                new UIText(g+"Contests Attended: "+bold+contestsAttended).setY(new SiblingConstraint(2f)).setChildOf(farmingContainer);

                if(contestsAttended>0) {
                    new UIText("§6§lGold§r"+g+" Medals: "+bold+"§6"+gold).setY(new SiblingConstraint(12f)).setChildOf(farmingContainer);
                    new UIText("§r§lSilver§r"+g+" Medals: "+bold+"§r"+silver).setY(new SiblingConstraint(2f)).setChildOf(farmingContainer);
                    new UIText("§c§lBronze§r"+g+" Medals: "+bold+"§c"+bronze).setY(new SiblingConstraint(2f)).setChildOf(farmingContainer);
                } else {
                    new UIText("§6§lGold§r"+g+" Medals: "+bold+"§60").setY(new SiblingConstraint(12f)).setChildOf(farmingContainer);
                    new UIText("§r§lSilver§r"+g+" Medals: "+bold+"§r0").setY(new SiblingConstraint(2f)).setChildOf(farmingContainer);
                    new UIText("§c§lBronze§r"+g+" Medals: "+bold+"§c0").setY(new SiblingConstraint(2f)).setChildOf(farmingContainer);
                }
            }
            {// Fishing
                new UIText(ChatFormatting.YELLOW+""+ChatFormatting.BOLD+"Fishing").setChildOf(fishingContainer).setY(new SiblingConstraint(4f)).setX(new CenterConstraint()).setTextScale(new PixelConstraint(2f));
                int caught = 0;
                int treasure = 0;
                if(ProfilePlayerResponse.get("trophy_fish").getAsJsonObject().has("total_caught")) {
                    caught = ProfilePlayerResponse.get("trophy_fish").getAsJsonObject().get("total_caught").getAsInt();
                }
                if(ProfilePlayerResponse.has("fishing_treasure_caught")) {
                    treasure = ProfilePlayerResponse.get("fishing_treasure_caught").getAsInt();
                }
                new UIText(g+"Trophy Fish Caught: "+bold+Utils.nf.format(caught)).setY(new SiblingConstraint(2f)).setChildOf(fishingContainer);
                new UIText(g+"Treasures Found: "+bold+Utils.nf.format(treasure)).setY(new SiblingConstraint(2f)).setChildOf(fishingContainer);
            }
        }

        if(categoryName.equals("Pets")) {
            UIComponent loadingText = new UIText(ChatFormatting.RED+"Waiting for SkyCrypt..")
                    .setTextScale(new PixelConstraint(2f))
                    .setChildOf(statsAreaContainer)
                    .setX(new CenterConstraint())
                    .setY(new CenterConstraint());

            new Thread(()->{
                // Wait for skycrypt before displaying the page
                while (profiles.entrySet().isEmpty()) {
                    try {
                        Thread.sleep(100); // Add a delay of 100 milliseconds
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                statsAreaContainer.removeChild(loadingText);
                String id = ProfileResponse.get("profile_id").toString().replaceAll("\"", "");
                JsonObject obj = profiles.get(id).getAsJsonObject();
                JsonObject raw = obj.get("raw").getAsJsonObject();
                JsonArray pets = raw.get("pets").getAsJsonArray();

                float index = -1;

                UIComponent otherPetsContainer = new UIBlock(clear).setY(new SiblingConstraint(10f)).setHeight(new ChildBasedSizeConstraint()).setWidth(new RelativeConstraint(1f)).setChildOf(statsAreaContainer);
                new UIText(bold+"Active Pet").setChildOf(otherPetsContainer).setY(new PixelConstraint(2f)).setX(new PixelConstraint(1f)).setTextScale(new PixelConstraint((float) (1f*fontScale)));
                List<JsonObject> sortedPets = getJsonObjects(pets);
                for(JsonObject pet:sortedPets) {
                    if(pet==null)continue;
                    String texturePath = pet.get("texture_path").getAsString();
                    CompletableFuture<BufferedImage> imageFuture = new CompletableFuture<>();
                    List<String> tooltip = parseLore(pet.get("lore").getAsString());

                    // Download the image from the remote URL using CompletableFuture and URLConnection
                    CompletableFuture.runAsync(() -> {
                        try {
                            URL imageUrl = new URL("https://sky.shiiyu.moe"+texturePath);
                            HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                            BufferedImage image = ImageIO.read(connection.getInputStream());

                            imageFuture.complete(image);
                        } catch (Exception e) {
                            e.printStackTrace();
                            imageFuture.completeExceptionally(e);
                        }
                    });

                    String name = pet.get("display_name").getAsString();
                    int lvl = pet.get("level").getAsJsonObject().get("level").getAsInt();
                    String coloredName = "AA";
                    String tier = pet.get("tier").getAsString();
                    try {coloredName = ChatFormatting.GRAY+"[Lvl "+lvl+"] "+ItemRarity.getRarityFromName(tier).getBaseColor()+name;} catch (Exception e) {}
                    tooltip.add(0, coloredName);

                    if(index==-1) {
                        index++;
                        UIComponent petComponent = ProfileViewerUtils.createPet(imageFuture,lvl,coloredName,tooltip,ProfileViewerUtils.getPetColor(tier))
                                                        .setChildOf(otherPetsContainer)
                                                        .setX(new PixelConstraint(0f))
                                                        .setY(new SiblingConstraint(3f));

                        new UIText(bold+"Other pets").setChildOf(otherPetsContainer).setY(new SiblingConstraint(13f)).setX(new PixelConstraint(1f)).setTextScale(new PixelConstraint((float) (1f*fontScale)));
                        petHoverables.put(petComponent, tooltip);
                        continue;
                    };
                    float x = (float) ((index-(Math.floor(index/16f)*16))*30f);
                    float y = (float) (Math.floor(index/16f)*35f)+63;
                    UIComponent petComponent = ProfileViewerUtils.createPet(imageFuture,lvl,coloredName,tooltip,ProfileViewerUtils.getPetColor(tier)).setChildOf(otherPetsContainer).setX(new PixelConstraint(x)).setY(new PixelConstraint(y));

                    petHoverables.put(petComponent, tooltip);
                    otherPetsContainer.setHeight(new PixelConstraint(45f*(Math.round(petHoverables.size()/16)+1)));
                    index++;
                }
            }).start();;
        }

        if(categoryName.equals("Collections")) {
            setCollectionsScreen();
        }

        if(categoryName.equals("Crimson")) {

        }

        if(categoryName.equals("Misc Stats")) {

        }
    }

    @NotNull
    private static List<JsonObject> getJsonObjects(JsonArray pets) {
        List<JsonObject> sortedPets = new ArrayList<>();
        JsonObject activePet = null;
        for(JsonElement element: pets) {
            JsonObject pet = element.getAsJsonObject();
            boolean isActive = pet.get("active").getAsBoolean();
            if(!isActive) sortedPets.add(pet);
            else {
                activePet = pet;
            }
        }
        sortedPets.sort((a,b)->{
            String aRarity = a.get("tier").getAsString();
            String bRarity = b.get("tier").getAsString();

            return APIUtils.getPetRarity(bRarity)-APIUtils.getPetRarity(aRarity);
        });
        sortedPets.add(0, activePet);
        return sortedPets;
    }

    private static JsonObject collectionsData = null;
    private static HashMap<String, JsonObject> categoryDataCache = new HashMap<>();
    private static UIComponent statsAreaContainerNew;

    public void loadCollectionsCategories() {
        System.out.println("Loading collections");

        if (collectionsData == null) {
            // Fetch the collections data only if it's not already cached
            collectionsData = APIUtils.getJSONResponse("https://api.hypixel.net/resources/skyblock/collections#CollectionsForPV").getAsJsonObject();
        }

        statsAreaContainerNew = new UIBlock(Color.red)
            .setWidth(new RelativeConstraint(0.75f))
            .setHeight(new RelativeConstraint(0.75f));

        new Thread(() -> {
            String[] categories = {"Farming", "Mining", "Combat", "Foraging", "Fishing", "Rift"};
            int totalHeight = 0;

            for (String category : categories) {
                if (!collectionsData.get("success").getAsBoolean()) {
                    System.out.println("Error: ");
                    return;
                }

                JsonObject collections = collectionsData.get("collections").getAsJsonObject();

                JsonObject categoryObject = categoryDataCache.computeIfAbsent(category, key -> collections.get(key.toUpperCase()).getAsJsonObject());

                UIComponent categoryComponent = new UIBlock(clear)
                        .setChildOf(statsAreaContainerNew)
                        .setY(new SiblingConstraint(10f))
                        .setHeight(new PixelConstraint(20f))
                        .setWidth(new RelativeConstraint(1f));

                new UIText(category, true)
                        .setChildOf(categoryComponent)
                        .setTextScale(new PixelConstraint((float) (fontScale * 1.3f)))
                        .setX(new PixelConstraint(0f))
                        .setY(new PixelConstraint(0f));

                UIComponent container = new UIBlock(clear)
                        .setChildOf(categoryComponent)
                        .setY(new SiblingConstraint(3f))
                        .setHeight(new RelativeConstraint(1f))
                        .setWidth(new RelativeConstraint(1f));

                int categoryHeight = loadCollectionsCategory(categoryObject, container);
                categoryComponent.setHeight(new PixelConstraint(categoryHeight + 23));
                totalHeight += categoryHeight + 23; // Add 23 to account for the height of the category header
            }
            
            statsAreaContainerNew.setHeight(new PixelConstraint(totalHeight)); // Set the parent component's height

            System.out.println("LOADED COLLECTIONS");
        }).start();
    }

    public void setCollectionsScreen() {
        statsAreaContainer.clearChildren();
        statsAreaContainer.addChild(new UIText("§cLoading..", true).setY(new CenterConstraint()).setX(new CenterConstraint()));
        
        new Thread(()->{
            while(statsAreaContainerNew.getChildren().size()<7) {
                if(!selectedCategory.equals("Collections")) break;
                if(statsAreaContainerNew.getChildren().size()==6) {
                    statsAreaContainer.clearChildren();
                    statsAreaContainerNew.getChildren().forEach((e)->{
                        statsAreaContainer.addChild(e);
                    });
                    break;
                }
            }
        }).start();
    }

    HashMap<String,String> coopNames = new HashMap<>();
    public static class CoopCollector {
        long total;
        String username;
        
        public CoopCollector(String u,Long t) {
            total = t;
            username = u;
        }

        public long getTotal() {
            return total;
        }
    }
    public int loadCollectionsCategory(JsonObject category, UIComponent component) {
        JsonObject items = category.get("items").getAsJsonObject();

        int maxItemsPerRow = 11;
        int numRows = (int) Math.ceil((double) items.entrySet().size() / maxItemsPerRow);
        int itemSpacing = 5;
        int totalHeight = numRows * (20 + itemSpacing) - itemSpacing; // Calculate total height for all rows

        int xStart = 0;
        int yStart = 0; // Initial y-position
        int rowWidth = 0; // Keep track of the current row's width
        int index = 0;
        int row = 0;

        for (Entry<String, JsonElement> item : items.entrySet()) {
            if (index >= maxItemsPerRow) {
                // Move to the next row
                rowWidth = 0; // Reset the current row's width
                index = 0;
                row++;
            }

            String itemId = item.getKey().replaceAll(":", "-");
            if (itemId.equals("MUSHROOM_COLLECTION")) itemId = "BROWN_MUSHROOM";
            
            ItemStack stack = ItemUtils.getSkyblockItem(itemId);

            List<String> lore = new ArrayList<>();
            List<CoopCollector> collectors = new ArrayList<>();
            JsonObject members = ProfileResponse.get("members").getAsJsonObject();
            long total = 0L;

            for(Entry<String, JsonElement> member:members.entrySet()) {
                if(!coopNames.containsKey(member.getKey())) {
                    String formattedName = APIUtils.getHypixelRank(member.getKey());
                    coopNames.put(member.getKey(), formattedName);
                }


                long value = 0L;
                try {
                    value = member.getValue().getAsJsonObject().get("collection").getAsJsonObject().get(item.getKey()).getAsLong();
                } catch (Exception e) {
                    // TODO: handle exception
                }
                total+=value;
                collectors.add(new CoopCollector(coopNames.get(member.getKey()), value));
            }
            // Create a list of CoopCollector objects
            // Sort the list based on the 'total' field in descending order
            collectors = collectors.stream()
                    .sorted(Comparator.comparingLong(CoopCollector::getTotal).reversed())
                    .collect(Collectors.toList());

            CollectionTier rank = getCollectionTier(item.getValue().getAsJsonObject(),total);
            String itemName = Utils.cleanColor(stack.getDisplayName());

            lore.add("§7Total Collected: §e"+Utils.nf.format(total));
            
            if(!rank.maxed) {
                lore.add("");
                lore.add("§7Progress to "+itemName+" "+(rank.tier+1)+": §e"+(Math.floor((double) total /(rank.untilNext+total)*1000)/10)+"§6%");
                lore.add(stringProgressBar(total,(int) (rank.untilNext+total)));
            }

            lore.add("");
            lore.add("§7Contributions:");

            for (CoopCollector collector : collectors) {
                lore.add(collector.username+"§7: §e"+Utils.nf.format(collector.total)+Utils.percentOf(collector.total, total));
            }

            // Calculate the position for the current item
            int xPos = xStart + rowWidth;
            int yPos = yStart + row * (20 + itemSpacing);
            Color color = new Color(100, 100, 100, 200);
            if(rank.maxed) color = new Color(218,165,32,200);

            int itemWidth = 20;
            UIComponent backgroundSlot = new UIRoundedRectangle(3f)
                    .setChildOf(component)
                    .setHeight(new PixelConstraint(20f))
                    .setWidth(new PixelConstraint(itemWidth))
                    .setX(new PixelConstraint(xPos))
                    .setY(new PixelConstraint(yPos))
                    .setColor(color);
            
            stack = ItemUtils.updateLore(stack, lore);

            stack.setStackDisplayName("§e"+itemName+" "+rank.tier);
            
            UIComponent itemStackComponent = new ItemStackComponent(stack)
                    .setHeight(new PixelConstraint(20f))
                    .setWidth(new PixelConstraint(itemWidth))
                    .setX(new CenterConstraint())
                    .setY(new CenterConstraint());
                
            backgroundSlot.addChild(itemStackComponent);

            // Update the current row's width and index for the next item
            rowWidth += itemWidth + itemSpacing;
            index++;
        }

        return totalHeight;
    }

    public String stringProgressBar(long total2,Integer total) {
        Double percent = (double) (total2/total);
        String progessed = "§2§l§m §2§l§m ";
        String unprogessed = "§f§l§m §f§l§m ";
        int times = (int) (percent*20);
        String out = "";
        for(int i=0;i<20;i++) {
            if(i<times) out+=progessed;
            else {
                out+=unprogessed;
            }
        }
        return out+"§r §e"+Utils.nf.format(total2)+"§6/§e"+Utils.formatNumber(total);
    }

    public class CollectionTier {
        boolean maxed;
        int tier;
        int untilNext;
        Double progress;
        CollectionTier(boolean maxed,int tier,int untilNext,Double progress) {
            this.maxed=maxed;
            this.tier=tier;
            this.untilNext=untilNext;
            this.progress=progress;
        }
    }
    public CollectionTier getCollectionTier(JsonObject itemObj, long total) {
        JsonArray tiers = itemObj.get("tiers").getAsJsonArray();
        int maxTiers = itemObj.get("maxTiers").getAsInt();
        boolean maxed = false;
        int tier = -1;
        int untilNext = -1;
        double progress = 0.0;

        for (int i = 0; i < tiers.size(); i++) {
            JsonObject tierObj = tiers.get(i).getAsJsonObject();
            int amountRequired = tierObj.get("amountRequired").getAsInt();

            if (total >= amountRequired) {
                if (i == maxTiers-1) {
                    // Maxed out the last tier
                    maxed = true;
                    tier = i + 1;
                    untilNext = -1;
                    progress = 1.0;
                } else {
                    // Reached a tier, but not maxed
                    maxed = false;
                    tier = i + 1;
                    untilNext = (int) (tiers.get(i + 1).getAsJsonObject().get("amountRequired").getAsInt() - total);
                    progress = (double) (total - amountRequired) / (double) (untilNext);
                }
            } else {
                // Not yet reached this tier
                tier = i;
                untilNext = (int) (amountRequired - total);
                progress = (double) total / (double) (amountRequired);
                break;
            }
        }

        return new CollectionTier(maxed, tier, untilNext, progress);
    }


    public static List<String> parseLore(String lore) {
        List<String> loreList = new ArrayList<>();
        String[] loreRows = lore.split("<span class=\"lore-row wrap\">");
    
        for (String loreRow : loreRows) {
            String cleanedLoreRow = ProfileViewerUtils.cleanLoreRow(loreRow);
            if(cleanedLoreRow.contains("-----") || cleanedLoreRow.contains("MAX LEVEL")) {
                loreList.add(cleanedLoreRow);
                break;
            } else
            if (!cleanedLoreRow.isEmpty()) {
                String[] splitLore = splitLongLoreRow(cleanedLoreRow);
                loreList.addAll(Arrays.asList(splitLore));
            }
        }
        
        return loreList;
    }
    
    public static String[] splitLongLoreRow(String loreRow) {
        if (loreRow.length() <= 34) {
            return new String[]{loreRow};
        } else {
            List<String> splitLoreList = new ArrayList<>();
            int index = 0;
            String formattingColor = "";
            while (index < loreRow.length()) {
                int endIndex = Math.min(index + 34, loreRow.length());
                if (endIndex < loreRow.length() && !Character.isWhitespace(loreRow.charAt(endIndex))) {
                    while (endIndex > index && !Character.isWhitespace(loreRow.charAt(endIndex))) {
                        endIndex--;
                    }
                }
                String splitLore = loreRow.substring(index, endIndex);
                if (formattingColor.isEmpty() && splitLore.startsWith(" ")) {
                    splitLore = "§7" + splitLore.trim();
                } else if (!formattingColor.isEmpty()) {
                    splitLore = formattingColor + splitLore;
                }
                splitLoreList.add(splitLore);
                index = endIndex;

                if(index < loreRow.length() && loreRow.charAt(index) == '§') {
                    formattingColor = loreRow.substring(index, index + 2);
                    index += 2;
                } else {
                    formattingColor = "";
                }
            }
            return splitLoreList.toArray(new String[0]);
        }
    }

    Integer totalDungeonRuns = 0;

    public void addDungeonFloors(JsonObject mode) {
        JsonObject floors = mode.get("floors").getAsJsonObject();
        boolean masterMode = !mode.get("id").getAsString().equals("dungeon_catacombs");
        int index = 0;
        Color c = new Color(100,100,100,100);

        // Create container for the mode
        UIComponent modeContainer = new UIBlock(clear)
            .setChildOf(statsAreaContainer)
            .setWidth(new RelativeConstraint(1f))
            .setHeight(new RelativeConstraint(0.7f))
            .setY(new SiblingConstraint(10f));

        // Create title for the mode container
        new UIText(masterMode ? ChatFormatting.RED+"Master Catacombs" : ChatFormatting.YELLOW+"Catacombs")
            .setChildOf(modeContainer)
            .setTextScale(new PixelConstraint((float) (2.5f*fontScale)))
            .setX(new CenterConstraint());

        UIComponent container = null;
        for (Entry<String, JsonElement> f : floors.entrySet()) {
            JsonObject floorStats = f.getValue().getAsJsonObject().get("stats").getAsJsonObject();
            String floorName = ProfileViewerUtils.formatTitle(f.getValue().getAsJsonObject().get("name").getAsString());

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

            UIComponent topUnderline = new UIBlock(masterMode ? new Color(255,85,85) : new Color(85,255,85))
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
                if(!masterMode) {
                    int timesPlayed = floorStats.get("times_played").getAsInt();
                    totalDungeonRuns += timesPlayed; 
                    new UIText(g + "  Times Played: " + bold + timesPlayed)
                        .setY(new SiblingConstraint(6f))
                        .setChildOf(box);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }   
            try {
                int timesPlayed = floorStats.get("tier_completions").getAsInt();
                totalDungeonRuns += timesPlayed;
                new UIText(g + "  Times Completed: " + bold + timesPlayed)
                    .setY(new SiblingConstraint(2f))
                    .setChildOf(box);
            } catch (Exception e) {
                // TODO: handle exception
                new UIText(g + "  Times Completed: " + bold + 0)
                    .setY(new SiblingConstraint(2f))
                    .setChildOf(box);
            }
            try {
                int score = floorStats.get("best_score").getAsInt();
                new UIText(g+"  Best Score: "+bold+score+" " /*+ScoreCalculation.getScore(score)*/)
                    .setY(new SiblingConstraint(2f))
                    .setChildOf(box);
            } catch (Exception e) {
                // TODO: handle exception
            }
            try {
                new UIText(g+"  Fastest Time: "+bold+(Utils.secondsToTime(floorStats.get("fastest_time").getAsInt()/1000)))
                    .setY(new SiblingConstraint(2f))
                    .setChildOf(box);   
            } catch (Exception e) {
                // TODO: handle exception
            }
            index++;
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

        UIComponent container = new UIBlock(clear).setWidth(new PixelConstraint(140f)).setHeight(new PixelConstraint(140f)).setX(new RelativeConstraint(0.5f)).setY(new CenterConstraint()).enableEffect(new OutlineEffect(Color.white, 3f)).setChildOf(miningContainer);
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
    
    static List<hotmUpgrade> tooltips = null;

    public static class hotmUpgrade {
        public Vector2f pos;
        public List<String> hover;
        public ItemStack stack;

        public hotmUpgrade(List<String> hover,Vector2f pos) {
            this.hover = hover;
            this.pos = pos;
        }
    }

}
