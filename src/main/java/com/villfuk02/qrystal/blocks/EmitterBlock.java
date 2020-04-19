package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import com.villfuk02.qrystal.tileentity.EmitterTileEntity;
import com.villfuk02.qrystal.util.CrystalUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndRodBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import static com.villfuk02.qrystal.Main.MODID;
import static com.villfuk02.qrystal.Main.MOD_ITEM_GROUP;

public class EmitterBlock extends EndRodBlock {
    public final int tier;
    
    public EmitterBlock(String name, int tier) {
        super(Block.Properties.create(Material.MISCELLANEOUS).lightValue(4).sound(SoundType.WOOD).hardnessAndResistance(0, 0));
        setRegistryName(MODID, name);
        ModBlocks.BLOCKS.add(this);
        Item item = new BlockItem(this, new Item.Properties().group(MOD_ITEM_GROUP));
        item.setRegistryName(MODID, name);
        ModItems.ITEMS.add(item);
        this.tier = tier;
    }
    
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
    
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTileEntityTypes.EMITTER.create();
    }
    
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if(!isValidPosition(state, worldIn, pos))
            worldIn.destroyBlock(pos, true);
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }
    
    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockState sittingOn = worldIn.getBlockState(pos.offset(state.get(FACING).getOpposite()));
        if(sittingOn.getBlock() instanceof ReceiverBlock)
            return ((ReceiverBlock)sittingOn.getBlock()).tier == tier;
        if(sittingOn.getBlock() instanceof QrystalBlock)
            return ((QrystalBlock)sittingOn.getBlock()).color == CrystalUtil.Color.QONDO && ((QrystalBlock)sittingOn.getBlock()).tier == tier;
        return sittingOn.getBlock() instanceof ReceiverBlock || (sittingOn.getBlock() instanceof QrystalBlock && ((QrystalBlock)sittingOn.getBlock()).color == CrystalUtil.Color.QONDO);
    }
    
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(state.getBlock() != newState.getBlock()) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof EmitterTileEntity) {
                ((EmitterTileEntity)tileEntity).deactivateBlocks(state);
            }
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }
    
    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }
}
