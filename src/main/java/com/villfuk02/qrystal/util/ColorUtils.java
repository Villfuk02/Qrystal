package com.villfuk02.qrystal.util;

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
    
}
