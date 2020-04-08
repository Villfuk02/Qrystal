package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.common.ToolType;

import static com.villfuk02.qrystal.Main.MODID;
import static com.villfuk02.qrystal.Main.MOD_ITEM_GROUP;

public class BlockBase extends Block {
    
    public BlockBase(String name, Material material, MaterialColor materialColor, SoundType sound, float hardness, float blastResistance, ToolType tool, int level) {
        this(name,
             Block.Properties.create(material, materialColor).sound(sound).hardnessAndResistance(hardness, blastResistance / 5).harvestTool(tool).harvestLevel(level));
    }
    
    public BlockBase(String name, Block.Properties properties) {
        super(properties);
        setRegistryName(MODID, name);
        ModBlocks.BLOCKS.add(this);
        Item item = new BlockItem(this, new Item.Properties().group(MOD_ITEM_GROUP));
        item.setRegistryName(MODID, name);
        ModItems.ITEMS.add(item);
    }
}
