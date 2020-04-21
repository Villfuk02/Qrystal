package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.container.EvaporatorContainer;
import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;

import javax.annotation.Nonnull;

public class BasicEvaporatorTileEntity extends EvaporatorTileEntity {
    
    public BasicEvaporatorTileEntity() {
        super(ModTileEntityTypes.BASIC_EVAPORATOR, (short)60, (byte)0, ModBlocks.BASIC_EVAPORATOR);
    }
    
    @Nonnull
    @Override
    public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
        return new EvaporatorContainer(windowId, inventory, this);
    }
    
    @Override
    boolean isPowered() {
        return true;
    }
}
