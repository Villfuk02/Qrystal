package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.util.CrystalUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.ToolType;

public class QrystalOre extends BlockBase {
    
    private final boolean rich;
    
    public QrystalOre(CrystalUtil.Color color, boolean rich) {
        super(color.toString() + "_ore" + (rich ? "_rich" : ""), Material.ROCK, MaterialColor.STONE, SoundType.STONE, 3f, 15f, ToolType.PICKAXE, rich ? 3 : 2);
        this.rich = rich;
    }
    
    @Override
    public int getExpDrop(BlockState state, IWorldReader world, BlockPos pos, int fortune, int silktouch) {
        return silktouch == 0 ? (rich ? 3 + RANDOM.nextInt(4) : 2 + RANDOM.nextInt(3)) : 0;
    }
}
