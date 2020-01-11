package com.villfuk02.qrystal.util.handlers;

import com.villfuk02.qrystal.init.ModBlocks;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class EventHandler {
    @SubscribeEvent
    public static void registerBlockColors(final ColorHandlerEvent.Block event) {
        event.getBlockColors().registerBlockColorHandler(new OreColorHandler(), ModBlocks.QRYSTAL_ORE);
    }
}
