package com.villfuk02.qrystal.crafting;

import com.google.gson.JsonObject;
import com.villfuk02.qrystal.dataserializers.MaterialManager;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.items.CrystalDust;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class DustSplittingRecipe extends SpecialRecipe {
    private DustSplittingRecipe(ResourceLocation id) {
        super(id);
    }
    
    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        boolean found = false;
        
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack itemStack = inv.getStackInSlot(i);
            if(itemStack.isEmpty())
                continue;
            if(!found && itemStack.getItem() instanceof CrystalDust && ((CrystalDust)itemStack.getItem()).size > 1) {
                if(itemStack.hasTag()) {
                    CompoundNBT tag = itemStack.getTag();
                    if(tag.contains("material", Constants.NBT.TAG_STRING) && MaterialManager.material_names.contains(tag.getString("material"))) {
                        found = true;
                        continue;
                    }
                }
            }
            return false;
        }
        
        return found;
    }
    
    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        long val = 0;
        CompoundNBT tag = null;
        
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack itemStack = inv.getStackInSlot(i);
            if(!itemStack.isEmpty()) {
                val = ((CrystalDust)itemStack.getItem()).size;
                tag = itemStack.getTag();
                break;
            }
        }
        
        if(val > 1) {
            return RecipeUtil.getStackWithTag(ModItems.DUSTS.get("dust_" + val / 6), 6, tag);
        }
        
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 1;
    }
    
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return null;
    }
    
    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<DustSplittingRecipe> {
        @Override
        public DustSplittingRecipe read(ResourceLocation recipeID, JsonObject json) {
            return new DustSplittingRecipe(recipeID);
        }
        
        @Override
        public DustSplittingRecipe read(ResourceLocation recipeID, PacketBuffer buffer) {
            return new DustSplittingRecipe(recipeID);
        }
        
        @Override
        public void write(PacketBuffer buffer, DustSplittingRecipe recipe) {
        }
    }
}
