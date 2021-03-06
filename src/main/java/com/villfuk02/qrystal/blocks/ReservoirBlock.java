package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import com.villfuk02.qrystal.tileentity.ReservoirTileEntity;
import com.villfuk02.qrystal.util.handlers.ISTERHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

import static com.villfuk02.qrystal.Main.MODID;
import static com.villfuk02.qrystal.Main.MOD_ITEM_GROUP;

public class ReservoirBlock extends Block {
    public ReservoirBlock() {
        super(Block.Properties.create(Material.GLASS).hardnessAndResistance(0.6f).sound(SoundType.GLASS).notSolid());
        setRegistryName(MODID, "reservoir");
        ModBlocks.BLOCKS.add(this);
        Item item = new ReservoirBlockItem(this);
        item.setRegistryName(MODID, "reservoir");
        ModItems.ITEMS.add(item);
    }
    
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
    
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTileEntityTypes.RESERVOIR.create();
    }
    
    @Override
    public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
    }
    
    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 1;
    }
    
    @Override
    public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
    }
    
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult traceResult) {
        ItemStack itemstack = player.getHeldItem(hand);
        if(world.getTileEntity(pos) instanceof ReservoirTileEntity) {
            ReservoirTileEntity te = (ReservoirTileEntity)world.getTileEntity(pos);
            if(itemstack.getItem() instanceof BucketItem) {
                IFluidHandler cap = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
                if(((BucketItem)itemstack.getItem()).getFluid() == Fluids.EMPTY) {
                    if((cap.drain(FluidAttributes.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE).getAmount() >= FluidAttributes.BUCKET_VOLUME &&
                            cap.drain(FluidAttributes.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE).getFluid().getFilledBucket() != null) || player.abilities.isCreativeMode) {
                        Fluid taken = cap.drain(FluidAttributes.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE).getFluid();
                        if(!player.abilities.isCreativeMode) {
                            itemstack.shrink(1);
                            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(taken.getFilledBucket()), player.inventory.currentItem);
                        }
                        world.playSound(player, pos, taken.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1, 1);
                        world.updateComparatorOutputLevel(pos, state.getBlock());
                        return ActionResultType.SUCCESS;
                    }
                } else if(cap.fill(new FluidStack(((BucketItem)itemstack.getItem()).getFluid(), FluidAttributes.BUCKET_VOLUME), IFluidHandler.FluidAction.SIMULATE) >= FluidAttributes.BUCKET_VOLUME) {
                    cap.fill(new FluidStack(((BucketItem)itemstack.getItem()).getFluid(), FluidAttributes.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
                    world.playSound(player, pos, ((BucketItem)itemstack.getItem()).getFluid().isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1,
                                    1);
                    if(!player.abilities.isCreativeMode) {
                        player.setHeldItem(hand, new ItemStack(Items.BUCKET));
                    }
                }
                world.updateComparatorOutputLevel(pos, state.getBlock());
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.FAIL;
    }
    
    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }
    
    @Override
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof ReservoirTileEntity)
            return ((ReservoirTileEntity)tileEntity).getComparatorLevel();
        return super.getComparatorInputOverride(blockState, worldIn, pos);
    }
    
    public class ReservoirBlockItem extends BlockItem {
        public ReservoirBlockItem(ReservoirBlock reservoirBlock) {
            super(reservoirBlock, new Item.Properties().group(MOD_ITEM_GROUP).setISTER(ISTERHandler::reservoir));
        }
        
        @Override
        public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
            super.addInformation(stack, worldIn, tooltip, flagIn);
            if(stack.hasTag() && stack.getTag().contains("BlockEntityTag", Constants.NBT.TAG_COMPOUND) && stack.getTag().getCompound("BlockEntityTag").contains("Amount", Constants.NBT.TAG_INT) &&
                    stack.getTag().getCompound("BlockEntityTag").contains("FluidName", Constants.NBT.TAG_STRING)) {
                if(stack.getTag().getCompound("BlockEntityTag").getInt("Amount") > 0) {
                    int a = stack.getTag().getCompound("BlockEntityTag").getInt("Amount");
                    Fluid f = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(stack.getTag().getCompound("BlockEntityTag").getString("FluidName")));
                    if(f != null)
                        tooltip.add(new TranslationTextComponent(new FluidStack(f, a).getTranslationKey()).appendSibling(new StringTextComponent(" " + a + "mB")).applyTextStyle(TextFormatting.YELLOW));
                    else
                        tooltip.add(new TranslationTextComponent("qrystal.invalid_fluid").applyTextStyle(TextFormatting.RED));
                }
            }
            if(Screen.hasShiftDown()) {
                tooltip.add(new TranslationTextComponent("qrystal.tooltip.reservoir.0").applyTextStyle(TextFormatting.BLUE));
                tooltip.add(new TranslationTextComponent("qrystal.tooltip.reservoir.1").applyTextStyle(TextFormatting.BLUE));
            } else {
                tooltip.add(new TranslationTextComponent("qrystal.tooltip.shift").applyTextStyle(TextFormatting.BLUE));
            }
        }
    }
    
}
