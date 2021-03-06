package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.tileentity.EvaporatorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import static com.villfuk02.qrystal.Main.MODID;
import static com.villfuk02.qrystal.Main.MOD_ITEM_GROUP;

public abstract class EvaporatorBlock extends HorizontalBlock {
    
    public EvaporatorBlock(String id) {
        super(Properties.create(Material.IRON, MaterialColor.IRON).sound(SoundType.STONE).hardnessAndResistance(2f, 60f / 5f));
        setRegistryName(MODID, id);
        ModBlocks.BLOCKS.add(this);
        Item item = new BlockBase.BlockItemWithTooltip(this, new Item.Properties().group(MOD_ITEM_GROUP), 2);
        item.setRegistryName(MODID, id);
        ModItems.ITEMS.add(item);
        setDefaultState(getDefaultState().with(HORIZONTAL_FACING, Direction.NORTH));
    }
    
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
    
    @Override
    public void onReplaced(BlockState oldState, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(oldState.getBlock() != newState.getBlock()) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof EvaporatorTileEntity) {
                ItemStackHandler inventory = ((EvaporatorTileEntity)tileEntity).inventory;
                for(int slot = 0; slot < inventory.getSlots(); ++slot)
                    InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(slot));
            }
            worldIn.updateComparatorOutputLevel(pos, this);
        }
        super.onReplaced(oldState, worldIn, pos, newState, isMoving);
    }
    
    
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof EvaporatorTileEntity)
                NetworkHooks.openGui((ServerPlayerEntity)player, (EvaporatorTileEntity)tileEntity, pos);
        }
        return ActionResultType.SUCCESS;
    }
    
    
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
    }
    
    
    @Override
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof EvaporatorTileEntity)
            return ItemHandlerHelper.calcRedstoneFromInventory(((EvaporatorTileEntity)tileEntity).inventory);
        return super.getComparatorInputOverride(blockState, worldIn, pos);
    }
    
    
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(HORIZONTAL_FACING);
    }
    
    
    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(HORIZONTAL_FACING, rot.rotate(state.get(HORIZONTAL_FACING)));
    }
    
    
    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(HORIZONTAL_FACING)));
    }
    
    
}
