package com.villfuk02.qrystal.crafting;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.villfuk02.qrystal.util.handlers.FluidStackHandler;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
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
    protected final ResourceLocation fluid1;
    protected final int fluid1Amt;
    protected final ResourceLocation fluid2;
    protected final int fluid2Amt;
    protected final ResourceLocation result;
    protected final int resultAmt;
    protected final int time;
    
    public static final int DEFAULT_TIME = 150;
    
    public FluidMixingRecipe(ResourceLocation id, ResourceLocation in1, ResourceLocation tag1, String nbt1, ResourceLocation in2, ResourceLocation tag2, String nbt2, ResourceLocation fluid1, int fluid1Amt,
                             ResourceLocation fluid2, int fluid2Amt, ResourceLocation result, int resultAmt, int time) {
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
        return true;
    }
    
    public static boolean testItem(ItemStack s, ResourceLocation in, ResourceLocation tag, CompoundNBT nbt) {
        if(s.isEmpty())
            return in.getPath().isEmpty() && tag.getPath().isEmpty();
        if(s.getItem().getTags().contains(tag) || s.getItem().getRegistryName().equals(in)) {
            if(nbt.isEmpty() || (s.hasTag() && s.getTag().equals(nbt)))
                return true;
        }
        return false;
    }
    
    public static boolean testFluid(FluidStack f, ResourceLocation fluid, int amt) {
        if(f.isEmpty())
            return fluid.getPath().isEmpty() || amt == 0;
        return f.getFluid().getRegistryName().equals(fluid) && f.getAmount() >= amt;
    }
    
    public boolean realMatch(IItemHandler inventory, FluidStackHandler fluids) {
        if(!fluids.getFluidInTank(2).isEmpty() && (fluids.getFluidInTank(2).getFluid() != getResult().getFluid() || fluids.getFluidInTank(2).getAmount() + resultAmt > fluids.getTankCapacity(2)))
            return false;
        if((!testItem(inventory.getStackInSlot(0), in1, tag1, nbt1) || !testItem(inventory.getStackInSlot(1), in2, tag2, nbt2)) &&
                (!testItem(inventory.getStackInSlot(1), in1, tag1, nbt1) || !testItem(inventory.getStackInSlot(0), in2, tag2, nbt2)))
            return false;
        return (testFluid(fluids.getFluidInTank(0), fluid1, fluid1Amt) && testFluid(fluids.getFluidInTank(1), fluid2, fluid2Amt)) ||
                (testFluid(fluids.getFluidInTank(1), fluid1, fluid1Amt) && testFluid(fluids.getFluidInTank(0), fluid2, fluid2Amt));
    }
    
    public int getTime() {
        return time;
    }
    
    public FluidStack getResult() {
        return new FluidStack(ForgeRegistries.FLUIDS.getValue(result), resultAmt);
    }
    
    public FluidStack getFluidStack(int i) {
        if((i == 0 && fluid1.getPath().isEmpty()) || (i == 1 && fluid2.getPath().isEmpty()))
            return FluidStack.EMPTY;
        return i == 0 ? new FluidStack(ForgeRegistries.FLUIDS.getValue(fluid1), fluid1Amt) : new FluidStack(ForgeRegistries.FLUIDS.getValue(fluid2), fluid2Amt);
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
            ResourceLocation fluid1 = new ResourceLocation(JSONUtils.getString(json, "fluid1", ""));
            int fluid1Amt = JSONUtils.getInt(json, "fluid1Amt", 100);
            ResourceLocation fluid2 = new ResourceLocation(JSONUtils.getString(json, "fluid2", ""));
            int fluid2Amt = JSONUtils.getInt(json, "fluid2Amt", 100);
            ResourceLocation result = new ResourceLocation(JSONUtils.getString(json, "result", "water"));
            int resultAmt = JSONUtils.getInt(json, "resultAmt", 100);
            int time = JSONUtils.getInt(json, "time", DEFAULT_TIME);
            return new FluidMixingRecipe(recipeID, in1, tag1, nbt1, in2, tag2, nbt2, fluid1, fluid1Amt, fluid2, fluid2Amt, result, resultAmt, time);
        }
        
        @Override
        public FluidMixingRecipe read(ResourceLocation recipeID, PacketBuffer buffer) {
            ResourceLocation in1 = buffer.readResourceLocation();
            ResourceLocation tag1 = buffer.readResourceLocation();
            String nbt1 = buffer.readString();
            ResourceLocation in2 = buffer.readResourceLocation();
            ResourceLocation tag2 = buffer.readResourceLocation();
            String nbt2 = buffer.readString();
            ResourceLocation fluid1 = buffer.readResourceLocation();
            int fluid1Amt = buffer.readVarInt();
            ResourceLocation fluid2 = buffer.readResourceLocation();
            int fluid2Amt = buffer.readVarInt();
            ResourceLocation result = buffer.readResourceLocation();
            int resultAmt = buffer.readVarInt();
            int time = buffer.readVarInt();
            return new FluidMixingRecipe(recipeID, in1, tag1, nbt1, in2, tag2, nbt2, fluid1, fluid1Amt, fluid2, fluid2Amt, result, resultAmt, time);
        }
        
        @Override
        public void write(PacketBuffer buffer, FluidMixingRecipe recipe) {
            buffer.writeResourceLocation(recipe.in1);
            buffer.writeResourceLocation(recipe.tag1);
            buffer.writeString(recipe.nbt1.toString());
            buffer.writeResourceLocation(recipe.in2);
            buffer.writeResourceLocation(recipe.tag2);
            buffer.writeString(recipe.nbt2.toString());
            buffer.writeResourceLocation(recipe.fluid1);
            buffer.writeVarInt(recipe.fluid1Amt);
            buffer.writeResourceLocation(recipe.fluid2);
            buffer.writeVarInt(recipe.fluid2Amt);
            buffer.writeResourceLocation(recipe.result);
            buffer.writeVarInt(recipe.resultAmt);
            buffer.writeVarInt(recipe.time);
        }
    }
}
