package com.villfuk02.qrystal.util.handlers;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

public class OreColorHandler implements IItemColor {
    
    private static final int[] colors = new int[]{0xCCCCCC, //
                                                  0xFFF26D, 0x1EC8FC, 0xF72B0C, //
                                                  0x00F46E, 0xA500F2, 0xEF5B00, //
                                                  0x555555,};
    
    
    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        return tintIndex == 1 ? colors[stack.getMetadata() % 8] : -1;
    }
}
