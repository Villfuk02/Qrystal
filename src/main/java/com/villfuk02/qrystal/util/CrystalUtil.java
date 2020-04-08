package com.villfuk02.qrystal.util;

import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class CrystalUtil {
    
    public static ItemStack createQrystalCrystal(ItemStack stack, String material) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("material", material);
        stack.setTag(tag);
        return stack;
    }
    
    public static String getRegistryNameFromNBT(ItemStack stack) {
        if(stack.hasTag()) {
            CompoundNBT tag = stack.getTag();
            return tag.getString("material");
        }
        return "qlear";
    }
    
    public enum Size {
        SEED("seed"), SMALL("small"), MEDIUM("medium"), LARGE("large");
        private final String size;
        
        private Size(String size) {
            this.size = size;
        }
        
        @Override
        public String toString() {
            return size;
        }
    }
    
    public enum Color {
        QLEAR("qlear", 0), QERI("qeri", 1), QAWA("qawa", 2), QINI("qini", 3), QITAE("qitae", 4), QOID("qoid", 5), QONDO("qondo", 6), QALB("qalb", 7);
        
        private final String name;
        private final int meta;
        
        private Color(String name, int meta) {
            this.name = name;
            this.meta = meta;
        }
        
        @Override
        public String toString() {
            return name;
        }
        
        public int getId() {
            return meta;
        }
        
        public static Color fromId(int meta) {
            switch(meta) {
                default:
                    return QLEAR;
                case 1:
                    return QERI;
                case 2:
                    return QAWA;
                case 3:
                    return QINI;
                case 4:
                    return QITAE;
                case 5:
                    return QOID;
                case 6:
                    return QONDO;
                case 7:
                    return QALB;
            }
        }
        
        public static Color fromString(String s) {
            switch(s) {
                default:
                    return QLEAR;
                case "qeri":
                    return QERI;
                case "qawa":
                    return QAWA;
                case "qini":
                    return QINI;
                case "qitae":
                    return QITAE;
                case "qoid":
                    return QOID;
                case "qondo":
                    return QONDO;
                case "qalb":
                    return QALB;
            }
        }
        
        public MaterialColor getMapColor() {
            switch(this) {
                case QLEAR:
                    return MaterialColor.SNOW;
                case QERI:
                    return MaterialColor.YELLOW;
                case QAWA:
                    return MaterialColor.DIAMOND;
                case QINI:
                    return MaterialColor.RED;
                case QITAE:
                    return MaterialColor.LIME;
                case QOID:
                    return MaterialColor.PURPLE;
                case QONDO:
                    return MaterialColor.ORANGE_TERRACOTTA;
                case QALB:
                    return MaterialColor.BLACK;
            }
            return MaterialColor.AIR;
        }
    }
}
