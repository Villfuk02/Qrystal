package com.villfuk02.qrystal.crafting;

import com.google.gson.JsonObject;
import com.villfuk02.qrystal.items.CondensedMaterial;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class UncondensingRecipe extends SpecialRecipe {
    private UncondensingRecipe(ResourceLocation id) {
        super(id);
    }
    
    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        boolean found = false;
        
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack itemStack = inv.getStackInSlot(i);
            Item item = itemStack.getItem();
            if(itemStack.isEmpty())
                continue;
            if(!found && item instanceof CondensedMaterial) {
                if(itemStack.hasTag()) {
                    CompoundNBT tag = itemStack.getTag();
                    if(tag.contains("power", Constants.NBT.TAG_INT) && tag.contains("item", Constants.NBT.TAG_COMPOUND) && tag.getInt("power") > 0) {
                        ItemStack stored = ItemStack.read(tag.getCompound("item"));
                        if(!stored.isEmpty())
                            found = true;
                    }
                }
            } else {
                return false;
            }
        }
        
        return found;
    }
    
    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack itemStack = inv.getStackInSlot(i);
            if(!itemStack.isEmpty()) {
                if(itemStack.getTag().getInt("power") > 1) {
                    ItemStack result = itemStack.copy();
                    result.getTag().putInt("power", result.getTag().getInt("power") - 1);
                    result.setCount(64);
                    return result;
                } else {
                    ItemStack result = ItemStack.read(itemStack.getTag().getCompound("item"));
                    result.setCount(64);
                    return result;
                }
            }
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
    
    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<UncondensingRecipe> {
        @Override
        public UncondensingRecipe read(ResourceLocation recipeID, JsonObject json) {
            return new UncondensingRecipe(recipeID);
        }
        
        @Override
        public UncondensingRecipe read(ResourceLocation recipeID, PacketBuffer buffer) {
            return new UncondensingRecipe(recipeID);
        }
        
        @Override
        public void write(PacketBuffer buffer, UncondensingRecipe recipe) {
        }
    }
}
