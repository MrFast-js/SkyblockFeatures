package mrfast.sbf;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.API.ItemAbilityAPI;
import mrfast.sbf.commands.*;
import mrfast.sbf.core.*;
import mrfast.sbf.events.*;
import mrfast.sbf.features.dungeons.*;
import mrfast.sbf.features.dungeons.solvers.*;
import mrfast.sbf.features.dungeons.solvers.terminals.ClickInOrderSolver;
import mrfast.sbf.features.events.*;
import mrfast.sbf.features.items.*;
import mrfast.sbf.features.mining.*;
import mrfast.sbf.features.misc.*;
import mrfast.sbf.features.overlays.*;
import mrfast.sbf.features.overlays.maps.CrimsonMap;
import mrfast.sbf.features.overlays.maps.CrystalHollowsMap;
import mrfast.sbf.features.overlays.maps.DwarvenMap;
import mrfast.sbf.features.overlays.menuOverlay.*;
import mrfast.sbf.features.render.*;
import mrfast.sbf.features.statDisplays.*;
import mrfast.sbf.features.statDisplays.bars.HealthBar;
import mrfast.sbf.features.statDisplays.bars.ManaBar;
import mrfast.sbf.features.termPractice.TerminalManager;
import mrfast.sbf.features.trackers.*;
import mrfast.sbf.gui.ConfigGui;
import mrfast.sbf.gui.GuiManager;
import mrfast.sbf.gui.ProfileViewerUtils;
import mrfast.sbf.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ICommand;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
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

    static boolean sendUpdateChangelogs = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Get player uuid
        String playerUUID = Utils.GetMC().getSession().getProfile().getId().toString();

        // Load blacklist
        initBlacklist(playerUUID);


        // Features to load
        List<Object> features = Arrays.asList(
                this,
                new ChatEventListener(),
                new DataManager(),
                GUIMANAGER,
                new SkyblockInfo(),
                new SpamHider(),
                new PricingData(),
                new NetworkUtils(),
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
                new ItemAbilityAPI(),
                new FireVeilTimer(),
                new TrashHighlighter(),
                new SpeedDisplay(),
                new EffectiveHealthDisplay(),
                new ManaDisplay(),
                new HealthDisplay(),
                new SecretDisplay(),
                new CryptDisplay(),
                new DefenseDisplay(),
                new OverflowManaDisplay(),
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
                new EntityOutlineRenderer(),
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
                new EntityOutlineRenderer(),
                new RiftFeatures(),
                new HealthBar(),
                new ManaBar(),
                new FinalDestinationOverlay(),
                new BlazeSolver(),
                new ThreeWeirdosSolver(),
                new SkyblockInfo(),
                new GlowingItems(),
                new Reparty(),
                new F2SpawnTimers(),
                new ProfileViewerUtils(),
                new GloomlockProtection(),
                new SalvageProtection(),
                new QuiverOverlay(),
                new PartyFinderFeatures(),
                new CollectionOverlay(),
                new RenderFeatures(),
                new FireFreezeTimer(),
                new BestiaryHelper()
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


        int timestarted = 0;
        if(DataManager.dataJson.has("timesStartedUp")) {
            timestarted = (int) DataManager.getData("timesStartedUp");
        }

        DataManager.saveData("timesStartedUp",timestarted+1);
        System.out.println("You have started Skyblock Features up " + timestarted + " times!");

        if(DataManager.dataJson.has("lastStartedVersion")) {
            if (!((String) DataManager.getData("lastStartedVersion")).equals(SkyblockFeatures.VERSION)) {
                sendUpdateChangelogs = true;
            }
        }
        DataManager.saveData("lastStartedVersion",SkyblockFeatures.VERSION);


        SkyblockFeatures.config.aucFlipperEnabled = false;
        ConfigManager.saveConfig();
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
        commands.add(new DebugCommand());
        commands.add(new SkyCommand());
        commands.add(new ConfigCommand());
        commands.add(new TerminalCommand());
        commands.add(new ShrugCommand());
        commands.add(new FlipsCommand());
        commands.add(new DungeonsCommand());
        commands.add(new RepartyCommand());
        commands.add(new DungeonPlayerInfoCommand());
        commands.add(new PingCommand());
        commands.add(new FakePlayerCommand());
        commands.add(new pvCommand());
        if (Utils.isDeveloper()) {
            commands.add(new ColorTestCommand());
        }

        for (ICommand command : commands) {
            if (!commandHandler.getCommands().containsValue(command)) {
                commandHandler.registerCommand(command);
            }
        }
    }

    public static boolean sentUpdateNotification = false;
    public static boolean updateChecked = false;
    GuiScreen lastOpenContainer = null;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (Utils.GetMC().thePlayer != null && SkyblockFeatures.config.updateNotify && !sentUpdateNotification && Utils.inSkyblock) {
            if(!updateChecked) {
                System.out.println("Silenting checking for SBF update");
                VersionManager.silentUpdateCheck();
                updateChecked = true;
            }
        }
        if (Utils.inSkyblock && sendUpdateChangelogs && Utils.GetMC().theWorld!=null) {
            sendUpdateChangelogs = false;
            new Thread(() -> {
                JsonObject currentUpdate = VersionManager.getCurrentGithubVersion();
                if (currentUpdate != null) {
                    String body = currentUpdate.get("body").getAsString();
                    body = body.replaceAll("\r", "").replaceAll("\\n# ", "§e§l").replaceAll("= ", "§7= ").replaceAll("- ", "§c- ").replaceAll("\\+ ", "§a+ ").replaceAll("```diff\\n", "").replaceAll("```", "");
                    IChatComponent component = new ChatComponentText("§eUpdated to version §6§l" + currentUpdate.get("name").getAsString() + " §r§7(hover)");
                    component.setChatStyle(component.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(body.trim()))));
                    Utils.playSound("random.orb", 0.1);
                    Utils.sendMessage(component);
                }
            }).start();
        }

        if (Utils.GetMC().currentScreen == null && lastOpenContainer instanceof GuiContainer) {
            MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.CloseWindowEvent((GuiContainer) lastOpenContainer, ((GuiContainer) lastOpenContainer).inventorySlots));
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

    @EventHandler
    public void initKeybinds(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientRegistry.registerKeyBinding(toggleSprint);
    }

    // Stop the escape key from closing the gui if listening for a keybind
    @SubscribeEvent
    public void onGuiKeyEvent(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (ConfigGui.listeningForKeybind && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
            event.setCanceled(true);
        }
    }

    // Send messages to the user from the server for needed announcements or something idk yet
    @SubscribeEvent
    public void onSocketMessage(SocketMessageEvent event) {
        if (event.type.equals("message")) {
            if (event.message.contains("Checking for auction flips") && !SkyblockFeatures.config.aucFlipperEnabled)
                return;
            Utils.sendMessage(event.message);
        }
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
