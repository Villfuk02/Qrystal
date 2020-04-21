package com.villfuk02.qrystal.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public class CustomCuttingRecipe implements IRecipe<IInventory> {
    
    protected final ResourceLocation id;
    protected final ResourceLocation[] inputs;
    protected final ResourceLocation[] tags;
    protected final int time;
    protected final RecipeOutput[] outputs;
    
    public static final int DEFAULT_TIME = 120; //1s = 40 steel, 60 diamond, 120 laser
    
    public static class RecipeOutput {
        public boolean hammer;
        public boolean saw;
        public boolean laser;
        public float amt;
        public Item item;
        public String material;
    }
    
    public CustomCuttingRecipe(ResourceLocation id, ResourceLocation[] inputs, ResourceLocation[] tags, int time, RecipeOutput[] outputs) {
        this.id = id;
        this.inputs = inputs;
        this.tags = tags;
        this.time = time;
        this.outputs = outputs;
    }
    
    @Override
    public boolean matches(IInventory inv, World worldIn) {
        return validInput(inv.getStackInSlot(0));
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
    
    public ResourceLocation[] getInputs() {
        return inputs;
    }
    
    public ResourceLocation[] getTags() {
        return tags;
    }
    
    public RecipeOutput[] getOutputs() {
        return outputs;
    }
    
    public int getTime() {
        return time;
    }
    
    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean canFit(int width, int height) {
        return false;
    }
    
    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }
    
    @Override
    public ResourceLocation getId() {
        return id;
    }
    
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return null;
    }
    
    @Override
    public IRecipeType<?> getType() {
        return CustomCuttingRecipeType.CUTTING;
    }
    
    public static class CustomCuttingRecipeType<CustomCuttingRecipe extends IRecipe<?>> implements IRecipeType<CustomCuttingRecipe> {
        public static final CustomCuttingRecipeType CUTTING = new CustomCuttingRecipeType<>();
    }
    
    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CustomCuttingRecipe> {
        @Override
        public CustomCuttingRecipe read(ResourceLocation recipeID, JsonObject json) {
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
            int time = JSONUtils.getInt(json, "time", DEFAULT_TIME);
            JsonObject[] outputObjects;
            if(JSONUtils.isJsonArray(json, "output")) {
                JsonArray outputArray = JSONUtils.getJsonArray(json, "output");
                outputObjects = StreamSupport.stream(outputArray.spliterator(), false).map(e -> e.getAsJsonObject()).toArray(JsonObject[]::new);
            } else {
                outputObjects = new JsonObject[]{JSONUtils.getJsonObject(json, "output")};
            }
            List<RecipeOutput> outs = new ArrayList<>();
            for(JsonObject o : outputObjects) {
                RecipeOutput out = new RecipeOutput();
                String[] types = StreamSupport.stream(JSONUtils.getJsonArray(o, "types", new JsonArray()).spliterator(), false).map(e -> e.getAsString()).toArray(String[]::new);
                for(String s : types) {
                    switch(s) {
                        case "hammer":
                            out.hammer = true;
                            break;
                        case "saw":
                            out.saw = true;
                            break;
                        case "laser":
                            out.laser = true;
                            break;
                    }
                }
                
                out.amt = JSONUtils.getFloat(o, "count", 1);
                out.item = JSONUtils.getItem(o, "item");
                out.material = JSONUtils.getString(o, "material", "");
                outs.add(out);
            }
            return new CustomCuttingRecipe(recipeID, ins, tags, time, outs.toArray(new RecipeOutput[0]));
        }
        
        @Override
        public CustomCuttingRecipe read(ResourceLocation recipeID, PacketBuffer buffer) {
            ResourceLocation[] inputs = new ResourceLocation[buffer.readVarInt()];
            for(int i = 0; i < inputs.length; i++) {
                inputs[i] = buffer.readResourceLocation();
            }
            ResourceLocation[] tags = new ResourceLocation[buffer.readVarInt()];
            for(int i = 0; i < tags.length; i++) {
                tags[i] = buffer.readResourceLocation();
            }
            int time = buffer.readVarInt();
            RecipeOutput[] outputs = new RecipeOutput[buffer.readVarInt()];
            for(int i = 0; i < outputs.length; i++) {
                RecipeOutput r = new RecipeOutput();
                r.hammer = buffer.readBoolean();
                r.saw = buffer.readBoolean();
                r.laser = buffer.readBoolean();
                r.amt = buffer.readFloat();
                r.item = buffer.readItemStack().getItem();
            }
            return new CustomCuttingRecipe(recipeID, inputs, tags, time, outputs);
        }
        
        @Override
        public void write(PacketBuffer buffer, CustomCuttingRecipe recipe) {
            buffer.writeVarInt(recipe.inputs.length);
            for(ResourceLocation rl : recipe.inputs) {
                buffer.writeResourceLocation(rl);
            }
            buffer.writeVarInt(recipe.tags.length);
            for(ResourceLocation rl : recipe.tags) {
                buffer.writeResourceLocation(rl);
            }
            buffer.writeVarInt(recipe.time);
            buffer.writeVarInt(recipe.outputs.length);
            for(RecipeOutput r : recipe.outputs) {
                buffer.writeBoolean(r.hammer);
                buffer.writeBoolean(r.saw);
                buffer.writeBoolean(r.laser);
                buffer.writeFloat(r.amt);
                buffer.writeItemStack(new ItemStack(r.item));
            }
        }
    }
}
