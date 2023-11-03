package mrfast.sbf;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.features.dungeons.*;
import net.minecraftforge.fml.common.ModContainer;
import org.lwjgl.input.Keyboard;

import mrfast.sbf.commands.DungeonsCommand;
import mrfast.sbf.commands.FakePlayerCommand;
import mrfast.sbf.commands.FlipsCommand;
import mrfast.sbf.commands.InventoryCommand;
import mrfast.sbf.commands.RepartyCommand;
import mrfast.sbf.commands.ShrugCommand;
import mrfast.sbf.commands.SkyCommand;
import mrfast.sbf.commands.TerminalCommand;
import mrfast.sbf.commands.configCommand;
import mrfast.sbf.commands.getNbtCommand;
import mrfast.sbf.commands.pingCommand;
import mrfast.sbf.commands.pvCommand;
import mrfast.sbf.commands.sidebarCommand;
import mrfast.sbf.core.Config;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.ChatEventListener;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.features.statDisplays.ActionBarListener;
import mrfast.sbf.features.statDisplays.CryptDisplay;
import mrfast.sbf.features.statDisplays.DefenceDisplay;
import mrfast.sbf.features.statDisplays.EffectiveHealthDisplay;
import mrfast.sbf.features.statDisplays.HealthDisplay;
import mrfast.sbf.features.statDisplays.ManaDisplay;
import mrfast.sbf.features.statDisplays.SecretDisplay;
import mrfast.sbf.features.statDisplays.SpeedDisplay;
import mrfast.sbf.features.dungeons.solvers.BlazeSolver;
import mrfast.sbf.features.dungeons.solvers.CreeperSolver;
import mrfast.sbf.features.dungeons.solvers.LividFinder;
import mrfast.sbf.features.dungeons.solvers.TeleportPadSolver;
import mrfast.sbf.features.dungeons.solvers.ThreeWeirdosSolver;
import mrfast.sbf.features.dungeons.solvers.WaterBoardSolver;
import mrfast.sbf.features.events.JerryTimer;
import mrfast.sbf.features.events.MayorJerry;
import mrfast.sbf.features.events.MythologicalEvent;
import mrfast.sbf.features.exoticAuctions.ExoticAuctions;
import mrfast.sbf.features.items.HideGlass;
import mrfast.sbf.features.items.ItemFeatures;
import mrfast.sbf.features.mining.CommisionsTracker;
import mrfast.sbf.features.mining.HighlightCobblestone;
import mrfast.sbf.features.mining.MetalDetectorSolver;
import mrfast.sbf.features.mining.MiningFeatures;
import mrfast.sbf.features.misc.AuctionFeatures;
import mrfast.sbf.features.misc.AutoAuctionFlip;
import mrfast.sbf.features.misc.ChronomotronSolver;
import mrfast.sbf.features.misc.ConjuringCooldown;
import mrfast.sbf.features.misc.CropCounter;
import mrfast.sbf.features.misc.MiscFeatures;
import mrfast.sbf.features.misc.PlayerDiguiser;
import mrfast.sbf.features.misc.SpamHider;
import mrfast.sbf.features.misc.TreecapCooldown;
import mrfast.sbf.features.misc.UltrasequencerSolver;
import mrfast.sbf.features.overlays.BaitCounterOverlay;
import mrfast.sbf.features.overlays.DamageOverlays;
import mrfast.sbf.features.overlays.FairySoulWaypoints;
import mrfast.sbf.features.overlays.GemstoneMiningOverlay;
import mrfast.sbf.features.overlays.GiftTracker;
import mrfast.sbf.features.overlays.GrandmaWolfTimer;
import mrfast.sbf.features.overlays.MiscOverlays;
import mrfast.sbf.features.overlays.RelicFinderWaypoints;
import mrfast.sbf.features.overlays.ZealotSpawnLocations;
import mrfast.sbf.features.overlays.maps.CrimsonMap;
import mrfast.sbf.features.overlays.maps.CrystalHollowsMap;
import mrfast.sbf.features.overlays.maps.DwarvenMap;
import mrfast.sbf.features.overlays.menuOverlay.CollectionOverlay;
import mrfast.sbf.features.overlays.menuOverlay.GardenFeatures;
import mrfast.sbf.features.overlays.menuOverlay.MinionOverlay;
import mrfast.sbf.features.overlays.menuOverlay.MissingTalismans;
import mrfast.sbf.features.overlays.menuOverlay.TradingOverlay;
import mrfast.sbf.features.render.DynamicFullbright;
import mrfast.sbf.features.render.HighlightCropArea;
import mrfast.sbf.features.render.RiftFeatures;
import mrfast.sbf.features.render.SlayerFeatures;
import mrfast.sbf.features.trackers.AutomatonTracker;
import mrfast.sbf.features.trackers.EnderNodeTracker;
import mrfast.sbf.features.trackers.GhostTracker;
import mrfast.sbf.features.trackers.IceTreasureTracker;
import mrfast.sbf.features.trackers.PowderTracker;
import mrfast.sbf.features.trackers.TrevorHelper;
import mrfast.sbf.gui.GuiManager;
import mrfast.sbf.gui.ProfileViewerUtils;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.CapeUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ICommand;
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

