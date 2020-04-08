package com.villfuk02.qrystal.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;

public class CutterTileEntityRenderer extends TileEntityRenderer<CutterTileEntity> {
    
    public CutterTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
        super(p_i226006_1_);
        
    }
    
    @Override
    public void render(CutterTileEntity tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int backupLight) {
        matrixStack.push();
        matrixStack.translate(0.5d, 0, 0.5d);
        float angle = (tileEntity.getBlockState().get(HorizontalBlock.HORIZONTAL_FACING)).rotateY().getHorizontalAngle();
        matrixStack.rotate(Vector3f.YP.rotationDegrees(-angle));
        if(!tileEntity.inventory.getStackInSlot(0).isEmpty()) {
            ItemStack stack = tileEntity.inventory.getStackInSlot(0);
            matrixStack.push();
            matrixStack.translate(0, 0.75d, 0);
            matrixStack.scale(1.2f, 1.2f, 0.4f);
            if(tileEntity.totalTime > 0) {
                Quaternion rot = Vector3f.ZP.rotationDegrees(((tileEntity.getWorld().getGameTime() + partialTicks) * -72) % 360);
                matrixStack.rotate(rot);
            }
            Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GROUND, 15728880, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
            matrixStack.pop();
        }
        if(!tileEntity.inventory.getStackInSlot(1).isEmpty()) {
            ItemStack stack = tileEntity.inventory.getStackInSlot(1).copy();
            stack.setCount(1);
            matrixStack.push();
            if(tileEntity.totalTime > 0)
                matrixStack.translate(0, 0.6875d - 0.05d - 0.3d * tileEntity.time / (double)tileEntity.totalTime, 0);
            else
                matrixStack.translate(0, 0.6875d - 0.05d, 0);
            Quaternion rot = Vector3f.YP.rotationDegrees(90);
            matrixStack.rotate(rot);
            Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GROUND, 15728880, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
            matrixStack.pop();
        }
        matrixStack.pop();
    }
}
