package mrfast.skyblockfeatures;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import gg.essential.api.EssentialAPI;
import mrfast.skyblockfeatures.commands.AccessoriesCommand;
import mrfast.skyblockfeatures.commands.ArmorCommand;
import mrfast.skyblockfeatures.commands.BankCommand;
import mrfast.skyblockfeatures.commands.DungeonsCommand;
import mrfast.skyblockfeatures.commands.FakePlayerCommand;
import mrfast.skyblockfeatures.commands.FlipsCommand;
import mrfast.skyblockfeatures.commands.GetkeyCommand;
import mrfast.skyblockfeatures.commands.InventoryCommand;
import mrfast.skyblockfeatures.commands.NetworthCommand;
import mrfast.skyblockfeatures.commands.PathTracerCommand;
import mrfast.skyblockfeatures.commands.RepartyCommand;
import mrfast.skyblockfeatures.commands.ShrugCommand;
import mrfast.skyblockfeatures.commands.SkyCommand;
import mrfast.skyblockfeatures.commands.TerminalCommand;
import mrfast.skyblockfeatures.commands.ViewModelCommand;
import mrfast.skyblockfeatures.commands.configCommand;
import mrfast.skyblockfeatures.commands.getNbtCommand;
import mrfast.skyblockfeatures.commands.pvCommand;
import mrfast.skyblockfeatures.commands.sidebarCommand;
import mrfast.skyblockfeatures.core.PricingData;
import mrfast.skyblockfeatures.core.Config;
import mrfast.skyblockfeatures.core.SkyblockInfo;
import mrfast.skyblockfeatures.events.ChatEventListener;
import mrfast.skyblockfeatures.events.PacketEvent;
import mrfast.skyblockfeatures.events.SecondPassedEvent;
import mrfast.skyblockfeatures.features.actionBar.ActionBarListener;
import mrfast.skyblockfeatures.features.actionBar.CryptDisplay;
import mrfast.skyblockfeatures.features.actionBar.DefenceDisplay;
import mrfast.skyblockfeatures.features.actionBar.EffectiveHealthDisplay;
import mrfast.skyblockfeatures.features.actionBar.HealthDisplay;
import mrfast.skyblockfeatures.features.actionBar.ManaDisplay;
import mrfast.skyblockfeatures.features.actionBar.SecretDisplay;
import mrfast.skyblockfeatures.features.actionBar.SpeedDisplay;
import mrfast.skyblockfeatures.features.dungeons.BetterParties;
import mrfast.skyblockfeatures.features.dungeons.ChestProfit;
import mrfast.skyblockfeatures.features.dungeons.DungeonMap;
import mrfast.skyblockfeatures.features.dungeons.DungeonsFeatures;
import mrfast.skyblockfeatures.features.dungeons.Nametags;
import mrfast.skyblockfeatures.features.dungeons.Reparty;
import mrfast.skyblockfeatures.features.dungeons.solvers.CreeperSolver;
import mrfast.skyblockfeatures.features.dungeons.ShadowAssasinFeatures;
import mrfast.skyblockfeatures.features.dungeons.solvers.BlazeSolver;

// import mrfast.skyblockfeatures.features.dungeons.ScoreCalculation;
// import mrfast.skyblockfeatures.features.dungeons.solvers.BoulderSolver;
// import mrfast.skyblockfeatures.features.dungeons.solvers.IceFillSolver;
// import mrfast.skyblockfeatures.features.dungeons.solvers.IcePathSolver;
// import mrfast.skyblockfeatures.features.dungeons.solvers.TicTacToeSolver;
// import mrfast.skyblockfeatures.features.dungeons.solvers.WaterBoardSolver;

