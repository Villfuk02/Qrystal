package com.villfuk02.qrystal.util;

import net.minecraft.util.math.MathHelper;

public class ColorUtils {
    
    public static int[] unwrapRGB(int color) {
        return new int[]{color >> 16 & 255, color >> 8 & 255, color & 255};
    }
    
    public static int[] unwrapRGBA(int color) {
        return new int[]{color & 255, color >> 8 & 255, color >> 16 & 255, color >> 24 & 255};
    }
    
    public static int wrap(int r, int g, int b) {
        return (r << 16) + (g << 8) + b;
    }
    
    public static int wrap(int r, int g, int b, int a) {
        return r + (g << 8) + (b << 16) + (a << 24);
    }
    
    public static int wrap(int... ints) {
        if(ints.length == 3)
            return wrap(ints[0], ints[1], ints[2]);
        if(ints.length == 4)
            return wrap(ints[0], ints[1], ints[2], ints[3]);
        return 0;
    }
    
    public static int[] float2Int(float... floats) {
        int[] ints = new int[floats.length];
        for(int i = 0; i < floats.length; i++) {
            ints[i] = (int)(floats[i] * 255 + 0.5f);
        }
        return ints;
    }
    
    public static float[] int2Float(int... ints) {
        float[] floats = new float[ints.length];
        for(int i = 0; i < ints.length; i++) {
            floats[i] = ints[i] / 255f;
        }
        return floats;
    }
    
    public static float[] hue2RGB(float hue) {
        return new float[]{hueCycle(hue + 1 / 3f), hueCycle(hue), hueCycle(hue + 2 / 3f)};
    }
    
    static float hueCycle(float in) {
        return MathHelper.clamp(Math.abs(Math.abs(in * 6 % 6 - 2) - 3) - 1, 0, 1);
    }
    
}
