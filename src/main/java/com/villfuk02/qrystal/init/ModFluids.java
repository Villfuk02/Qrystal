package com.villfuk02.qrystal.init;

import com.villfuk02.qrystal.fluids.FluidBase;
import net.minecraft.fluid.Fluid;

import java.util.ArrayList;
import java.util.List;

public class ModFluids {
    public static List<Fluid> FLUIDS = new ArrayList<>();
    
    public static final Fluid FILTERED_WATER = new FluidBase("filtered_water", 0xFF0076FF, 1000, 1000, 300);
    public static final Fluid PURIFIED_WATER = new FluidBase("purified_water", 0xFF00B6FF, 1000, 1000, 300);
    public static final Fluid DISTILLED_WATER = new FluidBase("distilled_water", 0xFF9EC9FF, 1000, 1000, 300);
    public static final Fluid SULFURIC_ACID = new FluidBase("sulfuric_acid", 0xFFFFFF99, 1830, 2000, 300);
    public static final Fluid NITRIC_ACID = new FluidBase("nitric_acid", 0xFFFFC477, 1510, 1300, 300);
    public static final Fluid HYDROCHLORIC_ACID = new FluidBase("hydrochloric_acid", 0xFFDEFF66, 1490, 1250, 300);
    public static final Fluid AQUA_REGIA = new FluidBase("aqua_regia", 0xFFFF4235, 1150, 300, 300);
    public static final Fluid WORT = new FluidBase("wort", 0xFFA88C45, 1200, 1500, 300);
    public static final Fluid ETHANOL = new FluidBase("ethanol", 0xFFAAFFFC, 790, 1200, 300);
    public static final Fluid INFERNAL_ESSENCE = new FluidBase("infernal_essence", 0xFFFF2200, 2000, 7000, 1000);
    public static final Fluid ENERGIZED_INFERNAL_ESSENCE = new FluidBase("energized_infernal_essence", 0xFFFF006A, 2500, 400, 1800);
    public static final Fluid VOID_ESSENCE = new FluidBase("void_essence", 0xFF8700FF, 10, 20000, 150);
    public static final Fluid SEA_WATER = new FluidBase("sea_water", 0xFF0018FF, 1020, 1000, 300);
    public static final Fluid VINEGAR = new FluidBase("vinegar", 0xFFCCB59B, 1000, 1000, 300);
    public static final Fluid DMSO = new FluidBase("dmso", 0xFF9FFFC6, 1100, 1100, 300);
    public static final Fluid ULTRA_SOLVENT = new FluidBase("ultra_solvent", 0xFF00FF66, 2000, 2000, 300);
    public static final Fluid CONCENTRATED_ULTRA_SOLVENT = new FluidBase("concentrated_ultra_solvent", 0xFF00FFAE, 4000, 4000, 300);
    
    
}
