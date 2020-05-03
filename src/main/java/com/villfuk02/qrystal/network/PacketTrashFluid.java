package com.villfuk02.qrystal.network;

import com.villfuk02.qrystal.tileentity.ITrashableFluids;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketTrashFluid {
    private final byte tank;
    private final int world;
    private final BlockPos pos;
    
    public PacketTrashFluid(byte tank, int world, BlockPos pos) {
        this.tank = tank;
        this.world = world;
        this.pos = pos;
    }
    
    public PacketTrashFluid(PacketBuffer buffer) {
        tank = buffer.readByte();
        world = buffer.readInt();
        pos = buffer.readBlockPos();
    }
    
    public void toBytes(PacketBuffer buffer) {
        buffer.writeByte(tank);
        buffer.writeInt(world);
        buffer.writeBlockPos(pos);
    }
    
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerWorld w = context.get().getSender().world.getServer().getWorld(DimensionType.getById(world));
            TileEntity te = w.getTileEntity(pos);
            if(te instanceof ITrashableFluids) {
                ((ITrashableFluids)te).trashFluid(tank);
            }
        });
        context.get().setPacketHandled(true);
    }
    
}

