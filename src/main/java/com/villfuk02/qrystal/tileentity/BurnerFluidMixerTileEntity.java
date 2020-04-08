package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.container.FluidMixerContainer;
import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;

import javax.annotation.Nonnull;

public class BurnerFluidMixerTileEntity extends FluidMixerTileEntity {
    public BurnerFluidMixerTileEntity() {
        super(ModTileEntityTypes.BURNER_FLUID_MIXER, 1, 9, ModBlocks.BURNER_FLUID_MIXER);
    }
    
    @Nonnull
    @Override
    public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
        return new FluidMixerContainer(windowId, inventory, this);
    }
}
