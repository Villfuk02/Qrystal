package com.villfuk02.qrystal.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.util.CrystalUtil;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class DryerTileEntityRenderer extends TileEntityRenderer<DryerTileEntity> {
    
    public DryerTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
        super(p_i226006_1_);
        
    }
    
    @Override
    public void render(DryerTileEntity tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int backupLight) {
        matrixStack.push();
        matrixStack.translate(0.5d, 0, 0.5d);
        int[] crystals = tileEntity.getCrystalData();
        String mat = tileEntity.getMaterial();
        if(crystals[4] > 0) {
            ItemStack stack = RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(1, CrystalUtil.Size.SEED), mat);
            for(int i = 0; i < Math.min(crystals[4], 10); i++)
                renderCrystal(tileEntity, matrixStack, "x" + i, 0.175f, stack, light, buffer);
        }
        if(crystals[0] > 0) {
            ItemStack stack = RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(1, CrystalUtil.Size.SMALL), mat);
            for(int i = 0; i < Math.min(crystals[0], 10); i++) {
                renderCrystal(tileEntity, matrixStack, "n" + i, 0.225f, stack, light, buffer);
            }
        }
        if(crystals[1] > 0) {
            ItemStack stack = RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(0, CrystalUtil.Size.LARGE), mat);
            for(int i = 0; i < Math.min(crystals[1], 10); i++) {
                renderCrystal(tileEntity, matrixStack, "l" + i, 0.25f, stack, light, buffer);
            }
        }
        if(crystals[2] > 0) {
            ItemStack stack = RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(0, CrystalUtil.Size.MEDIUM), mat);
            for(int i = 0; i < Math.min(crystals[2], 10); i++) {
                renderCrystal(tileEntity, matrixStack, "m" + i, 0.2f, stack, light, buffer);
            }
        }
        if(crystals[3] > 0) {
            ItemStack stack = RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(0, CrystalUtil.Size.SMALL), mat);
            for(int i = 0; i < Math.min(crystals[3], 10); i++) {
                renderCrystal(tileEntity, matrixStack, "s" + i, 0.15f, stack, light, buffer);
            }
        }
        if(tileEntity.getWaterLevel() > 0) {
            int mc = tileEntity.getMaterialColor();
            int mr = mc >> 16 & 255;
            int mg = mc >> 8 & 255;
            int mb = mc & 255;
            int wc = Fluids.WATER.getAttributes().getColor();
            int wr = wc >> 16 & 255;
            int wg = wc >> 8 & 255;
            int wb = wc & 255;
            float concentration = tileEntity.getConcentration() * 0.75f;
            int r = (int)(wr * (1 - concentration) + mr * concentration);
            int g = (int)(wg * (1 - concentration) + mg * concentration);
            int b = (int)(wb * (1 - concentration) + mb * concentration);
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("color", (r << 16) + (g << 8) + b);
            ItemStack stack = RecipeUtil.getStackWithTag(ModItems.SURFACE_RENDERER, tag);
            matrixStack.push();
            matrixStack.translate(0, 0.125f + 0.375f * tileEntity.getWaterLevel(), 0);
            matrixStack.scale(0.75f, 1, 0.75f);
            Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.NONE, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
            matrixStack.pop();
        }
        matrixStack.pop();
    }
    
    private void renderCrystal(DryerTileEntity tileEntity, MatrixStack matrixStack, String prefix, float y, ItemStack stack, int light, IRenderTypeBuffer buffer) {
        matrixStack.push();
        matrixStack.translate(tileEntity.getRenderValueSquared(prefix + "x") * 0.3d, y, tileEntity.getRenderValueSquared(prefix + "z") * 0.3d);
        Quaternion rot = Vector3f.ZP.rotationDegrees(tileEntity.getRenderValueSquared(prefix + "c") * 30);
        rot.multiply(Vector3f.XP.rotationDegrees(tileEntity.getRenderValueSquared(prefix + "a") * 30));
        rot.multiply(Vector3f.YP.rotationDegrees(tileEntity.getRenderValueSquared(prefix + "b") * 180));
        matrixStack.rotate(rot);
        matrixStack.scale(0.5f, 0.5f, 0.5f);
        Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.FIXED, light, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
        matrixStack.pop();
    }
}
