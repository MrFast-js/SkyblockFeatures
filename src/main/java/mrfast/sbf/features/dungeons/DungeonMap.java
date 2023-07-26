package mrfast.sbf.features.dungeons;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.TabListUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec4b;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DungeonMap {
	static String self = "";
	static double selfHeadPositionX = 0;
	static double selfHeadPositionY = 0;
	public static void renderOverlay() {
		if(!Utils.inDungeons) return;
        if(!SkyblockFeatures.config.dungeonMap) return;
		try {
			ItemStack[] items = Minecraft.getMinecraft().thePlayer.inventory.mainInventory;
			for (ItemStack item : items) {
				if (item != null) {
					if (item.getItem().isMap()) {
						if (item.getItem() instanceof ItemMap) {
							ItemMap mapitem = (ItemMap) item.getItem();
							mapData = mapitem.getMapData(item, Minecraft.getMinecraft().thePlayer.getEntityWorld());
						}
					}
				}
			}
			if (mapData == null) return;
		} catch (Error error) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Error loading map! Check your console!"));
			return;
		}
		int index = 0;
		for(Entry<String, Vec4b> decoration:mapData.mapDecorations.entrySet()) {
			index++;
			if(index == mapData.mapDecorations.size()) {
				self = decoration.getKey();
			}
		}

		try {
            Utils.drawGraySquareWithBorder(0, 0, 128, 128, 3);
			GlStateManager.pushMatrix();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			if(SkyblockFeatures.config.dungeonMapRotate) {
				GlStateManager.translate(64, 64, 0);
				GlStateManager.rotate(Utils.GetMC().thePlayer.rotationYawHead,0,0, 1);
				GlStateManager.translate(-64, -64, 0);
			}
			if(SkyblockFeatures.config.dungeonMapCenter) {
				GlStateManager.translate(128-selfHeadPositionX-64, 128-selfHeadPositionY-64, 0);
			}
			Minecraft.getMinecraft().entityRenderer.getMapItemRenderer().renderMap(mapData, true);

			drawPlayersOnMap();
			drawHeadOnMap();
			GlStateManager.popMatrix();
		} catch (Error error) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Error! Check your console!"));
		}
	}
	static MapData mapData;
	int seconds = 0;
	@SubscribeEvent
	public void onSecond(SecondPassedEvent event) {
		if(!Utils.inDungeons) return;
		seconds++;
		if(seconds==5) {
			seconds = 0;
			updateOffset = true;
		}
	}
	
	@SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
		mapData = null;
		playerHeadOffsetX = null;
		seconds = 0;
		playerHeadOffsetY = null;
		playerSkins.clear();
		playerNames.clear();
		dungeonTeammates.clear();
		farplayerPosition.clear();
		closePlayerPosition.clear();
    }

	public static class MapPosition {
	    double rotation = 0;
		double x = 0;
		double y = 0;
		public MapPosition(double x,double y, double rotation) {
			this.x = x;
			this.y = y;
			this.rotation = rotation;
		}
	}


	static HashMap<String, NetworkPlayerInfo> dungeonTeammates = new HashMap<String, NetworkPlayerInfo>();
	static HashMap<Integer, ResourceLocation> playerSkins = new HashMap<Integer, ResourceLocation>();
	static HashMap<Integer, String> playerNames = new HashMap<Integer, String>();
	static HashMap<String, MapPosition> farplayerPosition = new HashMap<>();
	static HashMap<String, MapPosition> closePlayerPosition = new HashMap<>();

	static Double playerHeadOffsetX = null;
	static Double playerHeadOffsetY = null;

	public static void drawPlayerNameOnMap(Double x,Double z,String name) {
		String shortName = name.length()>5?name.substring(0, 5):name;
		// Draw Username
		GlStateManager.pushMatrix();
		GlStateManager.scale(0.75f, 0.75f, 0);
		if(DungeonsFeatures.bloodguy!=null) {
			if(DungeonsFeatures.bloodguy.getName().contains(shortName)) {
				shortName = ChatFormatting.RED+shortName;
			}
		}
		Utils.GetMC().fontRendererObj.drawString(shortName, (float) (((x-2)-(Utils.GetMC().fontRendererObj.getStringWidth(shortName)/3))*1.33), (float) ((z-13)*1.33), 0xffffff, true);
		// ScreenRenderer.fontRenderer.drawString(shortName,(float) (((x-2)-(Utils.GetMC().fontRendererObj.getStringWidth(shortName)/3))*1.33), (float) ((z-13)*1.33),CommonColors.WHITE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NORMAL);
		GlStateManager.popMatrix();
	}

	public static void drawPlayersOnMap() {
		GlStateManager.pushMatrix();
		int i = 0;
		int k =0;
		int j = 0;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		float z = 1.0F;
		for (Entry<String,Vec4b> entry : mapData.mapDecorations.entrySet()) {
			Integer playerId = Integer.parseInt(entry.getKey().replaceAll("[^0-9]", ""));
			if(playerSkins.get(playerId)!=null) continue;
			Vec4b vec4b = entry.getValue();
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, z);
			GlStateManager.translate(i + vec4b.func_176112_b() / 2.0 + 64.0, j + vec4b.func_176113_c() / 2.0 + 64.0,-0.02);
			GlStateManager.rotate((vec4b.func_176111_d() * 360F) / 16.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.scale(4.0, 4.0, 1);
			GlStateManager.translate(-0.125, 0.125, 0.0);
			double b0 = vec4b.func_176110_a();
			double f1 = (b0 % 4) / 4.0;
			double f2 = (Math.floor(b0 / 4)) / 4.0;
			double f3 = (b0 % 4 + 1) / 4.0;
			double f4 = (Math.floor(b0 / 4) + 1) / 4.0;
			worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
			worldrenderer.pos(-1.0D, 1.0D, (double) ((float) k * -0.001F)).tex((double) f1, (double) f2).endVertex();
			worldrenderer.pos(1.0D, 1.0D, (double) ((float) k * -0.001F)).tex((double) f3, (double) f2).endVertex();
			worldrenderer.pos(1.0D, -1.0D, (double) ((float) k * -0.001F)).tex((double) f3, (double) f4).endVertex();
			worldrenderer.pos(-1.0D, -1.0D, (double) ((float) k * -0.001F)).tex((double) f1, (double) f4).endVertex();
			tessellator.draw();
			GlStateManager.popMatrix();
			k++;
			z++;
		}
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.scale(0.0F, 0.0F, -0.04F);
		GlStateManager.translate(1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	};

	// Draw head on map
	public static void DrawHead(Double x,Double z,ResourceLocation skin, Float rotation,String name) {
		drawPlayerNameOnMap(x,z,name);
		GlStateManager.pushMatrix();
		Minecraft.getMinecraft().getTextureManager().bindTexture(skin);
		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GlStateManager.translate(x, z, -0.02F);
		GlStateManager.rotate(rotation, 0.0F, 0.0F, 1.0F);
		GlStateManager.scale(SkyblockFeatures.config.dungeonMapHeadScale/100d, SkyblockFeatures.config.dungeonMapHeadScale/100d, 0);
		if(SkyblockFeatures.config.dungeonMapOutlineHeads) {
			Gui.drawRect(-8/2-1,-8/2-1, 8/2+1, 8/2+1, 0xff111111);
		}
		GlStateManager.color(1, 1, 1, 1);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(-8/2f, 8/2f, 30).tex(8/64f, 8/64f).endVertex();
		worldrenderer.pos(8/2f, 8/2f, 30).tex(16/64f, 8/64f).endVertex();
		worldrenderer.pos(8/2f, -8/2f, 30).tex(16/64f, 16/64f).endVertex();
		worldrenderer.pos(-8/2f, -8/2f, 30).tex(8/64f, 16/64f).endVertex();
		tessellator.draw();

		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(-8/2f, 8/2f, 30+0.001f).tex(8/64f+0.5f, 8/64f).endVertex();
		worldrenderer.pos(8/2f, 8/2f, 30+0.001f).tex(16/64f+0.5f, 8/64f).endVertex();
		worldrenderer.pos(8/2f, -8/2f, 30+0.001f).tex(16/64f+0.5f, 16/64f).endVertex();
		worldrenderer.pos(-8/2f, -8/2f, 30+0.001f).tex(8/64f+0.5f, 16/64f).endVertex();
		tessellator.draw();

		GlStateManager.popMatrix();
	}
	
	static Integer count = 0;
	static boolean updateOffset = false;
	
	public static void drawHeadOnMap() {
		int[] intArray = new int[]{5, 9, 13, 17, 1};
		List<NetworkPlayerInfo> tablist = TabListUtils.getTabEntries();
		count++;

		if(count == 200) {
			// System.out.println("----------------START-------------");
			// for(int i=0;i<intArray.length;i++) {
			// 	NetworkPlayerInfo player = tablist.get(intArray[i]);
			// 	if(player.getDisplayName().getUnformattedText().split(" ").length > 1) {
			// 		String name = Utils.cleanColor(player.getDisplayName().getUnformattedText().split(" ")[1]);
			// 		System.out.println("icon-"+i+" "+name);
			// 	}
			// }
			// System.out.println("----------------FINISH-------------");
			count = 0;
			for(int i=0;i<intArray.length;i++) {
				NetworkPlayerInfo player = tablist.get(intArray[i]);
				// Find out whos dead
				if(player.getDisplayName().getUnformattedText().split(" ").length > 1 && player.getDisplayName().getUnformattedText().contains("(DEAD)")) {
					String name = Utils.cleanColor(player.getDisplayName().getUnformattedText().split(" ")[1]);
					if(name != null && dungeonTeammates.containsKey("icon-"+i)) {	
						playerSkins.clear();
						playerNames.clear();
						dungeonTeammates.clear();
					}
				}
			}
		}
		for(int i=0;i<intArray.length;i++) {
			NetworkPlayerInfo player = tablist.get(intArray[i]);
			if(player.getDisplayName().getUnformattedText().split(" ").length > 1) {
				String name = Utils.cleanColor(player.getDisplayName().getUnformattedText().split(" ")[1]);
				if(name != null && !dungeonTeammates.containsKey("icon-"+i) && !player.getDisplayName().getUnformattedText().contains("(DEAD)")) {	
					dungeonTeammates.put("icon-"+i,player);
				}
			}
		}

		try {
		for(Entry<String,NetworkPlayerInfo> entry : dungeonTeammates.entrySet()) {
			// Icon-#
			String entrySelf = entry.getKey().replaceAll("[^0-9]", "");
			GlStateManager.pushMatrix();
			for (Entry<String,Vec4b> mapEntry : mapData.mapDecorations.entrySet()) {
				// Raw icon number
				Integer playerId = Integer.parseInt(mapEntry.getKey().replaceAll("[^0-9]", ""));
				// Draw self head
				if(self != "" && playerId == Integer.parseInt(self.replaceAll("[^0-9]", ""))) {
					EntityPlayer player = Utils.GetMC().thePlayer;
					if(player != null) {
						double x = (player.posX)/(mapData.scale*0.8);
						double z = (player.posZ)/(mapData.scale*0.8);
						double rotation = player.rotationYawHead;
						
						String shortName = player.getName().length()>5?player.getName().substring(0, 5):player.getName();
						AbstractClientPlayer aplayer = (AbstractClientPlayer) player;
						ResourceLocation skin = aplayer.getLocationSkin();
						if(playerHeadOffsetX == null || updateOffset) playerHeadOffsetX = Math.abs(x-Math.round((mapEntry.getValue().func_176112_b()/2)+64));
						if(playerHeadOffsetY == null || updateOffset) playerHeadOffsetY = Math.abs(z-Math.round((mapEntry.getValue().func_176113_c()/2)+64));
						if(updateOffset) updateOffset = false;

						if(closePlayerPosition.containsKey(shortName)) {
							x = closePlayerPosition.get(shortName).x-playerHeadOffsetX;
							z = closePlayerPosition.get(shortName).y-playerHeadOffsetY;
							rotation = closePlayerPosition.get(shortName).rotation;
							
							double newX = (player.posX)/(mapData.scale*0.8);
							double newZ = (player.posZ)/(mapData.scale*0.8);
							double newRotation = player.rotationYawHead;

							double deltaX = newX-x;
							double deltaZ = newZ-z;
							double deltaR = newRotation-rotation;

							x+=deltaX/50;
							z+=deltaZ/50;
							rotation+=deltaR/50;
						}
						x+=playerHeadOffsetX;
						z+=playerHeadOffsetY;
						selfHeadPositionX = x;
						selfHeadPositionY = z;
						
						if(skin != DefaultPlayerSkin.getDefaultSkin(aplayer.getUniqueID())) {
							closePlayerPosition.put(shortName,new MapPosition(x, z,rotation));
							float r = SkyblockFeatures.config.dungeonMapRotate?-player.rotationYawHead:player.rotationYawHead;
							if(SkyblockFeatures.config.dungeonMapCenter) {
								GlStateManager.translate(-(128-selfHeadPositionX-64), -(128-selfHeadPositionY-64), 0);
								DrawHead(64d,64d,skin,r,shortName);
								GlStateManager.translate((128-selfHeadPositionX-64), (128-selfHeadPositionY-64), 0);
							} else {
								DrawHead(x,z,skin,r,shortName);
							}
							GlStateManager.translate(-(128-selfHeadPositionX-64), -(128-selfHeadPositionY-64), 0);
							GlStateManager.translate((128-selfHeadPositionX-64), (128-selfHeadPositionY-64), 0);
						}
					}
				}

				// if # is same as the icon-#
				else if(playerId == Integer.parseInt(entrySelf)) {
					EntityPlayer player = Utils.GetMC().theWorld.getPlayerEntityByName(entry.getValue().getDisplayName().getUnformattedText().split(" ")[1]);
					if(player != null) {
						double x = (player.posX)/(mapData.scale*0.8);
						double z = (player.posZ)/(mapData.scale*0.8);
						double rotation = player.rotationYawHead;

						AbstractClientPlayer aplayer = (AbstractClientPlayer) player;
						ResourceLocation skin = aplayer.getLocationSkin();
						String shortName = "";

						if(playerNames.get(playerId) != null) {
							String name = playerNames.get(playerId);
							shortName = name.length()>5?name.substring(0, 5):name;
						}

						if(closePlayerPosition.containsKey(shortName)) {
							x = closePlayerPosition.get(shortName).x-playerHeadOffsetX;
							z = closePlayerPosition.get(shortName).y-playerHeadOffsetY;
							rotation = closePlayerPosition.get(shortName).rotation;
							
							double newX = (player.posX)/(mapData.scale*0.8);
							double newZ = (player.posZ)/(mapData.scale*0.8);
							double newRotation = player.rotationYawHead;

							double deltaX = newX-x;
							double deltaZ = newZ-z;
							double deltaR = newRotation-rotation;

							x+=deltaX/50;
							z+=deltaZ/50;
							rotation+=deltaR/50;

						}
						if(playerHeadOffsetX != null) x+=playerHeadOffsetX;
						if(playerHeadOffsetY != null) z+=playerHeadOffsetY;

						// Fancy Heads people close smooth
						if(skin != DefaultPlayerSkin.getDefaultSkin(aplayer.getUniqueID())) {
							playerSkins.put(playerId, skin);
							if(player.getName() != null) playerNames.put(playerId, player.getName());
							closePlayerPosition.put(shortName,new MapPosition(x, z,rotation));
							farplayerPosition.put(shortName,new MapPosition(x, z,rotation));
							DrawHead(x,z,skin,(float) rotation,shortName);
						}
					} else {
						// Draw skin just on the icons based off previous data
						if(playerSkins.get(playerId) != null) {
							String shortName = "";
							if(playerNames.get(playerId) != null) {
								String name = playerNames.get(playerId);
								shortName = name.length()>5?name.substring(0, 5):name;
							}
							double x = Math.round((mapEntry.getValue().func_176112_b()/2)+64);
							double z = Math.round((mapEntry.getValue().func_176113_c()/2)+64);
							double rotation = mapEntry.getValue().func_176111_d()* 360F;;
							if(farplayerPosition.containsKey(shortName)) {
								x = farplayerPosition.get(shortName).x;
								z = farplayerPosition.get(shortName).y;
								rotation = farplayerPosition.get(shortName).rotation;

								double newX = Math.round((mapEntry.getValue().func_176112_b()/2)+64);
								double newZ = Math.round((mapEntry.getValue().func_176113_c()/2)+64);
								double newRotation = mapEntry.getValue().func_176111_d()* 360F;
								double deltaX = newX-x;
								double deltaZ = newZ-z;
								double deltaR = newRotation-rotation;

								x+=deltaX/50;
								z+=deltaZ/50;
								rotation+=deltaR/50;

							}

							ResourceLocation skin = playerSkins.get(playerId);
							
							if(skin != null) {
								farplayerPosition.put(shortName,new MapPosition(x, z,rotation));
								closePlayerPosition.put(shortName,new MapPosition(x, z,rotation));
								DrawHead(x,z,skin,(float) ((rotation) / 16.0F),shortName);
							}
						}
					}
				}
			}
			GlStateManager.popMatrix();
			GlStateManager.enableBlend();
			GlStateManager.enableDepth();
		}
	} catch (Exception e) {
		System.out.println(e);
		// TODO: handle exception
	}
	}

	private static final Minecraft mc = Minecraft.getMinecraft();
    
    static {
        new DungeonMapMove();
    }   
    public static class DungeonMapMove extends UIElement {

        public DungeonMapMove() {
            super("DungeonMap", new Point(0, 5));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null) return;
			renderOverlay();
        }
        

        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null) return;
			Utils.drawTextWithStyle3(ChatFormatting.GREEN+"Dungeon Map", 50, 50);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.dungeonMap;
        }

        @Override
        public int getHeight() {
            return 128;
        }

        @Override
        public int getWidth() {
            return 128;
        }
    }
}
