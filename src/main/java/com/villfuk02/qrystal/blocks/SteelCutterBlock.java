package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class SteelCutterBlock extends CutterBlock {
    public SteelCutterBlock() {
        super("steel_cutter");
    }
    
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return ModTileEntityTypes.STEEL_CUTTER.create();
    }
}
