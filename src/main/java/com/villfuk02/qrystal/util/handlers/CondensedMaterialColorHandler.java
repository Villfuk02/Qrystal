package com.villfuk02.qrystal.util.handlers;

import com.villfuk02.qrystal.util.ColorUtils;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants;

public class CondensedMaterialColorHandler implements IItemColor {
    
    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        int power = 1;
        if(stack.hasTag() && stack.getTag().contains("power", Constants.NBT.TAG_INT))
            power = stack.getTag().getInt("power");
        return ColorUtils.wrap(ColorUtils.float2Int(ColorUtils.hue2RGB((power - 1) / 10f)));
    }
}
