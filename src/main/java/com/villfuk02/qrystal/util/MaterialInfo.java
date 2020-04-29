package com.villfuk02.qrystal.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class MaterialInfo {
    
    public String name;
    public Map<ResourceLocation, Integer> outputs;
    public Pair<Integer, Integer> color;
    public String lang;
    public CrystalUtil.Color seed;
    
    public int dataPriority;
    public int IOPriority;
    
    public MaterialInfo(String name, String lang) {
        this.name = name;
        this.lang = lang;
    }
    
    public static class Unit {
        public String material;
        public int value;
        
        public Unit(String mat, int value) {
            material = mat;
            this.value = value;
        }
    }
}
