package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class UltimateFluidMixerBlock extends FluidMixerBlock {
    public UltimateFluidMixerBlock() {
        super("ultimate_fluid_mixer");
    }
    
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTileEntityTypes.ULTIMATE_FLUID_MIXER.create();
    }
}
