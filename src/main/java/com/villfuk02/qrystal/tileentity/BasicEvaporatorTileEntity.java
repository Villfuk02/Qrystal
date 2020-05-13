package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.container.EvaporatorContainer;
import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nonnull;

public class BasicEvaporatorTileEntity extends EvaporatorTileEntity implements IBurnerEvaporator {
    
    public int heatLeft = 0;
    public int heatTotal = 0;
    
    public BasicEvaporatorTileEntity() {
        super(ModTileEntityTypes.BASIC_EVAPORATOR, (short)90, 7, (byte)0, ModBlocks.BASIC_EVAPORATOR);
    }
    
    @Nonnull
    @Override
    public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
        return new EvaporatorContainer(windowId, inventory, this);
    }
    
    @Override
    boolean isPowered() {
        if(heatLeft > 0) {
            heatLeft -= getSpeed();
            return true;
        } else {
            if(!inventory.getStackInSlot(6).isEmpty() && AbstractFurnaceTileEntity.isFuel(inventory.getStackInSlot(6))) {
                heatTotal = ForgeHooks.getBurnTime(inventory.getStackInSlot(6));
                heatLeft += heatTotal;
                inventory.extractItem(6, 1, false);
            } else {
                heatTotal = 0;
                heatLeft = 0;
                return false;
            }
        }
        return heatLeft > 0;
    }
    
    @Override
    public int getHeatLeft() {
        return heatLeft;
    }
    
    @Override
    public int getHeatTotal() {
        return heatTotal;
    }
    
    @Override
    public int getSpeed() {
        return 1;
    }
    
    @Override
    public void restoreHeat() {
        heatLeft += getSpeed();
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
