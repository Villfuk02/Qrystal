package com.villfuk02.qrystal.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.stream.StreamSupport;

public class SourceFlasksRecipe extends SpecialRecipe {
    
    protected final ResourceLocation[] inputs;
    protected final ResourceLocation[] tags;
    protected final int amt;
    protected final String fluid;
    
    private SourceFlasksRecipe(ResourceLocation id, ResourceLocation[] inputs, ResourceLocation[] tags, int amt, String fluid) {
        super(id);
        this.inputs = inputs;
        this.tags = tags;
        this.amt = amt;
        this.fluid = fluid;
    }
    
    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        int flasks = 0;
        boolean found = false;
        
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack itemStack = inv.getStackInSlot(i);
            Item item = itemStack.getItem();
            if(itemStack.isEmpty())
                continue;
            if(item == ModItems.FLASK) {
                flasks++;
            } else if(validInput(itemStack)) {
                if(found)
                    return false;
                else
                    found = true;
            } else {
                return false;
            }
        }
        
        return flasks > 0 && found && (amt / flasks == 25 || amt / flasks == 125 || amt / flasks == 250 || amt / flasks == 500);
    }
    
    public boolean validInput(ItemStack s) {
        if(s.isEmpty())
            return false;
        for(ResourceLocation r : tags) {
            if(s.getItem().getTags().contains(r))
                return true;
        }
        for(ResourceLocation r : inputs) {
            if(s.getItem() == ForgeRegistries.ITEMS.getValue(r))
                return true;
        }
        return false;
    }
    
    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        int flasks = 0;
        
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack itemStack = inv.getStackInSlot(i);
            if(itemStack.isEmpty())
                continue;
            Item item = itemStack.getItem();
            if(item == ModItems.FLASK) {
                flasks++;
            }
        }
        ItemStack result = RecipeUtil.getStackWithFluidTag(ModItems.FILLED_FLASKS.get(amt / flasks), fluid);
        result.setCount(flasks);
        return result;
    }
    
    
    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }
    
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return null;
    }
    
    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<SourceFlasksRecipe> {
        @Override
        public SourceFlasksRecipe read(final ResourceLocation recipeID, final JsonObject json) {
            ResourceLocation[] ins;
            if(JSONUtils.isJsonArray(json, "input")) {
                JsonArray inputArray = JSONUtils.getJsonArray(json, "input");
                ins = StreamSupport.stream(inputArray.spliterator(), false).map(e -> new ResourceLocation(e.getAsString())).toArray(ResourceLocation[]::new);
            } else {
                ins = new ResourceLocation[]{new ResourceLocation(JSONUtils.getString(json, "input", ""))};
            }
            ResourceLocation[] tags;
            if(JSONUtils.isJsonArray(json, "tag")) {
                JsonArray inputArray = JSONUtils.getJsonArray(json, "tag");
                tags = StreamSupport.stream(inputArray.spliterator(), false).map(e -> new ResourceLocation(e.getAsString())).toArray(ResourceLocation[]::new);
            } else {
                tags = new ResourceLocation[]{new ResourceLocation(JSONUtils.getString(json, "tag", ""))};
            }
            int amt = JSONUtils.getInt(json, "amount", 1000);
            String fluid = JSONUtils.getString(json, "fluid", "water");
            return new SourceFlasksRecipe(recipeID, ins, tags, amt, fluid);
        }
        
        @Override
        public SourceFlasksRecipe read(final ResourceLocation recipeID, final PacketBuffer buffer) {
            ResourceLocation[] inputs = new ResourceLocation[buffer.readVarInt()];
            for(int i = 0; i < inputs.length; i++) {
                inputs[i] = buffer.readResourceLocation();
            }
            ResourceLocation[] tags = new ResourceLocation[buffer.readVarInt()];
            for(int i = 0; i < tags.length; i++) {
                tags[i] = buffer.readResourceLocation();
            }
            int amt = buffer.readVarInt();
            String fluid = buffer.readString();
            return new SourceFlasksRecipe(recipeID, inputs, tags, amt, fluid);
        }
        
        @Override
        public void write(final PacketBuffer buffer, final SourceFlasksRecipe recipe) {
            buffer.writeVarInt(recipe.inputs.length);
            for(ResourceLocation rl : recipe.inputs) {
                buffer.writeResourceLocation(rl);
            }
            buffer.writeVarInt(recipe.tags.length);
            for(ResourceLocation rl : recipe.tags) {
                buffer.writeResourceLocation(rl);
            }
            buffer.writeVarInt(recipe.amt);
            buffer.writeString(recipe.fluid);
        }
    }
}