// import mrfast.skyblockfeatures.features.dungeons.solvers.TriviaSolver;
import mrfast.skyblockfeatures.features.dungeons.solvers.ThreeWeirdosSolver;
import mrfast.skyblockfeatures.features.dungeons.solvers.LividFinder;
import mrfast.skyblockfeatures.features.dungeons.solvers.TeleportPadSolver;
import mrfast.skyblockfeatures.features.events.JerryTimer;
import mrfast.skyblockfeatures.features.events.MayorJerry;
import mrfast.skyblockfeatures.features.events.MythologicalEvent;
import mrfast.skyblockfeatures.features.exoticAuctions.ExoticAuctions;
import mrfast.skyblockfeatures.features.items.HideGlass;
import mrfast.skyblockfeatures.features.mining.CommisionsTracker;
import mrfast.skyblockfeatures.features.mining.HighlightCobblestone;
import mrfast.skyblockfeatures.features.mining.MetalDetectorSolver;
import mrfast.skyblockfeatures.features.mining.MiningFeatures;
import mrfast.skyblockfeatures.features.mining.PathTracer;
import mrfast.skyblockfeatures.features.misc.AuctionFeatures;
import mrfast.skyblockfeatures.features.misc.AutoAuctionFlip;
import mrfast.skyblockfeatures.features.misc.ChronomotronSolver;
import mrfast.skyblockfeatures.features.misc.ConjuringCooldown;
import mrfast.skyblockfeatures.features.misc.CropCounter;
import mrfast.skyblockfeatures.features.misc.FishingHelper;
import mrfast.skyblockfeatures.features.misc.ItemFeatures;
import mrfast.skyblockfeatures.features.misc.MiscFeatures;
import mrfast.skyblockfeatures.features.misc.PlayerDiguiser;
import mrfast.skyblockfeatures.features.misc.SpamHider;
import mrfast.skyblockfeatures.features.misc.TreecapCooldown;
import mrfast.skyblockfeatures.features.misc.UltrasequencerSolver;
import mrfast.skyblockfeatures.features.overlays.BaitCounterOverlay;
import mrfast.skyblockfeatures.features.overlays.CollectionOverlay;
import mrfast.skyblockfeatures.features.overlays.ComposterOverlay;
import mrfast.skyblockfeatures.features.overlays.CrimsonMap;
import mrfast.skyblockfeatures.features.overlays.CrystalHollowsMap;
import mrfast.skyblockfeatures.features.overlays.DamageOverlays;
import mrfast.skyblockfeatures.features.overlays.DwarvenMap;
import mrfast.skyblockfeatures.features.overlays.FairySoulWaypoints;
import mrfast.skyblockfeatures.features.overlays.GardenVisitorOverlay;
import mrfast.skyblockfeatures.features.overlays.GemstoneMiningOverlay;
import mrfast.skyblockfeatures.features.overlays.GiftTracker;
import mrfast.skyblockfeatures.features.overlays.GrandmaWolfTimer;
import mrfast.skyblockfeatures.features.overlays.MinionOverlay;
import mrfast.skyblockfeatures.features.overlays.MiscOverlays;
import mrfast.skyblockfeatures.features.overlays.MissingTalismans;
import mrfast.skyblockfeatures.features.overlays.RelicFinderWaypoints;
import mrfast.skyblockfeatures.features.overlays.TradingOverlay;
import mrfast.skyblockfeatures.features.overlays.ZealotSpawnLocations;
import mrfast.skyblockfeatures.features.render.DynamicFullbright;
import mrfast.skyblockfeatures.features.render.HideStuff;
import mrfast.skyblockfeatures.features.render.HighlightCropArea;
import mrfast.skyblockfeatures.features.render.RiftFeatures;
import mrfast.skyblockfeatures.features.render.SlayerFeatures;
import mrfast.skyblockfeatures.features.trackers.AutomatonTracker;
import mrfast.skyblockfeatures.features.trackers.EnderNodeTracker;
import mrfast.skyblockfeatures.features.trackers.GhostTracker;
import mrfast.skyblockfeatures.features.trackers.IceTreasureTracker;
import mrfast.skyblockfeatures.features.trackers.PowderTracker;
import mrfast.skyblockfeatures.features.trackers.TrevorHelper;
import mrfast.skyblockfeatures.gui.GuiManager;
import mrfast.skyblockfeatures.gui.ProfileViewerUtils;
import mrfast.skyblockfeatures.utils.CapeUtils;
// import mrfast.skyblockfeatures.core.SkyblockInfo;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ICommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = SkyblockFeatures.MODID, name = SkyblockFeatures.MOD_NAME, version = "1.2.4", acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true)
public class SkyblockFeatures {
    public static final String MODID = "skyblockfeatures";
    public static final String MOD_NAME = "skyblockfeatures";
    public static String newAPIKey = "5c47ad45-09e6-4267-8a6a-fe1bcdcf8ced";
    public static String oldAPIKey = "a31515ff-cf53-4f25-8f7c-e0fa3f12a530";

    public static String VERSION = "Loading";
    public static final Minecraft mc = Minecraft.getMinecraft();

    public static Config config = new Config();
    public static File modDir = new File(new File(mc.mcDataDir, "config"), "skyblockfeatures");
    public static GuiManager GUIMANAGER;
    public static Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static int ticks = 0;

    public static ArrayDeque<String> sendMessageQueue = new ArrayDeque<>();
    public static boolean usingNEU = false;

    public static File jarFile = null;
    private static long lastChatMessage = 0;

