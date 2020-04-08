package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BurnerEvaporatorBlock extends EvaporatorBlock {
    public BurnerEvaporatorBlock() {
        super("burner_evaporator");
    }
    
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTileEntityTypes.BURNER_EVAPORATOR.create();
    }
}
