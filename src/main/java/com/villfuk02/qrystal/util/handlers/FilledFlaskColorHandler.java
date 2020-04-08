package com.villfuk02.qrystal.util.handlers;

import com.villfuk02.qrystal.util.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.registries.ForgeRegistries;

public class FilledFlaskColorHandler implements IItemColor {
    
    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if(tintIndex != 1)
            return 0xFFFFFF;
        if(!stack.hasTag() || !stack.getTag().contains("fluid") || !ForgeRegistries.FLUIDS.containsKey(new ResourceLocation(stack.getTag().getString("fluid"))))
            return 0xFF00FF;
        int[] color = getColor(new ResourceLocation(stack.getTag().getString("fluid")));
        return ColorUtils.wrap(color[0], color[1], color[2]);
    }
    
    public static int[] getColor(ResourceLocation fluid) {
        FluidAttributes fa = ForgeRegistries.FLUIDS.getValue(fluid).getAttributes();
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(fa.getStillTexture());
        int[] txc = ColorUtils.unwrapRGBA(sprite.getPixelRGBA(0, (int)(Minecraft.getInstance().world.getGameTime() / 41) % sprite.getWidth(),
                                                                    (int)(Minecraft.getInstance().world.getGameTime() / 29) % sprite.getHeight()));
        int[] tic = ColorUtils.unwrapRGB(fa.getColor());
        int r = (tic[0] * txc[0]) / 255;
        int g = (tic[1] * txc[1]) / 255;
        int b = (tic[2] * txc[2]) / 255;
        return new int[]{r, g, b};
    }
    
    
}
