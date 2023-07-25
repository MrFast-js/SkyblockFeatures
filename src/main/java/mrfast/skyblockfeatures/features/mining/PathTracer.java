package mrfast.skyblockfeatures.features.mining;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.utils.RenderUtil;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class PathTracer {
    public static List<Vec3> pathPoints = new ArrayList<>();
    public static HashMap<String,List<Vec3>> pathsAndPoints = new HashMap<>();
    public static boolean recordingMovement = false;
    public static boolean creatingPath = false;

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if(Utils.GetMC().thePlayer!=null && recordingMovement) {
            Vec3 Position = Utils.GetMC().thePlayer.getPositionVector().add(new Vec3(0,0.2,0));
            if(Position!=null && pathPoints!=null) {
                if(!pathPoints.contains(Position)) {
                    pathPoints.add(Position);
                }
            }
        }
    }
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if(Utils.GetMC().thePlayer!=null && pathPoints!=null) {
            GlStateManager.disableDepth();
            Vec3 previousPoint = null;
            for(Vec3 point:pathPoints) {
                if(point == null) continue;
                if(previousPoint==null) {
                    previousPoint = point;
                    continue;
                } else {
                    if(previousPoint.distanceTo(point)>3) {
                        RenderUtil.draw3DLine(previousPoint, point, 2, new Color(0x4030d1), event.partialTicks);
                    } else {
                        RenderUtil.draw3DLine(previousPoint, point, 2, new Color(0xde473c), event.partialTicks);
                    }
                    previousPoint = point;
                }
            }
            GlStateManager.enableDepth();
        }
    }

    public PathTracer() {
        saveFile = new File(SkyblockFeatures.modDir, "paths.json");
        readConfig("");
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static File saveFile;

    public static void readConfig(String name) {
        if(Utils.GetMC().thePlayer==null) return;
        JsonObject file;
        try (FileReader in = new FileReader(saveFile)) {
            file = gson.fromJson(in, JsonObject.class);
            for (Map.Entry<String, JsonElement> e : file.entrySet()) {
                try {
                    JsonArray a = e.getValue().getAsJsonArray();
                    List<Vec3> list = new ArrayList<>();
                    for(JsonElement c:a) {
                        JsonObject d = c.getAsJsonObject();
                        Vec3 position = new Vec3(d.get("field_72450_a").getAsFloat(), d.get("field_72448_b").getAsFloat(),d.get("field_72449_c").getAsFloat());
                        list.add(position);
                    }
                    pathsAndPoints.put(e.getKey(), list);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            if(name!="") {
                pathPoints = pathsAndPoints.get(name);
                Utils.SendMessage("§3Loaded path §a"+name);
            }
        } catch (Exception e) {
            e.printStackTrace();
            pathsAndPoints = new HashMap<>();
            try (FileWriter writer = new FileWriter(saveFile)) {
                gson.toJson(pathsAndPoints, writer);
            } catch (Exception ignored) {

            }
        }
    }

    public static void savePath(String pathName) {
        pathsAndPoints.put(pathName, pathPoints);
        Utils.SendMessage("§3Saved path §a"+pathName);
        PathTracer.saveConfig();
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(saveFile)) {
            gson.toJson(pathsAndPoints, writer);
        } catch (Exception ignored) {

        }
    }
}
