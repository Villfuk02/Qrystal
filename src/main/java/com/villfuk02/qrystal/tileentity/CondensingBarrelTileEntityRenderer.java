package com.villfuk02.qrystal.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;

public class CondensingBarrelTileEntityRenderer extends TileEntityRenderer<CondensingBarrelTileEntity> {
    
    public CondensingBarrelTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
        super(p_i226006_1_);
        
    }
    
    @Override
    public void render(CondensingBarrelTileEntity tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay) {
        matrixStack.push();
        matrixStack.translate(0.5d, 0, 0.5d);
        for(int i = 0; i < 4; i++) {
            matrixStack.rotate(Vector3f.YP.rotationDegrees(-i * 90));
            if(!tileEntity.item.isEmpty()) {
                ItemStack stack;
                if(Minecraft.getInstance().player.isShiftKeyDown())
                    stack = tileEntity.getStack(true).copy();
                else
                    stack = tileEntity.item.copy();
                stack.setCount(1);
                matrixStack.push();
                matrixStack.translate(0, 0.625d, 0.53125d);
                matrixStack.scale(0.625f, 0.625f, 0.625f);
                Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
                matrixStack.pop();
            }
            matrixStack.push();
            matrixStack.translate(0, 0.2d, -0.501d);
            matrixStack.scale(-0.009f, -0.009f, -0.009f);
            FontRenderer fontrenderer = renderDispatcher.getFontRenderer();
            String s = tileEntity.getRenderedText(Minecraft.getInstance().player.isShiftKeyDown());
            fontrenderer.renderString(s, fontrenderer.getStringWidth(s) / -2f, 0, 0, false, matrixStack.getLast().getMatrix(), buffer, false, 0, light);
            s = tileEntity.getSecondaryRenderedText(Minecraft.getInstance().player.isShiftKeyDown());
            fontrenderer.renderString(s, fontrenderer.getStringWidth(s) / -2f, -8, 0, false, matrixStack.getLast().getMatrix(), buffer, false, 0, light);
            matrixStack.pop();
        }
        matrixStack.pop();
    }
}
