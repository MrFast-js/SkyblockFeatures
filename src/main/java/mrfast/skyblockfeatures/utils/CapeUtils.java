package mrfast.skyblockfeatures.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import mrfast.skyblockfeatures.SkyblockFeatures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapeUtils {
    public static HashMap<String, String> final_name_list = get_names();

    @SubscribeEvent
    public void worldLoadEvent(WorldEvent.Load event) {
        reloadCapes();
    }

    public static void reloadCapes() {
        try {
            final_name_list = get_names();
        } catch(Exception e) {

        }
    }

    public static HashMap<String, String> get_names() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/MrFast-js/skyblockFeatures-Capes/main/capes.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            final HashMap<String, String> name_list = new HashMap<>();
            String s;

            while ((s = reader.readLine()) != null) {
                String name = s.toLowerCase().split(" ")[0];
                String capeName = s.split(" ")[1];
                name_list.put(name, capeName);
            }
            if(SkyblockFeatures.config.playerCapeURL!="") {
                if(convertImageToResourceLocation(SkyblockFeatures.config.playerCapeURL)!=null) {
                    name_list.put(Utils.GetMC().thePlayer.getName().toLowerCase(), SkyblockFeatures.config.playerCapeURL);
                }
            }
            return name_list;
        } catch (Exception ignored){
            return null;
        }
    }

    static HashMap<String,ResourceLocation> capes = new HashMap<>();
    static List<String> capesFetching = new ArrayList<>();
    public static ResourceLocation convertImageToResourceLocation(String imageUrl) {
        if(imageUrl==null) return null;
        if(capes.containsKey(imageUrl)) {
            return capes.get(imageUrl);
        }
        try {
            URL url = new URL(imageUrl);
            InputStream inputStream = url.openStream();
            BufferedImage image = ImageIO.read(inputStream);
            DynamicTexture dynamicTexture = new DynamicTexture(image);
            ResourceLocation resourceLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("image_" + imageUrl.hashCode(), dynamicTexture);
            capes.put(imageUrl,resourceLocation);
            capesFetching.remove(imageUrl);
            return resourceLocation;
        } catch (IOException e) {
            // e.printStackTrace();
        }
        return null;
    }
    // static int index = 0;
    // static float deciindex = 1f;
    // For maybe gifs in the future
    // public static ResourceLocation convertSpriteSheetToResourceLocation(String spriteSheetUrl) {
    //     if (spriteSheetUrl == null) return null;
    //     System.out.println("index "+index);
    //     deciindex+=0.025;
    //     index = Math.round(deciindex)-1;
    //     if(index==9) {
    //         index = 0;
    //         deciindex = 1f;
    //     }
    //     if (capes.containsKey(spriteSheetUrl + "_"+index)) {
    //         return capes.get(spriteSheetUrl + "_"+index);
    //     }
    //     try {
    //         URL url = new URL(spriteSheetUrl);
    //         BufferedImage image = ImageIO.read(url.openStream());
    //         int spriteWidth = 2047; // Width of each sprite in the sprite sheet
    //         int spriteHeight = 1024; // Height of each sprite in the sprite sheet
    //         int numSpritesPerRow = image.getWidth() / spriteWidth;

    //         // Loop through each sprite in the sprite sheet and create a dynamic texture for it
    //         for (int x = 0; x < numSpritesPerRow; x++) {
    //             BufferedImage spriteImage = image.getSubimage(x * spriteWidth, 0, spriteWidth, spriteHeight);
    //             DynamicTexture dynamicTexture = new DynamicTexture(spriteImage);
    //             ResourceLocation resourceLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("image_" + spriteSheetUrl.hashCode() + "_" + x, dynamicTexture);
    //             capes.put(spriteSheetUrl + "_" + x, resourceLocation);
    //         }
    
    //         return capes.get(spriteSheetUrl + "_"+index); // Return the resource location of the first sprite
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    //     return null;
    // }
    

    public static ResourceLocation getCape(String name) {
        name = name.toLowerCase();

        return convertImageToResourceLocation(final_name_list.get(name));
    }

    public static boolean is_name_valid(String name) {
        if(final_name_list==null) return false;
        if(name==null) return false;
        for (String u : final_name_list.keySet()) {
            if (u.contains(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
