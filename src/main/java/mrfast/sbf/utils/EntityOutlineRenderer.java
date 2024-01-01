package mrfast.sbf.utils;

import mrfast.sbf.events.RenderEntityOutlineEvent;
import mrfast.sbf.mixins.transformers.CustomRenderGlobal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Modified from SkyblockAddons under MIT license
 * @link https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
 * @author BiscuitDevelopment
 */
public class EntityOutlineRenderer {
    private static final CachedInfo entityRenderCache = new CachedInfo();
    private static boolean stopLookingForOptifine = false;
    private static Method isFastRender = null;
    private static Method isShaders = null;
    private static Method isAntialiasing = null;
    private static Framebuffer swapBuffer = null;

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        CustomRenderGlobal renderGlobal = (CustomRenderGlobal) Utils.GetMC().renderGlobal;
        renderGlobal.sbf$entityOutlineFramebuffer().framebufferClear();
    }

    /**
     * @return a new framebuffer with the size of the main framebuffer
     */
    private static Framebuffer initSwapBuffer() {
        Framebuffer main = Minecraft.getMinecraft().getFramebuffer();
        Framebuffer framebuffer = new Framebuffer(main.framebufferTextureWidth, main.framebufferTextureHeight, true);
        framebuffer.setFramebufferFilter(GL11.GL_NEAREST);
        framebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        return framebuffer;
    }

    private static void updateFramebufferSize()
    {
        if (swapBuffer == null) {
            swapBuffer = initSwapBuffer();
        }
        int width = Minecraft.getMinecraft().displayWidth;
        int height = Minecraft.getMinecraft().displayHeight;
        if (swapBuffer.framebufferWidth != width || swapBuffer.framebufferHeight != height) {
            swapBuffer.createBindFramebuffer(width, height);
        }
        CustomRenderGlobal renderGlobal = (CustomRenderGlobal) Utils.GetMC().renderGlobal;

        Framebuffer outlineBuffer = renderGlobal.sbf$entityOutlineFramebuffer();
        if (outlineBuffer.framebufferWidth != width || outlineBuffer.framebufferHeight != height) {
            outlineBuffer.createBindFramebuffer(width, height);
            renderGlobal.sbf$entityOutlineShader().createBindFramebuffers(width, height);
        }
    }


    /**
     * Renders xray and no-xray entity outlines.
     *
     * @param camera       the current camera
     * @param partialTicks the progress to the next tick
     * @param x            the camera x position
     * @param y            the camera y position
     * @param z            the camera z position
     */
    public static boolean renderEntityOutlines(ICamera camera, float partialTicks, double x, double y, double z) {
        boolean shouldRenderOutlines = shouldRenderEntityOutlines();

        if (shouldRenderOutlines && !isCacheEmpty() && MinecraftForgeClient.getRenderPass() == 0) {
            Minecraft mc = Minecraft.getMinecraft();
            CustomRenderGlobal renderGlobal = (CustomRenderGlobal) mc.renderGlobal;
            RenderManager renderManager = mc.getRenderManager();

            mc.theWorld.theProfiler.endStartSection("entityOutlines");
            updateFramebufferSize();
            // Clear and bind the outline framebuffer

            renderGlobal.sbf$entityOutlineFramebuffer().framebufferClear();
            renderGlobal.sbf$entityOutlineFramebuffer().bindFramebuffer(false);

            // Vanilla options
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableFog();
            mc.getRenderManager().setRenderOutlines(true);

            // SBA options
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_REPLACE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL13.GL_CONSTANT);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_REPLACE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);


            // Render x-ray outlines first, ignoring the depth buffer bit
            if (!isXrayCacheEmpty()) {

                // Xray is enabled by disabling depth testing
                GlStateManager.depthFunc(GL11.GL_ALWAYS);
                for (Map.Entry<Entity, Integer> entityAndColor : entityRenderCache.xrayCache.entrySet()) {
                    // Test if the entity should render, given the player's camera position
                    if (shouldRender(camera, entityAndColor.getKey(), x, y, z)) {
                        try {
                            if (!(entityAndColor.getKey() instanceof EntityLivingBase)) {
                                outlineColor(entityAndColor.getValue());
                            } else {
                                setColor(new Color(entityAndColor.getValue()));
                            }

                            renderManager.renderEntityStatic(entityAndColor.getKey(), partialTicks, false);
                        } catch (Exception ignored) {
                        }
                    }
                }
                // Reset depth function
                GlStateManager.depthFunc(GL11.GL_LEQUAL);
            }
            // Render no-xray outlines second, taking into consideration the depth bit
            if (!isNoXrayCacheEmpty()) {
                if (!isNoOutlineCacheEmpty()) {
                    // Render other entities + terrain that may occlude an entity outline into a depth buffer
                    swapBuffer.framebufferClear();
                    copyBuffers(mc.getFramebuffer(), swapBuffer, GL11.GL_DEPTH_BUFFER_BIT);
                    swapBuffer.bindFramebuffer(false);
                    // Copy terrain + other entities depth into outline frame buffer to now switch to no-xray outlines
                    if (entityRenderCache.noOutlineCache != null) {
                        for (Entity entity : entityRenderCache.noOutlineCache) {
                            // Test if the entity should render, given the player's instantaneous camera position
                            if (shouldRender(camera, entity, x, y, z)) {
                                try {
                                    renderManager.renderEntityStatic(entity, partialTicks, true);
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }

                    // Copy the entire depth buffer of everything that might occlude outline to outline framebuffer
                    copyBuffers(swapBuffer, renderGlobal.sbf$entityOutlineFramebuffer(), GL11.GL_DEPTH_BUFFER_BIT);
                    renderGlobal.sbf$entityOutlineFramebuffer().bindFramebuffer(false);
                }
                // If there are no entities that can occlude the outlines, just copy the terrain depth buffer over
                else {
                    copyBuffers(mc.getFramebuffer(), renderGlobal.sbf$entityOutlineFramebuffer(), GL11.GL_DEPTH_BUFFER_BIT);
                }

                // Xray disabled by re-enabling traditional depth testing
                for (Map.Entry<Entity, Integer> entityAndColor : entityRenderCache.noXrayCache.entrySet()) {
                    // Test if the entity should render, given the player's instantaneous camera position
                    if (shouldRender(camera, entityAndColor.getKey(), x, y, z)) {
                        try {
                            if (!(entityAndColor.getKey() instanceof EntityLivingBase)) {
                                outlineColor(entityAndColor.getValue());
                            } else {
                                setColor(new Color(entityAndColor.getValue()));
                            }

                            renderManager.renderEntityStatic(entityAndColor.getKey(), partialTicks, true);
                        } catch (Exception ignored) {
                            ignored.printStackTrace();
                        }
                    }
                }
            }

            // SBA options
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL11.GL_TEXTURE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);

            // Vanilla options
            RenderHelper.enableStandardItemLighting();
            mc.getRenderManager().setRenderOutlines(false);

            // Load the outline shader
            GlStateManager.depthMask(false);
            renderGlobal.sbf$entityOutlineShader().loadShaderGroup(partialTicks);
            GlStateManager.depthMask(true);

            // Reset GL/framebuffers for next render layers
            GlStateManager.enableLighting();
            mc.getFramebuffer().bindFramebuffer(false);
            GlStateManager.enableFog();
            GlStateManager.enableBlend();
            GlStateManager.enableColorMaterial();
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();
        }

        return !shouldRenderOutlines;
    }

    private static void setColor(Color color) {
        GL11.glColor4d(
                (color.getRed()/255f),
                (color.getGreen()/255f),
                (color.getBlue()/255f),
                (color.getAlpha()/255f)
        );
        GL11.glDisable(GL11.GL_BLEND);
    }

    /**
     * Caches optifine settings and determines whether outlines should be rendered
     *
     * @return {@code true} iff outlines should be rendered
     */
    public static boolean shouldRenderEntityOutlines() {
        Minecraft mc = Minecraft.getMinecraft();
        CustomRenderGlobal renderGlobal = (CustomRenderGlobal) mc.renderGlobal;

        // Vanilla Conditions
        if (renderGlobal.sbf$entityOutlineFramebuffer() == null || renderGlobal.sbf$entityOutlineShader() == null || mc.thePlayer == null)
            return false;

        // Skyblock Conditions
        if (!Utils.inSkyblock) {
            return false;
        }

        // Optifine Conditions
        if (!stopLookingForOptifine && isFastRender == null) {
            try {
                Class<?> config = Class.forName("Config");

                try {
                    isFastRender = config.getMethod("isFastRender");
                    isShaders = config.getMethod("isShaders");
                    isAntialiasing = config.getMethod("isAntialiasing");
                } catch (Exception ex) {
                    System.out.println("Couldn't find Optifine methods for entity outlines.");
                    stopLookingForOptifine = true;
                }
            } catch (Exception ex) {
                System.out.println("Couldn't find Optifine for entity outlines.");
                stopLookingForOptifine = true;
            }
        }

        boolean isFastRenderValue = false;
        boolean isShadersValue = false;
        boolean isAntialiasingValue = false;
        if (isFastRender != null) {
            try {
                isFastRenderValue = (boolean) isFastRender.invoke(null);
                isShadersValue = (boolean) isShaders.invoke(null);
                isAntialiasingValue = (boolean) isAntialiasing.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                System.out.println("An error occurred while calling Optifine methods for entity outlines..."+ ex);
            }
        }

        return !isFastRenderValue && !isShadersValue && !isAntialiasingValue;
    }

    /**
     * Apply the same rendering standards as in {@link net.minecraft.client.renderer.RenderGlobal#renderEntities(Entity, ICamera, float)} lines 659 to 669
     *
     * @param camera the current camera
     * @param entity the entity to render
     * @param x      the camera x position
     * @param y      the camera y position
     * @param z      the camera z position
     * @return whether the entity should be rendered
     */
    private static boolean shouldRender(ICamera camera, Entity entity, double x, double y, double z) {
        Minecraft mc = Minecraft.getMinecraft();
        //if (considerPass && !entity.shouldRenderInPass(MinecraftForgeClient.getRenderPass())) {
        //    return false;
        //}
        // Only render the view entity when sleeping or in 3rd person mode mode
        if (entity == mc.getRenderViewEntity() &&
                !((mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping()) ||
                        mc.gameSettings.thirdPersonView != 0)) {
            return false;
        }
        // Only render if renderManager would render and the world is loaded at the entity
        return mc.theWorld.isBlockLoaded(new BlockPos(entity)) && (mc.getRenderManager().shouldRender(entity, camera, x, y, z) || entity.riddenByEntity == mc.thePlayer);
    }

    /**
     * Function that copies a portion of a framebuffer to another framebuffer.
     * <p>
     * Note that this requires GL3.0 to function properly
     * <p>
     * The major use of this function is to copy the depth-buffer portion of the world framebuffer to the entity outline framebuffer.
     * This enables us to perform no-xray outlining on entities, as we can use the world framebuffer's depth testing on the outline frame buffer
     *
     * @param frameToCopy   the framebuffer from which we are copying data
     * @param frameToPaste  the framebuffer onto which we are copying the data
     * @param buffersToCopy the bit mask indicating the sections to copy (see {@link GL11#GL_DEPTH_BUFFER_BIT}, {@link GL11#GL_COLOR_BUFFER_BIT}, {@link GL11#GL_STENCIL_BUFFER_BIT})
     */
    private static void copyBuffers(Framebuffer frameToCopy, Framebuffer frameToPaste, int buffersToCopy) {
        if (OpenGlHelper.isFramebufferEnabled()) {
            OpenGlHelper.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, frameToCopy.framebufferObject);
            OpenGlHelper.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, frameToPaste.framebufferObject);
            GL30.glBlitFramebuffer(0, 0, frameToCopy.framebufferWidth, frameToCopy.framebufferHeight,
                    0, 0, frameToPaste.framebufferWidth, frameToPaste.framebufferHeight,
                    buffersToCopy, GL11.GL_NEAREST);
        }
    }

    public static boolean isCacheEmpty() {
        return isXrayCacheEmpty() && isNoXrayCacheEmpty();
    }

    private static boolean isXrayCacheEmpty() {
        return entityRenderCache.xrayCache == null || entityRenderCache.xrayCache.isEmpty();
    }

    private static boolean isNoXrayCacheEmpty() {
        return entityRenderCache.noXrayCache == null || entityRenderCache.noXrayCache.isEmpty();
    }

    private static boolean isNoOutlineCacheEmpty() {
        return entityRenderCache.noOutlineCache == null || entityRenderCache.noOutlineCache.isEmpty();
    }

    private static boolean emptyLastTick = false;

    /**
     * Updates the cache at the start of every minecraft tick to improve efficiency.
     * Identifies and caches all entities in the world that should be outlined.
     * <p>
     * Calls to {@link #shouldRender(ICamera, Entity, double, double, double)} are frustum based, rely on partialTicks,
     * and so can't be updated on a per-tick basis without losing information.
     * <p>
     * This works since entities are only updated once per tick, so the inclusion or exclusion of an entity
     * to be outlined can be cached each tick with no loss of data
     *
     * @param event the client tick event
     */
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            CustomRenderGlobal renderGlobal = (CustomRenderGlobal) mc.renderGlobal;

            if (mc.theWorld != null && shouldRenderEntityOutlines()) {
                // These events need to be called in this specific order for the xray to have priority over the no xray
                // Get all entities to render xray outlines
                RenderEntityOutlineEvent xrayOutlineEvent = new RenderEntityOutlineEvent(RenderEntityOutlineEvent.Type.XRAY, null);
                MinecraftForge.EVENT_BUS.post(xrayOutlineEvent);
                // Get all entities to render no xray outlines, using pre-filtered entities (no need to test xray outlined entities)
                RenderEntityOutlineEvent noxrayOutlineEvent = new RenderEntityOutlineEvent(RenderEntityOutlineEvent.Type.NO_XRAY, xrayOutlineEvent.entitiesToChooseFrom);
                MinecraftForge.EVENT_BUS.post(noxrayOutlineEvent);
                // Cache the entities for future use
                entityRenderCache.xrayCache = (xrayOutlineEvent.entitiesToOutline);
                entityRenderCache.noXrayCache = (noxrayOutlineEvent.entitiesToOutline);
                entityRenderCache.noOutlineCache = (noxrayOutlineEvent.entitiesToChooseFrom);

                if (isCacheEmpty()) {
                    if (!emptyLastTick) {
                        renderGlobal.sbf$entityOutlineFramebuffer().framebufferClear();
                    }
                    emptyLastTick = true;
                } else {
                    emptyLastTick = false;
                }
            } else if (!emptyLastTick) {
                entityRenderCache.xrayCache = null;
                entityRenderCache.noXrayCache = null;
                entityRenderCache.noOutlineCache = null;
                if (renderGlobal.sbf$entityOutlineFramebuffer() != null) renderGlobal.sbf$entityOutlineFramebuffer().framebufferClear();
                emptyLastTick = true;
            }
        }
    }

    private static class CachedInfo {
        private HashMap<Entity, Integer> xrayCache = null;
        private HashMap<Entity, Integer> noXrayCache = null;
        private HashSet<Entity> noOutlineCache = null;
    }
    private static final FloatBuffer BUF_FLOAT_4 = BufferUtils.createFloatBuffer(4);
    private static void outlineColor(int color) {
        BUF_FLOAT_4.put(0, (float)(color >> 16 & 255) / 255.0F);
        BUF_FLOAT_4.put(1, (float)(color >> 8 & 255) / 255.0F);
        BUF_FLOAT_4.put(2, (float)(color & 255) / 255.0F);
        BUF_FLOAT_4.put(3, 1);

        GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, BUF_FLOAT_4);
    }

}