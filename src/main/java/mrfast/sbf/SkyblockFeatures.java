package mrfast.sbf;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.commands.*;
import mrfast.sbf.core.*;
import mrfast.sbf.events.ChatEventListener;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.features.dungeons.*;
import mrfast.sbf.features.dungeons.solvers.*;
import mrfast.sbf.features.dungeons.solvers.terminals.ClickInOrderSolver;
import mrfast.sbf.features.events.JerryTimer;
import mrfast.sbf.features.events.MayorJerry;
import mrfast.sbf.features.events.MythologicalEvent;
import mrfast.sbf.features.items.CooldownTracker;
import mrfast.sbf.features.items.FireVeilTimer;
import mrfast.sbf.features.items.HideGlass;
import mrfast.sbf.features.items.ItemFeatures;
import mrfast.sbf.features.mining.CommisionsTracker;
import mrfast.sbf.features.mining.HighlightCobblestone;
import mrfast.sbf.features.mining.MetalDetectorSolver;
import mrfast.sbf.features.mining.MiningFeatures;
import mrfast.sbf.features.misc.*;
import mrfast.sbf.features.overlays.*;
import mrfast.sbf.features.overlays.maps.CrimsonMap;
import mrfast.sbf.features.overlays.maps.CrystalHollowsMap;
import mrfast.sbf.features.overlays.maps.DwarvenMap;
import mrfast.sbf.features.overlays.menuOverlay.*;
import mrfast.sbf.features.render.*;
import mrfast.sbf.features.statDisplays.*;
import mrfast.sbf.features.termPractice.TerminalManager;
import mrfast.sbf.features.trackers.*;
import mrfast.sbf.gui.GuiManager;
import mrfast.sbf.gui.ProfileViewerUtils;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.CapeUtils;
import mrfast.sbf.utils.OutlineUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ICommand;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod(modid = SkyblockFeatures.MODID, name = SkyblockFeatures.MOD_NAME, acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true)
public class SkyblockFeatures {
    public static final String MODID = "skyblockfeatures";
    public static final String MOD_NAME = "skyblockfeatures";
    public static String VERSION = ChatFormatting.RED + "Unknown";
    public static File SOURCE;
    public static Minecraft mc = Minecraft.getMinecraft();
    public static Config config = new Config();
    public static GuiManager GUIMANAGER;
    public static int ticks = 0;
    public static boolean usingNEU = false;
    public static File modDir = new File(new File(mc.mcDataDir, "config"), "skyblockfeatures");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!modDir.exists()) modDir.mkdirs();
        TrashHighlighter.initTrashFile();
        GUIMANAGER = new GuiManager();
        // Load the config
        ConfigManager.loadConfiguration(config);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Get player uuid
        String playerUUID = Utils.GetMC().getSession().getProfile().getId().toString();

        // Load blacklist
        initBlacklist(playerUUID);

        // Modify configurableClass properties manually or programmatically

        // Features to load
        List<Object> features = Arrays.asList(
                this,
                new ChatEventListener(),
                GUIMANAGER,
                new SkyblockInfo(),
                new SpamHider(),
                new PricingData(),
                new APIUtils(),
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
                new SkyblockMobDetector(),
                new CooldownTracker(),
                new FireVeilTimer(),
                new TrashHighlighter(),
                new SpeedDisplay(),
                new EffectiveHealthDisplay(),
                new ManaDisplay(),
                new HealthDisplay(),
                new SecretDisplay(),
                new CryptDisplay(),
                new DefenseDisplay(),
                new ActionBarListener(),
                new CommisionsTracker(),
                new FairySoulWaypoints(),
                new JerryTimer(),
                new GiftTracker(),
                new CropCounter(),
                new ClickInOrderSolver(),
                new TerminalManager(),
                new HideGlass(),
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
                new GhostTracker(),
                new CreeperSolver(),
                new PowderTracker(),
                new DwarvenMap(),
                new GrandmaWolfTimer(),
                new RelicFinderWaypoints(),
                new DynamicFullbright(),
                new GardenFeatures(),
                new HighlightCropArea(),
                new MythologicalEvent(),
                new TeleportPadSolver(),
                new WaterBoardSolver(),
                new ShadowAssasinFeatures(),
                new SlayerFeatures(),
                new CrimsonMap(),
                new RiftFeatures(),
                new BlazeSolver(),
                new ThreeWeirdosSolver(),
                new SkyblockInfo(),
                new Reparty(),
                new ProfileViewerUtils(),
                new PartyFinderFeatures(),
                new CollectionOverlay(),
                new RenderFeatures(),
                new FireFreezeTimer()
        );
        features.forEach(MinecraftForge.EVENT_BUS::register);
        // Checks mod folder for version of Skyblock Features your using
        List<ModContainer> modList = Loader.instance().getModList();
        for (ModContainer mod : modList) {
            if (mod.getModId().equals(MODID)) {
                VERSION = mod.getDisplayVersion();
                SOURCE = mod.getSource();
                break;
            }
        }

        SkyblockFeatures.config.timeStartedUp++;
        SkyblockFeatures.config.aucFlipperEnabled = false;

        ConfigManager.saveConfig(SkyblockFeatures.config);
        System.out.println("You have started Skyblock Features up " + SkyblockFeatures.config.timeStartedUp + " times!");
    }

    /*
    This hopefully shouldn't need to be used but in the case of people trying to sell or claim my mod as theirs, this is prevention.
     */
    public void initBlacklist(String playerUUID) {
        try {
            URL url = new URL("https://raw.githubusercontent.com/MrFast-js/SBF-Blacklist/main/blacklist.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String s;
            while ((s = reader.readLine()) != null) {
                if (s.equals(playerUUID)) {
                    throw new Error("You're blacklisted from using SBF! If you think this is a mistake contact 'mrfast' on discord.");
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        usingNEU = Loader.isModLoaded("notenoughupdates");
        ClientCommandHandler commandHandler = ClientCommandHandler.instance;

        List<ICommand> commands = new ArrayList<>();
        commands.add(new debugCommand());
        commands.add(new SkyCommand());
        commands.add(new configCommand());
        commands.add(new TerminalCommand());
        commands.add(new ShrugCommand());
        commands.add(new FlipsCommand());
        commands.add(new DungeonsCommand());
        commands.add(new RepartyCommand());
        commands.add(new pingCommand());
        commands.add(new FakePlayerCommand());
        commands.add(new pvCommand());

        for (ICommand command : commands) {
            if (!commandHandler.getCommands().containsValue(command)) {
                commandHandler.registerCommand(command);
            }
        }
    }

    boolean sentUpdateNotification = false;
    GuiScreen lastOpenContainer = null;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if(Utils.GetMC().thePlayer!=null && SkyblockFeatures.config.updateNotify && !sentUpdateNotification && Utils.inSkyblock) {
            sentUpdateNotification = true;
            VersionManager.silentUpdateCheck();
        }

        if(Utils.GetMC().currentScreen==null && lastOpenContainer instanceof GuiContainer) {
            MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.CloseWindowEvent((GuiContainer) lastOpenContainer,((GuiContainer) lastOpenContainer).inventorySlots));
        }
        lastOpenContainer = Utils.GetMC().currentScreen;

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

    private final static KeyBinding toggleSprint = new KeyBinding("Toggle Sprint", 0, "Skyblock Features");
    public final static KeyBinding reloadPartyFinder = new KeyBinding("Reload Party Finder", Keyboard.KEY_R, "Skyblock Features");
    public final static KeyBinding openBestFlipKeybind = new KeyBinding("Open Best Flip", Keyboard.KEY_J, "Skyblock Features");

    @EventHandler
    public void initKeybinds(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientRegistry.registerKeyBinding(openBestFlipKeybind);
        ClientRegistry.registerKeyBinding(reloadPartyFinder);
        ClientRegistry.registerKeyBinding(toggleSprint);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        OutlineUtils.entityOutlines.clear();
    }

    @SubscribeEvent
    public void onTick2(TickEvent.ClientTickEvent e) {
        if (toggleSprint.isPressed()) {
            if (config.toggleSprint) {
                Utils.sendMessage(EnumChatFormatting.RED + "Togglesprint disabled.");
            } else {
                Utils.sendMessage(EnumChatFormatting.GREEN + "Togglesprint enabled.");
            }
            config.toggleSprint = !config.toggleSprint;
        }
        if (config.toggleSprint) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }
}
