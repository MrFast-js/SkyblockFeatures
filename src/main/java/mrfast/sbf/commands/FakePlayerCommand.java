package mrfast.sbf.commands;

import java.util.UUID;

import mrfast.sbf.utils.NetworkUtils;

import com.mojang.authlib.GameProfile;

import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class FakePlayerCommand extends CommandBase {

	@Override
    public String getCommandName() {
        return "fakeplayer";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/fakeplayer [playerName]";
    }

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

    EntityOtherPlayerMP fakePlayer = null;
    static Minecraft mc = Utils.GetMC();
    
	@Override
	public void processCommand(ICommandSender arg0, String[] args) throws CommandException {
        if(fakePlayer != null) {
            mc.theWorld.removeEntity(fakePlayer);
        }
        UUID uuid = UUID.fromString(NetworkUtils.getUUID(args[0],true));
        GameProfile profile = new GameProfile(uuid, args[0]);
        Utils.GetMC().getSessionService().fillProfileProperties(profile, true);

		fakePlayer = new EntityOtherPlayerMP(mc.theWorld,profile);
        fakePlayer.copyLocationAndAnglesFrom(mc.thePlayer);
        fakePlayer.rotationYawHead = mc.thePlayer.rotationYawHead;

        mc.theWorld.addEntityToWorld(-1, fakePlayer);
	}


}