@Mod(modid = SkyblockFeatures.MODID, name = SkyblockFeatures.MOD_NAME, acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true)
public class SkyblockFeatures {
    public static final String MODID = "skyblockfeatures";
    public static final String MOD_NAME = "skyblockfeatures";
    public static String VERSION = ChatFormatting.RED+"Unknown";
    public static Minecraft mc = Minecraft.getMinecraft();

    public static Config config = new Config();
    public static GuiManager GUIMANAGER;
    public static int ticks = 0;
    public static boolean usingNEU = false;
    public static File jarFile = null;
    public static File modDir = new File(new File(mc.mcDataDir, "config"), "skyblockfeatures");


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
        initBlacklist(playerUUID);

        // Save the config
        config.preload();
        SkyblockFeatures.config.forceSave();

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
            new ConjuringCooldown(),
            new SpeedDisplay(),
            new EffectiveHealthDisplay(),
            new ManaDisplay(),
            new HealthDisplay(),
            new SecretDisplay(),
            new CryptDisplay(),
            new DefenceDisplay(),
            new ActionBarListener(),
            new CommisionsTracker(),
            new FairySoulWaypoints(),
            new JerryTimer(),
            new GiftTracker(),
            new CropCounter(),
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
            new BaitCounterOverlay(),
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
            new ExoticAuctions(),
            new PartyFinderFeatures(),
            new CollectionOverlay()
        );
        features.forEach(MinecraftForge.EVENT_BUS::register);
        // Checks mod folder for version of Skyblock Features your using
        List<ModContainer> modList = Loader.instance().getModList();
        for(ModContainer mod:modList) {
            if(mod.getModId().equals(MODID)) {
                VERSION = mod.getDisplayVersion();
                break;
            }
        }

        SkyblockFeatures.config.timeStartedUp++;
        SkyblockFeatures.config.aucFlipperEnabled = false;

        SkyblockFeatures.config.forceSave();
        System.out.println("You have started Skyblock Features up "+SkyblockFeatures.config.timeStartedUp+" times!");
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
                if(s.equals(playerUUID)) {
                    throw new Error("You're blacklisted from using SBF! If you think this is a mistake contact 'mrfast' on discord.");
                }
            }
        } catch (Exception ignored) {}
    }

    // List files in a directory (Used only for the mods folder)
    public Set<String> listFilesUsingJavaIO(String dir) {
        return Stream.of(Objects.requireNonNull(new File(dir).listFiles()))
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
        commands.add(new TerminalCommand());
        commands.add(new ShrugCommand());
        commands.add(new FlipsCommand());
        commands.add(new InventoryCommand());
        commands.add(new DungeonsCommand());
        commands.add(new RepartyCommand());
        commands.add(new pingCommand());
        commands.add(new sidebarCommand());
        commands.add(new FakePlayerCommand());
        commands.add(new pvCommand());

        for (ICommand command : commands) {
            if (!commandHandler.getCommands().containsValue(command)) {
                commandHandler.registerCommand(command);
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        // Small items

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

    private KeyBinding toggleSprint;
    private static boolean toggled = true;

    public final static KeyBinding reloadPartyFinder = new KeyBinding("Reload Party Finder", Keyboard.KEY_R, "Skyblock Features");
    public final static KeyBinding openBestFlipKeybind = new KeyBinding("Open Best Flip", Keyboard.KEY_J, "Skyblock Features");
    
    @EventHandler
    public void initKeybinds(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientRegistry.registerKeyBinding(openBestFlipKeybind);

        toggleSprint = new KeyBinding("Toggle Sprint", Keyboard.KEY_I, "Skyblock Features");
        ClientRegistry.registerKeyBinding(toggleSprint);
    }

    @SubscribeEvent
    public void onTick2(TickEvent.ClientTickEvent e) {
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
