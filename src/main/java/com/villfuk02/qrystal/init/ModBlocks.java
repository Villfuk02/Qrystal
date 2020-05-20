package com.villfuk02.qrystal.init;

import com.villfuk02.qrystal.blocks.*;
import com.villfuk02.qrystal.util.CrystalUtil;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.common.ToolType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModBlocks {
    
    public static final List<Block> BLOCKS = new ArrayList<>();
    
    public static final Block PEBBLE_BLOCK = new BlockBase("pebble_block", Material.ROCK, MaterialColor.STONE, SoundType.STONE, 1f, 20f, ToolType.PICKAXE, 0, 1);
    public static final Block QLEAR_ORE = new QrystalOre(CrystalUtil.Color.QLEAR, false);
    public static final Block QERI_ORE = new QrystalOre(CrystalUtil.Color.QERI, false);
    public static final Block QAWA_ORE = new QrystalOre(CrystalUtil.Color.QAWA, false);
    public static final Block QINI_ORE = new QrystalOre(CrystalUtil.Color.QINI, false);
    public static final Block QERI_ORE_RICH = new QrystalOre(CrystalUtil.Color.QERI, true);
    public static final Block QAWA_ORE_RICH = new QrystalOre(CrystalUtil.Color.QAWA, true);
    public static final Block QINI_ORE_RICH = new QrystalOre(CrystalUtil.Color.QINI, true);
    public static final Block QITAE_ORE_RICH = new QrystalOre(CrystalUtil.Color.QITAE, true);
    public static final Block QOID_ORE_RICH = new QrystalOre(CrystalUtil.Color.QOID, true);
    public static final Block QONDO_ORE_RICH = new QrystalOre(CrystalUtil.Color.QONDO, true);
    public static final Block DRYER = new DryerBlock();
    public static final Block STEEL_CUTTER = new SteelCutterBlock();
    public static final Block DIAMOND_CUTTER = new DiamondCutterBlock();
    public static final Block LASER_CUTTER = new LaserCutterBlock();
    public static final Block BURNER_FLUID_MIXER = new BurnerFluidMixerBlock();
    public static final Block POWERED_FLUID_MIXER = new PoweredFluidMixerBlock();
    public static final Block ULTIMATE_FLUID_MIXER = new UltimateFluidMixerBlock();
    public static final Block BASIC_EVAPORATOR = new BasicEvaporatorBlock();
    public static final Block BURNER_EVAPORATOR = new BurnerEvaporatorBlock();
    public static final Block POWERED_EVAPORATOR = new PoweredEvaporatorBlock();
    public static final Block ULTIMATE_EVAPORATOR = new UltimateEvaporatorBlock();
    public static final Block WOODEN_CONDENSING_BARREL = new CondensingBarrelBlock("wooden", 0, Material.WOOD, MaterialColor.WOOD, SoundType.WOOD);
    public static final Block STONE_CONDENSING_BARREL = new CondensingBarrelBlock("stone", 1, Material.ROCK, MaterialColor.STONE, SoundType.STONE);
    public static final Block IRON_CONDENSING_BARREL = new CondensingBarrelBlock("iron", 2, Material.IRON, MaterialColor.IRON, SoundType.STONE);
    public static final Block GOLD_CONDENSING_BARREL = new CondensingBarrelBlock("gold", 3, Material.IRON, MaterialColor.GOLD, SoundType.STONE);
    public static final Block IMBUED_CONDENSING_BARREL = new CondensingBarrelBlock("imbued", 4, Material.IRON, MaterialColor.DIAMOND, SoundType.STONE);
    public static final Block STEEL_CONDENSING_BARREL = new CondensingBarrelBlock("steel", 5, Material.IRON, MaterialColor.STONE, SoundType.STONE);
    public static final Block DIAMOND_CONDENSING_BARREL = new CondensingBarrelBlock("diamond", 6, Material.IRON, MaterialColor.DIAMOND, SoundType.STONE);
    public static final Block EMERALD_CONDENSING_BARREL = new CondensingBarrelBlock("emerald", 7, Material.IRON, MaterialColor.EMERALD, SoundType.STONE);
    public static final Block ENDSTEEL_CONDENSING_BARREL = new CondensingBarrelBlock("endsteel", 8, Material.IRON, MaterialColor.PURPLE, SoundType.STONE);
    public static Map<String, Block> QRYSTAL_BLOCKS = new HashMap<>();
    public static final Block IMBUED_ALLOY_BLOCK = new BlockBase("imbued_alloy_block", Material.IRON, MaterialColor.DIAMOND, SoundType.STONE, 4f, 10f, ToolType.PICKAXE, 0, 1);
    public static final Block CONDUCTIVE_ALLOY_BLOCK = new BlockBase("conductive_alloy_block", Material.IRON, MaterialColor.ORANGE_TERRACOTTA, SoundType.STONE, 4f, 10f, ToolType.PICKAXE, 0, 1);
    public static final Block STEEL_BLOCK = new BlockBase("steel_block", Material.IRON, MaterialColor.STONE, SoundType.STONE, 5f, 30f, ToolType.PICKAXE, 0, 1);
    public static final Block ENDSTEEL_BLOCK = new BlockBase("endsteel_block", Material.IRON, MaterialColor.PURPLE, SoundType.STONE, 6f, 60f, ToolType.PICKAXE, 0, 1);
    public static final Block EMITTER_0 = new EmitterBlock("emitter_0", 0);
    public static final Block EMITTER_1 = new EmitterBlock("emitter_1", 1);
    public static final Block EMITTER_2 = new EmitterBlock("emitter_2", 2);
    public static Map<String, Block> RECEIVERS = new HashMap<>();
    public static final Block IMBUED_ALLOY_CHASSIS = new BlockBase("imbued_alloy_chassis", Material.ROCK, MaterialColor.DIAMOND, SoundType.STONE, 3f, 15f, ToolType.PICKAXE, 0, 1);
    public static final Block STEEL_CHASSIS = new BlockBase("steel_chassis", Material.ROCK, MaterialColor.STONE, SoundType.STONE, 3f, 15f, ToolType.PICKAXE, 0, 1);
    public static final Block ENDSTEEL_CHASSIS = new BlockBase("endsteel_chassis", Material.ROCK, MaterialColor.PURPLE, SoundType.STONE, 3f, 15f, ToolType.PICKAXE, 0, 1);
    public static final Block RESERVOIR = new ReservoirBlock();
    
    public static void init() {
        for(CrystalUtil.Color c : CrystalUtil.Color.values()) {
            if(c == CrystalUtil.Color.QLEAR)
                continue;
            for(int i = 0; i < 3; i++) {
                QRYSTAL_BLOCKS.putIfAbsent(c.toString() + "_" + i, new QrystalBlock(c, i, false));
                QRYSTAL_BLOCKS.putIfAbsent("activated_" + c.toString() + "_" + i, new QrystalBlock(c, i, true));
            }
        }
        for(int i = 0; i < 3; i++) {
            RECEIVERS.putIfAbsent(Integer.toString(i), new ReceiverBlock(i, false));
            RECEIVERS.putIfAbsent("activated_" + i, new ReceiverBlock(i, true));
        }
    }
    
    
}
