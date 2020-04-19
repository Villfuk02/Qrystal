package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import com.villfuk02.qrystal.util.RecipeUtil;

public class DiamondCutterTileEntity extends CutterTileEntity {
    public DiamondCutterTileEntity() {
        super(ModTileEntityTypes.DIAMOND_CUTTER, 3, s -> s.getItem() == ModItems.DIAMOND_BLADE, s -> 1, (byte)1, RecipeUtil.CuttingType.SAW, ModBlocks.DIAMOND_CUTTER);
    }
}
