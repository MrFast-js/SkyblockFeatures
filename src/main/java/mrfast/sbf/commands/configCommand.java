package mrfast.sbf.commands;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.gui.EditLocationsGui;
import mrfast.sbf.gui.GuiManager;
import mrfast.sbf.gui.ConfigGui;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MXBean;
import javax.management.ObjectName;

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
            Utils.openGui(new ConfigGui(true));
            return;
        }
        String subcommand = args[0].toLowerCase(Locale.ENGLISH);
        
        switch (subcommand) {
            case "version":
                Utils.SendMessage(ChatFormatting.YELLOW+"Your using Skyblock Features v"+SkyblockFeatures.VERSION);
                break;
            case "loc":
                Utils.SendMessage(ChatFormatting.GRAY+"Local:'"+SkyblockInfo.getInstance().localLocation+"' Map:"+SkyblockInfo.getInstance().map+" Location:'"+SkyblockInfo.getInstance().location+"'");
                break;
            case "resetgui":
                GuiManager.GuiPositions.forEach((name,point)->{
                    Point pnt = GuiManager.names.get(name).getPos();
                    GuiManager.GuiPositions.put(name, pnt);
                });
                Utils.SendMessage("Gui Positions Reset!");
                break;
            case "config":
                Utils.openGui(new ConfigGui(true));
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
                Utils.openGui(new EditLocationsGui());
                break;
            default:
                player.addChatMessage(new ChatComponentText("§bSBF ➜ §cThis command doesn't exist!\n  §cUse §b/sbf help§c for a full list of commands"));
        }
    }
    @MXBean
    public interface DiagnosticCommandMXBean {
        String gcClassHistogram(String[] array);
    }

    private String generateDataUsage() throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = ObjectName.getInstance("com.sun.management:type=DiagnosticCommand");
        DiagnosticCommandMXBean proxy = JMX.newMXBeanProxy(
                server,
                objectName,
                DiagnosticCommandMXBean.class
        );
        return proxy.gcClassHistogram(new String[0]).replace("[", "[]");
    }
}