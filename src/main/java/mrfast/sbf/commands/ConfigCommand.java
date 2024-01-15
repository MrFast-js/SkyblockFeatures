package mrfast.sbf.commands;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.VersionManager;
import mrfast.sbf.gui.ConfigGui;
import mrfast.sbf.gui.EditLocationsGui;
import mrfast.sbf.gui.GuiManager;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ConfigCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "skyblockfeatures";
    }

    @Override
    public List<String> getCommandAliases() {
        return Lists.newArrayList("sf","sbf");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerSP player = (EntityPlayerSP) sender;
        if (args.length == 0) {
            GuiUtils.openGui(new ConfigGui(true));
            return;
        }
        String subcommand = args[0].toLowerCase(Locale.ENGLISH);
        String versionMessage = ChatFormatting.YELLOW + "Your using Skyblock Features v" + SkyblockFeatures.VERSION;
        switch (subcommand) {
            case "update":
                if(args.length>1) {
                    if(args[1].equals("close")) {
                        VersionManager.closeMinecraft();
                        return;
                    }
                    if(!args[1].equals("pre") && !args[1].equals("full")) {
                        Utils.sendMessage(ChatFormatting.RED + "Invalid Usage! " + ChatFormatting.YELLOW + "/update pre, full");
                    } else {
                        if(args.length>2) {
                            if(Objects.equals(args[2], "force")) {
                                VersionManager.checkForUpdates(args[1],true);
                                return;
                            }
                        }
                        VersionManager.checkForUpdates(args[1]);
                    }
                } else {
                    VersionManager.checkForUpdates("pre");
                }
                break;
            case "version":
                Utils.sendMessage(versionMessage);
                break;
            case "resetgui":
                GuiManager.GuiPositions.forEach((name,point)->{
                    try {
                        Point pnt = GuiManager.names.get(name).getPos();
                        GuiManager.GuiPositions.put(name, pnt);
                    } catch (Exception ignored) {}
                });
                Utils.sendMessage("Gui Positions Reset!");
                break;
            case "config":
                GuiUtils.openGui(new ConfigGui(true));
                break;
            case "help":
                String helpMessage = "§eSkyblock Features Commands and Info\n"
                        + " §7Current mod version: " + SkyblockFeatures.VERSION + "\n"
                        + "\n§e§l General Commands:\n"
                        + "  §3/jd <floor level> §l➡ §bJoins a dungeon floor.\n"
                        + "  §3/flips <name> §l➡ §bOpens coFLIPnet website of the person's auction flips.\n"
                        + "  §3/ping §l➡ §bMeasure ping.\n"
                        + "  §3/terminal §l➡ §bOpens a terminal GUI.\n"
                        + "  §3/pv, /sfpv <name> §l➡ §bOpens the SBF profile viewer.\n"
                        + "  §3/reparty §l➡ §bReparty.\n"
                        + "\n §e§lClient Commands:\n"
                        + "  §3/debug mobs,sidebar,tab,item,location §l➡ §bGives debug info for mobs, sidebar, tab, item, and location.\n"
                        + "  §3/sbf edit §l➡ §bOpens the location editing GUI.\n"
                        + "  §3/sbf version §l➡ §bDisplays the Skyblock Features version.\n"
                        + "  §3/sbf update §l➡ §bChecks and updates Skyblock Features.\n";

                player.addChatMessage(new ChatComponentText(helpMessage));
                break;
            case "edit":
                GuiUtils.openGui(new EditLocationsGui());
                break;
            default:
                ConfigGui.searchQuery = String.join(" ",args);
                GuiUtils.openGui(new ConfigGui(true));
        }
    }
}