package com.villfuk02.qrystal.util;

import javafx.util.Pair;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class MaterialInfo {
    
    public String name;
    public Map<ResourceLocation, Long> outputs;
    public int temp;
    public Pair<Integer, Integer> color;
    public String lang;
    public CrystalUtil.Color seed;
    
    public int dataPriority;
    public int IOPriority;
    
    public MaterialInfo(String name, String lang) {
        this.name = name;
        this.lang = lang;
    }
    
    public static class Crushable {
        public String material;
        public long min;
        public long max;
        
        public Crushable(String mat, long min, long max) {
            material = mat;
            this.min = min;
            this.max = max;
        }
    }
}
