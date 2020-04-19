package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import com.villfuk02.qrystal.util.RecipeUtil;

public class SteelCutterTileEntity extends CutterTileEntity {
    public SteelCutterTileEntity() {
        super(ModTileEntityTypes.STEEL_CUTTER, 2, s -> s.getItem() == ModItems.STEEL_BLADE, s -> 0, (byte)1, RecipeUtil.CuttingType.SAW, ModBlocks.STEEL_CUTTER);
    }
}
