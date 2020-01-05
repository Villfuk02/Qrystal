package com.villfuk02.qrystal.init;

import com.villfuk02.qrystal.blocks.BlockBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import java.util.ArrayList;
import java.util.List;

public class ModBlocks {
    public static final List<Block> BLOCKS = new ArrayList<>();
    
    public static final Block PEBBLE_BLOCK = new BlockBase("pebble_block", Material.ROCK);
}
