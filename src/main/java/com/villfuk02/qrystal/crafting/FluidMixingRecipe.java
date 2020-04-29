package com.villfuk02.qrystal.crafting;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class FluidMixingRecipe implements IRecipe<IInventory> {
    
    protected final ResourceLocation id;
    protected final ResourceLocation in1;
    protected final ResourceLocation tag1;
    protected final CompoundNBT nbt1;
    protected final ResourceLocation in2;
    protected final ResourceLocation tag2;
    protected final CompoundNBT nbt2;
    protected final String fluid1;
    protected final int fluid1Amt;
    protected final String fluid2;
    protected final int fluid2Amt;
    protected final String result;
    protected final int resultAmt;
    protected final ResourceLocation resultItem;
    protected final int time;
    
    public static final int DEFAULT_TIME = 600;//600 for evaporating 1 fluid, 1200 for two fluids, 300 for mixing, 1800 for long processes
    
    public FluidMixingRecipe(ResourceLocation id, ResourceLocation in1, ResourceLocation tag1, String nbt1, ResourceLocation in2, ResourceLocation tag2, String nbt2, String fluid1, int fluid1Amt, String fluid2,
                             int fluid2Amt, String result, int resultAmt, ResourceLocation resultItem, int time) {
        this.id = id;
        this.in1 = in1;
        this.tag1 = tag1;
        this.in2 = in2;
        this.tag2 = tag2;
        this.fluid1 = fluid1;
        this.fluid1Amt = fluid1Amt;
        this.fluid2 = fluid2;
        this.fluid2Amt = fluid2Amt;
        this.result = result;
        this.resultAmt = resultAmt;
        this.resultItem = resultItem;
        this.time = time;
        CompoundNBT tmp1;
        CompoundNBT tmp2;
        try {
            tmp1 = JsonToNBT.getTagFromJson(nbt1);
        } catch(CommandSyntaxException e) {
            tmp1 = new CompoundNBT();
        }
        try {
            tmp2 = JsonToNBT.getTagFromJson(nbt2);
        } catch(CommandSyntaxException e) {
            tmp2 = new CompoundNBT();
        }
        this.nbt1 = tmp1;
        this.nbt2 = tmp2;
    }
    
    @Override
    public boolean matches(IInventory inv, World worldIn) {
        if(resultItem.getPath().isEmpty()) {
            if(inv.getStackInSlot(0).getItem() != ModItems.FLASK)
                return false;
        } else {
            if(!inv.getStackInSlot(0).isEmpty())
                return false;
        }
        if(testFluid(inv.getStackInSlot(1), fluid1, fluid1Amt)) {
            if(!testFluid(inv.getStackInSlot(2), fluid2, fluid2Amt))
                return false;
        } else {
            if(!testFluid(inv.getStackInSlot(2), fluid1, fluid1Amt) || !testFluid(inv.getStackInSlot(1), fluid2, fluid2Amt))
                return false;
        }
        if(testItem(inv.getStackInSlot(3), in1, tag1, nbt1)) {
            if(!testItem(inv.getStackInSlot(4), in2, tag2, nbt2))
                return false;
        } else {
            if(!testItem(inv.getStackInSlot(4), in1, tag1, nbt1) || !testItem(inv.getStackInSlot(3), in2, tag2, nbt2))
                return false;
        }
        ItemStack out = inv.getStackInSlot(5);
        if(!out.isEmpty() && (!ItemHandlerHelper.canItemStacksStack(getResult(), out) || out.getCount() >= out.getMaxStackSize()))
            return false;
        if(!inv.getStackInSlot(1).isEmpty() && !testEmptyFlaskOutput(inv.getStackInSlot(6)))
            return false;
        if(!inv.getStackInSlot(2).isEmpty() && !testEmptyFlaskOutput(inv.getStackInSlot(7)))
            return false;
        return true;
    }
    
    public static boolean testFluid(ItemStack s, String fluid, int amt) {
        if(s.isEmpty())
            return fluid.isEmpty();
        if(!ModItems.FILLED_FLASKS.containsKey(amt)) {
            Main.LOGGER.error("Flasks can only have 25, 125, 250 or 500 mB of fluid");
            return false;
        }
        if(s.getItem() == ModItems.FILLED_FLASKS.get(amt) && s.hasTag() && s.getTag().contains("fluid", Constants.NBT.TAG_STRING) && s.getTag().getString("fluid").equals(fluid))
            return true;
        return false;
    }
    
    public static boolean testItem(ItemStack s, ResourceLocation in, ResourceLocation tag, CompoundNBT nbt) {
        if(s.isEmpty())
            return in.getPath().isEmpty() && tag.getPath().isEmpty();
        if(s.getItem().getTags().contains(tag) || s.getItem() == ForgeRegistries.ITEMS.getValue(in)) {
            if(nbt.isEmpty() || (s.hasTag() && s.getTag().equals(nbt)))
                return true;
        }
        return false;
    }
    
    public static boolean testEmptyFlaskOutput(ItemStack s) {
        return s.isEmpty() || (s.getItem() == ModItems.FLASK && s.getCount() < s.getMaxStackSize());
    }
    
    public int getTime() {
        return time;
    }
    
    public ItemStack getResult() {
        if(resultItem.getPath().isEmpty()) {
            if(!ModItems.FILLED_FLASKS.containsKey(resultAmt)) {
                Main.LOGGER.error("Flasks can only have 25, 125, 250 or 500 mB of fluid");
                return ItemStack.EMPTY;
            }
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString("fluid", result);
            return RecipeUtil.getStackWithTag(ModItems.FILLED_FLASKS.get(resultAmt), nbt);
        }
        return new ItemStack(ForgeRegistries.ITEMS.getValue(resultItem));
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
        return FluidMixingRecipeType.FLUID_MIXING;
    }
    
    public static class FluidMixingRecipeType<FluidMixingRecipe extends IRecipe<?>> implements IRecipeType<com.villfuk02.qrystal.crafting.FluidMixingRecipe> {
        public static final FluidMixingRecipeType FLUID_MIXING = new FluidMixingRecipeType<>();
    }
    
    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<FluidMixingRecipe> {
        @Override
        public FluidMixingRecipe read(ResourceLocation recipeID, JsonObject json) {
            ResourceLocation in1 = new ResourceLocation(JSONUtils.getString(json, "in1", ""));
            ResourceLocation tag1 = new ResourceLocation(JSONUtils.getString(json, "tag1", ""));
            String nbt1 = JSONUtils.getString(json, "nbt1", "");
            ResourceLocation in2 = new ResourceLocation(JSONUtils.getString(json, "in2", ""));
            ResourceLocation tag2 = new ResourceLocation(JSONUtils.getString(json, "tag2", ""));
            String nbt2 = JSONUtils.getString(json, "nbt2", "");
            String fluid1 = JSONUtils.getString(json, "fluid1", "");
            int fluid1Amt = JSONUtils.getInt(json, "fluid1Amt", 500);
            String fluid2 = JSONUtils.getString(json, "fluid2", "");
            int fluid2Amt = JSONUtils.getInt(json, "fluid2Amt", 500);
            String result = JSONUtils.getString(json, "result", "water");
            int resultAmt = JSONUtils.getInt(json, "resultAmt", 500);
            ResourceLocation resultItem = new ResourceLocation(JSONUtils.getString(json, "resultItem", ""));
            int time = JSONUtils.getInt(json, "time", DEFAULT_TIME);
            return new FluidMixingRecipe(recipeID, in1, tag1, nbt1, in2, tag2, nbt2, fluid1, fluid1Amt, fluid2, fluid2Amt, result, resultAmt, resultItem, time);
        }
        
        @Override
        public FluidMixingRecipe read(ResourceLocation recipeID, PacketBuffer buffer) {
            ResourceLocation in1 = buffer.readResourceLocation();
            ResourceLocation tag1 = buffer.readResourceLocation();
            String nbt1 = buffer.readString();
            ResourceLocation in2 = buffer.readResourceLocation();
            ResourceLocation tag2 = buffer.readResourceLocation();
            String nbt2 = buffer.readString();
            String fluid1 = buffer.readString();
            int fluid1Amt = buffer.readVarInt();
            String fluid2 = buffer.readString();
            int fluid2Amt = buffer.readVarInt();
            String result = buffer.readString();
            int resultAmt = buffer.readVarInt();
            ResourceLocation resultItem = buffer.readResourceLocation();
            int time = buffer.readVarInt();
            return new FluidMixingRecipe(recipeID, in1, tag1, nbt1, in2, tag2, nbt2, fluid1, fluid1Amt, fluid2, fluid2Amt, result, resultAmt, resultItem, time);
        }
        
        @Override
        public void write(PacketBuffer buffer, FluidMixingRecipe recipe) {
            buffer.writeResourceLocation(recipe.in1);
            buffer.writeResourceLocation(recipe.tag1);
            buffer.writeString(recipe.nbt1.toString());
            buffer.writeResourceLocation(recipe.in2);
            buffer.writeResourceLocation(recipe.tag2);
            buffer.writeString(recipe.nbt2.toString());
            buffer.writeString(recipe.fluid1);
            buffer.writeVarInt(recipe.fluid1Amt);
            buffer.writeString(recipe.fluid2);
            buffer.writeVarInt(recipe.fluid2Amt);
            buffer.writeString(recipe.result);
            buffer.writeVarInt(recipe.resultAmt);
            buffer.writeResourceLocation(recipe.resultItem);
            buffer.writeVarInt(recipe.time);
        }
    }
}
