package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.List;

import static com.villfuk02.qrystal.Main.MODID;
import static com.villfuk02.qrystal.Main.MOD_ITEM_GROUP;

public class BlockBase extends Block {
    
    public BlockBase(String name, Material material, MaterialColor materialColor, SoundType sound, float hardness, float blastResistance, ToolType tool, int level, int tooltips) {
        this(name, Block.Properties.create(material, materialColor).sound(sound).hardnessAndResistance(hardness, blastResistance / 5).harvestTool(tool).harvestLevel(level), tooltips);
    }
    
    public BlockBase(String name, Block.Properties properties, int tooltips) {
        super(properties);
        setRegistryName(MODID, name);
        ModBlocks.BLOCKS.add(this);
        Item item = new BlockItemWithTooltip(this, new Item.Properties().group(MOD_ITEM_GROUP), tooltips);
        item.setRegistryName(MODID, name);
        ModItems.ITEMS.add(item);
    }
    
    static class BlockItemWithTooltip extends BlockItem {
        final int tooltips;
        
        public BlockItemWithTooltip(Block blockIn, Properties builder, int tooltips) {
            super(blockIn, builder);
            this.tooltips = tooltips;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
            super.addInformation(stack, worldIn, tooltip, flagIn);
            if(tooltips <= 0)
                return;
            if(Screen.hasShiftDown()) {
                for(int i = 0; i < tooltips; i++) {
                    tooltip.add(new TranslationTextComponent("qrystal.tooltip." + stack.getItem().getRegistryName().getPath() + "." + i).applyTextStyle(TextFormatting.BLUE));
                }
            } else {
                tooltip.add(new TranslationTextComponent("qrystal.tooltip.shift").applyTextStyle(TextFormatting.BLUE));
            }
        }
    }
}
