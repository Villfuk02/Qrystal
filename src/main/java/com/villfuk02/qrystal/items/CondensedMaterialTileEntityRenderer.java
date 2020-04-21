package com.villfuk02.qrystal.items;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;

public class CondensedMaterialTileEntityRenderer extends ItemStackTileEntityRenderer {
    
    
    @Override
    public void render(ItemStack stack, MatrixStack matrix, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if(stack.getItem() != ModItems.CONDENSED_MATERIAL)
            return;
        matrix.push();
        matrix.translate(0.5, 0.5, 0.5);
        if(stack.hasTag() && stack.getTag().contains("item") && stack.getTag().contains("power") && !ItemStack.read(stack.getTag().getCompound("item")).isEmpty()) {
            matrix.push();
            Quaternion q = Vector3f.YN.rotationDegrees(225);
            q.multiply(Vector3f.XN.rotationDegrees(30));
            matrix.rotate(q);
            matrix.scale(1.2f, 1.2f, 1.2f);
            Minecraft.getInstance()
                    .getItemRenderer()
                    .renderItem(ItemStack.read(stack.getTag().getCompound("item")), ItemCameraTransforms.TransformType.GUI, combinedLightIn, combinedOverlayIn, matrix, bufferIn);
            matrix.pop();
        }
        Minecraft.getInstance()
                .getItemRenderer()
                .renderItem(RecipeUtil.getStackWithTag(ModItems.CONDENSED_MATERIAL_CAGE_RENDERER, 1, stack.getTag()), ItemCameraTransforms.TransformType.NONE, combinedLightIn, combinedOverlayIn, matrix,
                            bufferIn);
        matrix.pop();
    }
}
