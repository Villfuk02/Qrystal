package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.init.ModTileEntityTypes;
import com.villfuk02.qrystal.tileentity.DryerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class DryerBlock extends BlockBase {
    
    protected static final VoxelShape BOTTOM = Block.makeCuboidShape(2, 0, 2, 14, 2, 14);
    protected static final VoxelShape X_CROSS = Block.makeCuboidShape(0, 2, 2, 16, 10, 14);
    protected static final VoxelShape Z_CROSS = Block.makeCuboidShape(2, 2, 0, 14, 10, 16);
    protected static final VoxelShape COMBINED = VoxelShapes.or(BOTTOM, VoxelShapes.combine(X_CROSS, Z_CROSS, IBooleanFunction.NOT_SAME));
    
    public DryerBlock() {
        super("dryer", Block.Properties.create(Material.ROCK, MaterialColor.ADOBE)
                .sound(SoundType.STONE)
                .hardnessAndResistance(4f, 60f / 5f)
                .harvestTool(ToolType.PICKAXE)
                .harvestLevel(0));
    }
    
    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }
    
    
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return ModTileEntityTypes.DRYER.create();
    }
    
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if(tileentity instanceof DryerTileEntity) {
                ((DryerTileEntity)tileentity).crystallize();
                InventoryHelper.dropInventoryItems(worldIn, pos, (DryerTileEntity)tileentity);
                ((DryerTileEntity)tileentity).drop(false, ((DryerTileEntity)tileentity).removeWasteAsDust());
                ((DryerTileEntity)tileentity).drop(false, ((DryerTileEntity)tileentity).removeSeeds());
                worldIn.updateComparatorOutputLevel(pos, this);
            }
            
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }
    
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return COMBINED;
    }
    
    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }
    
    @Override
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if(tileentity instanceof DryerTileEntity) {
            return ((DryerTileEntity)tileentity).getComparatorLevel();
        }
        return 0;
    }
    
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult traceResult) {
        ItemStack itemstack = player.getHeldItem(hand);
        if(itemstack.isEmpty()) {
            return ActionResultType.CONSUME;
        } else {
            if(world.getTileEntity(pos) instanceof DryerTileEntity) {
                DryerTileEntity te = (DryerTileEntity)world.getTileEntity(pos);
                if(!world.isRemote) {
                    if(te.acceptsItemAmt(itemstack) > 0) {
                        if(itemstack.getItem() == Items.WATER_BUCKET) {
                            if(!player.abilities.isCreativeMode) {
                                player.setHeldItem(hand, new ItemStack(Items.BUCKET));
                            }
                            te.refill();
                            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        } else {
                            te.setInventorySlotContents(0, itemstack.split(te.acceptsItemAmt(itemstack)));
                            player.setHeldItem(hand, itemstack);
                            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                    } else {
                        return ActionResultType.CONSUME;
                    }
                }
                return ActionResultType.SUCCESS;
            }
        }
        world.notifyBlockUpdate(pos, state, state, 2);
        return ActionResultType.CONSUME;
    }
}
