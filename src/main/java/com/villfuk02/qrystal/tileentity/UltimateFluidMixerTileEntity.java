package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.container.FluidMixerContainer;
import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;

public class UltimateFluidMixerTileEntity extends FluidMixerTileEntity implements IPowerConsumer {
    private byte powered;
    
    public UltimateFluidMixerTileEntity() {
        super(ModTileEntityTypes.ULTIMATE_FLUID_MIXER, 8, 8, ModBlocks.ULTIMATE_FLUID_MIXER);
    }
    
    @Nonnull
    @Override
    public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
        return new FluidMixerContainer(windowId, inventory, this);
    }
    
    
    @Override
    public byte getPower() {
        return powered;
    }
    
    @Override
    public void setPower(byte power) {
        powered = power;
        if(pos != null && world != null)
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
    }
    
    @Override
    public void readClient(CompoundNBT compound) {
        powered = compound.getByte("powered");
        super.readClient(compound);
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putByte("powered", powered);
        return super.write(compound);
    }
    
    @Override
    protected boolean isPowered() {
        return powered >= 3;
    }
}
