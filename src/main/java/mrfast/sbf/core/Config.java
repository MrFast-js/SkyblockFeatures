package mrfast.sbf.core;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.*;
import mrfast.sbf.SkyblockFeatures;

import java.awt.Color;
import java.io.File;

public class Config extends Vigilant {
    @Property(
            type = PropertyType.SLIDER,
            name = "Times Game Restarted",
            description = "",
            category = "General",
            subcategory = "Reparty",
            hidden = true,
            max = 100000
    )
    public int timeStartedUp = 0;

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
            name = "Hide Red Numbers From Sidebar",
            description = "Hide the red numbers from the sidebar",
            category = "General",
            subcategory = "Sidebar"
    )
    public boolean hideRedNumbers = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Draw Text With Shadow",
            description = "Draws the text on the sidebar with a shadow",
            category = "General",
            subcategory = "Sidebar"
    )
    public boolean drawTextWithShadow = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Remove Hypixel From sidebar",
            description = "Hide the www.hypixel.net the sidebar bottom",
            category = "General",
            subcategory = "Sidebar"
    )
    public boolean hideHypixelSidebar = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Box Shadow Assasins",
            description = "Draws a box around invisible shadow assasins when their sword is visible.",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean boxShadowAssasins = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Shadow Assassin Notify",
            description = "Notify when there is a nearby shadow assasin thats invisible based off their sword.",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean shadowAssassinNotify = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Quick Close Chest",
            description = "Press any key or click to close secret chest screen",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean quickCloseChest = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Highlight Doors",
            description = "Highlights wither door and blood doors",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean highlightDoors = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Disable Blood Music",
            description = "Stops the music from playing when the blood room is open",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean stopBloodMusic = false;

    @Property(
        type = PropertyType.SWITCH,
        name = "Blaze Solver",
        description = "Highlights the correct blazes to shoot.",
        category = "§1§rDungeons",
        subcategory = "Solvers"
    )
    public boolean blazeSolver = false;

    @Property(
        type = PropertyType.SWITCH,
        name = "Water Board Solver",
        description = "Highlights the correct levers to flip to solve for the water puzzle.",
        category = "§1§rDungeons",
        subcategory = "Solvers"
    )
    public boolean WaterBoardSolver = false;

    @Property(
        type = PropertyType.SWITCH,
        name = "Three Weirdo Solver",
        description = "Highlights the correct chest to solve for the riddle puzzle.",
        category = "§1§rDungeons",
        subcategory = "Solvers"
    )
    public boolean ThreeWeirdosSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Teleport Pad Solver",
            description = "Highlights teleport pads that you have stepped on",
            category = "§1§rDungeons",
            subcategory = "Solvers"
    )
    public boolean teleportPadSolver = false;
    
    @Property(
            type = PropertyType.SWITCH,
            name = "Creeper Solver",
            description = "Highlights the lanterns to shoot in Creeper puzzle.",
            category = "§1§rDungeons",
            subcategory = "Solvers"
    )
    public boolean creeperSolver = false;

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
            name = "Highlight Bats",
            description = "Draws a box around bats to make bats easier to find",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous",
            searchTags = {"parent"}
    )
    public boolean highlightBats = false;

    @Property(
            type = PropertyType.COLOR,
            name = "Bat Highlight Color",
            description = "",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous",
            searchTags = {"Highlight Bats"}
    )
    public Color highlightBatColor = Color.green;

    @Property(
            type = PropertyType.SWITCH,
            name = "Highlight Gifts",
            description = "Highlights with a box of where gifts are at the Jerry's workshop.",
            category = "Render",
	    subcategory = "Highlights",
            searchTags = {"parent"}
    )
    public boolean presentWaypoints = false;
    
    @Property(
            type = PropertyType.COLOR,
            name = "Gift Highlight Color",
            description = "",
            category = "Render",
	    subcategory = "Highlights",
            searchTags = {"Highlight Gifts"}
    )
    public Color presentWaypointsColor = Color.yellow;

    @Property(
            type = PropertyType.SWITCH,
            name = "Glacial Cave Treasure Finder",
            description = "Highlights ice treasures in the wall when inside the Glacial Cave",
            category = "Mining",
	    subcategory = "Glacial Cave",
            searchTags = {"parent"}
    )
    public boolean icecaveHighlight = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Ice Treasure Through Walls",
            description = "§cWarning Use At Own Risk",
            category = "Mining",
	    subcategory = "Glacial Cave",
            searchTags = {"Glacial Cave Treasure Finder"}
    )
    public boolean icecaveHighlightWalls = false;

     @Property(
            type = PropertyType.SWITCH,
            name = "Glacial Cave Treasure Tracker",
            description = "Tracks the items you get from ice treasures",
            category = "Mining",
	    subcategory = "Trackers"
    )
    public boolean IceTreasureTracker = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Highlight Thrown Beacon",
            description = "Highlights the beacon thats thrown by the enderman slayer.",
            category = "Slayers",
	    subcategory = "Voidgloom",
            searchTags = {"parent"}
    )
    public boolean highlightBeacons = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Highlight Through Walls",
            description = "Highlights the beacon thats thrown by the enderman slayer through walls. §cWarning Use At Own Risk",
            category = "Slayers",
	    subcategory = "Voidgloom",
            searchTags = {"Highlight Thrown Beacon"}
    )
    public boolean highlightBeaconsThroughWalls = false;
    
    @Property(
            type = PropertyType.COLOR,
            name = "Beacon Highlight Color",
            description = "",
            category = "Slayers",
	    subcategory = "Voidgloom",
            searchTags = {"Highlight Thrown Beacon"}
    )
    public Color highlightBeaconsColor = Color.green;

    @Property(
            type = PropertyType.SWITCH,
            name = "Ender Node Tracker",
            description = "Tracks the items you get from ender nodes",
            category = "§1§rThe End",
	    subcategory = "Trackers"
    )
    public boolean EnderNodeTracker = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Day Tracker",
            description = "Tracks the day in the Crystal Hollows",
            category = "Mining",
	    subcategory = "Trackers"
    )
    public boolean dayTracker = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Highlight Ender Nodes",
            description = "Highlights the sparkly blocks in the end",
            category = "§1§rThe End",
	    subcategory = "Mining",
            searchTags = {"parent"}
    )
    public boolean highlightEnderNodes = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Nodes Through the walls",
            description = "Makes the Ender Node Highlight go through walls. §cWarning Use At Own Risk",
            category = "§1§rThe End",
	    subcategory = "Mining",
            searchTags = {"Highlight Ender Nodes"}
    )
    public boolean highlightEnderNodesWalls = false;

    @Property(
            type = PropertyType.COLOR,
            name = "Endstone Node Color",
            description = "",
            category = "§1§rThe End",
	    subcategory = "Mining",
            searchTags = {"Highlight Ender Nodes"}
    )
    public Color highlightEnderNodesEndstoneColor = Color.magenta;

    @Property(
            type = PropertyType.COLOR,
            name = "Obsidian Node Color",
            description = "",
            category = "§1§rThe End",
	    subcategory = "Mining",
            searchTags = {"Highlight Ender Nodes"}
    )
    public Color highlightEnderNodesObiColor = new Color(0x4f024f);

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
            description = "Moveable mana",
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
            subcategory = "Miscellaneous"
    )
    public boolean SecretsDisplay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Better Party Finder",
            description = "Creates a better user interface for the dungeon party finder",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean betterPartyFinder = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Highlight Correct Livid",
            description = "Highlights the incorrect livid",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous",
            searchTags = {"parent"}
    )
    public boolean highlightCorrectLivid = false;
    @Property(
            type = PropertyType.COLOR,
            name = "Livid Highlight Color",
            description = "",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous",
            searchTags = {"Highlight Correct Livid"}
    )
    public Color correctLividColor = Color.cyan;

    @Property(
            type = PropertyType.SWITCH,
            name = "Fairy Soul Helper",
            description = "Highlights nearby fairy souls using waypoints",
            category = "Render",
            subcategory = "Highlights",
            searchTags = {"parent"}
    )
    public boolean fairySoulHelper = false;
    @Property(
            type = PropertyType.COLOR,
            name = "§1§rUncollected Color",
            description = "",
            category = "Render",
            subcategory = "Highlights",
            searchTags = {"Fairy Soul Helper"}
    )
    public Color fairySoulUnFound = Color.magenta;
    @Property(
            type = PropertyType.COLOR,
            name = "§1§rCollected Color",
            description = "",
            category = "Render",
            subcategory = "Highlights",
            searchTags = {"Fairy Soul Helper"}
    )
    public Color fairySoulFound = Color.GREEN;

    @Property(
        type = PropertyType.SWITCH,
        name = "Rift Enigma Soul Helper",
        description = "Highlights nearby Enigma souls using waypoints in the Rift",
        category = "The Rift",
        subcategory = "General",
        searchTags = {"parent"}
    )
    public boolean riftSouls = false;
     @Property(
            type = PropertyType.COLOR,
            name = "Uncollected Color",
            description = "",
            category = "The Rift",
            subcategory = "General",
            searchTags = {"Rift Enigma Soul Helper"}
    )
    public Color riftSoulUnFound = Color.magenta;
    @Property(
            type = PropertyType.COLOR,
            name = "Collected Color",
            description = "",
            category = "The Rift",
            subcategory = "General",
            searchTags = {"Rift Enigma Soul Helper"}
    )
    public Color riftSoulFound = Color.GREEN;

    @Property(
        type = PropertyType.SWITCH,
        name = "Rift Mirrorverse Helper",
        description = "Solvers for some of the puzzles in the mirrorverse in the Rift",
        category = "The Rift",
        subcategory = "General",
        searchTags = {"parent"}
    )
    public boolean riftMirrorverseHelper = false;

    @Property(
            type = PropertyType.COLOR,
            name = "Main Highlight Color",
            description = "",
            category = "The Rift",
            subcategory = "General",
            searchTags = {"Rift Mirrorverse Helper"}
    )
    public Color riftMirrorverseHelperColor = new Color(0x00FFFF);

    @Property(
        type = PropertyType.SWITCH,
        name = "Rift Spell Helper",
        description = "Highlights the best line to trace spells in the rift",
        category = "The Rift",
        subcategory = "General"
    )
    public boolean riftSpellLines = false;

    @Property(
        type = PropertyType.SWITCH,
        name = "Rift Hacking Helper",
        description = "Highlights the correct numbers in the hacking gui",
        category = "The Rift",
        subcategory = "General"
    )
    public boolean riftHackingHelper = false;
    
    @Property(
        type = PropertyType.SWITCH,
        name = "Relic Helper",
        description = "Highlights relics in the §cSpider's Den§r using waypoints",
        category = "General",
        subcategory = "Other"
    )
    public boolean spiderRelicHelper = false;

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
            name = "Hide Player Nametags",
            description = "Stops player's nametags from renderering",
            category = "Render",
            subcategory = "Hide Things"
    )
    public boolean hidePlayerNametags = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Normal Fullbright",
            description = "Normal classic full bright everywhere",
            category = "Render",
            subcategory = "Fullbright"
    )
    public boolean fullbright = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Dynamic Fullbright",
            description = "Turns on Fullbright in §aCrystal Hollows§r,§aYour Island§r,§aDungeons",
            category = "Render",
            subcategory = "Fullbright",
            searchTags = {"parent"}
    )
    public boolean DynamicFullbright = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Enabled Value",
            description = "Value of brightness to set when in the certain locations",
            category = "Render",
            subcategory = "Fullbright",
            max = 100,
            min = 1,
            searchTags = {"Dynamic Fullbright"}
    )
    public int DynamicFullbrightDisabled = 100;

    @Property(
            type = PropertyType.SLIDER,
            name = "Disabled Value",
            description = "Value of brightness to set when everywhere else",
            category = "Render",
            subcategory = "Fullbright",
            max = 100,
            min = 1,
            searchTags = {"Dynamic Fullbright"}
    )
    public int DynamicFullbrightElsewhere = 1;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide All Nametags",
            description = "Stops all nametags from renderering",
            category = "Render",
            subcategory = "Hide Things"
    )
    public boolean hideAllNametags = false;

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide players near NPC's",
        description = "Disables Player models from rendering near NPC's",
        category = "Render",
        subcategory = "Hide Things"
    )
    public boolean hidePlayersNearNPC = false;

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Arrows",
        description = "Stops arrows from being rendered.",
        category = "Render",
        subcategory = "Hide Things"
    )
    public boolean hideArrows = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Bait Display",
            description = "Displays the current bait and amount in your Fishing Bag",
            category = "Quality of Life",
            subcategory = "Fishing"
    )
    public boolean baitCounter = false;
    
    @Property(
            type = PropertyType.SWITCH,
            name = "Display Tree Capitator Cooldown",
            description = "Displays the cooldown for the treecapitator",
            category = "Quality of Life",
            subcategory = "Foraging"
    )
    public boolean treecapitatorCooldown = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Display Conjuring Cooldown",
            description = "Displays the cooldown for the Conjuring",
            category = "Quality of Life",
            subcategory = "Dungeons"
    )
    public boolean ConjuringCooldown = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Onscreen Clock",
            description = "Display a clock",
            category = "Miscellaneous",
            subcategory = "Visual"
    )
    public boolean clock = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Jerry Timer",
            description = "Shows the cooldown for spawning jerry's",
            category = "Miscellaneous",
            subcategory = "Mayor Jerry"
    )
    public boolean jerry = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Use Smooth Font",
            description = "Uses a smoother font to render text. §cRequires restart",
            category = "§2§rCustomization",
            subcategory = "§1§rGui"
    )
    public boolean customFont = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "§6§lFurfSky§r Themed",
            description = "Uses §6§lFurfSky§r textures for the Gui",
            category = "§2§rCustomization",
            subcategory = "§1§rGui"
    )
    public boolean furfSkyThemed = false;

    @Property(type = PropertyType.COLOR,name = "Gui Lines",description = "",category = "§2§rCustomization",subcategory = "§1§rGui")
    public Color guiLines = new Color(0x000000);

    @Property(type = PropertyType.COLOR,name = "Selected Category Text",description = "",category = "§2§rCustomization",subcategory = "§1§rGui")
    public Color selectedCategory = new Color(0x02A9EA);

    @Property(type = PropertyType.COLOR,name = "Hovered Category Text",description = "",category = "§2§rCustomization",subcategory = "§1§rGui")
    public Color hoveredCategory = new Color(0x2CC8F7);

    @Property(type = PropertyType.COLOR,name = "Default Category Text",description = "",category = "§2§rCustomization",subcategory = "§1§rGui")
    public Color defaultCategory = new Color(0xFFFFFF);

    @Property(type = PropertyType.COLOR,name = "Feature Box Outline",description = "",category = "§2§rCustomization",subcategory = "§1§rGui")
    public Color featureBoxOutline = new Color(0xa9a9a9);

    @Property(type = PropertyType.COLOR,name = "Feature Description Text",description = "",category = "§2§rCustomization",subcategory = "§1§rGui")
    public Color featureDescription = new Color(0xbbbbbb);

    @Property(type = PropertyType.COLOR,name = "Main Box Background",description = "",category = "§2§rCustomization",subcategory = "§1§rGui")
    public Color mainBackground = new Color(25,25,25,200);

    @Property(type = PropertyType.COLOR,name = "Search Box Background",description = "",category = "§2§rCustomization",subcategory = "§1§rGui")
    public Color searchBoxBackground = new Color(120,120,120,60);

    @Property(type = PropertyType.COLOR,name = "Button Background",description = "",category = "§2§rCustomization",subcategory = "§1§rGui")
    public Color editGuiUnhovered = new Color(0,0,0,50);
    
    @Property(type = PropertyType.COLOR,name = "Button Hover Background",description = "",category = "§2§rCustomization",subcategory = "§1§rGui")
    public Color editGuiHovered = new Color(0,0,0,75);

    @Property(type = PropertyType.COLOR,name = "Edit Gui Text",description = "",category = "§2§rCustomization",subcategory = "§1§rGui")
    public Color editGuiText = new Color(0xFFFFFF);

    @Property(type = PropertyType.COLOR,name = "Title Text",description = "",category = "§2§rCustomization",subcategory = "§1§rGui")
    public Color titleColor = new Color(0x00FFFF);

    @Property(type = PropertyType.COLOR,name = "Version Text",description = "",category = "§2§rCustomization",subcategory = "§1§rGui")
    public Color versionColor = new Color(0xFFFFFF);

    @Property(
            type = PropertyType.SWITCH,
            name = "Player Disguiser",
            description = "Disguises players as different things",
            category = "§2§rCustomization",
            subcategory = "Player",
            searchTags = {"parent"}
    )
    public boolean playerDiguiser = false;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Disguise Players As",
            category = "§2§rCustomization",
            subcategory = "Player",
            options = {"Cow","Pig","Sheep","Zombie","Jerry","Enderman","Giant","Baby Player","Monki"},
            searchTags = {"Disguise Players As"}
    )
    public int DisguisePlayersAs = 0;

    @Property(
            type = PropertyType.PARAGRAPH,
            name = "Player Cape",
            category = "§2§rCustomization",
            description = "Paste a image url to give yourself a cape!\n§aEx. https://i.imgur.com/wHk1W6X.png (This is only visible to you)",
            subcategory = "Player"
    )
    public String playerCapeURL = "";

    @Property(
            type = PropertyType.SWITCH,
            name = "Diana Mythological Helper",
            description = "Draw an extended line of where the Mythological burrow could be",
            category = "Miscellaneous",
            subcategory = "Diana"
    )
    public boolean MythologicalHelper = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Crop Counter",
            description = "Shows the amount of crops on the hoe your holding",
            category = "§1§rFarming",
            subcategory = "Garden"
    )
    public boolean Counter = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Composter Overlay",
            description = "Shows extra info inside of the composter menu",
            category = "§1§rFarming",
            subcategory = "Garden"
    )
    public boolean composterOverlay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Trevor The Trapper Helper",
            description = "Shows the biome and location of the hunted mob",
            category = "General",
            subcategory = "Other"
    )
    public boolean trevorHelper = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Auto Accept Reparty",
            description = "Auto joins part when someone does reparty",
            category = "Quality of Life",
            subcategory = "Dungeons"
    )
    public boolean autoAcceptReparty = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Auto Reparty",
            description = "Auto does reparty when the dungeon ends",
            category = "Quality of Life",
            subcategory = "Dungeons"
    )
    public boolean autoReparty = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "1.7 Animations",
            description = "",
            category = "Quality of Life",
            subcategory = "Visual"
    )
    public boolean oldAnimations = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Far Entitys in hub",
            description = "",
            category = "Render",
            subcategory = "Hide Things"
    )
    public boolean HideFarEntity = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Damage Tint",
            description = "Makes your screen get more red the lower in health you are",
            category = "Quality of Life",
            subcategory = "Visual"
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
            name = "Highlight Trash",
            description = "Draws a red box around items that just fill up your inventory. \nExample §aDreadlord Sword§r, §aMachine Gun Bow",
            category = "§1§rDungeons",
            subcategory = "Miscellaneous"
    )
    public boolean highlightTrash = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Dungeon Map",
            description = "Render a moveable dungeon map on screen",
            category = "§1§rDungeons",
            subcategory = "Dungeon Map",
            searchTags = {"parent"}
    )
    public boolean dungeonMap = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Center Player on Dungeon Map",
            description = "Locks your player to the center of the dungeon map",
            category = "§1§rDungeons",
            subcategory = "Dungeon Map",
            searchTags = {"Dungeon Map"}
    )
    public boolean dungeonMapCenter = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Render Player Names",
            description = "Draws names above the players",
            category = "§1§rDungeons",
            subcategory = "Dungeon Map",
            searchTags = {"Dungeon Map"}
    )
    public boolean dungeonMapPlayerNames= true;

    @Property(
            type = PropertyType.SWITCH,
            name = "Blood Door Highlight",
            description = "Marks the players name red if they opened the blood door",
            category = "§1§rDungeons",
            subcategory = "Dungeon Map",
            searchTags = {"Dungeon Map"}
    )
    public boolean dungeonMapBloodGuy = true;

    @Property(
            type = PropertyType.SWITCH,
            name = "Render Player Heads",
            description = "Adds an outline the the player heads on the dungeon map",
            category = "§1§rDungeons",
            subcategory = "Dungeon Map",
            searchTags = {"Dungeon Map"}
    )
    public boolean dungeonMapHeads = true;

    @Property(
            type = PropertyType.SLIDER,
            name = "Player Head Scale",
            description = "Scale the size of the heads on the dungeon map §3(Percent)",
            category = "§1§rDungeons",
            subcategory = "Dungeon Map",
            min = 50,
            max = 150,
            searchTags = {"Dungeon Map"}
    )
    public int dungeonMapHeadScale = 100;

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
            category = "Render",
            subcategory = "1.9 Glow Effect"
    )
    public boolean glowingDungeonPlayers = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Glowing Players",
            description = "Make visible players anywhere glow",
            category = "Render",
            subcategory = "1.9 Glow Effect"
    )
    public boolean glowingPlayers = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Glowing Items!",
            description = "Make items glow depending on rarity (Requires Fast render to be off.)",
            category = "Render",
            subcategory = "1.9 Glow Effect"
    )
    public boolean glowingItems = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hidden Jerry Alert",
            description = "Displays an alert when you find a hidden Jerry.",
            category = "Miscellaneous",
            subcategory = "Mayor Jerry"
    )
    public boolean hiddenJerryAlert = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Treasure Chest Solver",
            description = "Highlights the particles to look at when opening a treasure chest.",
            category = "Mining",
            subcategory = "Solvers"
    )
    public boolean treasureChestSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Automaton Loot Tracker",
            description = "Tracks the loot from Automatons. Starts after a Automaton is killed",
            category = "Mining",
            subcategory = "Trackers"
    )
    public boolean AutomatonTracker = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Gemstone Tracker",
            description = "Tracks the stats from mining gemstones like Coins per hour",
            category = "Mining",
            subcategory = "Trackers"
    )
    public boolean gemstoneTracker = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Ghost Loot Tracker",
            description = "Tracks the loot gained from killing Ghosts",
            category = "Mining",
            subcategory = "Trackers"
    )
    public boolean ghostTracker = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Powder Mining Tracker",
            description = "Tracks the stats from mining gemstones like Coins per hour",
            category = "Mining",
            subcategory = "Trackers"
    )
    public boolean PowderTracker = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Commissions Tracker",
            description = "Tracks your progress on commissions",
            category = "Quality of Life",
            subcategory = "Mining"
    )
    public boolean CommisionsTracker = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Highlight Placed Cobblestone",
            description = "Highlights the cobblestone you place in crystal hollows",
            category = "Quality of Life",
            subcategory = "Mining",
            searchTags = {"parent"}
    )
    public boolean highlightCobblestone = false;
    @Property(
            type = PropertyType.COLOR,
            name = "Cobblestone Color",
            description = "",
            category = "Quality of Life",
            subcategory = "Mining",
            searchTags = {"Highlight Placed Cobblestone"}
    )
    public Color highlightCobblestoneColor = Color.cyan;

    @Property(
            type = PropertyType.SWITCH,
            name = "Crystal Hollows Map",
            description = "Show a map of the crystal hollows",
            category = "Mining",
            subcategory = "Map",
            searchTags = {"parent"}
    )
    public boolean CrystalHollowsMap = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Crystal Hollows Map Heads",
            description = "Show a heads instead of a marker on the crystal hollows map",
            category = "Mining",
            subcategory = "Map",
            searchTags = {"Crystal Hollows Map"}
    )
    public boolean CrystalHollowsMapHeads = true;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Breadcrumb Trail On Map",
            description = "Show a trail of where you have been",
            category = "Mining",
            subcategory = "Map",
            searchTags = {"Crystal Hollows Map"}
    )
    public boolean CrystalHollowsMapTrail = true;

    @Property(
            type = PropertyType.SWITCH,
            name = "Dwarven Mines Map",
            description = "Show a map of the dwarven map",
            category = "Mining",
            subcategory = "Map"
    )
    public boolean dwarvenMinesMap = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Mines of Divan Metal Detector Solver",
            description = "Shows where the treasure chest is in the Mines of Divan",
            category = "Mining",
            subcategory = "Solvers"
    )
    public boolean MetalDetectorSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show NPC Sell Price",
            description = "Shows the NPC Sell Price on certain items.",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showNPCSellPrice = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Crimson Isles Map",
            description = "Show a map of the Crimson Isles",
            category = "Quality of Life",
            subcategory = "Crimson Isle"
    )
    public boolean crimsonsIslesMap = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Skyblock Item ID",
            description = "Shows an items skyblock ID in the lore.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean showSkyblockID = false;
    
    @Property(
            type = PropertyType.SWITCH,
            name = "TNT Timer",
            description = "Shows the time till tnt exploads",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean tntTimer = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Prehistoric Egg Distance Counter",
            description = "Shows the blocks walked on the prehistoric egg item",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean egg = false;

     @Property(
            type = PropertyType.SWITCH,
            name = "Show teleport overlay",
            description = "Highlights the block that your teleporting to with Aspect of the End or Aspect of the Void",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean teleportDestination = false;

     @Property(
            type = PropertyType.SWITCH,
            name = "Chat Timestamps",
            description = "Add Chat Timestamps to Messages",
            category = "Quality of Life",
            subcategory = "Visual"
    )
    public boolean timestamps = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Enchanting Solvers",
            description = "Solvers for ultrasequencer and chronomotron",
            category = "Quality of Life",
            subcategory = "Solvers"
    )
    public boolean enchantingSolvers = false;
    
    @Property(
            type = PropertyType.SWITCH,
            name = "Hide White Square",
            description = "Hide the hover highlight Square in inventories",
            category = "Quality of Life",
            subcategory = "Visual"
    )
    public boolean hideWhiteSquare = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Zealot Spawn Areas & Spawn Timer",
            description = "Draws where zealots spawn and when zealots will spawn. (this includes bruisers)",
            category = "§1§rThe End",
            subcategory = "Zealots"
    )
    public boolean showZealotSpawns = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Garden Visitor Overlay",
            description = "Shows the extra information inside the Garden Visitor Gui.",
            category = "§1§rFarming",
            subcategory = "Garden"
    )
    public boolean GardenVisitorOverlay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Blocks to Destroy Overlay",
            description = "Shows the blocks needed to destroy when clearing a plot in the garden.",
            category = "§1§rFarming",
            subcategory = "Garden"
    )
    public boolean GardenBlocksToRemove = false;

    @Property(
        type = PropertyType.SWITCH,
        name = "Make Zealots Glow",
        description = "Applys the 1.9 glow effect to zealots to make them glow and shiny :D ",
        category = "§1§rThe End",
        subcategory = "Zealots"
    )
    public boolean glowingZealots = false;

    @Property(
        type = PropertyType.SWITCH,
        name = "Highlight Glowing Mushrooms",
        description = "Highlights glowing mushrooms in the Glowing Mushroom Cave",
        category = "§1§rFarming",
        subcategory = "Glowing Mushroom Cave",
        searchTags = {"parent"}
    )
    public boolean highlightMushrooms = false;

    @Property(
        type = PropertyType.COLOR,
        name = "Mushroom Highlight Color",
        description = "",
        category = "§1§rFarming",
        subcategory = "Glowing Mushroom Cave",
        searchTags = {"Highlight Glowing Mushrooms"}
    )
    public Color highlightMushroomsColor = Color.green;

    @Property(
        type = PropertyType.SWITCH,
        name = "1.12 Crop Hitbox",
        description = "Applys full sized hitbox for crops",
        category = "§1§rFarming",
        subcategory = "Garden"
    )
    public boolean cropBox = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Air Display",
            description = "Prevents the game from rendering the air bubbles while underwater.",
            category = "Quality of Life",
            subcategory = "Visual"
    )
    public boolean hideAirDisplay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Geyser Particles",
            description = "Hides the annoying particles in the §6Blazing Volcano.",
            category = "Render",
            subcategory = "Hide Things"
    )
    public boolean hideGeyserParticles = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Highlight Geyser Box",
            description = "Creates a box of where the geyser area is in the §6Blazing Volcano",
            category = "Render",
            subcategory = "Highlights"
    )
    public boolean geyserBoundingBox = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "No Fire",
            description = "Removes first-person fire overlay when you are burning.",
            category = "Render",
            subcategory = "Hide Things"
    )
    public boolean noFire = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "No Hurtcam",
            description = "Removes the screen shake when you are hurt.",
            category = "Quality of Life",
            subcategory = "Visual"
    )
    public boolean noHurtcam = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Lowest BIN Price",
            description = "Shows the lowest Buy It Now price for various items in Skyblock.",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showLowestBINPrice = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Price Paid",
            description = "Shows the price you bought an item for.",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showPricePaid = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Bazaar Price",
            description = "Shows the bazaar price for various items in Skyblock.",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showBazaarPrice = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Sales Per Day",
            description = "Shows the sales per day for various items in Skyblock.",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showSalesPerDay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Estimated Price",
            description = "Shows the estimated price for various items in Skyblock. Calculates using things like enchants and stars",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showEstimatedPrice = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Average BIN Price",
            description = "Shows the average Buy It Now price for various items in Skyblock.",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showAvgLowestBINPrice = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Helpful Auction Guis",
            description = "Shows the extra information about your own and others auctions.",
            category = "§1§rAuction house",
            subcategory = "Auction Utils"
    )
    public boolean auctionGuis = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Condense Item Price Info",
            description = "Only shows the things like Average BIN, Lowest BIN, Sales/Day when the Shift key is held",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    public boolean showPriceInfoOnShift = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Minion Overlay",
            description = "Shows the extra information inside the minion gui.",
            category = "Miscellaneous",
            subcategory = "Overlay"
    )
    public boolean minionOverlay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Collections Leaderboard Overlay",
            description = "Shows a leaderboard for the collection types",
            category = "Miscellaneous",
            subcategory = "Overlay"
    )
    public boolean collecitonsLeaderboard = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "SBF Trade Gui",
            description = "Shows extra information inside the trade gui, including estimated prices",
            category = "Miscellaneous",
            subcategory = "Overlay"
    )
    public boolean tradeOverlay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Missing Accessories",
            description = "Shows a list of what talismans your missing when in your accessory bag",
            category = "Miscellaneous",
            subcategory = "Overlay"
    )
    public boolean showMissingAccessories = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Extra Profile Info",
            description = "Shows a a players networth,discord,weight, and skill avg when you right click on someone",
            category = "Miscellaneous",
            subcategory = "Overlay"
    )
    public boolean extraProfileInfo = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Highlight Auctions For Flipping",
            description = "Highlights auctions that have 100,000 profit or more.",
            category = "§1§rAuction house",
            subcategory = "Auction Utils"
    )
    public boolean highlightAuctionProfit = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Highlight Losing Auctions Red",
            description = "Highlights auctions that you arent winning",
            category = "§1§rAuction house",
            subcategory = "Auction Utils"
    )
    public boolean highlightlosingAuction = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Flipper Active",
            description = "Enables or disables the flipper with its current settings",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public boolean aucFlipperEnabled = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "BIN Flipper",
            description = "Shows you BIN that have a flip value of more than your margin.\n§cDo not put 100% trust in the mod, it can and probably will make mistakes.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public boolean aucFlipperBins = false;

    @Property(
        type = PropertyType.SWITCH,
        name = "Include Auction Flips",
        description = "Check auctions for flips",
        category = "§1§rAuction Flipper",
        subcategory = "Flipper Settings"
    )
    public boolean aucFlipperAucs = true;
    
    @Property(
        type = PropertyType.SWITCH,
        name = "Include BIN Flips",
        description = "Check BIN for flips §c(Risky)",
        category = "§1§rAuction Flipper",
        subcategory = "Flipper Settings"
    )
    public boolean autoFlipBIN = true;

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
            type = PropertyType.SWITCH,
            name = "Make Purse Max Amount",
            description = "Make the amount of money you can spend on an auction equal to your purse.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public boolean autoAuctionFlipSetPurse = false;

    @Property(
        type = PropertyType.SWITCH,
        name = "Change Item Estimation",
        description = "Include stars and enchants into item value estimation.",
        category = "§1§rAuction Flipper",
        subcategory = "Flipper Settings"
    )
    public boolean autoFlipAddEnchAndStar = true;

    @Property(
            type = PropertyType.SWITCH,
            name = "Refresh Countdown",
            description = "Show the countdown till refreshing.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public boolean autoAuctionFlipCounter = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Auto Open",
            description = "Opens up the bid menu for the item with the highest profit. \n§cThis is slower than holding down key",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    public boolean autoAuctionFlipOpen = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Easy Auction Buying",
            description = "By spam clicking you will auto buy/bid the item from that is currently viewed.",
            category = "§1§rAuction house",
            subcategory = "Flipper Settings"
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
            type = PropertyType.PARAGRAPH,
            name = "Blacklist",
            description = "Filters out any blacklisted items. Seperate with §a;§r.§aExample: 'bonemerang;stick'",
            category = "§1§rAuction Flipper",
            subcategory = "§1§rAuction Flipper Filter"
    )
    public String autoAuctionBlacklist = "bonemerang;soldier;jungle pick;";

    @Property(
            type = PropertyType.SWITCH,
            name = "Exotic Auctions Finder",
            description = "Shows you auctions of exotic, fairy, crystal, spooky dyed armor status.",
            category = "§1§rExotic Finder",
            subcategory = "§1§rExotic Finder Settings"
    )
    public boolean exoticAuctionFinder = false;

    @Property(
            type = PropertyType.CHECKBOX,
            name = "Include Fairy Dyed",
            description = "Shows you auctions of fairy dyed armor status.",
            category = "§1§rExotic Finder",
            subcategory = "§1§rExotic Finder Settings"
    )
    public boolean fairyExotics = true;

    @Property(
            type = PropertyType.CHECKBOX,
            name = "Include Bleached",
            description = "Shows you auctions of bleached (default brown) armor status.",
            category = "§1§rExotic Finder",
            subcategory = "§1§rExotic Finder Settings"
    )
    public boolean bleachedExotics = true;

    @Property(
            type = PropertyType.CHECKBOX,
            name = "Include Crystal Dyed",
            description = "Shows you auctions of crystal dyed armor status.",
            category = "§1§rExotic Finder",
            subcategory = "§1§rExotic Finder Settings"
    )
    public boolean crystalExotics = true;

    @Property(
            type = PropertyType.CHECKBOX,
            name = "Include Spooky Dyed",
            description = "Shows you auctions of spooky armor status.",
            category = "§1§rExotic Finder",
            subcategory = "§1§rExotic Finder Settings"
    )
    public boolean spookyExotics = true;

    @Property(
            type = PropertyType.CHECKBOX,
            name = "Include Exotics",
            description = "Shows you auctions of non orignal dyed armor status.",
            category = "§1§rExotic Finder",
            subcategory = "§1§rExotic Finder Settings"
    )
    public boolean exoticsExotics = true;

    @Property(
            type = PropertyType.SWITCH,
            name = "Granda Wolf Pet Combo Timer",
            description = "Shows time until your combo expires on the Grandma Wolf Pet",
            category = "General",
            subcategory = "Pets"
    )
    public boolean GrandmaWolfTimer = false;

     @Property(
            type = PropertyType.SWITCH,
            name = "Ad Blocker",
            description = "Hides auction/lowballing advertisments in chat",
            category = "General",
            subcategory = "Other"
    )
    public boolean hideAdvertisments = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Small Items",
            description = "Makes the items you hold smaller",
            category = "General",
            subcategory = "Other"
    )
    public boolean smallItems = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Auto Party Chat",
            description = "Auto sends §a/chat p§r after joining a party §cWarning Use At Own Risk",
            category = "General",
            subcategory = "Other"
    )
    public boolean autoPartyChat = false;

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
    
    @Property(
            type = PropertyType.SLIDER,
            name = "gMaWolf5Second",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int gMaWolf5Second = 0;

    @Property(
            type = PropertyType.SLIDER,
            name = "gMaWolf10Second",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int gMaWolf10Second = 0;

    @Property(
            type = PropertyType.SLIDER,
            name = "gMaWolf15Second",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int gMaWolf15Second = 0;

    @Property(
            type = PropertyType.SLIDER,
            name = "gMaWolf20Second",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int gMaWolf20Second = 0;

    @Property(
            type = PropertyType.SLIDER,
            name = "gMaWolf25Second",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int gMaWolf25Second = 0;

    @Property(
            type = PropertyType.SLIDER,
            name = "gMaWolf30Second",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int gMaWolf30Second = 0;

    @Property(
            type = PropertyType.SLIDER,
            name = "composterSpeedLvl",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int speedLvl = 0;

    @Property(
            type = PropertyType.SLIDER,
            name = "composterMultiLvl",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int multiLvl = 0;

    @Property(
            type = PropertyType.SLIDER,
            name = "composterFuelLvl",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int fuelLvl = 0;

    @Property(
            type = PropertyType.SLIDER,
            name = "composterOrgLvl",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int orgLvl = 0;

    @Property(
            type = PropertyType.SLIDER,
            name = "composterCostLvl",
            description = "",
            category = "General",
            subcategory = "Item Fov",
            hidden = true
    )
    public int costLvl = 0;

    public static File file = new File("./config/skyblockfeatures/config.toml");
    public Config() {
        super(file);
        initialize();
    }

    public void forceSave() {
        SkyblockFeatures.config.markDirty();
        SkyblockFeatures.config.writeData();
    }
}
