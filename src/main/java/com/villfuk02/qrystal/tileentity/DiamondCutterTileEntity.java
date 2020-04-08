package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraftforge.common.ToolType;

public class DiamondCutterTileEntity extends CutterTileEntity {
    public DiamondCutterTileEntity() {
        super(ModTileEntityTypes.DIAMOND_CUTTER, 3, s -> s.getToolTypes().contains(ToolType.PICKAXE), s -> 1, RecipeUtil.CuttingType.SAW, ModBlocks.DIAMOND_CUTTER);
    }
}
