package com.villfuk02.qrystal.util.handlers;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants;

public class SurfaceColorHandler implements IItemColor {
    
    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        int c = 0xFFFFFF;
        if(stack.hasTag() && stack.getTag().contains("color", Constants.NBT.TAG_INT)) {
            c = stack.getTag().getInt("color");
        }
        return tintIndex == 0 ? c : 0xFFFFFF;
    }
    
    
}
