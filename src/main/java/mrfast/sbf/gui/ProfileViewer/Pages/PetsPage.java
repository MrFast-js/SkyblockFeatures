package mrfast.sbf.gui.ProfileViewer.Pages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.components.UIBlock;
import gg.essential.elementa.components.UIImage;
import gg.essential.elementa.components.UIRoundedRectangle;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.constraints.*;
import mrfast.sbf.gui.ProfileViewer.ProfileViewerGui;
import mrfast.sbf.gui.ProfileViewer.ProfileViewerUtils;
import mrfast.sbf.utils.ItemRarity;
import mrfast.sbf.utils.NetworkUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PetsPage extends ProfileViewerGui.ProfileViewerPage {
    @Override
    public void loadPage() {
        UIComponent loadingText = new UIText(ChatFormatting.RED + "Loading")
                .setTextScale(new PixelConstraint(2f))
                .setChildOf(this.mainComponent)
                .setX(new CenterConstraint())
                .setY(new CenterConstraint());
        String selectedProfileUUID = ProfileViewerGui.selectedProfileUUID;
        String playerUUID = ProfileViewerGui.playerUuid;

        new Thread(() -> {
            // Wait for skycrypt before displaying the page
            double loadingIndex = 0;
            JsonObject SkycryptProfiles = NetworkUtils.getJSONResponse("https://sky.shiiyu.moe/api/v2/profile/" + NetworkUtils.getName(playerUUID)).get("profiles").getAsJsonObject();

            while (SkycryptProfiles.entrySet().isEmpty()) {
                try {
                    ((UIText) loadingText).setText(ProfileViewerUtils.loadingStages[(int) Math.floor(loadingIndex)]);
                    loadingIndex += 0.5;
                    loadingIndex %= 3;
                    Thread.sleep(100);
                } catch (Exception ignored) {
                }
            }
            System.out.println(selectedProfileUUID + " ");
            for (Map.Entry<String, JsonElement> stringJsonElementEntry : SkycryptProfiles.entrySet()) {
                System.out.println(stringJsonElementEntry.getKey());
            }
            JsonObject skycryptPetsObject = SkycryptProfiles.get(selectedProfileUUID).getAsJsonObject()
                    .get("data").getAsJsonObject()
                    .get("pets").getAsJsonObject();

            JsonArray skycryptPetList = skycryptPetsObject.get("pets").getAsJsonArray();
            loadingText.parent.removeChild(loadingText);

            float index = -1;

            UIComponent otherPetsContainer = new UIBlock(new Color(0, 0, 0, 0)).setY(new SiblingConstraint(10f)).setHeight(new ChildBasedSizeConstraint()).setWidth(new RelativeConstraint(1f)).setChildOf(this.mainComponent);
            new UIText("§lActive Pet").setChildOf(otherPetsContainer).setY(new PixelConstraint(2f)).setX(new PixelConstraint(1f)).setTextScale(new PixelConstraint((float) (1f)));
            if (!ProfileViewerGui.selectedCategory.equals("Pets")) return;

            for (JsonElement petElement : skycryptPetList) {
                if (petElement == null) continue;
                JsonObject pet = petElement.getAsJsonObject();
                String texturePath = pet.get("texture_path").getAsString();
                CompletableFuture<BufferedImage> imageFuture = new CompletableFuture<>();
                List<String> tooltip = parseLore(pet.get("lore").getAsString());

                // Download the image from the remote URL using CompletableFuture and URLConnection
                CompletableFuture.runAsync(() -> {
                    try {
                        URL imageUrl = new URL("https://sky.shiiyu.moe" + texturePath);
                        HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                        BufferedImage image = ImageIO.read(connection.getInputStream());

                        imageFuture.complete(image);
                    } catch (Exception e) {
                        imageFuture.completeExceptionally(e);
                    }
                });

                String name = pet.get("display_name").getAsString();
                int lvl = pet.get("level").getAsJsonObject().get("level").getAsInt();
                String coloredName = ChatFormatting.GRAY + "[Lvl 0] Unknown Pet";
                String tier = pet.get("tier").getAsString();
                try {
                    coloredName = ChatFormatting.GRAY + "[Lvl " + lvl + "] " + ItemRarity.getRarityFromName(tier).getBaseColor() + name;
                } catch (Exception ignored) {
                }
                tooltip.add(0, coloredName);

                if (index == -1) {
                    index++;
                    // Create the pet
                    UIComponent petComponent = createPet(imageFuture, lvl, getPetColor(tier))
                            .setChildOf(otherPetsContainer)
                            .setX(new PixelConstraint(0f))
                            .setY(new SiblingConstraint(3f));

                    // Other Pets Label
                    new UIText("§lOther pets")
                            .setChildOf(otherPetsContainer)
                            .setY(new SiblingConstraint(13f))
                            .setX(new PixelConstraint(1f))
                            .setTextScale(new PixelConstraint(1f));

                    this.hoverables.put(petComponent, tooltip);
                    continue;
                }
                float x = (float) ((index - (Math.floor(index / 16f) * 16)) * 30f);
                float y = (float) (Math.floor(index / 16f) * 35f) + 63;
                UIComponent petComponent = createPet(imageFuture, lvl, getPetColor(tier)).setChildOf(otherPetsContainer).setX(new PixelConstraint(x)).setY(new PixelConstraint(y));

                this.hoverables.put(petComponent, tooltip);
                otherPetsContainer.setHeight(new PixelConstraint(45f * (Math.round((float) this.hoverables.size() / 16) + 1)));
                index++;
            }
        }).start();
    }

    public static List<String> parseLore(String lore) {
        List<String> loreList = new ArrayList<>();
        String[] loreRows = lore.split("<span class=\"lore-row wrap\">");

        for (String loreRow : loreRows) {
            String cleanedLoreRow = ProfileViewerUtils.cleanLoreRow(loreRow);
            if (cleanedLoreRow.contains("-----") || cleanedLoreRow.contains("MAX LEVEL")) {
                loreList.add(cleanedLoreRow);
                break;
            } else if (!cleanedLoreRow.isEmpty()) {
                String[] splitLore = splitLongLoreRow(cleanedLoreRow);
                loreList.addAll(Arrays.asList(splitLore));
            }
        }

        return loreList;
    }

    public static String[] splitLongLoreRow(String loreRow) {
        if (loreRow.length() <= 34) {
            return new String[]{loreRow};
        } else {
            List<String> splitLoreList = new ArrayList<>();
            int index = 0;
            String formattingColor = "";
            while (index < loreRow.length()) {
                int endIndex = Math.min(index + 34, loreRow.length());
                if (endIndex < loreRow.length() && !Character.isWhitespace(loreRow.charAt(endIndex))) {
                    while (endIndex > index && !Character.isWhitespace(loreRow.charAt(endIndex))) {
                        endIndex--;
                    }
                }
                String splitLore = loreRow.substring(index, endIndex);
                if (formattingColor.isEmpty() && splitLore.startsWith(" ")) {
                    splitLore = "§7" + splitLore.trim();
                } else if (!formattingColor.isEmpty()) {
                    splitLore = formattingColor + splitLore;
                }
                splitLoreList.add(splitLore);
                index = endIndex;

                if (index < loreRow.length() && loreRow.charAt(index) == '§') {
                    formattingColor = loreRow.substring(index, index + 2);
                    index += 2;
                } else {
                    formattingColor = "";
                }
            }
            return splitLoreList.toArray(new String[0]);
        }
    }

    public static UIComponent createPet(CompletableFuture<BufferedImage> texture, int lvl, Color color) {
        UIComponent background = new UIRoundedRectangle(5f).setColor(color).setWidth(new PixelConstraint(128f / 5)).setHeight(new PixelConstraint(128f / 5));
        new UIImage(texture).setChildOf(background).setX(new CenterConstraint()).setY(new CenterConstraint()).setWidth(new PixelConstraint(128f / 5)).setHeight(new PixelConstraint(120f / 5));
        new UIText("LVL " + lvl).setChildOf(background).setY(new SiblingConstraint(2f)).setX(new CenterConstraint()).setTextScale(new PixelConstraint(0.5f));
        return background;
    }

    public static Color getPetColor(String tier) {
        Color color = new Color(0x929292);
        if (tier.equals("UNCOMMON")) color = new Color(0x40bb40);
        if (tier.equals("RARE")) color = new Color(0x4444f3);
        if (tier.equals("EPIC")) color = new Color(0xa305a3);
        if (tier.equals("LEGENDARY")) color = new Color(0xd88f07);
        if (tier.equals("MYTHIC")) color = new Color(0xFF55FF);
        return color;
    }

    public PetsPage(UIComponent main) {
        super(main);
    }
}
