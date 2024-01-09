package mrfast.sbf.commands;

import mrfast.sbf.features.dungeons.PartyFinderFeatures;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;


public class DungeonPlayerInfoCommand  extends  CommandBase{
    @Override
    public String getCommandName() {
        return "sfcata";
    }

    @Override
    public String getCommandUsage(ICommandSender arg0) {
        return "/" + getCommandName() + " [name]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }


    @Override
    public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
        PartyFinderFeatures instance = new PartyFinderFeatures();
        if(arg1.length == 0) instance.showDungeonPlayerInfo(Minecraft.getMinecraft().thePlayer.getName(),false);
        if(arg1.length ==1) instance.showDungeonPlayerInfo(arg1[0],false);
    }
}
