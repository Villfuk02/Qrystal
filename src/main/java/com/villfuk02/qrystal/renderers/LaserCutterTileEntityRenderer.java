package com.villfuk02.qrystal.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.villfuk02.qrystal.tileentity.LaserCutterTileEntity;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class LaserCutterTileEntityRenderer extends TileEntityRenderer<LaserCutterTileEntity> {
    
    public static final ResourceLocation TEXTURE_BEACON_BEAM = new ResourceLocation("textures/entity/beacon_beam.png");
    
    public LaserCutterTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
        super(p_i226006_1_);
        
    }
    
    @Override
    public void render(LaserCutterTileEntity tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int backupLight) {
        matrixStack.push();
        matrixStack.translate(0.5d, 0, 0.5d);
        float angle = (tileEntity.getBlockState().get(HorizontalBlock.HORIZONTAL_FACING)).rotateY().getHorizontalAngle();
        matrixStack.rotate(Vector3f.YP.rotationDegrees(-angle));
        if(!tileEntity.inventory.getStackInSlot(0).isEmpty()) {
            ItemStack stack = tileEntity.inventory.getStackInSlot(0);
            matrixStack.push();
            matrixStack.translate(0, 0.875f, -0.1875f);
            matrixStack.scale(0.375f, 0.375f, 0.375f);
            matrixStack.push();
            Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
            matrixStack.pop();
            matrixStack.pop();
        }
        if(!tileEntity.inventory.getStackInSlot(1).isEmpty()) {
            ItemStack stack = tileEntity.inventory.getStackInSlot(1).copy();
            stack.setCount(1);
            matrixStack.push();
            if(tileEntity.totalTime > 0)
                matrixStack.translate(0, 0.6875d - 0.05d - 0.375d * tileEntity.time / (double)tileEntity.totalTime, 0.3f);
            else
                matrixStack.translate(0, 0.6875d - 0.05d, 0.3f);
            Quaternion rot = Vector3f.YP.rotationDegrees(90);
            matrixStack.rotate(rot);
            matrixStack.push();
            matrixStack.scale(1.25f, 1.25f, 1.25f);
            Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GROUND, 15728880, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
            matrixStack.pop();
            matrixStack.pop();
        }
        if(tileEntity.totalTime > 0) {
            matrixStack.push();
            matrixStack.scale(0.8f, 0.8f, 0.8f);
            matrixStack.translate(-0.5f, 0.675f, -0.625f);
            Quaternion rot = Vector3f.XN.rotationDegrees(72);
            matrixStack.rotate(rot);
            renderBeamSegment(matrixStack, buffer, partialTicks, tileEntity.getWorld().getGameTime());
            matrixStack.pop();
        }
        matrixStack.pop();
    }
    
    private static void renderBeamSegment(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, float partialTicks, long totalWorldTime) {
        renderBeamSegment(matrixStackIn, bufferIn, TEXTURE_BEACON_BEAM, partialTicks, 1.0F, totalWorldTime, -1.5f, 1, new float[]{1, 0, 0}, 0.04f, 0.06f);
    }
    
    public static void renderBeamSegment(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, ResourceLocation textureLocation, float partialTicks, float textureScale, long totalWorldTime, float yOffset,
                                         int height, float[] colors, float beamRadius, float glowRadius) {
        float i = yOffset + height;
        matrixStackIn.push();
        matrixStackIn.translate(0.5D, 0.5D, 0.5D);
        matrixStackIn.push();
        float f = (float)Math.floorMod(totalWorldTime, 40L) + partialTicks;
        float f1 = height < 0 ? f : -f;
        float f2 = MathHelper.frac(f1 * 0.2F - (float)MathHelper.floor(f1 * 0.1F));
        float f3 = colors[0];
        float f4 = colors[1];
        float f5 = colors[2];
        matrixStackIn.push();
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(f * 4.5F - 45.0F));
        float f9 = -beamRadius;
        float f12 = -beamRadius;
        float f15 = -1.0F + f2;
        float f16 = (float)height * textureScale * (0.5F / beamRadius) + f15;
        renderPart(matrixStackIn, bufferIn.getBuffer(RenderType.getBeaconBeam(textureLocation, true)), f3, f4, f5, 0.5f, yOffset, i, 0.0F, beamRadius, beamRadius, 0.0F, f9, 0.0F, 0.0F, f12, 0.0F, 1.0F, f16,
                   f15);
        matrixStackIn.pop();
        float f6 = -glowRadius;
        float f7 = -glowRadius;
        float f8 = -glowRadius;
        f9 = -glowRadius;
        f15 = -1.0F + f2;
        f16 = (float)height * textureScale + f15;
        matrixStackIn.push();
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(f * -4.5F - 45.0F));
        renderPart(matrixStackIn, bufferIn.getBuffer(RenderType.getBeaconBeam(textureLocation, true)), f3, f4, f5, 0.3f, yOffset, i, f6, f7, glowRadius, f8, f9, glowRadius, glowRadius, glowRadius, 0.0F, 1.0F,
                   f16, f15);
        matrixStackIn.pop();
        matrixStackIn.pop();
        matrixStackIn.pop();
    }
    
    private static void renderPart(MatrixStack matrixStackIn, IVertexBuilder bufferIn, float red, float green, float blue, float alpha, float yMin, float yMax, float p_228840_8_, float p_228840_9_,
                                   float p_228840_10_, float p_228840_11_, float p_228840_12_, float p_228840_13_, float p_228840_14_, float p_228840_15_, float u1, float u2, float v1, float v2) {
        MatrixStack.Entry matrixstack$entry = matrixStackIn.getLast();
        Matrix4f matrix4f = matrixstack$entry.getMatrix();
        Matrix3f matrix3f = matrixstack$entry.getNormal();
        addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, yMin, yMax, p_228840_8_, p_228840_9_, p_228840_10_, p_228840_11_, u1, u2, v1, v2);
        addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, yMin, yMax, p_228840_14_, p_228840_15_, p_228840_12_, p_228840_13_, u1, u2, v1, v2);
        addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, yMin, yMax, p_228840_10_, p_228840_11_, p_228840_14_, p_228840_15_, u1, u2, v1, v2);
        addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, yMin, yMax, p_228840_12_, p_228840_13_, p_228840_8_, p_228840_9_, u1, u2, v1, v2);
    }
    
    private static void addQuad(Matrix4f matrixPos, Matrix3f matrixNormal, IVertexBuilder bufferIn, float red, float green, float blue, float alpha, float yMin, float yMax, float x1, float z1, float x2,
                                float z2, float u1, float u2, float v1, float v2) {
        addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMax, x1, z1, u2, v1);
        addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMin, x1, z1, u2, v2);
        addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMin, x2, z2, u1, v2);
        addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMax, x2, z2, u1, v1);
    }
    
    private static void addVertex(Matrix4f matrixPos, Matrix3f matrixNormal, IVertexBuilder bufferIn, float red, float green, float blue, float alpha, float y, float x, float z, float texU, float texV) {
        bufferIn.pos(matrixPos, x, y, z).color(red, green, blue, alpha).tex(texU, texV).overlay(OverlayTexture.NO_OVERLAY).lightmap(15728880).normal(matrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
    }
}
