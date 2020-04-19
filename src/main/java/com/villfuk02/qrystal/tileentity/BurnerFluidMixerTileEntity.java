package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.container.FluidMixerContainer;
import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nonnull;

public class BurnerFluidMixerTileEntity extends FluidMixerTileEntity {
    
    public int heatLeft = 0;
    public int heatTotal = 0;
    
    public BurnerFluidMixerTileEntity() {
        super(ModTileEntityTypes.BURNER_FLUID_MIXER, 2, 9, ModBlocks.BURNER_FLUID_MIXER);
    }
    
    @Nonnull
    @Override
    public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
        return new FluidMixerContainer(windowId, inventory, this);
    }
    
    @Override
    protected boolean isPowered() {
        if(heatLeft > 0)
            heatLeft -= 2;
        if(heatLeft <= 0) {
            if(!inventory.getStackInSlot(8).isEmpty() && AbstractFurnaceTileEntity.isFuel(inventory.getStackInSlot(8))) {
                heatTotal = ForgeHooks.getBurnTime(inventory.getStackInSlot(8));
                heatLeft += heatTotal;
                inventory.extractItem(8, 1, false);
            } else
                heatTotal = 0;
        }
        return heatLeft > 0;
    }
    
    @Override
    public void readClient(CompoundNBT compound) {
        super.readClient(compound);
        heatLeft = compound.getInt("heatLeft");
        heatTotal = compound.getInt("heatTotal");
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound = super.write(compound);
        compound.putInt("heatLeft", heatLeft);
        compound.putInt("heatTotal", heatTotal);
        return compound;
    }
}
