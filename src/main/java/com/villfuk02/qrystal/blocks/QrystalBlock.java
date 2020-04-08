package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.crafting.ActivationRecipe;
import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.util.CrystalUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import static com.villfuk02.qrystal.Main.MODID;
import static com.villfuk02.qrystal.Main.MOD_ITEM_GROUP;

public class QrystalBlock extends Block {
    
    private final boolean activated;
    private final int tier;
    private final CrystalUtil.Color color;
    
    public QrystalBlock(CrystalUtil.Color color, int tier, boolean activated) {
        super(Block.Properties.create(Material.ROCK, color.getMapColor()).sound(SoundType.STONE).hardnessAndResistance(3f, 15f / 5).harvestTool(ToolType.PICKAXE).harvestLevel(0));
        String name = (activated ? "activated_" : "") + color.toString() + "_block_" + tier;
        this.activated = activated;
        this.tier = tier;
        this.color = color;
        setRegistryName(MODID, name);
        ModBlocks.BLOCKS.add(this);
        Item item = new QrystalBlockItem(this, new Item.Properties().group(MOD_ITEM_GROUP));
        item.setRegistryName(MODID, name);
        ModItems.ITEMS.add(item);
    }
    
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        checkActivation(worldIn, pos);
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }
    
    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        checkActivation(worldIn, pos);
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
    }
    
    public void checkActivation(World worldIn, BlockPos pos) {
        if(activated || color == CrystalUtil.Color.QONDO)
            return;
        ActivationRecipe recipe = (ActivationRecipe)worldIn.getRecipeManager()
                .getRecipes()
                .stream()
                .filter(r -> r.getType() == ActivationRecipe.ActivationRecipeType.ACTIVATION)
                .filter(r -> ((ActivationRecipe)r).color == color)
                .findFirst()
                .get();
        ResourceLocation up = tier == 0 ? recipe.tier1 : (tier == 1 ? recipe.tier2 : recipe.tier3);
        if(worldIn.getBlockState(pos.west()).getBlock().getRegistryName().equals(up) && worldIn.getBlockState(pos.east()).getBlock().getRegistryName().equals(up) &&
                worldIn.getBlockState(pos.south()).getBlock().getRegistryName().equals(up) && worldIn.getBlockState(pos.north()).getBlock().getRegistryName().equals(up)) {
            worldIn.setBlockState(pos, ModBlocks.QRYSTAL_BLOCKS.get("activated_" + color.toString() + "_" + tier).getDefaultState(), 3);
            worldIn.setBlockState(pos.west(), Blocks.AIR.getDefaultState(), 3);
            worldIn.setBlockState(pos.east(), Blocks.AIR.getDefaultState(), 3);
            worldIn.setBlockState(pos.north(), Blocks.AIR.getDefaultState(), 3);
            worldIn.setBlockState(pos.south(), Blocks.AIR.getDefaultState(), 3);
            worldIn.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, 0.8F);
        }
    }
    
    class QrystalBlockItem extends BlockItem {
        public QrystalBlockItem(Block blockIn, Properties builder) {
            super(blockIn, builder);
        }
        
        @Override
        public ITextComponent getDisplayName(ItemStack stack) {
            if(activated)
                return new TranslationTextComponent("qrystal.activated").appendText(" ")
                        .appendSibling(new TranslationTextComponent("qrystal.mat." + color.toString()))
                        .appendText(" ")
                        .appendSibling(new TranslationTextComponent("qrystal.block+tier"))
                        .appendText(" " + (tier + 1));
            return new TranslationTextComponent("qrystal.mat." + color.toString()).appendText(" ").appendSibling(new TranslationTextComponent("qrystal.block+tier")).appendText(" " + (tier + 1));
        }
    }
}
