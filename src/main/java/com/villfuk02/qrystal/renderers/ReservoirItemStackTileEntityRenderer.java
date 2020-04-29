package com.villfuk02.qrystal.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.villfuk02.qrystal.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.registries.ForgeRegistries;

public class ReservoirItemStackTileEntityRenderer extends ItemStackTileEntityRenderer {
    @Override
    public void render(ItemStack itemStack, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        matrixStack.push();
        matrixStack.translate(0.5, 0.5, 0.5);
        Minecraft.getInstance().getItemRenderer().renderItem(new ItemStack(ModItems.RESERVOIR_RENDERER), ItemCameraTransforms.TransformType.NONE, combinedLightIn, combinedOverlayIn, matrixStack, buffer);
        matrixStack.pop();
        
        if(itemStack.hasTag() && itemStack.getTag().contains("BlockEntityTag", Constants.NBT.TAG_COMPOUND) && itemStack.getTag().getCompound("BlockEntityTag").contains("Amount", Constants.NBT.TAG_INT) &&
                itemStack.getTag().getCompound("BlockEntityTag").contains("FluidName", Constants.NBT.TAG_STRING)) {
            if(itemStack.getTag().getCompound("BlockEntityTag").getInt("Amount") > 0) {
                float v = itemStack.getTag().getCompound("BlockEntityTag").getInt("Amount") / (float)(FluidAttributes.BUCKET_VOLUME * 8) * 0.98f;
                Fluid f = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(itemStack.getTag().getCompound("BlockEntityTag").getString("FluidName")));
                int wc = f.getAttributes().getColor();
                ResourceLocation rl = f.getAttributes().getStillTexture();
                int textureHeight = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(rl).getFrameCount();
                int offset = (int)(Minecraft.getInstance().world.getGameTime() / 2) % (textureHeight * 2 - 2);
                if(offset >= textureHeight)
                    offset = textureHeight * 2 - 2 - offset;
                matrixStack.push();
                ReservoirTileEntityRenderer.renderFluid(matrixStack, buffer.getBuffer(RenderType.getBeaconBeam(new ResourceLocation(rl.getNamespace(), "textures/" + rl.getPath() + ".png"), false)), wc, v,
                                                        offset, textureHeight);
                matrixStack.pop();
                
            }
        }
    }
}
