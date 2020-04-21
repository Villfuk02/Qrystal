package com.villfuk02.qrystal.util.handlers;

import com.villfuk02.qrystal.items.CondensedMaterialTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

import java.util.concurrent.Callable;

public class ISTERHandler {
    
    public static Callable<ItemStackTileEntityRenderer> condensedMaterial() {
        return CondensedMaterialTileEntityRenderer::new;
    }
    
}
