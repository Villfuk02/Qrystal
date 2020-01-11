package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.Main;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import static com.villfuk02.qrystal.Main.qrystalColor;

public class QrystalOre extends BlockBase {
    
    public static final IProperty<Boolean> rich = PropertyBool.create("rich");
    
    int[] valid = new int[]{0, 1, 2, 3, 9, 10, 11, 12, 13, 14, 15};
    
    public QrystalOre() {
        super("qrystal_ore", Material.ROCK, 3f, 15f, "pickaxe", 2);
        setDefaultState(blockState.getBaseState().withProperty(qrystalColor, Main.EnumColor.QLEAR).withProperty(rich, false));
    }
    
    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, qrystalColor, rich);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(rich) ? 8 : 0) + state.getValue(qrystalColor).getMeta();
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return blockState.getBaseState().withProperty(qrystalColor, Main.EnumColor.fromMeta(meta % 8)).withProperty(rich, meta >= 8);
    }
    
    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        if(getItemDropped(state, RANDOM, fortune) != Item.getItemFromBlock(this)) {
            return 3 + RANDOM.nextInt(4);
        }
        return 0;
    }
    
    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for(int i : valid) {
            items.add(new ItemStack(this, 1, i));
        }
    }
    
    @Override
    public void registerModels() {
        for(int i : valid) {
            Main.proxy.registerItemRenderer(Item.getItemFromBlock(this), i, "inventory");
        }
    }
}
