package dev.cloudmc.mixins;

import dev.cloudmc.Cloud;
import dev.cloudmc.feature.mod.impl.FreelookMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Render.class)
public abstract class RenderMixin {

    @Final @Shadow
    protected RenderManager renderManager;

    @Shadow
    public FontRenderer getFontRendererFromRenderManager() {
        return null;
    }

    // 🔥 你的图标资源路径（改成你的实际图片名）
    private static final ResourceLocation ICON_TEXTURE = 
        new ResourceLocation("cloudmc", "icon/nametag_icon.png");

    /**
     * @author duplicat
     * @reason NameTag tweaks
     */
    @Overwrite
    protected void renderLivingLabel(Entity entityIn, String str, double x, double y, double z, int maxDistance) {
        double d0 = entityIn.getDistanceSqToEntity(this.renderManager.livingPlayer);

        boolean nameTagToggled = Cloud.INSTANCE.modManager.getMod("NameTag").isToggled();
        int color = Cloud.INSTANCE.settingManager.getSettingByModAndName("NameTag", "Font Color").getColor().getRGB();
        float alpha = nameTagToggled ? Cloud.INSTANCE.settingManager.getSettingByModAndName("NameTag", "Opacity").getCurrentNumber() / 255f : 0.25f;
        float scale = nameTagToggled ? Cloud.INSTANCE.settingManager.getSettingByModAndName("NameTag", "Size").getCurrentNumber() : 1;
        float yPos = nameTagToggled ? Cloud.INSTANCE.settingManager.getSettingByModAndName("NameTag", "Y Position").getCurrentNumber() - 2.5f : 0;

        if (d0 <= (double) (maxDistance * maxDistance)) {
            FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
            float f = 1.6F;
            float f1 = 0.016666668F * f;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x + 0.0F, (float) y + entityIn.height + 0.5F + yPos, (float) z);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);

            // 🔥 FreeLook 激活时使用 FreeLook 的摄像机角度
            if (FreelookMod.cameraToggled) {
                GlStateManager.rotate(-FreelookMod.cameraYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(FreelookMod.cameraPitch, 1.0F, 0.0F, 0.0F);
            } else {
                GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            }

            GlStateManager.scale(-f1 * scale, -f1 * scale, f1);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            int i = 0;

            if (str.equals("deadmau5")) {
                i = -10;
            }

            int j = fontrenderer.getStringWidth(str) / 2;

            // 🔥 绘制头顶图标（在名字左侧）
            if (nameTagToggled) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5F, 0.5F, 1.0F);

                Minecraft.getMinecraft().getTextureManager().bindTexture(ICON_TEXTURE);

                int iconSize = 16;
                int xPos = (-j - iconSize - 4) * 2;
                int yPosIcon = (i - 2) * 2;

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos(xPos, yPosIcon + iconSize, 0.0D).tex(0, 1).endVertex();
                worldrenderer.pos(xPos + iconSize, yPosIcon + iconSize, 0.0D).tex(1, 1).endVertex();
                worldrenderer.pos(xPos + iconSize, yPosIcon, 0.0D).tex(1, 0).endVertex();
                worldrenderer.pos(xPos, yPosIcon, 0.0D).tex(0, 0).endVertex();
                tessellator.draw();

                GlStateManager.popMatrix();
            }

            // 渲染名字背景阴影
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.disableTexture2D();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(-j - 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, alpha).endVertex();
            worldrenderer.pos(-j - 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, alpha).endVertex();
            worldrenderer.pos(j + 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, alpha).endVertex();
            worldrenderer.pos(j + 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, alpha).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();

            // 渲染名字
            fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, nameTagToggled ? color : 553648127);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, nameTagToggled ? color : -1);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }
}
