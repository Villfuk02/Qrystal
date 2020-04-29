package com.villfuk02.qrystal.util.handlers;

import com.villfuk02.qrystal.renderers.CondensedMaterialTileEntityRenderer;
import com.villfuk02.qrystal.renderers.ReservoirItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

import java.util.concurrent.Callable;

public class ISTERHandler {
    
    public static Callable<ItemStackTileEntityRenderer> condensedMaterial() {
        return CondensedMaterialTileEntityRenderer::new;
    }
    
    public static Callable<ItemStackTileEntityRenderer> reservoir() {
        return ReservoirItemStackTileEntityRenderer::new;
    }
    
}
