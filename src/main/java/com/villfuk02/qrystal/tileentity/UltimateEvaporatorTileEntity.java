package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.container.EvaporatorContainer;
import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;

public class UltimateEvaporatorTileEntity extends EvaporatorTileEntity implements IPowerConsumer {
    private byte powered;
    
    public UltimateEvaporatorTileEntity() {
        super(ModTileEntityTypes.ULTIMATE_EVAPORATOR, (short)2, 6, (byte)3, ModBlocks.ULTIMATE_EVAPORATOR);
    }
    
    @Nonnull
    @Override
    public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
        return new EvaporatorContainer(windowId, inventory, this);
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
    boolean isPowered() {
        return powered >= requiredPower;
    }
}
