package com.villfuk02.qrystal.network;

import com.villfuk02.qrystal.tileentity.IAutoIO;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCycleAutoIO {
    private final byte btn;
    private final int world;
    private final BlockPos pos;
    
    public PacketCycleAutoIO(byte btn, int world, BlockPos pos) {
        this.btn = btn;
        this.world = world;
        this.pos = pos;
    }
    
    public PacketCycleAutoIO(PacketBuffer buffer) {
        btn = buffer.readByte();
        world = buffer.readInt();
        pos = buffer.readBlockPos();
    }
    
    public void toBytes(PacketBuffer buffer) {
        buffer.writeByte(btn);
        buffer.writeInt(world);
        buffer.writeBlockPos(pos);
    }
    
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerWorld w = context.get().getSender().world.getServer().getWorld(DimensionType.getById(world));
            TileEntity te = w.getTileEntity(pos);
            if(te instanceof IAutoIO) {
                IAutoIO io = (IAutoIO)te;
                io.cycleButton(btn);
            }
        });
        context.get().setPacketHandled(true);
    }
    
}

