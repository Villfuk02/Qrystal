package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class PoweredFluidMixerBlock extends FluidMixerBlock {
    public PoweredFluidMixerBlock() {
        super("powered_fluid_mixer");
    }
    
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return ModTileEntityTypes.POWERED_FLUID_MIXER.create();
    }
}
