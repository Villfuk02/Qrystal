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
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.Minecraft;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;

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
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        checkActivation(worldIn, pos, false);
    }
    
    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
        checkActivation(worldIn, pos, true);
    }
    
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onReplaced(state, worldIn, pos, newState, isMoving);
        if(color == CrystalUtil.Color.QONDO)
            scheduleChecks(worldIn, pos);
        else
            checkActivation(worldIn, pos, false);
    }
    
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return checkActivationForPlacement(context.getWorld(), context.getPos());
    }
    
    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
    
    public void checkActivation(World worldIn, BlockPos pos, boolean placed) {
        if(color == CrystalUtil.Color.QALB) {
            boolean[] colors = new boolean[8];
            Direction qondoDir = null;
            for(Direction dir : Direction.values()) {
                if(worldIn.getBlockState(pos.offset(dir)).getBlock() instanceof QrystalBlock) {
                    QrystalBlock b = (QrystalBlock)worldIn.getBlockState(pos.offset(dir)).getBlock();
                    if(b.tier == tier && (b.activated || b.color == CrystalUtil.Color.QONDO)) {
                        colors[b.color.getId()] = true;
                        if(b.color == CrystalUtil.Color.QONDO)
                            qondoDir = dir;
                    }
                }
            }
            if(qondoDir != null && colors[1] && colors[2] && colors[3] && colors[4] && colors[5] && colors[6]) {
                boolean outer = true;
                if(tier != 0) {
                    for(Direction dir : Direction.values()) {
                        if(dir == qondoDir)
                            continue;
                        if(!(worldIn.getBlockState(pos.offset(dir, tier == 2 ? 4 : 2)).getBlock() instanceof QrystalBlock)) {
                            outer = false;
                            break;
                        }
                        QrystalBlock b = (QrystalBlock)worldIn.getBlockState(pos.offset(dir, tier == 2 ? 4 : 2)).getBlock();
                        if(b.tier != tier - 1 || !b.activated || b.color != CrystalUtil.Color.QONDO) {
                            outer = false;
                            break;
                        }
                    }
                }
                if((placed || !activated) && outer) {
                    activate(worldIn, pos);
                    BlockState QBS = worldIn.getBlockState(pos.offset(qondoDir));
                    if(QBS.getBlock() instanceof QrystalBlock && ((QrystalBlock)QBS.getBlock()).tier == tier && ((QrystalBlock)QBS.getBlock()).color == CrystalUtil.Color.QONDO)
                        ((QrystalBlock)QBS.getBlock()).activate(worldIn, pos.offset(qondoDir));
                } else if((placed || activated) && !outer) {
                    deactivate(worldIn, pos);
                    BlockState QBS = worldIn.getBlockState(pos.offset(qondoDir));
                    if(QBS.getBlock() instanceof QrystalBlock && ((QrystalBlock)QBS.getBlock()).tier == tier && ((QrystalBlock)QBS.getBlock()).color == CrystalUtil.Color.QONDO)
                        ((QrystalBlock)QBS.getBlock()).deactivate(worldIn, pos.offset(qondoDir));
                }
            } else {
                if(placed || activated) {
                    deactivate(worldIn, pos);
                    if(qondoDir != null) {
                        BlockState QBS = worldIn.getBlockState(pos.offset(qondoDir));
                        if(QBS.getBlock() instanceof QrystalBlock && ((QrystalBlock)QBS.getBlock()).tier == tier && ((QrystalBlock)QBS.getBlock()).color == CrystalUtil.Color.QONDO)
                            ((QrystalBlock)QBS.getBlock()).deactivate(worldIn, pos.offset(qondoDir));
                    }
                }
            }
        } else if(color != CrystalUtil.Color.QONDO) {
            if(!activated) {
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
                    worldIn.setBlockState(pos.west(), Blocks.AIR.getDefaultState(), 3);
                    worldIn.setBlockState(pos.east(), Blocks.AIR.getDefaultState(), 3);
                    worldIn.setBlockState(pos.north(), Blocks.AIR.getDefaultState(), 3);
                    worldIn.setBlockState(pos.south(), Blocks.AIR.getDefaultState(), 3);
                    for(int i = 0; i < 10; ++i) {
                        double d0 = worldIn.rand.nextGaussian() * 0.02D;
                        double d1 = worldIn.rand.nextGaussian() * 0.02D;
                        double d2 = worldIn.rand.nextGaussian() * 0.02D;
                        Minecraft.getInstance().world.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 1 + worldIn.rand.nextFloat(), pos.getY() + worldIn.rand.nextFloat(),
                                                                  pos.getZ() + worldIn.rand.nextFloat(), d0, d1, d2);
                        Minecraft.getInstance().world.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() - 1 + worldIn.rand.nextFloat(), pos.getY() + worldIn.rand.nextFloat(),
                                                                  pos.getZ() + worldIn.rand.nextFloat(), d0, d1, d2);
                        Minecraft.getInstance().world.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + worldIn.rand.nextFloat(), pos.getY() + worldIn.rand.nextFloat(),
                                                                  pos.getZ() + 1 + worldIn.rand.nextFloat(), d0, d1, d2);
                        Minecraft.getInstance().world.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + worldIn.rand.nextFloat(), pos.getY() + worldIn.rand.nextFloat(),
                                                                  pos.getZ() - 1 + worldIn.rand.nextFloat(), d0, d1, d2);
                    }
                    activate(worldIn, pos);
                }
            }
        }
    }
    
    public BlockState checkActivationForPlacement(World worldIn, BlockPos pos) {
        if(color == CrystalUtil.Color.QONDO)
            return ModBlocks.QRYSTAL_BLOCKS.get(color.toString() + "_" + tier).getDefaultState();
        if(color == CrystalUtil.Color.QALB) {
            boolean[] colors = new boolean[8];
            Direction qondoDir = null;
            for(Direction dir : Direction.values()) {
                if(worldIn.getBlockState(pos.offset(dir)).getBlock() instanceof QrystalBlock) {
                    QrystalBlock b = (QrystalBlock)worldIn.getBlockState(pos.offset(dir)).getBlock();
                    if(b.tier == tier && (b.activated || b.color == CrystalUtil.Color.QONDO)) {
                        colors[b.color.getId()] = true;
                        if(b.color == CrystalUtil.Color.QONDO)
                            qondoDir = dir;
                    }
                }
            }
            if(qondoDir != null && colors[1] && colors[2] && colors[3] && colors[4] && colors[5] && colors[6]) {
                boolean outer = true;
                if(tier != 0) {
                    for(Direction dir : Direction.values()) {
                        if(dir == qondoDir)
                            continue;
                        if(!(worldIn.getBlockState(pos.offset(dir, tier == 2 ? 4 : 2)).getBlock() instanceof QrystalBlock)) {
                            outer = false;
                            break;
                        }
                        QrystalBlock b = (QrystalBlock)worldIn.getBlockState(pos.offset(dir, tier == 2 ? 4 : 2)).getBlock();
                        if(b.tier != tier - 1 || !b.activated || b.color != CrystalUtil.Color.QONDO) {
                            outer = false;
                            break;
                        }
                    }
                }
                return ModBlocks.QRYSTAL_BLOCKS.get((outer ? "activated_" : "") + color.toString() + "_" + tier).getDefaultState();
            } else {
                return ModBlocks.QRYSTAL_BLOCKS.get(color.toString() + "_" + tier).getDefaultState();
            }
        } else {
            if(!activated) {
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
                    worldIn.setBlockState(pos.west(), Blocks.AIR.getDefaultState(), 3);
                    worldIn.setBlockState(pos.east(), Blocks.AIR.getDefaultState(), 3);
                    worldIn.setBlockState(pos.north(), Blocks.AIR.getDefaultState(), 3);
                    worldIn.setBlockState(pos.south(), Blocks.AIR.getDefaultState(), 3);
                    for(int i = 0; i < 10; ++i) {
                        double d0 = worldIn.rand.nextGaussian() * 0.02D;
                        double d1 = worldIn.rand.nextGaussian() * 0.02D;
                        double d2 = worldIn.rand.nextGaussian() * 0.02D;
                        worldIn.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 1 + worldIn.rand.nextFloat(), pos.getY() + worldIn.rand.nextFloat(), pos.getZ() + worldIn.rand.nextFloat(), d0, d1, d2);
                        worldIn.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() - 1 + worldIn.rand.nextFloat(), pos.getY() + worldIn.rand.nextFloat(), pos.getZ() + worldIn.rand.nextFloat(), d0, d1, d2);
                        worldIn.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + worldIn.rand.nextFloat(), pos.getY() + worldIn.rand.nextFloat(), pos.getZ() + 1 + worldIn.rand.nextFloat(), d0, d1, d2);
                        worldIn.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + worldIn.rand.nextFloat(), pos.getY() + worldIn.rand.nextFloat(), pos.getZ() - 1 + worldIn.rand.nextFloat(), d0, d1, d2);
                    }
                    worldIn.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, 2.5F);
                    return ModBlocks.QRYSTAL_BLOCKS.get("activated_" + color.toString() + "_" + tier).getDefaultState();
                }
            }
        }
        return ModBlocks.QRYSTAL_BLOCKS.get((activated ? "activated_" : "") + color.toString() + "_" + tier).getDefaultState();
    }
    
    public void activate(World worldIn, BlockPos pos) {
        if(!activated) {
            worldIn.setBlockState(pos, ModBlocks.QRYSTAL_BLOCKS.get("activated_" + color.toString() + "_" + tier).getDefaultState(), 3);
            worldIn.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, 2.5F);
            if(color == CrystalUtil.Color.QONDO)
                scheduleChecks(worldIn, pos);
        }
        if(color == CrystalUtil.Color.QALB) {
            for(int i = 0; i < 20 + 10 * tier; ++i) {
                double d0 = worldIn.rand.nextGaussian() * 0.04D;
                double d1 = worldIn.rand.nextGaussian() * 0.04D;
                double d2 = worldIn.rand.nextGaussian() * 0.04D;
                float offset = (2 + tier) * 0.5f;
                int diameter = 3 + tier;
                Minecraft.getInstance().world.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() - offset + worldIn.rand.nextFloat() * diameter, pos.getY() - offset + worldIn.rand.nextFloat() * diameter,
                                                          pos.getZ() - offset + worldIn.rand.nextFloat() * diameter, d0, d1, d2);
                Minecraft.getInstance().world.addParticle(ParticleTypes.PORTAL, pos.getX() - offset + worldIn.rand.nextFloat() * diameter, pos.getY() - offset + worldIn.rand.nextFloat() * diameter,
                                                          pos.getZ() - offset + worldIn.rand.nextFloat() * diameter, d0, d1, d2);
                Minecraft.getInstance().world.addParticle(ParticleTypes.END_ROD, pos.getX() - offset + worldIn.rand.nextFloat() * diameter, pos.getY() - offset + worldIn.rand.nextFloat() * diameter,
                                                          pos.getZ() - offset + worldIn.rand.nextFloat() * diameter, d0, d1, d2);
            }
        }
    }
    
    public void deactivate(World worldIn, BlockPos pos) {
        if(activated) {
            worldIn.setBlockState(pos, ModBlocks.QRYSTAL_BLOCKS.get(color.toString() + "_" + tier).getDefaultState(), 3);
            worldIn.playSound(null, pos, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.BLOCKS, 0.2F, 1.5F);
            if(color == CrystalUtil.Color.QONDO)
                scheduleChecks(worldIn, pos);
        }
        if(color == CrystalUtil.Color.QALB) {
            for(int i = 0; i < 20 + 10 * tier; ++i) {
                double d0 = worldIn.rand.nextGaussian() * 0.04D;
                double d1 = worldIn.rand.nextGaussian() * 0.04D;
                double d2 = worldIn.rand.nextGaussian() * 0.04D;
                float offset = (2 + tier) * 0.5f;
                int diameter = 3 + tier;
                Minecraft.getInstance().world.addParticle(ParticleTypes.EXPLOSION, pos.getX() - offset + worldIn.rand.nextFloat() * diameter, pos.getY() - offset + worldIn.rand.nextFloat() * diameter,
                                                          pos.getZ() - offset + worldIn.rand.nextFloat() * diameter, d0, d1, d2);
                Minecraft.getInstance().world.addParticle(ParticleTypes.FLAME, pos.getX() - offset + worldIn.rand.nextFloat() * diameter, pos.getY() - offset + worldIn.rand.nextFloat() * diameter,
                                                          pos.getZ() - offset + worldIn.rand.nextFloat() * diameter, d0, d1, d2);
            }
        }
    }
    
    void scheduleChecks(World worldIn, BlockPos pos) {
        if(tier == 2)
            return;
        for(Direction dir : Direction.values()) {
            if(worldIn.getBlockState(pos.offset(dir, tier == 1 ? 4 : 2)).getBlock() instanceof QrystalBlock)
                ((QrystalBlock)worldIn.getBlockState(pos.offset(dir, tier == 1 ? 4 : 2)).getBlock()).checkActivation(worldIn, pos.offset(dir, tier == 1 ? 4 : 2), false);
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
