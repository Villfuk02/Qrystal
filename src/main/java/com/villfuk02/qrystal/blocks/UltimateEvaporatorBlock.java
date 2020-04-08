package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class UltimateEvaporatorBlock extends EvaporatorBlock {
    public UltimateEvaporatorBlock() {
        super("ultimate_evaporator");
    }
    
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTileEntityTypes.ULTIMATE_EVAPORATOR.create();
    }
}
