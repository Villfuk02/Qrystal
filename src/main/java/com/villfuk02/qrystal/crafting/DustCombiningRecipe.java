package com.villfuk02.qrystal.crafting;

import com.google.gson.JsonObject;
import com.villfuk02.qrystal.dataserializers.MaterialManager;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.items.CrystalDust;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class DustCombiningRecipe extends SpecialRecipe {
    private DustCombiningRecipe(ResourceLocation id) {
        super(id);
    }
    
    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        int size = 0;
        int amt = 0;
        String mat = "";
        
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack itemStack = inv.getStackInSlot(i);
            Item item = itemStack.getItem();
            if(itemStack.isEmpty())
                continue;
            if(item instanceof CrystalDust) {
                if(itemStack.hasTag()) {
                    CompoundNBT tag = itemStack.getTag();
                    if(tag.contains("material") && MaterialManager.material_names.contains(tag.getString("material"))) {
                        if(mat.isEmpty())
                            mat = tag.getString("material");
                        else if(!mat.equals(tag.getString("material")))
                            return false;
                        if(size == 0) {
                            size = ((CrystalDust)item).size;
                            amt++;
                        } else if(size == ((CrystalDust)item).size) {
                            amt++;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        return amt == 6;
    }
    
    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        int total = 0;
        CompoundNBT tag = null;
        
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack itemStack = inv.getStackInSlot(i);
            if(!itemStack.isEmpty()) {
                total += ((CrystalDust)itemStack.getItem()).size;
                if(tag == null)
                    tag = itemStack.getTag();
            }
        }
        
        return RecipeUtil.getStackWithTag(ModItems.DUSTS.get("dust_" + total), tag);
    }
    
    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 6;
    }
    
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return null;
    }
    
    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<DustCombiningRecipe> {
        @Override
        public DustCombiningRecipe read(ResourceLocation recipeID, JsonObject json) {
            return new DustCombiningRecipe(recipeID);
        }
        
        @Override
        public DustCombiningRecipe read(ResourceLocation recipeID, PacketBuffer buffer) {
            return new DustCombiningRecipe(recipeID);
        }
        
        @Override
        public void write(PacketBuffer buffer, DustCombiningRecipe recipe) {
        }
    }
}
