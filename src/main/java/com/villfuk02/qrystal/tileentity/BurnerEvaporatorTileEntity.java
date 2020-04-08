package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.container.EvaporatorContainer;
import com.villfuk02.qrystal.dataserializers.HeatRegulatorManager;
import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class BurnerEvaporatorTileEntity extends EvaporatorTileEntity {
    
    public int heatLeft = 0;
    public int heatTotal = 0;
    
    public BurnerEvaporatorTileEntity() {
        super(ModTileEntityTypes.BURNER_EVAPORATOR, (short)16, 15, ModBlocks.BURNER_EVAPORATOR);
    }
    
    @Nonnull
    @Override
    public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
        return new EvaporatorContainer(windowId, inventory, this);
    }
    
    @Override
    public int tickTemperature(int move) {
        if(material.isEmpty())
            return move;
        
        int shift = (tempTarget - temperature) - (tempTarget - temperature) * 79 / 80;
        boolean opposing = move != 0 && MathHelper.signum(move) != MathHelper.signum(shift);
        
        if((materialTemp != temperature || opposing) && heatLeft <= 0) {
            if(!inventory.getStackInSlot(14).isEmpty() && HeatRegulatorManager.heat_regulators.containsKey(inventory.getStackInSlot(14).getItem().getRegistryName())) {
                HeatRegulatorManager.HeatRegulatorInfo in = HeatRegulatorManager.heat_regulators.get(inventory.getStackInSlot(14).getItem().getRegistryName());
                heatTotal = in.time / 2;
                heatLeft = heatTotal;
                tempTarget = in.temperature;
                inventory.extractItem(14, 1, false);
                if(in.hasContainer) {
                    ItemStack container = new ItemStack(ForgeRegistries.ITEMS.getValue(in.container));
                    if(inventory.getStackInSlot(3).isEmpty()) {
                        inventory.insertItem(3, container, false);
                    } else {
                        for(int i = 9; i < 15; i++) {
                            if(inventory.getStackInSlot(i).isEmpty()) {
                                inventory.insertItem(i, container, false);
                                break;
                            }
                        }
                    }
                }
            } else {
                heatTotal = 0;
                tempTarget = 0;
                return move;
            }
        }
        
        
        if(opposing) {
            int heatUsed = Math.min(heatLeft, MathHelper.abs(move));
            heatLeft -= heatUsed;
            move -= MathHelper.signum(move) * heatUsed;
            if(heatLeft <= 0)
                return move;
        }
        
        shift = MathHelper.clamp(shift, -heatLeft, heatLeft);
        heatLeft -= MathHelper.abs(shift);
        return move + shift;
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
