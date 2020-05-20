package com.villfuk02.qrystal.blocks;

import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.tileentity.EmitterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;

import static com.villfuk02.qrystal.Main.MODID;
import static com.villfuk02.qrystal.Main.MOD_ITEM_GROUP;

public class ReceiverBlock extends Block {
    
    public final boolean activated;
    public final int tier;
    
    public ReceiverBlock(int tier, boolean activated) {
        super(Properties.create(Material.ROCK, MaterialColor.BLACK).sound(SoundType.STONE).hardnessAndResistance(2f, 10f / 5).harvestTool(ToolType.PICKAXE).harvestLevel(0));
        String name = (activated ? "activated_" : "") + "receiver_" + tier;
        this.activated = activated;
        this.tier = tier;
        setRegistryName(MODID, name);
        ModBlocks.BLOCKS.add(this);
        Item item;
        if(activated)
            item = new ReceiverBlockItem(this, new Item.Properties().group(MOD_ITEM_GROUP));
        else
            item = new ReceiverBlockItem(this, new Item.Properties());
        item.setRegistryName(MODID, name);
        ModItems.ITEMS.add(item);
    }
    
    
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return ModBlocks.RECEIVERS.get(Integer.toString(tier)).getDefaultState();
    }
    
    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
    
    public void activate(World worldIn, BlockPos pos, HashSet<BlockPos> uPos, BlockPos source) {
        if(!activated) {
            worldIn.setBlockState(pos, ModBlocks.RECEIVERS.get("activated_" + tier).getDefaultState(), 3);
        }
        for(Direction dir : Direction.values()) {
            if(worldIn.getBlockState(pos.offset(dir)).getBlock() instanceof EmitterBlock && worldIn.getBlockState(pos.offset(dir)).get(DirectionalBlock.FACING) == dir) {
                if(uPos.contains(pos.offset(dir))) {
                    worldIn.createExplosion(null, pos.offset(dir).getX(), pos.offset(dir).getY(), pos.offset(dir).getZ(), 1.5f, true, Explosion.Mode.BREAK);
                } else {
                    EmitterTileEntity te = (EmitterTileEntity)worldIn.getTileEntity(pos.offset(dir));
                    te.uPos = (HashSet<BlockPos>)uPos.clone();
                    te.uPos.add(source);
                }
            }
        }
        
    }
    
    public void deactivate(World worldIn, BlockPos pos) {
        if(activated)
            worldIn.setBlockState(pos, ModBlocks.RECEIVERS.get(Integer.toString(tier)).getDefaultState(), 3);
        
    }
    
    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(ModBlocks.RECEIVERS.get(Integer.toString(tier)));
    }
    
    class ReceiverBlockItem extends BlockItem {
        public ReceiverBlockItem(Block blockIn, Properties builder) {
            super(blockIn, builder);
        }
        
        @Override
        public ITextComponent getDisplayName(ItemStack stack) {
            return new TranslationTextComponent("qrystal.receiver+tier").appendText(" " + (tier + 1));
        }
        
        @Override
        public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
            super.addInformation(stack, worldIn, tooltip, flagIn);
            if(Screen.hasShiftDown()) {
                tooltip.add(new TranslationTextComponent("qrystal.tooltip.receiver.0", tier + 1).applyTextStyle(TextFormatting.BLUE));
                tooltip.add(new TranslationTextComponent("qrystal.tooltip.receiver.1", tier + 1).applyTextStyle(TextFormatting.BLUE));
            } else {
                tooltip.add(new TranslationTextComponent("qrystal.tooltip.shift").applyTextStyle(TextFormatting.BLUE));
            }
        }
    }
}
