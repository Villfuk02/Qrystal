package com.villfuk02.qrystal.init;

import com.villfuk02.qrystal.items.*;
import com.villfuk02.qrystal.util.CrystalUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModItems {
    public static final int[] DUST_SIZES = new int[]{1296, 216, 36, 6, 1};
    
    public static final List<Item> ITEMS = new ArrayList<>();
    public static final Map<Integer, Item> FILLED_FLASKS = new HashMap<>();
    
    public static final Item STONE_PEBBLES = new ItemBase("stone_pebbles");
    public static Map<String, Item> CRYSTALS = new HashMap<>();
    public static final Item CONDENSED_MATERIAL = new CondensedMaterial();
    public static final Item STONE_HAMMER = new Hammer("stone_hammer", ItemTier.STONE);
    public static final Item IRON_HAMMER = new Hammer("iron_hammer", ItemTier.IRON);
    public static final Item DIAMOND_HAMMER = new Hammer("diamond_hammer", ItemTier.DIAMOND);
    public static Map<String, Item> DUSTS = new HashMap<>();
    public static final Item CRYSTALLIC_OBSIDIAN = new ItemBase("crystallic_obsidian");
    public static final Item CRYSTALLIC_ENDER_PEARL = new ItemBase("crystallic_ender_pearl");
    public static final Item COAL_DUST = new ItemFuel("coal_dust", 800);
    public static final Item CHARCOAL_DUST = new ItemFuel("charcoal_dust", 800);
    public static final Item SULFUR_DUST = new ItemFuel("sulfur_dust", 800);
    public static final Item NITER_DUST = new ItemBase("niter_dust");
    public static final Item QUARTZ_DUST = new ItemBase("quartz_dust");
    public static final Item QUARTZ_DUST_PILE = new ItemBase("quartz_dust_pile");
    public static final Item ACTIVATED_CARBON = new ItemFuel("activated_carbon", 400);
    public static final Item FUSED_QUARTZ = new ItemBase("fused_quartz");
    public static final Item TUBE = new ItemBase("tube");
    public static final Item FUNNEL = new ItemWithContainer("funnel");
    public static final Item CONDENSER = new ItemBase("condenser");
    public static final Item FILTER_PAPER = new ItemBase("filter_paper");
    public static final Item UPGRADE_0 = new BarrelUpgrade(0, "stone");
    public static final Item UPGRADE_1 = new BarrelUpgrade(1, "iron");
    public static final Item UPGRADE_2 = new BarrelUpgrade(2, "gold");
    public static final Item UPGRADE_3 = new BarrelUpgrade(3, "imbued");
    public static final Item UPGRADE_4 = new BarrelUpgrade(4, "steel");
    public static final Item UPGRADE_5 = new BarrelUpgrade(5, "diamond");
    public static final Item UPGRADE_6 = new BarrelUpgrade(6, "emerald");
    public static final Item UPGRADE_7 = new BarrelUpgrade(7, "endsteel");
    public static final Item IMBUED_ALLOY_MIX = new ItemBase("imbued_alloy_mix");
    public static final Item CONDUCTIVE_ALLOY_MIX = new ItemBase("conductive_alloy_mix");
    public static final Item STEEL_MIX = new ItemBase("steel_mix");
    public static final Item ENDSTEEL_MIX = new ItemBase("endsteel_mix");
    public static final Item IMBUED_ALLOY_INGOT = new ItemBase("imbued_alloy_ingot");
    public static final Item CONDUCTIVE_ALLOY_INGOT = new ItemBase("conductive_alloy_ingot");
    public static final Item STEEL_INGOT = new ItemBase("steel_ingot");
    public static final Item ENDSTEEL_INGOT = new ItemBase("endsteel_ingot");
    public static final Item STEEL_BLADE = new Blade("steel_blade", 320);
    public static final Item DIAMOND_BLADE = new Blade("diamond_blade", 640);
    public static final Item REDSTONE_COIL = new ItemBase("redstone_coil");
    public static final Item CONDUCTIVE_ALLOY_COIL = new ItemBase("conductive_alloy_coil");
    public static final Item ENDER_COIL = new ItemBase("ender_coil");
    public static final Item HEAT_CONDUIT = new ItemBase("heat_conduit");
    public static final Item NANO_MESH = new ItemBase("nano_mesh");
    public static final Item ACTUATOR = new ItemBase("actuator");
    
    public static final Item SURFACE_RENDERER = new Item(new Item.Properties()).setRegistryName("surface_renderer");
    public static final Item CONDENSED_MATERIAL_CAGE_RENDERER = new Item(new Item.Properties()).setRegistryName("condensed_material_cage_renderer");
    public static final Item RESERVOIR_RENDERER = new Item(new Item.Properties()).setRegistryName("reservoir_renderer");
    
    public static final Item FILTERED_WATER_BUCKET = new CustomBucketItem(ModFluids.FILTERED_WATER);
    public static final Item PURIFIED_WATER_BUCKET = new CustomBucketItem(ModFluids.PURIFIED_WATER);
    
    public static void init() {
        for(int i = 0; i < 16; i++) {
            for(CrystalUtil.Size s : CrystalUtil.Size.values()) {
                if(s != CrystalUtil.Size.SEED || i > 0) {
                    Item item = new Crystal(s, i);
                    CRYSTALS.put(s.toString() + "_" + i, item);
                }
            }
        }
        for(int l : DUST_SIZES) {
            DUSTS.put("dust_" + l, new CrystalDust(l));
        }
        
        ITEMS.add(SURFACE_RENDERER);
        ITEMS.add(CONDENSED_MATERIAL_CAGE_RENDERER);
        ITEMS.add(RESERVOIR_RENDERER);
    }
}
