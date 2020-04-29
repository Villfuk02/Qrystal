package com.villfuk02.qrystal.items;

import com.google.common.collect.Multimap;
import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.crafting.CustomCuttingRecipe;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

import static com.villfuk02.qrystal.Main.MOD_ITEM_GROUP;

public class Hammer extends ToolItem {
    
    private static Set<Block> effectiveOn;
    private static boolean withWorld;
    
    public Hammer(String name, IItemTier tierIn) {
        super(7 + (int)tierIn.getAttackDamage() * 0.5f, -3.2f - 0.1f * tierIn.getAttackDamage(), tierIn, new HashSet<>(), new Item.Properties().group(MOD_ITEM_GROUP));
        setRegistryName(Main.MODID, name);
        ModItems.ITEMS.add(this);
    }
    
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos blockpos = context.getPos();
        if(context.getFace() != Direction.DOWN && world.isAirBlock(blockpos.up())) {
            if(world.getBlockState(blockpos).getBlock() == Blocks.OBSIDIAN || world.getBlockState(blockpos).getBlock() == Blocks.SMOOTH_STONE || world.getBlockState(blockpos).getBlock() == Blocks.IRON_BLOCK) {
                PlayerEntity playerentity = context.getPlayer();
                List<ItemEntity> ies = world.getEntitiesWithinAABB(EntityType.ITEM, new AxisAlignedBB(blockpos.up()), e -> true);
                for(ItemEntity ie : ies) {
                    if(RecipeUtil.doesCut(ie.getItem(), world, false)) {
                        if(!world.isRemote) {
                            ArrayList<ItemStack> res = RecipeUtil.getResult(RecipeUtil.getCuttingRecipe(RecipeUtil.CuttingType.HAMMER, 0, ie.getItem(), world, false), world.rand);
                            if(res.size() > 0) {
                                for(ItemStack is : res) {
                                    ItemEntity itementity = new ItemEntity(world, ie.getPositionVec().x, ie.getPositionVec().y, ie.getPositionVec().z, is);
                                    itementity.setDefaultPickupDelay();
                                    world.addEntity(itementity);
                                }
                                ie.remove();
                                
                                if(playerentity != null) {
                                    context.getItem().damageItem(ie.getItem().getCount(), playerentity, (p_220043_1_) -> {
                                        p_220043_1_.sendBreakAnimation(context.getHand());
                                    });
                                }
                                if(world.getBlockState(blockpos).getBlock() == Blocks.SMOOTH_STONE && world.rand.nextFloat() < 0.1f) {
                                    world.setBlockState(blockpos, Blocks.COBBLESTONE.getDefaultState(), 11);
                                    world.playSound(null, blockpos, SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.BLOCKS, 0.8F, 0.8F);
                                }
                                world.playSound(null, blockpos, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
                                return ActionResultType.SUCCESS;
                            }
                        } else {
                            return ActionResultType.SUCCESS;
                        }
                    }
                }
            }
        }
        return ActionResultType.PASS;
    }
    
    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damageItem(2, attacker, (p_220042_0_) -> {
            p_220042_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);
        });
        return true;
    }
    
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(equipmentSlot);
        if(equipmentSlot == EquipmentSlotType.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", attackDamage, AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", attackSpeed, AttributeModifier.Operation.ADDITION));
        }
        
        return multimap;
    }
    
    @Override
    public boolean canHarvestBlock(BlockState blockIn) {
        return false;
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return getEffectiveOn(null).contains(state.getBlock()) ? efficiency * (state.getMaterial().isToolNotRequired() ? 1 : 5) : 1f;
    }
    
    public static Set<Block> getEffectiveOn(World world) {
        if(effectiveOn == null || !withWorld && world != null) {
            Set<Block> blocks = new HashSet<>();
            if(world != null) {
                withWorld = true;
                Set<ResourceLocation> tags = new HashSet<>();
                Set<ResourceLocation> inputs = new HashSet<>();
                world.getRecipeManager().getRecipes().stream().filter(r -> r.getType() == CustomCuttingRecipe.CustomCuttingRecipeType.CUTTING).map(r -> (CustomCuttingRecipe)r).forEach(r -> {
                    tags.addAll(Arrays.asList(r.getTags()));
                    inputs.addAll(Arrays.asList(r.getInputs()));
                });
                for(ResourceLocation rl : inputs) {
                    Block b = ForgeRegistries.BLOCKS.getValue(rl);
                    if(b != Blocks.AIR)
                        blocks.add(b);
                }
                for(Block b : ForgeRegistries.BLOCKS.getValues()) {
                    for(ResourceLocation rl : tags) {
                        if(b.asItem().getTags().contains(rl))
                            blocks.add(b);
                    }
                }
            }
            effectiveOn = blocks;
        }
        return effectiveOn;
    }
    
    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        Block block = state.getBlock();
        int i = getTier().getHarvestLevel();
        if(getEffectiveOn(world).contains(block) && i >= state.getHarvestLevel()) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            ArrayList<ItemStack> res = RecipeUtil.getResult(RecipeUtil.getCuttingRecipe(RecipeUtil.CuttingType.HAMMER, 0, new ItemStack(block), world, false), world.rand);
            if(res.size() > 0) {
                for(ItemStack is : res) {
                    double dx = (double)(world.rand.nextFloat() * 0.5F) + 0.25D;
                    double dy = (double)(world.rand.nextFloat() * 0.5F) + 0.25D;
                    double dz = (double)(world.rand.nextFloat() * 0.5F) + 0.25D;
                    ItemEntity itementity = new ItemEntity(world, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, is);
                    itementity.setDefaultPickupDelay();
                    world.addEntity(itementity);
                }
            }
        }
        return super.onBlockDestroyed(stack, world, state, pos, entityLiving);
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.EFFICIENCY || enchantment == Enchantments.MENDING || enchantment == Enchantments.KNOCKBACK;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new TranslationTextComponent("qrystal.hammer.tooltip").applyTextStyle(TextFormatting.YELLOW));
        tooltip.add(new TranslationTextComponent("qrystal.hammer.tooltip2").applyTextStyle(TextFormatting.GOLD));
    }
    
    @Override
    public void onCreated(ItemStack stack, World worldIn, PlayerEntity playerIn) {
        if(!withWorld && !worldIn.isRemote())
            getEffectiveOn(worldIn);
        super.onCreated(stack, worldIn, playerIn);
    }
    
    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if(!withWorld && !worldIn.isRemote())
            getEffectiveOn(worldIn);
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }
}
