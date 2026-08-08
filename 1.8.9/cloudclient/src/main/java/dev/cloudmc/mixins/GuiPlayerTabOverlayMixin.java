package dev.cloudmc.mixins;

import dev.cloudmc.Cloud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GuiPlayerTabOverlay.class)
public class GuiPlayerTabOverlayMixin {

    private static final ResourceLocation ICON_TEXTURE = 
        new ResourceLocation("cloudmc", "icon/nametag_icon.png");

    @Inject(
        method = "renderPlayerlist",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I",
            ordinal = 0
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void renderPlayerlist(int width, int height, CallbackInfo ci,
                                   Minecraft mc, java.util.List<?> players,
                                   int i, int j, int k, int l,
                                   int x, int y, int slotHeight,
                                   int nameWidth, int pingWidth) {
        boolean nameTagToggled = Cloud.INSTANCE.modManager.getMod("NameTag").isToggled();
        if (!nameTagToggled) return;

        try {
            GlStateManager.pushMatrix();
            mc.getTextureManager().bindTexture(ICON_TEXTURE);

            int iconSize = 8;
            int xPos = x - iconSize - 2;
            int yPos = y + 2;

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(xPos, yPos + iconSize, 0.0D).tex(0, 1).endVertex();
            worldrenderer.pos(xPos + iconSize, yPos + iconSize, 0.0D).tex(1, 1).endVertex();
            worldrenderer.pos(xPos + iconSize, yPos, 0.0D).tex(1, 0).endVertex();
            worldrenderer.pos(xPos, yPos, 0.0D).tex(0, 0).endVertex();
            tessellator.draw();

            GlStateManager.popMatrix();
        } catch (Exception e) {
            // 忽略
        }
    }
}
