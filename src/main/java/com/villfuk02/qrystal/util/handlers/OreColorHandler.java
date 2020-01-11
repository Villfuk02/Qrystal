package com.villfuk02.qrystal.util.handlers;

import com.villfuk02.qrystal.Main;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class OreColorHandler implements IBlockColor, IItemColor {
    
    private static final int[] colors = new int[]{0xCCCCCC, //
                                                  0xFFF26D, 0x1EC8FC, 0xF72B0C, //
                                                  0x00F46E, 0xA500F2, 0xEF5B00, //
                                                  0x555555,};
    
    @Override
    public int colorMultiplier(IBlockState state, @Nullable IBlockAccess world, @Nullable BlockPos pos, int tintIndex) {
        return tintIndex == 1 ? colors[state.getValue(Main.qrystalColor).getMeta()] : -1;
    }
    
    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        return tintIndex == 1 ? colors[stack.getMetadata() % 8] : -1;
    }
}