    @Mod.Instance(MODID)
    public static SkyblockFeatures INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!modDir.exists()) modDir.mkdirs();
        GUIMANAGER = new GuiManager();
        jarFile = event.getSourceFile();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Get player uuid
        String playerUUID = Utils.GetMC().getSession().getProfile().getId().toString();

        // Load blacklist
        try {
            URL url = new URL("https://raw.githubusercontent.com/MrFast-js/SBF-Blacklist/main/blacklist.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String s;
            while ((s = reader.readLine()) != null) {
                if(s.equals(playerUUID)) {
                    throw new Error("You're blacklisted from using SBF! If you think this is a mistake contact MrFast#7146 on discord.");
                }
            }
        } catch (Exception ignored) {}

        // Save the config
        config.preload();
        SkyblockFeatures.config.markDirty();
        SkyblockFeatures.config.writeData();

        
        EssentialAPI.getCommandRegistry().registerCommand(new ViewModelCommand());

        // Features to load
        List<Object> features = Arrays.asList(
            this,
            new ChatEventListener(),
            GUIMANAGER,
            SkyblockInfo.getInstance(),
            new SpamHider(),
            new PricingData(),
            new ZealotSpawnLocations(),
            new ChestProfit(),
            new DungeonMap(),
            new DungeonsFeatures(),
            new ItemFeatures(),
            new CrystalHollowsMap(),
            new MayorJerry(),
            new MiningFeatures(),
            new MiscFeatures(),
            new DamageOverlays(),
            new Nametags(),
            new ConjuringCooldown(),
            new SpeedDisplay(),
            new EffectiveHealthDisplay(),
            new ManaDisplay(),
            new HealthDisplay(),
            new SecretDisplay(),
            new CryptDisplay(),
            new DefenceDisplay(),
            new HideStuff(),
            new ActionBarListener(),
            new BetterParties(),
            new CommisionsTracker(),
            new FairySoulWaypoints(),
            new JerryTimer(),
            new GiftTracker(),
            new CropCounter(),
            new HideGlass(),
            new FishingHelper(),
            new AuctionFeatures(),
            new CapeUtils(),
            new MinionOverlay(),
            new AutomatonTracker(),
            new GemstoneMiningOverlay(),
            new TreecapCooldown(),
            new LividFinder(),
            new IceTreasureTracker(),
            new EnderNodeTracker(),
            new HighlightCobblestone(),
            new MissingTalismans(),
            new PlayerDiguiser(),
            new AutoAuctionFlip(),
            new MetalDetectorSolver(),
            new ChronomotronSolver(),
            new UltrasequencerSolver(),
            new TradingOverlay(),
            new MiscOverlays(),
            new TrevorHelper(),
            new PathTracer(),
            new GhostTracker(),
            new CreeperSolver(),
            new PowderTracker(),
            new DwarvenMap(),
            new GrandmaWolfTimer(),
            new RelicFinderWaypoints(),
            new DynamicFullbright(),
            new GardenVisitorOverlay(),
            new BaitCounterOverlay(),
            new HighlightCropArea(),
            new MythologicalEvent(),
            new TeleportPadSolver(),
            new ShadowAssasinFeatures(),
            new ComposterOverlay(),
            new SlayerFeatures(),
            new CrimsonMap(),
            new RiftFeatures(),
            new BlazeSolver(),
            new ThreeWeirdosSolver(),
            new SkyblockInfo(),
            new Reparty(),
            new ProfileViewerUtils(),
            new ExoticAuctions(),
            new CollectionOverlay()
        );
        features.forEach((feature)->{
            MinecraftForge.EVENT_BUS.register(feature);
        });
        // Checks mod folder for version of Skyblock Features your using
        for(String modName:listFilesUsingJavaIO(Minecraft.getMinecraft().mcDataDir.getAbsolutePath()+"/mods")) {
            if(modName.contains("Skyblock-Features")) {
                // Filters out the mod name to just the version
                VERSION = modName.substring(0, modName.length()-4).replaceAll("Skyblock-Features-", "");
                break;
            }
        }
        // Swap over old API keys to new application based ones
        if(SkyblockFeatures.config.apiKey!=oldAPIKey) {
            SkyblockFeatures.config.apiKey = oldAPIKey;
        }
        SkyblockFeatures.config.timeStartedUp++;
        System.out.println("You have started Skyblock Features up "+SkyblockFeatures.config.timeStartedUp+" times!");
    }
    // List files in a directory (Used only for the mods folder)
    public Set<String> listFilesUsingJavaIO(String dir) {
        return Stream.of(new File(dir).listFiles())
          .filter(file -> !file.isDirectory())
          .map(File::getName)
          .collect(Collectors.toSet());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        usingNEU = Loader.isModLoaded("notenoughupdates");
        ClientCommandHandler commandHandler = ClientCommandHandler.instance;

        List<ICommand> commands = new ArrayList<>();
        commands.add(new getNbtCommand());
        commands.add(new SkyCommand());
        commands.add(new configCommand());
        commands.add(new AccessoriesCommand());
        commands.add(new TerminalCommand());
        commands.add(new ShrugCommand());
        commands.add(new FlipsCommand());
        commands.add(new BankCommand());
        commands.add(new ArmorCommand());
        commands.add(new InventoryCommand());
        commands.add(new GetkeyCommand());
        commands.add(new DungeonsCommand());
        commands.add(new RepartyCommand());
        commands.add(new sidebarCommand());
        commands.add(new PathTracerCommand());
        commands.add(new FakePlayerCommand());
        commands.add(new NetworthCommand());
        commands.add(new pvCommand());

        for (ICommand command : commands) {
            if (!commandHandler.getCommands().containsValue(command)) {
                commandHandler.registerCommand(command);
            }
        }
    }


    public static boolean auctionPricesLoaded = false;
    public static boolean smallItems = false;
    public boolean start = true;
    public boolean loadedBlacklist = false;
    public boolean checkedIfBlacklisted = false;
    ArrayList<String> blacklist = new ArrayList<>();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
       
        SkyblockFeatures.config.autoAuctionFlipMargin = SkyblockFeatures.config.autoAuctionFlipMargin.replaceAll("[^0-9]", "");
        if (event.phase != TickEvent.Phase.START) return;
        // Small items
        if(start) {
            smallItems = SkyblockFeatures.config.smallItems;
            start = false;
        } else {
            if(smallItems && !SkyblockFeatures.config.smallItems) {
                SkyblockFeatures.config.armX = 0;
                SkyblockFeatures.config.armY = 0;
                SkyblockFeatures.config.armZ = 0;
            }
            if(!smallItems && SkyblockFeatures.config.smallItems) {
                SkyblockFeatures.config.armX = 30;
                SkyblockFeatures.config.armY = -5;
                SkyblockFeatures.config.armZ = -60;
            }
            smallItems = SkyblockFeatures.config.smallItems;
        }
        if (mc.thePlayer != null && sendMessageQueue.size() > 0 && System.currentTimeMillis() - lastChatMessage > 200) {
            String msg = sendMessageQueue.pollFirst();
            if (msg != null) {
                mc.thePlayer.sendChatMessage(msg);
            }
        }
        
        if (ticks % 20 == 0) {
            if (mc.thePlayer != null) {
                Utils.checkForSkyblock();
                Utils.checkForDungeons();
            }
            MinecraftForge.EVENT_BUS.post(new SecondPassedEvent());
            ticks = 0;
        }

        ticks++;
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.SendEvent event) {
        if (event.packet instanceof C01PacketChatMessage) {
            lastChatMessage = System.currentTimeMillis();
        }
    }
    GuiScreen lastGui = null;
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !Minecraft.getMinecraft().isSingleplayer() && Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().getNetHandler() != null && EssentialAPI.getMinecraftUtil().isHypixel()) {
            try {
                Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();
                ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(1);
                Collection<Score> collection = scoreboard.getSortedScores(scoreObjective);
                for (Score score1 : collection)
                {
                    ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(score1.getPlayerName());
                    String scoreText = EnumChatFormatting.getTextWithoutFormattingCodes(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score1.getPlayerName()));

                    if (scoreText.contains("⏣")) {
                        locationString = keepLettersAndNumbersOnly(scoreText.replace("⏣", ""));
                    }
                }
            } catch (NullPointerException  e) {
                //TODO: handle exception
            }
        }
    }
    
    public static String locationString = "Unknown";
    private static final Pattern LETTERS_NUMBERS = Pattern.compile("[^a-z A-Z:0-9/'()]");

    private String keepLettersAndNumbersOnly(String text) {
        return LETTERS_NUMBERS.matcher(EnumChatFormatting.getTextWithoutFormattingCodes(text)).replaceAll("");
    }

    private KeyBinding toggleSprint;
    private static boolean toggled = true;

    public final static KeyBinding reloadAH = new KeyBinding("Reload Party Finder/Auction House", Keyboard.KEY_R, "Skyblock Features");
    public final static KeyBinding openBestFlipKeybind = new KeyBinding("Open Best Flip", Keyboard.KEY_J, "Skyblock Features");
    
    @EventHandler
    public void inist(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientRegistry.registerKeyBinding(reloadAH);
        ClientRegistry.registerKeyBinding(openBestFlipKeybind);

        toggleSprint = new KeyBinding("Toggle Sprint", Keyboard.KEY_I, "Skyblock Features");
        ClientRegistry.registerKeyBinding(toggleSprint);
    }

    @SubscribeEvent
    public void onTsick(TickEvent.ClientTickEvent e) {
        if (toggleSprint.isPressed()) {
            if (toggled) {
                Utils.SendMessage(EnumChatFormatting.RED + "Togglesprint disabled.");
            } else {
                Utils.SendMessage(EnumChatFormatting.GREEN + "Togglesprint enabled.");
            }
            toggled = !toggled;
        }
        if (toggled) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }
}
