package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class BlockBase extends Block implements IHasModel {
    
    public BlockBase(String name, Material material) {
        super(material);
        setUnlocalizedName(name);
        setRegistryName(name);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }
    
    public BlockBase(String name, Material material, float hardness, float blastResistance) {
        this(name, material);
        setHardness(hardness);
        setResistance(blastResistance / 3f);
    }
    
    public BlockBase(String name, Material material, float hardness, float blastResistance, String tool, int level) {
        this(name, material, hardness, blastResistance);
        setHarvestLevel(tool, level);
    }
    
    @Override
    public void registerModels() {
        Main.proxy.registerItemRenderer(Item.getItemFromBlock(this), 0, "inventory");
    }
}
