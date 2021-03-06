package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.QrystalConfig;
import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import com.villfuk02.qrystal.items.BarrelUpgrade;
import com.villfuk02.qrystal.items.Hammer;
import com.villfuk02.qrystal.tileentity.CondensingBarrelTileEntity;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

import static com.villfuk02.qrystal.Main.MODID;
import static com.villfuk02.qrystal.Main.MOD_ITEM_GROUP;

public class CondensingBarrelBlock extends Block {
    
    public int tier;
    
    public CondensingBarrelBlock(String name, int tier, Material mat, MaterialColor color, SoundType sound) {
        super(Properties.create(mat, color).sound(sound).hardnessAndResistance(2f, 60f / 5f));
        String id = name + "_condensing_barrel";
        this.tier = tier;
        setRegistryName(MODID, id);
        ModBlocks.BLOCKS.add(this);
        Item item = new CondensingBarrelBlockItem(this, new Item.Properties().group(MOD_ITEM_GROUP));
        item.setRegistryName(MODID, id);
        ModItems.ITEMS.add(item);
    }
    
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
    
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTileEntityTypes.CONDENSING_BARREL.create();
    }
    
    @Override
    public void onReplaced(BlockState oldState, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(oldState.getBlock() != newState.getBlock()) {
            if(newState.getBlock() instanceof CondensingBarrelBlock) {
                return;
            } else {
                TileEntity tileEntity = worldIn.getTileEntity(pos);
                if(tileEntity instanceof CondensingBarrelTileEntity)
                    ((CondensingBarrelTileEntity)tileEntity).drop();
            }
            worldIn.updateComparatorOutputLevel(pos, this);
        }
        super.onReplaced(oldState, worldIn, pos, newState, isMoving);
    }
    
    
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof CondensingBarrelTileEntity) {
            CondensingBarrelTileEntity e = (CondensingBarrelTileEntity)tileEntity;
            if(!worldIn.isRemote) {
                if(player.isShiftKeyDown()) {
                    e.cycleMode();
                    return ActionResultType.SUCCESS;
                } else {
                    ItemStack itemstack = player.getHeldItem(hand);
                    if(!itemstack.isEmpty()) {
                        if(itemstack.getItem() instanceof Hammer) {
                            e.giveAll(player);
                        } else if(itemstack.getItem() instanceof BarrelUpgrade && ((BarrelUpgrade)itemstack.getItem()).level == tier) {
                            worldIn.setBlockState(pos, ForgeRegistries.BLOCKS.getValue(new ResourceLocation(MODID, ((BarrelUpgrade)itemstack.getItem()).target + "_condensing_barrel")).getDefaultState(), 3);
                            itemstack.shrink(1);
                        } else if(e.acceptedAmt(itemstack) > 0) {
                            e.modifyAmount(itemstack.split(Math.min(e.acceptedAmt(itemstack), itemstack.getCount())), false);
                        }
                    } else {
                        ItemStack stack = e.getStack(false);
                        e.modifyAmount(stack, true);
                        ItemHandlerHelper.giveItemToPlayer(player, stack, player.inventory.currentItem);
                    }
                }
            }
        }
        return ActionResultType.SUCCESS;
    }
    
    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }
    
    @Override
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof CondensingBarrelTileEntity)
            return ((CondensingBarrelTileEntity)tileEntity).getComparatorLevel();
        return super.getComparatorInputOverride(blockState, worldIn, pos);
    }
    
    public static class CondensingBarrelBlockItem extends BlockItem {
        
        public CondensingBarrelBlockItem(Block blockIn, Properties builder) {
            super(blockIn, builder);
        }
        
        @Override
        public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
            super.addInformation(stack, worldIn, tooltip, flagIn);
            if(Screen.hasShiftDown()) {
                tooltip.add(new TranslationTextComponent("qrystal.tooltip.condensing_barrel.0", String.format(Locale.US, "%,d", QrystalConfig.barrel_base_size *
                        RecipeUtil.longPositivePower(QrystalConfig.barrel_tier_multiplier, ((CondensingBarrelBlock)getBlock()).tier)).replace(",", " ")).applyTextStyle(TextFormatting.BLUE));
                tooltip.add(new TranslationTextComponent("qrystal.tooltip.condensing_barrel.1").applyTextStyle(TextFormatting.BLUE));
                tooltip.add(new TranslationTextComponent("qrystal.tooltip.condensing_barrel.2").applyTextStyle(TextFormatting.BLUE));
                tooltip.add(new TranslationTextComponent("qrystal.tooltip.condensing_barrel.3").applyTextStyle(TextFormatting.BLUE));
            } else {
                tooltip.add(new TranslationTextComponent("qrystal.tooltip.shift").applyTextStyle(TextFormatting.BLUE));
            }
        }
    }
}
