package mrfast.skyblockfeatures.core;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.*;

import java.io.File;

public class Config extends Vigilant {

    @Property(
            type = PropertyType.TEXT,
            name = "skyblockfeatures Data",
            description = "URL for skyblockfeatures data.",
            category = "General",
            subcategory = "API",
            hidden = true
    )
    public String dataURL = "https://raw.githubusercontent.com/skyblockfeatures/skyblockfeaturessMod-Data/main/";

    @Property(
            type = PropertyType.TEXT,
            name = "Hypixel API Key",
            description = "Your Hypixel API key, which can be obtained from /api new. Required for some features.",
            category = "General",
            subcategory = "API"
    )
    public String apiKey = "";

    @Property(
            type = PropertyType.SWITCH,
            name = "First Launch",
            description = "Used to see if the user is a new user of skyblockfeatures.",
            category = "General",
            subcategory = "Other",
            hidden = true
    )
    public boolean firstLaunch = true;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Potion Effects",
            description = "ide the potion effects inside your inventory while on skyblock",
            category = "General",
            subcategory = "Other"
    )
    public boolean hidepotion = true;

    @Property(
            type = PropertyType.SWITCH,
            name = "Override other reparty commands",
            description = "Uses skyblockfeatures' reparty command instead of other mods'. \n§cRequires restart to work",
            category = "General",
            subcategory = "Reparty"
    )
    public boolean overrideReparty = true;

    @Property(
            type = PropertyType.SWITCH,
            name = "Auto-Accept Reparty",
            description = "Automatically accepts reparty invites",
            category = "General",
            subcategory = "Reparty"
    )
    public boolean autoReparty = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Auto-Accept Reparty Timeout",
            description = "Timeout in seconds for accepting a reparty invite",
            category = "General",
            subcategory = "Reparty",
            max = 120
    )
    public int autoRepartyTimeout = 60;

    @Property(
            type = PropertyType.SWITCH,
            name = "Dungeon Blocks",
            description = "Highlights important blocks in Dungeons.",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean dungeonBlocks = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Crypt Display",
            description = "Big count of how many crypts have been killed",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean cryptCount = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Blessings Viewer",
            description = "Displays the current blessings in a dungeons",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean blessingViewer = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Gift Compass Waypoints",
            description = "Shows waypoints for where to go when Gift Compass is held in hand.",
            category = "General",
	    subcategory = "Rendering"
    )
    public boolean presentWaypoints = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Dungeon Chest Profit",
            description = "Shows the estimated profit for items from chests in dungeons.",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean dungeonChestProfit = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Health Bar",
            description = "health bar thing",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean healthbar = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Health and Mana",
            description = "Hides Health and Mana from action bar",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean hidethings = false;

     @Property(
            type = PropertyType.SWITCH,
            name = "Health Display",
            description = "Moveable health display",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean HealthDisplay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Speed Display",
            description = "Moveable Speed display",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean SpeedDisplay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Effective Health Display",
            description = "Moveable Effective Health display",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean EffectiveHealthDisplay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Mana Display",
            description = "Moveable mana pog",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean ManaDisplay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Defence Display",
            description = "Moveable defence display",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean DefenceDisplay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Secrets Display",
            description = "Moveable Secrets display",
            category = "§1§rDungeons",
            subcategory = "Health & Mana Bars"
    )
    public boolean SecretsDisplay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Revive Stone Names",
            description = "Shows names next to the heads on the Revive Stone menu.",
            category = "§1§rDungeons",
            subcategory = "Quality of Life"
    )
    public boolean reviveStoneNames = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Spirit Leap Names",
            description = "Shows names next to the heads on the Spirit Leap menu.",
            category = "§1§rDungeons",
            subcategory = "Quality of Life"
    )
    public boolean spiritLeapNames = false;

     @Property(
            type = PropertyType.SWITCH,
            name = "Better Party Finder",
            description = "Highlight parties you can't join with red",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean betterpartys = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Fairy Soul Helper",
            description = "Highlights nearby fairy souls using waypoints",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean fairy = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Armor Bar",
            description = "Hide the armor icons above health bar",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean armorbar = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Hunger Bar",
            description = "Hide the food icons above hotbar",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean hungerbar = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Health Hearts",
            description = "Hide the health icons above health bar",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean healthsbar = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Fishing Helper",
            description = "Display a message when you need to pull the fish up.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean fishthing = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Jerry Timer",
            description = "Shows the cooldown for spawning jerry's",
            category = "§1§rEvents",
            subcategory = "Mayor Jerry"
    )
    public boolean jerry = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Crop Counter",
            description = "Shows the amount of crops on the hoe your holding",
            category = "§1§rFarming",
            subcategory = "Quality of Life"
    )
    public boolean Counter = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "School work reminder",
            description = "Remindes you every 30 minutes to do schoolwork",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean SchoolworkReminder = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "1.7 Animations",
            description = "",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean oldAnimations = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Far Entitys in hub",
            description = "",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean HideFarEntity = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Damage Tint",
            description = "Makes your screen get more red the lower in health you are",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean damagetint = false;


    @Property(
            type = PropertyType.SWITCH,
            name = "NameTags",
            description = "Render better nametags in dungeons",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean NameTags = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Dungeon Map",
            description = "Render a moveable dungeon map on screen",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean dungeonMap = false;

     @Property(
            type = PropertyType.SWITCH,
            name = "Quick Start",
            description = "Sends a chat message at the end of a dungeon that can be used to reparty or warp out of a dungeon",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean quickStart = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Glowing Dungeon Teammates!",
            description = "Make your teamates glow based on there class in dungeons.",
            category = "General",
            subcategory = "1.9 Glow Effect"
    )
    public boolean glowingPlayers = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Glowing Players",
            description = "Make visible players anywhere glow",
            category = "General",
            subcategory = "1.9 Glow Effect"
    )
    public boolean playeresp = false;


    @Property(
            type = PropertyType.SWITCH,
            name = "Party Glow!",
            description = "Makes your party members glow blue!",
            category = "General",
            subcategory = "1.9 Glow Effect"
    )
    public boolean glowingParty = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Glowing Items!",
            description = "Make items glow depending on rarity",
            category = "General",
            subcategory = "1.9 Glow Effect"
    )
    public boolean glowingItems = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hidden Jerry Alert",
            description = "Displays an alert when you find a hidden Jerry.",
            category = "§1§rEvents",
            subcategory = "Mayor Jerry"
    )
    public boolean hiddenJerryAlert = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Fetchur Solver",
            description = "Tells you what item Fetchur wants.",
            category = "Mining",
            subcategory = "Solvers"
    )
    public boolean fetchurSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Commissions Tracker",
            description = "Tracks your progress on commissions",
            category = "Mining",
            subcategory = "Quality of Life"
    )
    public boolean CommisionsTracker = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Crystal Hollows Map",
            description = "Show a map of the crystal hollows",
            category = "Mining",
            subcategory = "Quality of Life"
    )
    public boolean CrystalHollowsMap = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Puzzler Solver",
            description = "Shows which block to mine for Puzzler.",
            category = "Mining",
            subcategory = "Solvers"
    )
    public boolean puzzlerSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show NPC Sell Price",
            description = "Shows the NPC Sell Price on certain items.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean showNPCSellPrice = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Prehistoric Egg Distance Counter",
            description = "Shows the blocks walked on the prehistoric egg item",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean egg = false;

    @Property(
        type = PropertyType.SELECTOR,
        name = "Prettify Damage Markings",
        description = "Adds Commas or Truncations to Damage Numbers",
        category = "Miscellaneous",
        subcategory = "Quality of Life",
        options = {"Normal","Truncation","Commas"}
    )
    public int PrettyDamage = 0;

     @Property(
            type = PropertyType.SWITCH,
            name = "Timestamps",
            description = "Add Chat Timestamps to Messages",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean timestamps = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Zealot Spawn Areas & Spawn Timer",
            description = "Draws a square around the areas that zealots spawn in the end & shows a timer on screen of when the zealots will spawn",
            category = "§1§rFarming",
            subcategory = "Quality of Life"
    )
    public boolean showZealotSpawns = false;

    @Property(
        type = PropertyType.SWITCH,
        name = "Make Zealots Glow",
        description = "Applys the 1.9 glow effect to zealots",
        category = "§1§rFarming",
        subcategory = "Quality of Life"
    )
    public boolean glowingZealots = false;

    @Property(
        type = PropertyType.SWITCH,
        name = "1.18 Crop Hitbox",
        description = "Applys full sized hitbox for crops",
        category = "§1§rFarming",
        subcategory = "Quality of Life"
    )
    public boolean cropBox = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Remove Armor Stands",
            description = "Hides ALL Named Armor Stands, Except those of starred mobs. May slightly increase preformance",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean removeArmorStands = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Air Display",
            description = "Prevents the game from rendering the air bubbles while underwater.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean hideAirDisplay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Fire on Entities",
            description = "Prevents the game from rendering fire on burning entities.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean hideEntityFire = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Jerry Rune",
            description = "Prevents the game from rendering the items spawned by the Jerry rune.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean hideJerryRune = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Lightning",
            description = "Prevents all lightning from rendering.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean hideLightning = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Mob Death Particles",
            description = "Hides the smoke particles created when mobs die.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean hideDeathParticles = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Geyser Particles",
            description = "Hides the annoying particles in the §6Blazing Volcano.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean hideGeyserParticles = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Highlight Geyser Box",
            description = "Creates a box of where the geyser area is in the §6Blazing Volcano",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean geyserBoundingBox = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "No Fire",
            description = "Removes first-person fire overlay when you are burning.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean noFire = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "No Hurtcam",
            description = "Removes the screen shake when you are hurt.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean noHurtcam = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Lowest BIN Price",
            description = "Shows the lowest Buy It Now price for various items in Skyblock.",
            category = "Miscellaneous",
            subcategory = "Auction Utilities"
    )
    public boolean showLowestBINPrice = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Average BIN Price",
            description = "Shows the average Buy It Now price for various items in Skyblock.",
            category = "Miscellaneous",
            subcategory = "Auction Utilities"
    )
    public boolean showAvgLowestBINPrice = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Helpful Auction Guis",
            description = "Shows the extra information about your own and others auctions.",
            category = "Miscellaneous",
            subcategory = "Auction Utilities"
    )
    public boolean auctionGuis = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Minion Overlay",
            description = "Shows the extra information inside the minion gui.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean minionOverlay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Highlight Auctions For Flipping",
            description = "Highlights items that have 100,000 profit or more.",
            category = "Miscellaneous",
            subcategory = "Auction Utilities"
    )
    public boolean highlightAuctionProfit = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Favorite Pets",
            description = "Highlights Favorite Pets",
            category = "Pets",
            subcategory = "Quality of Life"
    )
    public boolean FavoritePets = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Fishing Hooks",
            description = "Hides fishing hooks from other players",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean hideFishingHooks = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Compact Chat",
            description = "Compact Chat allows you to have a cleaner chat by stacking duplicates into a single message.",
            category = "Spam",
            subcategory = "Display"
    )
    public boolean compactChat = false;

     @Property(
            type = PropertyType.SWITCH,
            name = "Ad Blocker",
            description = "Hides auction advertisments in chat",
            category = "Spam",
            subcategory = "Miscellaneous"
    )
    public boolean hideAdvertisments = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "X",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int armX = 0;

     @Property(
            type = PropertyType.SLIDER,
            name = "Y",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int armY = 0;

     @Property(
            type = PropertyType.SLIDER,
            name = "Z",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int armZ = 0;

    public Config() {
        super(new File("./config/skyblockfeatures/config.toml"));
        initialize();
    }
}
