package com.villfuk02.qrystal.init;

import com.villfuk02.qrystal.blocks.BlockBase;
import com.villfuk02.qrystal.blocks.QrystalOre;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import java.util.ArrayList;
import java.util.List;

public class ModBlocks {
    
    public static final List<Block> BLOCKS = new ArrayList<>();
    
    public static final Block PEBBLE_BLOCK = new BlockBase("pebble_block", Material.ROCK, 1f, 20f, "pickaxe", 0);
    public static final Block QRYSTAL_ORE = new QrystalOre();
}
