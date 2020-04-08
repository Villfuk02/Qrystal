package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraftforge.common.ToolType;

public class SteelCutterTileEntity extends CutterTileEntity {
    public SteelCutterTileEntity() {
        super(ModTileEntityTypes.STEEL_CUTTER, 2, s -> s.getToolTypes().contains(ToolType.PICKAXE), s -> 0, RecipeUtil.CuttingType.SAW, ModBlocks.STEEL_CUTTER);
    }
}
