package com.villfuk02.qrystal.util.handlers;

import com.villfuk02.qrystal.dataserializers.MaterialManager;
import com.villfuk02.qrystal.util.ColorUtils;
import javafx.util.Pair;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

public class CrystalColorHandler implements IItemColor {
    
    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if(tintIndex == 0)
            return 0xFFFFFF;
        if(stack.hasTag() && stack.getTag().contains("material") && MaterialManager.material_names.contains(stack.getTag().getString("material"))) {
            return getColor(stack.getTag().getString("material"), tintIndex);
        } else {
            return tintIndex == 2 ? 0 : 0xFF00FF;
        }
    }
    
    public static int getColor(String material, int tintIndex) {
        if(!MaterialManager.material_names.contains(material))
            return tintIndex == 2 ? 0 : 0xFF00FF;
        Pair<Integer, Integer> p = MaterialManager.materials.get(material).color;
        if(tintIndex >= 3) {
            int[] mc = ColorUtils.unwrapRGB(p.getKey());
            int[] nc = ColorUtils.unwrapRGB(p.getValue());
            int r = mc[0] + nc[0];
            int g = mc[1] + nc[1];
            int b = mc[2] + nc[2];
            int s = Math.min(r, Math.min(g, Math.min(b, 96)));
            r -= s;
            g -= s;
            b -= s;
            int m = Math.max(r, Math.max(g, Math.max(b, 255)));
            r = (r * 255) / m;
            g = (g * 255) / m;
            b = (b * 255) / m;
            return ColorUtils.wrap(r, g, b);
        }
        return tintIndex == 1 ? p.getKey() : p.getValue();
    }
    
    
}
