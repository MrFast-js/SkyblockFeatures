package mrfast.sbf.commands;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import mrfast.sbf.features.mining.PathTracer;
import mrfast.sbf.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;

public class PathTracerCommand extends CommandBase {

	@Override
    public String getCommandName() {
        return "pathtracer";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/pathtracer [message]";
    }

    @Override
	public List<String> getCommandAliases() {
        return Collections.singletonList("path");
    }

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	
	@Override
	public void processCommand(ICommandSender arg0, String[] args) throws CommandException {
        String subcommand = "";
        String help = "§9§l➜ /path Usage:" + "\n" +
        " §3/path start §l➡ §bStarts the path creating process" + "\n" +
        " §3/path add <x> <y> <z> §l➡ §bAdds a point to the path your creating" + "\n" +
        " §3/path record <start|stop> §l➡ §bStarts/stops recording your movement on the path your creating" + "\n" +
        " §3/path load <name>§l➡ §bLoads a saved path" + "\n" +
        " §3/path unload §l➡ §bUnloads your current path" + "\n" +
        " §3/path list §l➡ §bLists your saved paths" + "\n" +
        " §3/path save <name>§l➡ §bSaves created path as file" + "\n" +
        " §3/path delete <name>§l➡ §bDeletes a saved path";
        if (args.length == 0) {
            Utils.SendMessage(help);
        } else {
            subcommand = args[0].toLowerCase(Locale.ENGLISH);;
        }
        if(subcommand!="") {
            if(subcommand.contains("start")) {
                PathTracer.pathPoints.clear();
                PathTracer.creatingPath = true;
                Utils.SendMessage("§3Path creation started. Use §a/path record start§3 to record your movement");
            }
            else if(subcommand.contains("record")) {
                if(!PathTracer.creatingPath) {
                    Utils.SendMessage("§cNo path being created. Make one with §a/path start");
                    return;
                }
                if(args.length>=2) {
                    if(args[1].contains("start")) {
                        PathTracer.pathPoints.clear();
                        PathTracer.recordingMovement = true;
                        Utils.SendMessage("§3Movement recording started started. Use §a/path recording stop§3 to stop");
                    }
                    if(args[1].contains("stop")) {
                        PathTracer.recordingMovement = false;
                        Utils.SendMessage("§3Movement recording stopped. Use §a/path save <name>§3 to save your path");
                    }
                } else {
                    Utils.SendMessage("§cIncorrect Command Usage:§a /path record <start|stop>");
                }
            }

            else if(subcommand.contains("add")) {
                if(!PathTracer.creatingPath) {
                    Utils.SendMessage("§cNo path being created. Make one with §a/path start");
                    return;
                }
                if(args.length>=4) {
                    try {
                        Float x = Float.parseFloat(args[1]);
                        Float y = Float.parseFloat(args[2]);
                        Float z = Float.parseFloat(args[3]);
                        Utils.SendMessage("§3Added Point ("+x+","+y+","+z+") to your current path");
                        PathTracer.pathPoints.add(new Vec3(x,y,z));
                    } catch (Exception e) {
                        Utils.SendMessage("§cIncorrect Command Usage:§a /path add <x> <y> <z>");
                    }
                }
            }

            else if(subcommand.contains("unload")) {
                PathTracer.pathPoints.clear();
                Utils.SendMessage("§cCurrent path unloaded");
            }

            else if(subcommand.contains("load")) {
                if(args.length>=2) {
                    PathTracer.readConfig(args[1]);
                } else {
                    Utils.SendMessage("§cYou didnt specify the name of path.§a /path load <name>");
                }
            }

            else if(subcommand.contains("save")) {
                if(args.length>=2) {
                    PathTracer.savePath(args[1]);
                    PathTracer.creatingPath = false;
                    PathTracer.recordingMovement = false;
                } else {
                    Utils.SendMessage("§cYou didnt specify the name of path.§a /path save <name>");
                }
            }

            else if(subcommand.contains("list")) {
                PathTracer.readConfig("");
                Utils.SendMessage("§9§l➜ Saved Paths:");
                for(String pathName:PathTracer.pathsAndPoints.keySet()) {
                    ChatComponentText message = new ChatComponentText(" §3"+ pathName);

                    message.setChatStyle(message.getChatStyle()
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/path load "+pathName))
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GREEN+"/path load "+pathName))));
                    Utils.SendMessage(message);
                }
                if(PathTracer.pathsAndPoints.size()==0) {
                    Utils.SendMessage(" §cNo paths found.");
                }
            }
            
            else if(subcommand.contains("delete")) {
                if(args.length>=2) {
                    if(PathTracer.pathsAndPoints.containsKey(args[1])) {
                        PathTracer.pathsAndPoints.remove(args[1]);
                        Utils.SendMessage("§cDeleted Path §a"+args[1]);
                        PathTracer.saveConfig();
                    }
                } else {
                    Utils.SendMessage("§cYou didnt specify the name of path.§a /path delete <name>");
                }
            } else {
                Utils.SendMessage(help);
            }
        }
	}
}
