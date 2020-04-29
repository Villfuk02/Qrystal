package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import com.villfuk02.qrystal.items.Crystal;
import com.villfuk02.qrystal.util.CrystalUtil;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraftforge.common.util.Constants;

public class LaserCutterTileEntity extends CutterTileEntity {
    public LaserCutterTileEntity() {
        super(ModTileEntityTypes.LASER_CUTTER, 6, s -> s.getItem() instanceof Crystal && ((Crystal)s.getItem()).size == CrystalUtil.Size.LARGE && ((Crystal)s.getItem()).tier >= 6 && s.hasTag() &&
                      s.getTag().contains("material", Constants.NBT.TAG_STRING) && s.getTag().getString("material").equals(CrystalUtil.Color.QALB.toString()), s -> ((Crystal)s.getItem()).tier - 6, (byte)3,
              RecipeUtil.CuttingType.LASER, ModBlocks.LASER_CUTTER);
    }
}
