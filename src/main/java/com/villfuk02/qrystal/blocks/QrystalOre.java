package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.Main;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class QrystalOre extends BlockBase {
    
    public QrystalOre(Main.EnumColor color, boolean rich) {
        super(color.toString() + "_ore" + (rich ? "_rich" : ""), Material.ROCK, 3f, 15f, "pickaxe", 2);
    }
    
    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        if(getItemDropped(state, RANDOM, fortune) != Item.getItemFromBlock(this)) {
            return 3 + RANDOM.nextInt(4);
        }
        return 0;
    }
}
