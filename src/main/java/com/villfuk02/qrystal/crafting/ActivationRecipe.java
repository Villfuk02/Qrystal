package com.villfuk02.qrystal.crafting;

import com.google.gson.JsonObject;
import com.villfuk02.qrystal.util.CrystalUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ActivationRecipe implements IRecipe<IInventory> {
    
    protected final ResourceLocation id;
    public final CrystalUtil.Color color;
    public final ResourceLocation tier1;
    public final ResourceLocation tier2;
    public final ResourceLocation tier3;
    
    public ActivationRecipe(ResourceLocation id, CrystalUtil.Color color, ResourceLocation tier1, ResourceLocation tier2, ResourceLocation tier3) {
        this.id = id;
        this.color = color;
        this.tier1 = tier1;
        this.tier2 = tier2;
        this.tier3 = tier3;
    }
    
    @Override
    public boolean matches(IInventory inv, World worldIn) {
        return false;
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
        return ActivationRecipeType.ACTIVATION;
    }
    
    public static class ActivationRecipeType<ActivationRecipe extends IRecipe<?>> implements IRecipeType<ActivationRecipe> {
        public static final ActivationRecipeType ACTIVATION = new ActivationRecipeType<>();
    }
    
    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ActivationRecipe> {
        @Override
        public ActivationRecipe read(ResourceLocation recipeID, JsonObject json) {
            ResourceLocation tier1 = new ResourceLocation(JSONUtils.getString(json, "tier1", ""));
            ResourceLocation tier2 = new ResourceLocation(JSONUtils.getString(json, "tier2", ""));
            ResourceLocation tier3 = new ResourceLocation(JSONUtils.getString(json, "tier3", ""));
            CrystalUtil.Color color = CrystalUtil.Color.fromString(JSONUtils.getString(json, "color", ""));
            return new ActivationRecipe(recipeID, color, tier1, tier2, tier3);
        }
        
        @Override
        public ActivationRecipe read(ResourceLocation recipeID, PacketBuffer buffer) {
            ResourceLocation tier1 = buffer.readResourceLocation();
            ResourceLocation tier2 = buffer.readResourceLocation();
            ResourceLocation tier3 = buffer.readResourceLocation();
            CrystalUtil.Color color = CrystalUtil.Color.fromString(buffer.readString());
            return new ActivationRecipe(recipeID, color, tier1, tier2, tier3);
        }
        
        @Override
        public void write(PacketBuffer buffer, ActivationRecipe recipe) {
            buffer.writeResourceLocation(recipe.tier1);
            buffer.writeResourceLocation(recipe.tier2);
            buffer.writeResourceLocation(recipe.tier3);
            buffer.writeString(recipe.color.toString());
        }
    }
}
