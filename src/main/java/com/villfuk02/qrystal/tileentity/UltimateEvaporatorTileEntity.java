package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.container.EvaporatorContainer;
import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;

public class UltimateEvaporatorTileEntity extends EvaporatorTileEntity {
    public UltimateEvaporatorTileEntity() {
        super(ModTileEntityTypes.ULTIMATE_EVAPORATOR, (short)2, 14, ModBlocks.ULTIMATE_EVAPORATOR);
    }
    
    @Nonnull
    @Override
    public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
        return new EvaporatorContainer(windowId, inventory, this);
    }
    
    @Override
    public int tickTemperature(int move) {
        final boolean powered = true;
        if(material.isEmpty() || !powered)
            return move;
        
        tempTarget = materialTemp;
        int shift = (tempTarget - temperature) - (tempTarget - temperature) * 49 / 50;
        boolean opposing = move != 0 && MathHelper.signum(move) != MathHelper.signum(shift);
        if(opposing)
            return shift;
        return move + shift;
    }
}
