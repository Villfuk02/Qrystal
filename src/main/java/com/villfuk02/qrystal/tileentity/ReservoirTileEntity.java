package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.init.ModTileEntityTypes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.TileFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class ReservoirTileEntity extends TileFluidHandler {
    public ReservoirTileEntity() {
        super(ModTileEntityTypes.RESERVOIR);
        tank = new FluidTank(FluidAttributes.BUCKET_VOLUME * 8);
    }
    
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), 1, write(new CompoundNBT()));
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(pkt.getNbtCompound());
    }
    
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();
        return write(tag);
    }
    
    public int getComparatorLevel() {
        if(tank.isEmpty())
            return 0;
        return (15 * tank.getFluidAmount()) / (FluidAttributes.BUCKET_VOLUME * 8);
    }
}
