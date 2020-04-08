package com.villfuk02.qrystal.crafting;

import com.google.gson.JsonObject;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.items.FilledFlask;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class FlaskRecipe extends SpecialRecipe {
    
    private FlaskRecipe(final ResourceLocation id) {
        super(id);
    }
    
    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        int amt = 0;
        int flasks = 0;
        int empty_flasks = 0;
        String fluid = "";
        
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack itemStack = inv.getStackInSlot(i);
            Item item = itemStack.getItem();
            if(itemStack.isEmpty())
                continue;
            if(item instanceof FilledFlask) {
                flasks++;
                if(itemStack.hasTag()) {
                    CompoundNBT tag = itemStack.getTag();
                    if(tag.contains("fluid") && ForgeRegistries.FLUIDS.containsKey(new ResourceLocation(itemStack.getTag().getString("fluid")))) {
                        if(fluid.isEmpty())
                            fluid = tag.getString("fluid");
                        else if(!fluid.equals(tag.getString("fluid")))
                            return false;
                        amt += ((FilledFlask)item).amt;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else if(item == ModItems.FLASK) {
                flasks++;
                empty_flasks++;
            } else {
                return false;
            }
        }
        
        return (empty_flasks == 0 && flasks > 1 && (amt == 125 || amt == 250 || amt == 500)) ||
                (empty_flasks > 0 && (amt / flasks == 25 || amt / flasks == 125 || amt / flasks == 250));
    }
    
    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        int amt = 0;
        int flasks = 0;
        int empty_flasks = 0;
        String fluid = "";
        
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack itemStack = inv.getStackInSlot(i);
            Item item = itemStack.getItem();
            if(itemStack.isEmpty())
                continue;
            
            if(item instanceof FilledFlask) {
                flasks++;
                CompoundNBT tag = itemStack.getTag();
                fluid = tag.getString("fluid");
                amt += ((FilledFlask)item).amt;
            } else if(item == ModItems.FLASK) {
                flasks++;
                empty_flasks++;
            } else {
                return ItemStack.EMPTY;
            }
        }
        
        if(empty_flasks > 0) {
            ItemStack result = RecipeUtil.getStackWithFluidTag(ModItems.FILLED_FLASKS.get(amt / flasks), fluid);
            result.setCount(flasks);
            return result;
        }
        return RecipeUtil.getStackWithFluidTag(ModItems.FILLED_FLASKS.get(amt), fluid);
        
    }
    
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        NonNullList<ItemStack> list = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        boolean first = false;
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            if(!inv.getStackInSlot(i).isEmpty()) {
                if(inv.getStackInSlot(i).getItem() == ModItems.FLASK)
                    return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
                if(first)
                    list.set(i, new ItemStack(ModItems.FLASK));
                else
                    first = true;
            }
        }
        return list;
    }
    
    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }
    
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return null;
    }
    
    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<FlaskRecipe> {
        @Override
        public FlaskRecipe read(final ResourceLocation recipeID, final JsonObject json) {
            return new FlaskRecipe(recipeID);
        }
        
        @Override
        public FlaskRecipe read(final ResourceLocation recipeID, final PacketBuffer buffer) {
            return new FlaskRecipe(recipeID);
        }
        
        @Override
        public void write(final PacketBuffer buffer, final FlaskRecipe recipe) {
        }
    }
}
