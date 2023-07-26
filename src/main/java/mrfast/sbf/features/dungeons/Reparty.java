package mrfast.sbf.features.dungeons;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Reparty {
    private boolean waitingForInvite = false;
    private String partyLeader= null;;
    private long startTime = 0;

    private boolean isPartyLeader = false;
    private List<String> memberList = new ArrayList<>();
    private static long lastRepartyTime = 0;
    private boolean lookingForLine = false;

    @SubscribeEvent
    public void onChatMessageReceived(ClientChatReceivedEvent event) {
        if (event.type == 2) return;
        String message = event.message.getUnformattedText();

        if(event.message.getFormattedText().contains("§6> §e§lEXTRA STATS §6<") && SkyblockFeatures.config.autoReparty) {
            Reparty.doReparty();
        }

        if(SkyblockFeatures.config.autoAcceptReparty) {
            if (waitingForInvite && message.contains(partyLeader+" has invited you to join their party!")) {
                Utils.GetMC().thePlayer.sendChatMessage("/party accept " + partyLeader);
                resetListener();
            }

            if (message.contains(" has disbanded the party")) {
                String[] parts = message.split(" has disbanded the party");
                partyLeader = clearRanks(parts[0]);
                waitingForInvite = true;
                startTime = System.currentTimeMillis();
            }
        }

        long currentTime = System.currentTimeMillis();
        if (lastRepartyTime > 0 && currentTime - lastRepartyTime <= 20000) {
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

    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (waitingForInvite && hasTimeExpired()) {
                resetListener();
            }
        }
    }

    private void resetListener() {
        waitingForInvite = false;
        partyLeader = null;
        startTime = 0;
    }

    private boolean hasTimeExpired() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - startTime) >= 60000; // 60 seconds = 60000 milliseconds
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
        message = message.substring(15, message.length());

        List<String> memberNames = new ArrayList<>();
        for (String name : message.split(" ● ")) {
            memberNames.add(name);
        }

        return memberNames;
    }

    public static String clearRanks(String input) {
        Pattern pattern = Pattern.compile("\\[[^\\[\\]]+\\] ");
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll("");
    }


    public static void doReparty() {
        Utils.GetMC().thePlayer.sendChatMessage("/p list");
        lastRepartyTime = System.currentTimeMillis();
    }

}
