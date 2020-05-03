package com.villfuk02.qrystal.network;

import com.villfuk02.qrystal.Main;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Networking {
    public static SimpleChannel INSTANCE;
    private static int ID = 0;
    
    public static int nextID() {
        return ID++;
    }
    
    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Main.MODID, "network_channel"), () -> "1.0", s -> true, s -> true);
        
        INSTANCE.registerMessage(nextID(), PacketCycleAutoIO.class, PacketCycleAutoIO::toBytes, PacketCycleAutoIO::new, PacketCycleAutoIO::handle);
        INSTANCE.registerMessage(nextID(), PacketTrashFluid.class, PacketTrashFluid::toBytes, PacketTrashFluid::new, PacketTrashFluid::handle);
    }
    
}
