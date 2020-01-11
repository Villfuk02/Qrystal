package com.villfuk02.qrystal.init;

import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.blocks.BlockBase;
import com.villfuk02.qrystal.blocks.QrystalOre;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import java.util.ArrayList;
import java.util.List;

public class ModBlocks {
    
    public static final List<Block> BLOCKS = new ArrayList<>();
    
    public static final Block PEBBLE_BLOCK = new BlockBase("pebble_block", Material.ROCK, 1f, 20f, "pickaxe", 0);
    public static final Block QLEAR_ORE = new QrystalOre(Main.EnumColor.QLEAR, false);
    public static final Block QERI_ORE = new QrystalOre(Main.EnumColor.QERI, false);
    public static final Block QAWA_ORE = new QrystalOre(Main.EnumColor.QAWA, false);
    public static final Block QINI_ORE = new QrystalOre(Main.EnumColor.QINI, false);
    public static final Block QERI_ORE_RICH = new QrystalOre(Main.EnumColor.QERI, true);
    public static final Block QAWA_ORE_RICH = new QrystalOre(Main.EnumColor.QAWA, true);
    public static final Block QINI_ORE_RICH = new QrystalOre(Main.EnumColor.QINI, true);
    public static final Block QITAE_ORE_RICH = new QrystalOre(Main.EnumColor.QITAE, true);
    public static final Block QOID_ORE_RICH = new QrystalOre(Main.EnumColor.QOID, true);
    public static final Block QONDO_ORE_RICH = new QrystalOre(Main.EnumColor.QONDO, true);
}
