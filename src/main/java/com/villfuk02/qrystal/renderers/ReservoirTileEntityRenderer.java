package com.villfuk02.qrystal.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.villfuk02.qrystal.tileentity.ReservoirTileEntity;
import com.villfuk02.qrystal.util.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ReservoirTileEntityRenderer extends TileEntityRenderer<ReservoirTileEntity> {
    
    public ReservoirTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }
    
    @Override
    public void render(ReservoirTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        IFluidHandler cap = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
        if(!cap.getFluidInTank(0).isEmpty()) {
            te.lastFluid = cap.getFluidInTank(0).getRawFluid();
            te.lastHeight = (te.lastHeight * 9 + cap.getFluidInTank(0).getAmount() / (float)(FluidAttributes.BUCKET_VOLUME * 8) * 0.98f) / 10;
        } else {
            te.lastHeight = te.lastHeight * 9 / 10;
        }
        
        if(te.lastFluid != null && te.lastHeight > 0.0001f) {
            int wc = te.lastFluid.getAttributes().getColor();
            ResourceLocation rl = te.lastFluid.getAttributes().getStillTexture();
            int textureHeight = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(rl).getFrameCount();
            int offset;
            if(textureHeight <= 1) {
                offset = 0;
            } else {
                offset = (int)(Minecraft.getInstance().world.getGameTime() / 2) % (textureHeight * 2 - 2);
                if(offset >= textureHeight)
                    offset = textureHeight * 2 - 2 - offset;
            }
            matrixStack.push();
            renderFluid(matrixStack, buffer.getBuffer(RenderType.getBeaconBeam(new ResourceLocation(rl.getNamespace(), "textures/" + rl.getPath() + ".png"), false)), wc, te.lastHeight, offset, textureHeight);
            matrixStack.pop();
        }
    }
    
    public static void renderFluid(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int color, float height, int offset, int textureHeight) {
        MatrixStack.Entry matrixstack$entry = matrixStackIn.getLast();
        Matrix4f matrix4f = matrixstack$entry.getMatrix();
        Matrix3f matrix3f = matrixstack$entry.getNormal();
        float[] c = ColorUtils.int2Float(ColorUtils.unwrapRGB(color));
        addVerticalQuad(matrix4f, matrix3f, bufferIn, c[0], c[1], c[2], 1, 0.01f, 0.01f + height, 0.01f, 0.01f, 0.01f, 0.99f, 0.01f, 0.99f, (0.01f + height + offset) / textureHeight,
                        (0.01f + offset) / textureHeight);
        addVerticalQuad(matrix4f, matrix3f, bufferIn, c[0], c[1], c[2], 1, 0.01f, 0.01f + height, 0.01f, 0.99f, 0.99f, 0.99f, 0.01f, 0.99f, (0.01f + height + offset) / textureHeight,
                        (0.01f + offset) / textureHeight);
        addVerticalQuad(matrix4f, matrix3f, bufferIn, c[0], c[1], c[2], 1, 0.01f, 0.01f + height, 0.99f, 0.99f, 0.99f, 0.01f, 0.01f, 0.99f, (0.01f + height + offset) / textureHeight,
                        (0.01f + offset) / textureHeight);
        addVerticalQuad(matrix4f, matrix3f, bufferIn, c[0], c[1], c[2], 1, 0.01f, 0.01f + height, 0.99f, 0.01f, 0.01f, 0.01f, 0.01f, 0.99f, (0.01f + height + offset) / textureHeight,
                        (0.01f + offset) / textureHeight);
        addHorizontalQuad(matrix4f, matrix3f, bufferIn, c[0], c[1], c[2], 1, 0.01f, 0.01f, 0.99f, 0.99f, 0.01f, 0.01f, 0.99f, (0.99f + offset) / textureHeight, (0.01f + offset) / textureHeight, true);
        addHorizontalQuad(matrix4f, matrix3f, bufferIn, c[0], c[1], c[2], 1, 0.01f + height, 0.01f, 0.99f, 0.99f, 0.01f, 0.01f, 0.99f, (0.99f + offset) / textureHeight, (0.01f + offset) / textureHeight, false);
    }
    
    private static void addVerticalQuad(Matrix4f matrixPos, Matrix3f matrixNormal, IVertexBuilder bufferIn, float red, float green, float blue, float alpha, float y1, float y2, float x1, float x2, float z1,
                                        float z2, float u1, float u2, float v1, float v2) {
        addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, y2, x1, z1, u2, v1);
        addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, y1, x1, z1, u2, v2);
        addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, y1, x2, z2, u1, v2);
        addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, y2, x2, z2, u1, v1);
    }
    
    private static void addHorizontalQuad(Matrix4f matrixPos, Matrix3f matrixNormal, IVertexBuilder bufferIn, float red, float green, float blue, float alpha, float y, float x1, float x2, float z1, float z2,
                                          float u1, float u2, float v1, float v2, boolean clockwise) {
        addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, y, x1, z1, u1, v1);
        if(clockwise) {
            addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, y, x1, z2, u1, v2);
            addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, y, x2, z2, u2, v2);
            addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, y, x2, z1, u2, v1);
        } else {
            addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, y, x2, z1, u2, v1);
            addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, y, x2, z2, u2, v2);
            addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, y, x1, z2, u1, v2);
        }
    }
    
    private static void addVertex(Matrix4f matrixPos, Matrix3f matrixNormal, IVertexBuilder bufferIn, float red, float green, float blue, float alpha, float y, float x, float z, float texU, float texV) {
        bufferIn.pos(matrixPos, x, y, z).color(red, green, blue, alpha).tex(texU, texV).overlay(OverlayTexture.NO_OVERLAY).lightmap(15728880).normal(matrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
    }
}
