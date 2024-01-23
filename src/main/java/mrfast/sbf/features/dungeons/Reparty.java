package mrfast.sbf.features.dungeons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Reparty {
// TODO setup this file as a party API rather than reparty
    private boolean waitingForInvite = false;
    private String partyLeader= null;

    private boolean isPartyLeader = false;
    private final List<String> memberList = new ArrayList<>();
    private boolean lookingForLine = false;

    @SubscribeEvent
    public void onChatMessageReceived(ClientChatReceivedEvent event) {
        if (event.type == 2) return;
        String message = event.message.getUnformattedText();

        long currentTime = System.currentTimeMillis();

        if (message.contains("Party Leader:")) {
            memberList.clear();
            String partyLeader = extractPartyLeader(message);
            isPartyLeader = partyLeader.contains(Utils.GetMC().thePlayer.getName());
        }
        if (isPartyLeader && message.contains("Party Moderators:")) {
            memberList.addAll(extractMemberNames(message));
            lookingForLine = true;
        }
        if (isPartyLeader && message.contains("Party Members:")) {
            memberList.addAll(extractMemberNames(message));
            lookingForLine = true;
        }
        if(lookingForLine && message.equals("-----------------------------------------------------")) {
            Utils.setTimeout(()->{
                Utils.GetMC().thePlayer.sendChatMessage("/p disband");
            }, 250);
            lookingForLine = false;
            for(int i=0;i<memberList.size();i++) {
                int a = i;
                Utils.setTimeout(()->{
                    Utils.GetMC().thePlayer.sendChatMessage("/p "+memberList.get(a));
                }, (a+2)*350);
            }
        }
    }

    private String extractPartyLeader(String message) {
        message = message.split(": ")[1];
        Pattern pattern = Pattern.compile("\\[\\w+\\+?\\] (\\w+) ●");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private List<String> extractMemberNames(String message) {
        message = clearRanks(message);
        message = message.substring(15);

        List<String> memberNames = new ArrayList<>();
        Collections.addAll(memberNames, message.split(" ● "));

        return memberNames;
    }

    public static String clearRanks(String input) {
        Pattern pattern = Pattern.compile("\\[[^\\[\\]]+\\] ");
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll("");
    }
}
