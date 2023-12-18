package mrfast.sbf.core;

import mrfast.sbf.features.dungeons.TrashHighlighter;

import java.awt.*;

public class Config extends ConfigManager {
    @Property(
            type = PropertyType.NUMBER,
            name = "Times Game Restarted",
            description = "",
            category = "General",
            subcategory = "Reparty",
            hidden = true
    )
    public int timeStartedUp = 0;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Outdated Version Notification",
            description = "Receive a chat notification when using an outdated version of Skyblock Features",
            category = "§2§rCustomization",
            subcategory = "Mod"
    )
    public boolean updateNotify = true;
    @Property(
            type = PropertyType.DROPDOWN,
            name = "Update Check Type",
            description = "Choose between Full and Beta Releases for update checks",
            category = "§2§rCustomization",
            subcategory = "Mod",
            options = {"Full Releases", "Beta Releases"}
    )
    public int updateCheckType = 0;
    @Property(
            type = PropertyType.TOGGLE,
            name = "§cDeveloper Mode",
            description = "§eDeveloper Mode§r causes more logs to happen, aswell as enabling certain debug features.",
            category = "§2§rCustomization",
            subcategory = "Mod"
    )
    public boolean developerMode = false;
    @Property(
            type = PropertyType.TOGGLE,
            name = "Use At Own Risk Features",
            description = "Toggles whether §cUse At Own Risk§r features will show inside of the config menu",
            category = "§2§rCustomization",
            subcategory = "Mod"
    )
    public boolean riskyFeatures = true;

    @Property(
            type = PropertyType.TOGGLE,
            name = "First Launch",
            description = "Used to see if the user is a new user of skyblockfeatures.",
            category = "General",
            subcategory = "Other",
            hidden = true
    )
    public boolean firstLaunch = true;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Customizable Sidebar",
            description = "Make the sidebar customizable",
            category = "General",
            subcategory = "Sidebar",
            isParent = true
    )
    public boolean customSidebar = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Remove Background",
            description = "Stops the background of the sidebar from rendering",
            category = "General",
            subcategory = "Sidebar",
            parentName = "Customizable Sidebar"
    )
    public boolean removeSidebarBackground = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Red Numbers From Sidebar",
            description = "Hide the red numbers from the sidebar",
            category = "General",
            subcategory = "Sidebar",
            parentName = "Customizable Sidebar"
    )
    public boolean removeSidebarRedNumbers = true;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Draw Text With Shadow",
            description = "Draws the text on the sidebar with a shadow",
            category = "General",
            subcategory = "Sidebar",
            parentName = "Customizable Sidebar"
    )
    public boolean useShadowOnSidebar = true;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Remove Hypixel From sidebar",
            description = "Hide the www.hypixel.net the sidebar bottom",
            category = "General",
            subcategory = "Sidebar",
            parentName = "Customizable Sidebar"
    )
    public boolean hideHypixelFromSidebar = true;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Floor 3 Fire Freeze Timer",
            description = "Shows a timer of when to use the §5Fire Freeze Staff",
            category = "§1§rDungeons",
            subcategory = "Items"
    )
    public boolean fireFreezeTimer = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Box Shadow Assasins",
            description = "Draws a box around invisible shadow assasins when their sword is visible.",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean boxShadowAssasins = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Shadow Assassin Notify",
            description = "Notify when there is a nearby shadow assassin that's invisible based off their sword.",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean shadowAssassinNotify = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Quick Close Chest",
            description = "Press any key or click to close secret chest screen",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean quickCloseChest = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Doors",
            description = "Highlights wither door and blood doors",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean highlightDoors = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Disable Blood Music",
            description = "Stops the music from playing when the blood room is open",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean stopBloodMusic = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Click in order Terminal solver",
            description = "Highlights the correct order for the Click in order terminal.",
            category = "§1§rDungeons",
            subcategory = "Terminal Solvers",
            isParent = true
    )
    public boolean clickInOrderSolver = false;
    @Property(
            type = PropertyType.COLOR,
            name = "Current Color",
            description = "",
            category = "§1§rDungeons",
            subcategory = "Terminal Solvers",
            parentName = "Click in order Terminal solver"
    )
    public Color clickInOrderSolverCurrent = new Color(0, 60, 60);
    @Property(
            type = PropertyType.COLOR,
            name = "Next Color",
            description = "",
            category = "§1§rDungeons",
            subcategory = "Terminal Solvers",
            parentName = "Click in order Terminal solver"
    )
    public Color clickInOrderSolverNext = new Color(0, 140, 140);
    @Property(
            type = PropertyType.COLOR,
            name = "Next Next Color",
            description = "",
            category = "§1§rDungeons",
            subcategory = "Terminal Solvers",
            parentName = "Click in order Terminal solver"
    )
    public Color clickInOrderSolverNext2 = new Color(0, 250, 250);

    @Property(
            type = PropertyType.TOGGLE,
            name = "Blaze Solver",
            description = "Highlights the correct blazes to shoot.",
            category = "§1§rDungeons",
            subcategory = "Solvers"
    )
    public boolean blazeSolver = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Water Board Solver",
            description = "Highlights the correct levers to flip to solve for the water puzzle.",
            category = "§1§rDungeons",
            subcategory = "Solvers"
    )
    public boolean WaterBoardSolver = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Three Weirdo Solver",
            description = "Highlights the correct chest to solve for the riddle puzzle.",
            category = "§1§rDungeons",
            subcategory = "Solvers"
    )
    public boolean ThreeWeirdosSolver = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Teleport Pad Solver",
            description = "Highlights teleport pads that you have stepped on",
            category = "§1§rDungeons",
            subcategory = "Solvers"
    )
    public boolean teleportPadSolver = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Creeper Solver",
            description = "Highlights the lanterns to shoot in Creeper puzzle.",
            category = "§1§rDungeons",
            subcategory = "Solvers"
    )
    public boolean creeperSolver = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Crypt Display",
            description = "Big count of how many crypts have been killed",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean cryptCount = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Blessings Viewer",
            description = "Displays the current blessings in a dungeons",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean blessingViewer = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Bats",
            description = "Draws a box around bats to make bats easier to find",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous",
            isParent = true
    )
    public boolean highlightBats = false;

    @Property(
            type = PropertyType.COLOR,
            name = "Bat Highlight Color",
            description = "",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous",
            parentName = "Highlight Bats"
    )
    public Color highlightBatColor = Color.green;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Gifts",
            description = "Highlights with a box of where gifts are at the Jerry's workshop.",
            category = "§1§rEvents",
            subcategory = "Jerrys Workshop",
            isParent = true
    )
    public boolean presentWaypoints = false;

    @Property(
            type = PropertyType.COLOR,
            name = "Gift Highlight Color",
            description = "",
            category = "§1§rEvents",
            subcategory = "Jerrys Workshop",
            parentName = "Highlight Gifts"
    )
    public Color presentWaypointsColor = Color.yellow;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Gifts To You",
            description = "Highlights gifts that are given to you",
            category = "§1§rEvents",
            subcategory = "Jerrys Workshop",
            isParent = true
    )
    public boolean highlightSelfGifts = false;

    @Property(
            type = PropertyType.COLOR,
            name = "Gift Color§1§r",
            description = "",
            category = "§1§rEvents",
            subcategory = "Jerrys Workshop",
            parentName = "Highlight Gifts To You"
    )
    public Color selfGiftHighlightColor = Color.red;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Glacial Cave Treasure Finder",
            description = "Highlights ice treasures in the wall when inside the Glacial Cave",
            category = "§1§rEvents",
            subcategory = "Glacial Cave",
            isParent = true
    )
    public boolean icecaveHighlight = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Ice Treasure Through Walls",
            description = "§cWarning Use At Own Risk",
            category = "§1§rEvents",
            subcategory = "Glacial Cave",
            parentName = "Glacial Cave Treasure Finder",
            risky = true
    )
    public boolean icecaveHighlightWalls = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Gifting Info",
            description = "Displays your current count of unique gifts given along with the corresponding milestone achieved",
            category = "§1§rEvents",
            subcategory = "Jerrys Workshop"
    )
    public boolean showGiftingInfo = false;

    @Property(
            type = PropertyType.NUMBER,
            name = "Unique Gifts Given",
            description = "",
            category = "§1§rEvents",
            subcategory = "Jerrys Workshop",
            hidden = true
    )
    public int uniqueGiftsGiven = 0;
    @Property(
            type = PropertyType.NUMBER,
            name = "December Hypixel Winter Events",
            description = "Tracks the year that the event was happening last, and if its different than this year than reset",
            category = "§1§rEvents",
            subcategory = "Jerrys Workshop",
            hidden = true
    )
    public int winterYear = 0;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Glacial Cave Treasure Tracker",
            description = "Tracks the items you get from ice treasures",
            category = "§1§rEvents",
            subcategory = "Jerrys Workshop"
    )
    public boolean IceTreasureTracker = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Other Player Gifts",
            description = "Stops other players gifts from rendering if not given to you",
            category = "§1§rEvents",
            subcategory = "Jerrys Workshop"
    )
    public boolean hideOtherGifts = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Gift Particles",
            description = "Stops particles from gifts from rendering.",
            category = "§1§rEvents",
            subcategory = "Jerrys Workshop"
    )
    public boolean hideGiftParticles = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Slayers",
            description = "Shows a glow effect on summoned slayers.",
            category = "Slayers",
            subcategory = "Highlight Slayers",
            isParent = true
    )
    public boolean highlightSlayers = false;
    @Property(
            type = PropertyType.COLOR,
            name = "Slayer Color",
            description = "",
            category = "Slayers",
            subcategory = "Highlight Slayers",
            parentName = "Highlight Slayers"
    )
    public Color highlightSlayerColor = Color.orange;
    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Mini-bosses",
            description = "Highlights spawned mini-bosses with a glowing effect",
            category = "Slayers",
            subcategory = "Highlight Slayers",
            parentName = "Highlight Slayers"
    )
    public boolean highlightSlayerMiniboss = false;
    @Property(
            type = PropertyType.COLOR,
            name = "Miniboss Color",
            description = "",
            category = "Slayers",
            subcategory = "Highlight Slayers",
            parentName = "Highlight Slayers"
    )
    public Color highlightSlayerMinibossColor = Color.green;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Voidgloom Stage Colors",
            description = "Changes the glow effect depending the stage of the voidgloom. §cRequires Highlight Slayers to be enabled!",
            category = "Slayers",
            subcategory = "Highlight Slayers",
            isParent = true
    )
    public boolean highlightVoidgloomColors = false;

    @Property(
            type = PropertyType.COLOR,
            name = "Hit Phase Color",
            description = "",
            category = "Slayers",
            subcategory = "Highlight Slayers",
            parentName = "Highlight Voidgloom Stage Colors"
    )
    public Color highlightVoidgloomHitPhase = Color.MAGENTA;

    @Property(
            type = PropertyType.COLOR,
            name = "Laser Phase Color",
            description = "",
            category = "Slayers",
            subcategory = "Highlight Slayers",
            parentName = "Highlight Voidgloom Stage Colors"
    )
    public Color highlightVoidgloomLaserPhase = Color.cyan;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Slayer Timer",
            description = "Shows different timers for slayers including time to spawn and kill.",
            category = "Slayers",
            subcategory = "Slayer Timer"
    )
    public boolean slayerTimer = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Thrown Beacon",
            description = "Highlights the beacon thats thrown by the enderman slayer.",
            category = "Slayers",
            subcategory = "Voidgloom",
            isParent = true
    )
    public boolean highlightBeacons = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Through Walls",
            description = "Highlights the beacon that's thrown by the enderman slayer through walls. §cWarning Use At Own Risk",
            category = "Slayers",
            subcategory = "Voidgloom",
            parentName = "Highlight Thrown Beacon",
            risky = true
    )
    public boolean highlightBeaconsThroughWalls = false;

    @Property(
            type = PropertyType.COLOR,
            name = "Beacon Highlight Color",
            description = "",
            category = "Slayers",
            subcategory = "Voidgloom",
            parentName = "Highlight Thrown Beacon"
    )
    public Color highlightBeaconsColor = Color.green;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Ender Node Tracker",
            description = "Tracks the items you get from ender nodes",
            category = "§1§rThe End",
            subcategory = "Trackers"
    )
    public boolean EnderNodeTracker = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Day Tracker",
            description = "Tracks the day in the Crystal Hollows",
            category = "Mining",
            subcategory = "Trackers"
    )
    public boolean dayTracker = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Ender Nodes",
            description = "Highlights the sparkly blocks in the end",
            category = "§1§rThe End",
            subcategory = "Mining",
            isParent = true
    )
    public boolean highlightEnderNodes = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Nodes Through the walls",
            description = "Makes the Ender Node Highlight go through walls. §cWarning Use At Own Risk",
            category = "§1§rThe End",
            subcategory = "Mining",
            parentName = "Highlight Ender Nodes",
            risky = true
    )
    public boolean highlightEnderNodesWalls = false;

    @Property(
            type = PropertyType.COLOR,
            name = "Endstone Node Color",
            description = "",
            category = "§1§rThe End",
            subcategory = "Mining",
            parentName = "Highlight Ender Nodes"
    )
    public Color highlightEnderNodesEndstoneColor = Color.magenta;

    @Property(
            type = PropertyType.COLOR,
            name = "Obsidian Node Color",
            description = "",
            category = "§1§rThe End",
            subcategory = "Mining",
            parentName = "Highlight Ender Nodes"
    )
    public Color highlightEnderNodesObiColor = new Color(0x4f024f);

    @Property(
            type = PropertyType.TOGGLE,
            name = "Dungeon Chest Profit",
            description = "Shows the estimated profit for items from chests in dungeons.",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean dungeonChestProfit = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Cleaner Action Bar",
            description = "Hides Health, Mana and other attributes from action bar",
            category = "General",
            subcategory = "Health & Mana Bars",
            isParent = true
    )
    public boolean cleanerActionBar = false;
    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Health",
            description = "Hides health from action bar",
            category = "General",
            subcategory = "Health & Mana Bars",
            parentName = "Cleaner Action Bar"
    )
    public boolean hideHealthFromBar = true;
    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Mana",
            description = "Hides mana from action bar",
            category = "General",
            subcategory = "Health & Mana Bars",
            parentName = "Cleaner Action Bar"
    )
    public boolean hideManaFromBar = true;
    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Overflow Mana",
            description = "Hides overflow mana from action bar",
            category = "General",
            subcategory = "Health & Mana Bars",
            parentName = "Cleaner Action Bar"
    )
    public boolean hideOverflowManaFromBar = true;
    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Defense",
            description = "Hides defense from action bar",
            category = "General",
            subcategory = "Health & Mana Bars",
            parentName = "Cleaner Action Bar"
    )
    public boolean hideDefenseFromBar = true;
    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Dungeon Secrets",
            description = "Hides secrets from action bar",
            category = "General",
            subcategory = "Health & Mana Bars",
            parentName = "Cleaner Action Bar"
    )
    public boolean hideSecretsFromBar = true;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Health Display",
            description = "Movable health display",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean HealthDisplay = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Speed Display",
            description = "Movable Speed display",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean SpeedDisplay = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Effective Health Display",
            description = "Movable Effective Health display",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean EffectiveHealthDisplay = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Mana Display",
            description = "Movable mana",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean ManaDisplay = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Defense Display",
            description = "Movable defense display",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean DefenseDisplay = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Secrets Display",
            description = "Movable Secrets display",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean SecretsDisplay = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Better Party Finder Menu",
            description = "Creates a better user interface for the dungeon party finder",
            category = "§1§rDungeons",
            subcategory = "Party Finder",
            isParent = true
    )
    public boolean betterPartyFinder = false;
    @Property(
            type = PropertyType.TOGGLE,
            name = "Side Menu",
            description = "Displays the hovered party info in a separate area",
            category = "§1§rDungeons",
            subcategory = "Party Finder",
            parentName = "Better Party Finder Menu"
    )
    public boolean betterPartyFinderSideMenu = true;
    @Property(
            type = PropertyType.TOGGLE,
            name = "Block Carries",
            description = "Ignore parties that are dungeon carries",
            category = "§1§rDungeons",
            subcategory = "Party Finder",
            parentName = "Better Party Finder Menu"
    )
    public boolean betterPartyFinderNoCarry = true;
    @Property(
            type = PropertyType.TOGGLE,
            name = "Block Dupes",
            description = "Ignore parties that already have a player of your class",
            category = "§1§rDungeons",
            subcategory = "Party Finder",
            parentName = "Better Party Finder Menu"
    )
    public boolean betterPartyFinderNoDupe = true;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Dungeon Party Display",
            description = "Shows who all is in your dungeon party, including class and class lvl",
            category = "§1§rDungeons",
            subcategory = "Party Finder",
            isParent = true
    )
    public boolean dungeonPartyDisplay = false;
    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Duplicate Class",
            description = "Highlight Duplicate Classes in the Dungeon Party Display",
            category = "§1§rDungeons",
            subcategory = "Party Finder",
            parentName = "Dungeon Party Display"
    )
    public boolean dungeonPartyDisplayDupes = true;
    @Property(
            type = PropertyType.TOGGLE,
            name = "Party Finder Join Info",
            description = "Shows stats of players when they join such as, avg secrets, cata lvl, etc.",
            category = "§1§rDungeons",
            subcategory = "Party Finder"
    )
    public boolean partyFinderJoinMessages = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Non-Starred Mobs",
            description = "Prevents mobs that arent starred from rendering during the dungeon.",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean hideNonStarredMobs = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Starred Mobs",
            description = "Highlights starred mobs in dungeons by making them glowing.",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous",
            isParent = true
    )
    public boolean boxStarredMobs = false;
    @Property(
            type = PropertyType.COLOR,
            name = "Starred Mobs Color",
            description = "",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous",
            parentName = "Highlight Starred Mobs"
    )
    public Color boxStarredMobsColor = new Color(0xFFAA00);

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Correct Livid",
            description = "Highlights the incorrect livid",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous",
            isParent = true
    )
    public boolean highlightCorrectLivid = false;
    @Property(
            type = PropertyType.COLOR,
            name = "Livid Highlight Color",
            description = "",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous",
            parentName = "Highlight Correct Livid"
    )
    public Color correctLividColor = Color.cyan;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Fairy Soul Helper",
            description = "Highlights nearby fairy souls using waypoints",
            category = "Render",
            subcategory = "Highlights",
            isParent = true
    )
    public boolean fairySoulHelper = false;
    @Property(
            type = PropertyType.COLOR,
            name = "§1§rUncollected Color",
            description = "",
            category = "Render",
            subcategory = "Highlights",
            parentName = "Fairy Soul Helper"
    )
    public Color fairySoulUnFound = Color.magenta;
    @Property(
            type = PropertyType.COLOR,
            name = "§1§rCollected Color",
            description = "",
            category = "Render",
            subcategory = "Highlights",
            parentName = "Fairy Soul Helper"
    )
    public Color fairySoulFound = Color.GREEN;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Rift Enigma Soul Helper",
            description = "Highlights nearby Enigma souls using waypoints in the Rift",
            category = "The Rift",
            subcategory = "General",
            isParent = true
    )
    public boolean riftSouls = false;
    @Property(
            type = PropertyType.COLOR,
            name = "Uncollected Color",
            description = "",
            category = "The Rift",
            subcategory = "General",
            parentName = "Rift Enigma Soul Helper"
    )
    public Color riftSoulUnFound = Color.magenta;
    @Property(
            type = PropertyType.COLOR,
            name = "Collected Color",
            description = "",
            category = "The Rift",
            subcategory = "General",
            parentName = "Rift Enigma Soul Helper"
    )
    public Color riftSoulFound = Color.GREEN;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Rift Mirrorverse Helper",
            description = "Solvers for some of the puzzles in the mirrorverse in the Rift",
            category = "The Rift",
            subcategory = "General",
            isParent = true
    )
    public boolean riftMirrorverseHelper = false;

    @Property(
            type = PropertyType.COLOR,
            name = "Main Highlight Color",
            description = "",
            category = "The Rift",
            subcategory = "General",
            parentName = "Rift Mirrorverse Helper"
    )
    public Color riftMirrorverseHelperColor = new Color(0x00FFFF);

    @Property(
            type = PropertyType.TOGGLE,
            name = "Rift Hacking Helper",
            description = "Highlights the correct numbers in the hacking gui",
            category = "The Rift",
            subcategory = "General"
    )
    public boolean riftHackingHelper = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Relic Helper",
            description = "Highlights relics in the §cSpider's Den§r using waypoints",
            category = "General",
            subcategory = "Other"
    )
    public boolean spiderRelicHelper = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Armor Bar",
            description = "Hide the armor icons above health bar",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean hideArmorBar = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Hunger Bar",
            description = "Hide the food icons above hotbar",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean hideHungerBar = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Health Hearts",
            description = "Hide the health icons above health bar",
            category = "General",
            subcategory = "Health & Mana Bars"
    )
    public boolean hideHealthHearts = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Player Nametags",
            description = "Stops player's nametags from renderering",
            category = "Render",
            subcategory = "Hide Things"
    )
    public boolean hidePlayerNametags = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Normal Fullbright",
            description = "Normal classic full bright everywhere",
            category = "Render",
            subcategory = "Fullbright"
    )
    public boolean fullbright = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Dynamic Fullbright",
            description = "Turns on Fullbright in §aCrystal Hollows§r,§aYour Island§r,§aDungeons",
            category = "Render",
            subcategory = "Fullbright",
            isParent = true
    )
    public boolean DynamicFullbright = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Enabled Value",
            description = "Value of brightness to set when in the certain locations",
            category = "Render",
            subcategory = "Fullbright",
            min = 1,
            parentName = "Dynamic Fullbright"
    )
    public int DynamicFullbrightDisabled = 100;

    @Property(
            type = PropertyType.SLIDER,
            name = "Disabled Value",
            description = "Value of brightness to set when everywhere else",
            category = "Render",
            subcategory = "Fullbright",
            min = 1,
            parentName = "Dynamic Fullbright"
    )
    public int DynamicFullbrightElsewhere = 1;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide All Nametags",
            description = "Stops all nametags from renderering",
            category = "Render",
            subcategory = "Hide Things"
    )
    public boolean hideAllNametags = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Players Near NPC's",
            description = "Disables Players from rendering near NPC's",
            category = "Render",
            subcategory = "Hide Things"
    )
    public boolean hidePlayersNearNPC = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Arrows",
            description = "Stops arrows from being rendered.",
            category = "Render",
            subcategory = "Hide Things"
    )
    public boolean hideArrows = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Display Tree Capitator Cooldown",
            description = "Displays the cooldown for the treecapitator",
            category = "Quality of Life",
            subcategory = "Foraging"
    )
    public boolean treecapitatorCooldown = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Display Conjuring Cooldown",
            description = "Displays the cooldown for the Conjuring",
            category = "Quality of Life",
            subcategory = "Dungeons"
    )
    public boolean ConjuringCooldown = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Onscreen Clock",
            description = "Display a clock",
            category = "Miscellaneous",
            subcategory = "Visual"
    )
    public boolean clock = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Jerry Timer",
            description = "Shows the cooldown for spawning jerry's",
            category = "§1§rEvents",
            subcategory = "Mayor Jerry"
    )
    public boolean jerryTimer = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Use Smooth Font",
            description = "Uses a smoother font to render text. §cRequires restart",
            category = "§2§rCustomization",
            subcategory = "§1§rGui"
    )
    public boolean customFont = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "§6§lFurfSky§r Themed",
            description = "Uses §6§lFurfSky§r textures for the Gui",
            category = "§2§rCustomization",
            subcategory = "§1§rGui"
    )
    public boolean furfSkyThemed = false;

    @Property(type = PropertyType.COLOR, name = "Gui Lines", description = "", category = "§2§rCustomization", subcategory = "§1§rGui")
    public Color guiLines = new Color(0x8d8d8d);

    @Property(type = PropertyType.COLOR, name = "Selected Category Text", description = "", category = "§2§rCustomization", subcategory = "§1§rGui")
    public Color selectedCategory = new Color(0x02A9EA);

    @Property(type = PropertyType.COLOR, name = "Hovered Category Text", description = "", category = "§2§rCustomization", subcategory = "§1§rGui")
    public Color hoveredCategory = new Color(0x2CC8F7);

    @Property(type = PropertyType.COLOR, name = "Default Category Text", description = "", category = "§2§rCustomization", subcategory = "§1§rGui")
    public Color defaultCategory = new Color(0xFFFFFF);

    @Property(type = PropertyType.COLOR, name = "Feature Description Text", description = "", category = "§2§rCustomization", subcategory = "§1§rGui")
    public Color featureDescription = new Color(0xbbbbbb);

    @Property(type = PropertyType.COLOR, name = "Main Box Background", description = "", category = "§2§rCustomization", subcategory = "§1§rGui")
    public Color mainBackground = new Color(25, 25, 25, 200);

    @Property(type = PropertyType.COLOR, name = "Search Box Background", description = "", category = "§2§rCustomization", subcategory = "§1§rGui")
    public Color searchBoxBackground = new Color(120, 120, 120, 60);

    @Property(type = PropertyType.COLOR, name = "Edit Gui Text", description = "", category = "§2§rCustomization", subcategory = "§1§rGui")
    public Color editGuiText = new Color(0xFFFFFF);

    @Property(type = PropertyType.COLOR, name = "Title Text", description = "", category = "§2§rCustomization", subcategory = "§1§rGui")
    public Color titleColor = new Color(0x00FFFF);

    @Property(type = PropertyType.COLOR, name = "Version Text", description = "", category = "§2§rCustomization", subcategory = "§1§rGui")
    public Color versionColor = new Color(0xFFFFFF);

    @Property(
            type = PropertyType.TOGGLE,
            name = "Player Disguiser",
            description = "Disguises players as different things",
            category = "§2§rCustomization",
            subcategory = "Player",
            isParent = true
    )
    public boolean playerDiguiser = false;

    @Property(
            type = PropertyType.DROPDOWN,
            name = "Disguise Players As",
            category = "§2§rCustomization",
            subcategory = "Player",
            options = {"Cow", "Pig", "Sheep", "Zombie", "Jerry", "Enderman", "Giant", "Baby Player", "Monki"},
            parentName = "Player Disguiser"
    )
    public int DisguisePlayersAs = 0;

    @Property(
            type = PropertyType.TEXT,
            name = "Player Cape",
            category = "§2§rCustomization",
            description = "Paste a image url to give yourself a cape!\n§aEx. https://i.imgur.com/wHk1W6X.png (This is only visible to you)",
            subcategory = "Player"
    )
    public String playerCapeURL = "";

    @Property(
            type = PropertyType.TOGGLE,
            name = "Diana Mythological Helper",
            description = "Draw an extended line of where the Mythological burrow could be",
            category = "§1§rEvents",
            subcategory = "Diana",
            isParent = true

    )
    public boolean MythologicalHelper = false;

    @Property(
            type = PropertyType.COLOR,
            name = "Actual Line Color",
            description = "",
            category = "§1§rEvents",
            subcategory = "Diana",
            parentName = "Diana Mythological Helper"
    )
    public Color MythologicalHelperActualColor = new Color(255, 85, 85);
    @Property(
            type = PropertyType.COLOR,
            name = "Prediction Line Color",
            description = "",
            category = "§1§rEvents",
            subcategory = "Diana",
            parentName = "Diana Mythological Helper"
    )
    public Color MythologicalHelperPredictionColor = Color.WHITE;
    @Property(
            type = PropertyType.COLOR,
            name = "Next Burrow Line Color",
            description = "",
            category = "§1§rEvents",
            subcategory = "Diana",
            parentName = "Diana Mythological Helper"
    )
    public Color MythologicalHelperNextColor = Color.CYAN;
    @Property(
            type = PropertyType.COLOR,
            name = "Default Burrow Color",
            description = "",
            category = "§1§rEvents",
            subcategory = "Diana",
            parentName = "Diana Mythological Helper"
    )
    public Color MythologicalHelperDefaultColor = Color.GREEN;
    @Property(
            type = PropertyType.COLOR,
            name = "Mob Burrow Color",
            description = "",
            category = "§1§rEvents",
            subcategory = "Diana",
            parentName = "Diana Mythological Helper"
    )
    public Color MythologicalHelperMobColor = Color.RED;
    @Property(
            type = PropertyType.COLOR,
            name = "Treasure Burrow Color",
            description = "",
            category = "§1§rEvents",
            subcategory = "Diana",
            parentName = "Diana Mythological Helper"
    )
    public Color MythologicalHelperTreasureColor = new Color(0xFFAA00);

    @Property(
            type = PropertyType.TOGGLE,
            name = "Crop Counter",
            description = "Shows the amount of crops on the hoe your holding",
            category = "§1§rFarming",
            subcategory = "Garden"
    )
    public boolean Counter = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Pests",
            description = "Shows where pests are in your garden",
            category = "§1§rFarming",
            subcategory = "Garden",
            isParent = true
    )
    public boolean highlightPests = false;
    @Property(
            type = PropertyType.COLOR,
            name = "Pest Highlight Color",
            description = "",
            category = "§1§rFarming",
            subcategory = "Garden",
            parentName = "Highlight Pests"
    )
    public Color highlightPestColor = Color.red;
    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Pests Through Walls",
            description = "§cWarning Use At Own Risk",
            category = "§1§rFarming",
            subcategory = "Garden",
            parentName = "Highlight Pests",
            risky = true
    )
    public boolean highlightPestThroughWalls = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Composter Overlay",
            description = "Shows extra info inside of the composter menu",
            category = "§1§rFarming",
            subcategory = "Garden"
    )
    public boolean composterOverlay = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Trevor The Trapper Helper",
            description = "Shows the biome and location of the hunted mob",
            category = "General",
            subcategory = "Other"
    )
    public boolean trevorHelper = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Auto Accept Reparty",
            description = "Auto joins part when someone does reparty",
            category = "Quality of Life",
            subcategory = "Dungeons"
    )
    public boolean autoAcceptReparty = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Auto Reparty",
            description = "Auto does reparty when the dungeon ends",
            category = "Quality of Life",
            subcategory = "Dungeons"
    )
    public boolean autoReparty = false;
    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Far Entitys in hub",
            description = "",
            category = "Render",
            subcategory = "Hide Things"
    )
    public boolean HideFarEntity = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Damage Tint",
            description = "Makes your screen get more red the lower in health you are",
            category = "Quality of Life",
            subcategory = "Visual"
    )
    public boolean damagetint = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "NameTags",
            description = "Render better nametags in dungeons",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean NameTags = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Trash",
            description = "Draws a red box around items that just fill up your inventory.",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous",
            isParent = true
    )
    public boolean highlightTrash = false;

    @Property(
            type = PropertyType.BUTTON,
            name = "§eEdit Trash",
            description = "The trash list will be updated once you save the file. \nTrash is item whos Skyblock ID contains any of the entries.",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous",
            placeholder = "§cEdit Trash",
            parentName = "Highlight Trash"
    )
    public Runnable editTrash = TrashHighlighter::openTrashFile;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Dungeon Map",
            description = "Render a moveable dungeon map on screen",
            category = "§1§rDungeons",
            subcategory = "Dungeon Map",
            isParent = true
    )
    public boolean dungeonMap = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Player Head Scale",
            description = "Scale the size of the heads on the dungeon map §3(Percent)",
            category = "§1§rDungeons",
            subcategory = "Dungeon Map",
            min = 50,
            max = 150,
            parentName = "Dungeon Map"
    )
    public int dungeonMapHeadScale = 100;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Center Player on Dungeon Map",
            description = "Locks your player to the center of the dungeon map",
            category = "§1§rDungeons",
            subcategory = "Dungeon Map",
            parentName = "Dungeon Map"
    )
    public boolean dungeonMapCenter = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Render Player Names",
            description = "Draws names above the players",
            category = "§1§rDungeons",
            subcategory = "Dungeon Map",
            parentName = "Dungeon Map"
    )
    public boolean dungeonMapPlayerNames = true;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Blood Door Highlight",
            description = "Marks the players name red if they opened the blood door",
            category = "§1§rDungeons",
            subcategory = "Dungeon Map",
            parentName = "Dungeon Map"
    )
    public boolean dungeonMapBloodGuy = true;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Render Player Heads",
            description = "Adds an outline the the player heads on the dungeon map",
            category = "§1§rDungeons",
            subcategory = "Dungeon Map",
            parentName = "Dungeon Map"
    )
    public boolean dungeonMapHeads = true;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Quick Start",
            description = "Sends a chat message at the end of a dungeon that can be used to reparty or warp out of a dungeon",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean quickStart = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Glowing Dungeon Teammates!",
            description = "Make your teamates glow based on there class in dungeons. §cSignificant performance impact.",
            category = "Render",
            subcategory = "1.9 Glow Effect"
    )
    public boolean glowingDungeonPlayers = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Glowing Items!",
            description = "Make items glow depending on rarity. (Requires Fast render to be off) §cSignificant performance impact.",
            category = "Render",
            subcategory = "1.9 Glow Effect"
    )
    public boolean glowingItems = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hidden Jerry Alert",
            description = "Displays an alert when you find a hidden Jerry.",
            category = "§1§rEvents",
            subcategory = "Mayor Jerry"
    )
    public boolean hiddenJerryAlert = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Treasure Chest Solver",
            description = "Highlights the particles to look at when opening a treasure chest.",
            category = "Mining",
            subcategory = "Solvers"
    )
    public boolean treasureChestSolver = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Automaton Loot Tracker",
            description = "Tracks the loot from Automatons. Starts after a Automaton is killed",
            category = "Mining",
            subcategory = "Trackers"
    )
    public boolean AutomatonTracker = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Gemstone Tracker",
            description = "Tracks the stats from mining gemstones like Coins per hour",
            category = "Mining",
            subcategory = "Trackers"
    )
    public boolean gemstoneTracker = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Ghost Loot Tracker",
            description = "Tracks the loot gained from killing Ghosts",
            category = "Mining",
            subcategory = "Trackers"
    )
    public boolean ghostTracker = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Powder Mining Tracker",
            description = "Tracks the stats from mining gemstones like Coins per hour",
            category = "Mining",
            subcategory = "Trackers"
    )
    public boolean PowderTracker = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Commissions Tracker",
            description = "Tracks your progress on commissions",
            category = "Quality of Life",
            subcategory = "Mining"
    )
    public boolean CommisionsTracker = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Placed Cobblestone",
            description = "Highlights the cobblestone you place in crystal hollows",
            category = "Quality of Life",
            subcategory = "Mining",
            isParent = true
    )
    public boolean highlightCobblestone = false;
    @Property(
            type = PropertyType.COLOR,
            name = "Cobblestone Color",
            description = "",
            category = "Quality of Life",
            subcategory = "Mining",
            parentName = "Highlight Placed Cobblestone"
    )
    public Color highlightCobblestoneColor = Color.cyan;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Crystal Hollows Map",
            description = "Show a map of the crystal hollows",
            category = "Mining",
            subcategory = "Map",
            isParent = true
    )
    public boolean CrystalHollowsMap = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Crystal Hollows Map Heads",
            description = "Show a heads instead of a marker on the crystal hollows map",
            category = "Mining",
            subcategory = "Map",
            parentName = "Crystal Hollows Map"
    )
    public boolean CrystalHollowsMapHeads = true;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Breadcrumb Trail On Map",
            description = "Show a trail of where you have been",
            category = "Mining",
            subcategory = "Map",
            parentName = "Crystal Hollows Map"
    )
    public boolean CrystalHollowsMapTrail = true;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Dwarven Mines Map",
            description = "Show a map of the dwarven map",
            category = "Mining",
            subcategory = "Map"
    )
    public boolean dwarvenMinesMap = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Mines of Divan Metal Detector Solver",
            description = "Shows where the treasure chest is in the Mines of Divan",
            category = "Mining",
            subcategory = "Solvers"
    )
    public boolean MetalDetectorSolver = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show NPC Sell Price",
            description = "Shows the NPC Sell Price on certain items.",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showNPCSellPrice = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Crimson Isles Map",
            description = "Show a map of the Crimson Isles",
            category = "Quality of Life",
            subcategory = "Crimson Isle"
    )
    public boolean crimsonsIslesMap = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Skyblock Item ID",
            description = "Shows an items skyblock ID in the lore.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean showSkyblockID = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Fire Veil Timer",
            description = "Shows the time until the fire viel ability ends.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean fireVeilTimer = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Prehistoric Egg Distance Counter",
            description = "Shows the blocks walked on the prehistoric egg item",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean prehistoricEggDistance = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show teleport overlay",
            description = "Highlights the block that your teleporting to with Aspect of the End or Aspect of the Void",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean teleportDestination = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Cooldown Display",
            description = "Shows a display with your hotbar items cooldowns.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean cooldownDisplay = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Chat Timestamps",
            description = "Add Chat Timestamps to Messages",
            category = "Quality of Life",
            subcategory = "Visual"
    )
    public boolean timestamps = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Enchanting Solvers",
            description = "Solvers for ultrasequencer and chronomotron",
            category = "Quality of Life",
            subcategory = "Solvers"
    )
    public boolean enchantingSolvers = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide White Square",
            description = "Hide the hover highlight Square in inventories",
            category = "Quality of Life",
            subcategory = "Visual"
    )
    public boolean hideWhiteSquare = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Zealot Spawn Areas & Spawn Timer",
            description = "Draws where zealots spawn and when zealots will spawn. (this includes bruisers)",
            category = "§1§rThe End",
            subcategory = "Zealots"
    )
    public boolean showZealotSpawnAreas = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Advanced Dragon Hitbox",
            description = "Draws a better hitbox for dragons. Useful for §cMaster Mode 7§r and §eDragons",
            category = "Render",
            subcategory = "Highlights",
            isParent = true
    )
    public boolean advancedDragonHitbox = false;

    @Property(
            type = PropertyType.COLOR,
            name = "Dragon Hitbox Color",
            description = "",
            category = "Render",
            subcategory = "Highlights",
            parentName = "Show Advanced Dragon Hitbox"
    )
    public Color advancedDragonHitboxColor = Color.green;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Garden Visitor Overlay",
            description = "Shows the extra information inside the Garden Visitor Gui.",
            category = "§1§rFarming",
            subcategory = "Garden"
    )
    public boolean GardenVisitorOverlay = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Blocks to Destroy Overlay",
            description = "Shows the blocks needed to destroy when clearing a plot in the garden.",
            category = "§1§rFarming",
            subcategory = "Garden"
    )
    public boolean GardenBlocksToRemove = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Make Zealots Glow",
            description = "Applys the 1.9 glow effect to zealots to make them glow and shiny. §cSignificant performance impact.",
            category = "§1§rThe End",
            subcategory = "Zealots",
            isParent = true
    )
    public boolean glowingZealots = false;

    @Property(
            type = PropertyType.COLOR,
            name = "Zealot Color",
            description = "",
            category = "§1§rThe End",
            subcategory = "Zealots",
            parentName = "Make Zealots Glow"
    )
    public Color glowingZealotsColor = Color.MAGENTA;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Glowing Mushrooms",
            description = "Highlights glowing mushrooms in the Glowing Mushroom Cave",
            category = "§1§rFarming",
            subcategory = "Glowing Mushroom Cave",
            isParent = true
    )
    public boolean highlightMushrooms = false;

    @Property(
            type = PropertyType.COLOR,
            name = "Mushroom Highlight Color",
            description = "",
            category = "§1§rFarming",
            subcategory = "Glowing Mushroom Cave",
            parentName = "Highlight Glowing Mushrooms"
    )
    public Color highlightMushroomsColor = Color.green;

    @Property(
            type = PropertyType.TOGGLE,
            name = "1.12 Crop Hitbox",
            description = "Applys full sized hitbox for crops",
            category = "§1§rFarming",
            subcategory = "Garden"
    )
    public boolean cropBox = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Air Display",
            description = "Prevents the game from rendering the air bubbles while underwater.",
            category = "Quality of Life",
            subcategory = "Visual"
    )
    public boolean hideAirDisplay = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Hide Geyser Particles",
            description = "Hides the annoying particles in the §6Blazing Volcano.",
            category = "Render",
            subcategory = "Hide Things"
    )
    public boolean hideGeyserParticles = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Geyser Box",
            description = "Creates a box of where the geyser area is in the §6Blazing Volcano",
            category = "Render",
            subcategory = "Highlights"
    )
    public boolean geyserBoundingBox = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "No Fire",
            description = "Removes first-person fire overlay when you are burning.",
            category = "Render",
            subcategory = "Hide Things"
    )
    public boolean noFire = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "No Hurtcam",
            description = "Removes the screen shake when you are hurt.",
            category = "Quality of Life",
            subcategory = "Visual"
    )
    public boolean noHurtcam = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Lowest BIN Price",
            description = "Shows the lowest Buy It Now price for various items in Skyblock.",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showLowestBINPrice = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Price Paid",
            description = "Shows the price you bought an item for.",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showPricePaid = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Bazaar Price",
            description = "Shows the bazaar price for various items in Skyblock.",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showBazaarPrice = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Sales Per Day",
            description = "Shows the sales per day for various items in Skyblock.",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showSalesPerDay = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Estimated Price",
            description = "Shows the estimated price for various items in Skyblock. Calculates using things like enchants and stars",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showEstimatedPrice = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Average BIN Price",
            description = "Shows the average Buy It Now price for various items in Skyblock.",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showAvgLowestBINPrice = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Helpful Auction Guis",
            description = "Shows the extra information about your own and others auctions.",
            category = "§1§rAuction house",
            subcategory = "Auction Utils"
    )
    public boolean auctionGuis = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Condense Item Price Info",
            description = "Only shows the things like Average BIN, Lowest BIN, Sales/Day when the Shift key is held",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showPriceInfoOnShift = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Minion Overlay",
            description = "Shows the extra information inside the minion gui.",
            category = "Miscellaneous",
            subcategory = "Overlay"
    )
    public boolean minionOverlay = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Quiver Overlay",
            description = "Shows the arrows in currently your quiver. §cThis will also estimate the count after arrows are shot",
            category = "Miscellaneous",
            subcategory = "Overlay",
            isParent = true
    )
    public boolean quiverOverlay = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Only show when holding bow",
            description = "",
            category = "Miscellaneous",
            subcategory = "Overlay",
            parentName = "Quiver Overlay"
    )
    public boolean quiverOverlayOnlyBow = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Quiver Overlay Count",
            description = "Shows the arrows in currently your quiver. §cThis will also estimate the count after arrows are shot",
            category = "Miscellaneous",
            subcategory = "Overlay",
            hidden = true
    )
    public int quiverOverlayCount = 0;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Collections Leaderboard Overlay",
            description = "Shows a leaderboard for the collection types",
            category = "Miscellaneous",
            subcategory = "Overlay"
    )
    public boolean collecitonsLeaderboard = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "SBF Trade Gui",
            description = "Shows extra information inside the trade gui, including estimated prices",
            category = "Miscellaneous",
            subcategory = "Overlay"
    )
    public boolean tradeOverlay = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Missing Accessories",
            description = "Shows a list of what talismans your missing when in your accessory bag",
            category = "Miscellaneous",
            subcategory = "Overlay"
    )
    public boolean showMissingAccessories = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Extra Profile Info",
            description = "Shows a a players networth,discord,weight, and skill avg when you right click on someone",
            category = "Miscellaneous",
            subcategory = "Overlay"
    )
    public boolean extraProfileInfo = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Auctions For Flipping",
            description = "Highlights auctions that have 100,000 profit or more.",
            category = "§1§rAuction house",
            subcategory = "Auction Utils"
    )
    public boolean highlightAuctionProfit = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Highlight Losing Auctions Red",
            description = "Highlights auctions that you arent winning",
            category = "§1§rAuction house",
            subcategory = "Auction Utils"
    )
    public boolean highlightlosingAuction = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Flipper Active",
            description = "Enables or disables the flipper with its current settings.\n§cDo not put 100% trust in the mod, it can and probably will make mistakes.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public boolean aucFlipperEnabled = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Include BIN Flips",
            description = "Check BINs for flips. §cThis is risky you need to know what your doing.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public boolean aucFlipperBins = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Include Auction Flips",
            description = "Check auctions for flips",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public boolean aucFlipperAucs = true;

    @Property(
            type = PropertyType.NUMBER,
            name = "Profit Margin",
            description = "The minimum amount of profit for an auction to be shown to you.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public int autoAuctionFlipMargin = 200000;

    @Property(
            type = PropertyType.NUMBER,
            name = "Minimum Volume",
            description = "The minimum amount of sales per day for an auction to be shown to you.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public int autoAuctionFlipMinVolume = 1;

    @Property(
            type = PropertyType.NUMBER,
            name = "Minimum Flip Percent",
            description = "The minimum percent of profit from an auction to be shown to you.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public int autoAuctionFlipMinPercent = 5;

    @Property(
            type = PropertyType.NUMBER,
            name = "Max Amount Of Auctions",
            description = "The max amount of flips to be show to you, this will prevent lag.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public int autoAuctionFlipMaxAuc = 50;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Make Purse Max Amount",
            description = "Make the amount of money you can spend on an auction equal to your purse.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public boolean autoAuctionFlipSetPurse = false;

//    @Property(
//        type = PropertyType.TOGGLE,
//        name = "Change Item Estimation",
//        description = "Include stars and enchants into item value estimation. §cNot 100% accurate",
//        category = "§1§rAuction Flipper",
//        subcategory = "Flipper Settings"
//    )
//    public boolean autoFlipAddEnchAndStar = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Refresh Countdown",
            description = "Show the countdown till refreshing.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public boolean autoAuctionFlipCounter = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Auto Open",
            description = "Opens up the bid menu for the item with the highest profit. \n§cThis is slower than holding down key",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public boolean autoAuctionFlipOpen = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Easy Auction Buying",
            description = "By spam clicking you will auto buy/bid the item from that is currently viewed.",
            category = "§1§rAuction house",
            subcategory = "Auction Utils"
    )
    public boolean autoAuctionFlipEasyBuy = false;

//   Auto Auction Filters

    @Property(
            type = PropertyType.CHECKBOX,
            name = "Filter Out Pets",
            description = "Filters out pets from Auto Flipper",
            category = "§1§rAuction Flipper",
            subcategory = "§1§rAuction Flipper Filter"
    )
    public boolean autoAuctionFilterOutPets = false;

    @Property(
            type = PropertyType.CHECKBOX,
            name = "Filter Out Skins",
            description = "Filters out minion skins, armor skins, and pet skins from Auto Flipper",
            category = "§1§rAuction Flipper",
            subcategory = "§1§rAuction Flipper Filter"
    )
    public boolean autoAuctionFilterOutSkins = false;

    @Property(
            type = PropertyType.CHECKBOX,
            name = "Filter Out Furniture",
            description = "Filters out furniture from Auto Flipper",
            category = "§1§rAuction Flipper",
            subcategory = "§1§rAuction Flipper Filter"
    )
    public boolean autoAuctionFilterOutFurniture = false;

    @Property(
            type = PropertyType.CHECKBOX,
            name = "Filter Out Dyes",
            description = "Filters out dyes from Auto Flipper",
            category = "§1§rAuction Flipper",
            subcategory = "§1§rAuction Flipper Filter"
    )
    public boolean autoAuctionFilterOutDyes = false;

    @Property(
            type = PropertyType.CHECKBOX,
            name = "Filter Out Runes",
            description = "Filters out runes from Auto Flipper",
            category = "§1§rAuction Flipper",
            subcategory = "§1§rAuction Flipper Filter"
    )
    public boolean autoAuctionFilterOutRunes = false;

    @Property(
            type = PropertyType.TEXT,
            name = "Blacklist",
            description = "Filters out any blacklisted items. Seperate with §a;§r.§aExample: 'bonemerang;stick'",
            category = "§1§rAuction Flipper",
            subcategory = "§1§rAuction Flipper Filter"
    )
    public String autoAuctionBlacklist = "bonemerang;soldier;jungle pick;";

    @Property(
            type = PropertyType.TOGGLE,
            name = "Granda Wolf Pet Combo Timer",
            description = "Shows time until your combo expires on the Grandma Wolf Pet",
            category = "General",
            subcategory = "Pets"
    )
    public boolean GrandmaWolfTimer = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Ad Blocker",
            description = "Hides auction/lowballing advertisments in chat",
            category = "General",
            subcategory = "Other"
    )
    public boolean hideAdvertisments = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Small Items",
            description = "Makes the items you hold smaller",
            category = "General",
            subcategory = "Other"
    )
    public boolean smallItems = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Auto Party Chat",
            description = "Auto sends §a/chat p§r after joining a party §cWarning Use At Own Risk",
            category = "General",
            subcategory = "Other",
            risky = true
    )
    public boolean autoPartyChat = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "toggle sprint",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public boolean toggleSprint = false;

    @Property(
            type = PropertyType.NUMBER,
            name = "gMaWolf5Second",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int gMaWolf5Second = 0;

    @Property(
            type = PropertyType.NUMBER,
            name = "gMaWolf10Second",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int gMaWolf10Second = 0;

    @Property(
            type = PropertyType.NUMBER,
            name = "gMaWolf15Second",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int gMaWolf15Second = 0;

    @Property(
            type = PropertyType.NUMBER,
            name = "gMaWolf20Second",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int gMaWolf20Second = 0;

    @Property(
            type = PropertyType.NUMBER,
            name = "gMaWolf25Second",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int gMaWolf25Second = 0;

    @Property(
            type = PropertyType.NUMBER,
            name = "gMaWolf30Second",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int gMaWolf30Second = 0;

    @Property(
            type = PropertyType.NUMBER,
            name = "composterSpeedLvl",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int speedLvl = 0;

    @Property(
            type = PropertyType.NUMBER,
            name = "composterMultiLvl",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int multiLvl = 0;

    @Property(
            type = PropertyType.NUMBER,
            name = "composterFuelLvl",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int fuelLvl = 0;

    @Property(
            type = PropertyType.NUMBER,
            name = "composterOrgLvl",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int orgLvl = 0;

    @Property(
            type = PropertyType.NUMBER,
            name = "composterCostLvl",
            description = "",
            category = "General",
            subcategory = "Hidden",
            hidden = true
    )
    public int costLvl = 0;
    @Property(
            type = PropertyType.TEXT,
            name = "temporaryAuthKey",
            category = "General",
            description = "",
            subcategory = "Hidden",
            hidden = true
    )
    public String temporaryAuthKey = "";

    @Property(
            type = PropertyType.TEXT,
            name = "Mod API Url",
            category = "§eDeveloper",
            description = "§cDo not change this if you do not know what your doing!",
            subcategory = "Settings"
    )
    public String modAPIURL = "https://app.mrfast-developer.com/";

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show mob ids",
            category = "§eDeveloper",
            description = "Shows skyblock mob ids on mobs in the world using Skyblock Mob Detector",
            subcategory = "Settings"
    )
    public boolean showMobIds = false;

    @Property(
            type = PropertyType.TOGGLE,
            name = "Show Inspector in Guis",
            category = "§eDeveloper",
            description = "",
            subcategory = "Settings"
    )
    public boolean showInspector = false;


}
