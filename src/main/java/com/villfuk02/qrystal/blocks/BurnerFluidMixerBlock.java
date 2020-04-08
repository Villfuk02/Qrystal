package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BurnerFluidMixerBlock extends FluidMixerBlock {
    public BurnerFluidMixerBlock() {
        super("burner_fluid_mixer");
    }
    
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return ModTileEntityTypes.BURNER_FLUID_MIXER.create();
    }
}
