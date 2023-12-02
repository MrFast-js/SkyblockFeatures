package mrfast.sbf.commands;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.core.VersionManager;
import mrfast.sbf.gui.EditLocationsGui;
import mrfast.sbf.gui.GuiManager;
import mrfast.sbf.gui.ConfigGui;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

public class configCommand extends CommandBase {

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
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return (args.length >= 1) ? getListOfStringsMatchingLastWord(args, Utils.getListOfPlayerUsernames()) : null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerSP player = (EntityPlayerSP) sender;
        if (args.length == 0) {
            // GuiUtil.open(Objects.requireNonNull(skyblockfeatures.config.gui()));
            GuiUtils.openGui(new ConfigGui(true));
            return;
        }
        String subcommand = args[0].toLowerCase(Locale.ENGLISH);
        
        switch (subcommand) {
            case "update":
                VersionManager.checkForUpdates();
                break;
            case "version":
                Utils.sendMessage(ChatFormatting.YELLOW+"Your using Skyblock Features v"+SkyblockFeatures.VERSION);
                break;
            case "loc":
                Utils.sendMessage(ChatFormatting.GRAY+"Local:'"+SkyblockInfo.localLocation+"' Map:"+SkyblockInfo.map+" Location:'"+SkyblockInfo.location+"'");
                break;
            case "resetgui":
                GuiManager.GuiPositions.forEach((name,point)->{
                    Point pnt = GuiManager.names.get(name).getPos();
                    GuiManager.GuiPositions.put(name, pnt);
                });
                Utils.sendMessage("Gui Positions Reset!");
                break;
            case "config":
                GuiUtils.openGui(new ConfigGui(true));
                break;
            case "help":
                player.addChatMessage(new ChatComponentText("§9➜ Skyblock Features Commands and Info" + "\n" +
                " §2§l ❣ §7§oThe current mod version is §f§o" + SkyblockFeatures.VERSION + "§7§o." + "\n" +
                "§9§l➜ Setup:" + "\n" +
                " §3/sbf §l➡ §bOpens the configuration GUI." + "\n" +
                " §3/sbf help §l➡ §bShows this help menu." + "\n" +
                " §3/sbf edit §l➡ §bOpens the location editing GUI." + "\n" +
                "§9§l➜ Miscellaneous:" + "\n" +
                " §3/terminal §l➡ §bDisplays a gui with a f7 terminal for practice." + "\n" +
                " §3/vm §l➡ §bDisplays a gui with item position offsets." + "\n" +
                " §3/shrug §l➡ §bSends a chat message with '¯\\_(ツ)_/¯'" + "\n" +
                " §3/inventory §l➡ §bOpens a gui displaying the specified players inventory & armor." + "\n"+
                " §3/accessories §l➡ §bOpens a gui displaying the specified players accessory bag." + "\n"+
                " §3/bank §l➡ §bDisplays in chat the specified players bank and purse balance." + "\n"+
                " §3/armor §l➡ §bDisplays in chat the specified players armor." + "\n"+
                " §3/skills §l➡ §bDisplays in chat the specified players skills." + "\n"+
                " §3/sky §l➡ §bGives the link to the specified players Skycrypt profile."));
                break;
            case "edit":
                GuiUtils.openGui(new EditLocationsGui());
                break;
            default:
                player.addChatMessage(new ChatComponentText("§bSBF ➜ §cThis command doesn't exist!\n  §cUse §b/sbf help§c for a full list of commands"));
        }
    }
}