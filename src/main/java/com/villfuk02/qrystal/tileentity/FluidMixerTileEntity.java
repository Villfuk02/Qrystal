package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.crafting.FluidMixingRecipe;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.items.FilledFlask;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class FluidMixerTileEntity extends TileEntity implements INamedContainerProvider, ITickableTileEntity {
    
    
    public final ItemStackHandler inventory;
    public final ItemStackHandler externalInventory;
    private final LazyOptional<ItemStackHandler> inventoryCapabilityExternal;
    
    public short time;
    public short totalTime;
    public final int speed;
    private final Block block;
    
    public FluidMixerTileEntity(TileEntityType<?> type, int speed, int slots, Block block) {
        super(type);
        this.speed = speed;
        this.block = block;
        
        inventory = new ItemStackHandler(slots) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if(stack.isEmpty())
                    return false;
                switch(slot) {
                    case 0:
                        return stack.getItem() == ModItems.FLASK;
                    case 1:
                        return stack.getItem() instanceof FilledFlask && !ItemHandlerHelper.canItemStacksStack(stack, inventory.getStackInSlot(2)) && slots <= 8 ||
                                !(AbstractFurnaceTileEntity.isFuel(stack) && !inventory.getStackInSlot(8).isEmpty() && ItemHandlerHelper.canItemStacksStack(stack, inventory.getStackInSlot(8)));
                    case 2:
                        return stack.getItem() instanceof FilledFlask && !ItemHandlerHelper.canItemStacksStack(stack, inventory.getStackInSlot(1)) && slots <= 8 ||
                                !(AbstractFurnaceTileEntity.isFuel(stack) && !inventory.getStackInSlot(8).isEmpty() && ItemHandlerHelper.canItemStacksStack(stack, inventory.getStackInSlot(8)));
                    case 3:
                        return !(stack.getItem() == ModItems.FLASK || stack.getItem() instanceof FilledFlask) && !ItemHandlerHelper.canItemStacksStack(stack, inventory.getStackInSlot(4));
                    case 4:
                        return !(stack.getItem() == ModItems.FLASK || stack.getItem() instanceof FilledFlask) && !ItemHandlerHelper.canItemStacksStack(stack, inventory.getStackInSlot(3));
                    case 8:
                        return AbstractFurnaceTileEntity.isFuel(stack);
                    default:
                        return false;
                }
            }
            
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                reevaluate(false);
                world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
                markDirty();
            }
        };
        externalInventory = new ItemStackHandler(slots) {
            
            @Override
            public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
                inventory.setStackInSlot(slot, stack);
            }
            
            @Override
            @Nonnull
            public ItemStack getStackInSlot(int slot) {
                return inventory.getStackInSlot(slot);
            }
            
            @Override
            @Nonnull
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                return inventory.insertItem(slot, stack, simulate);
            }
            
            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if(slot <= 4 || slot >= 8)
                    return ItemStack.EMPTY;
                return inventory.extractItem(slot, amount, simulate);
            }
            
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return inventory.isItemValid(slot, stack);
            }
            
            @Override
            public CompoundNBT serializeNBT() {
                return inventory.serializeNBT();
            }
            
            @Override
            public void deserializeNBT(CompoundNBT nbt) {
                setSize(nbt.contains("Size", Constants.NBT.TAG_INT) ? nbt.getInt("Size") : stacks.size());
                ListNBT tagList = nbt.getList("Items", Constants.NBT.TAG_COMPOUND);
                for(int i = 0; i < tagList.size(); i++) {
                    CompoundNBT itemTags = tagList.getCompound(i);
                    int slot = itemTags.getInt("Slot");
                    
                    if(slot >= 0 && slot < stacks.size()) {
                        stacks.set(slot, ItemStack.read(itemTags));
                    }
                }
                inventory.deserializeNBT(nbt);
                onLoad();
            }
        };
        inventoryCapabilityExternal = LazyOptional.of(() -> externalInventory);
    }
    
    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        inventory.deserializeNBT(compound.getCompound("Items"));
        time = compound.getShort("time");
        totalTime = compound.getShort("totalTime");
    }
    
    
    public void readClient(CompoundNBT compound) {
        super.read(compound);
        inventory.deserializeNBT(compound.getCompound("Items"));
        time = compound.getShort("time");
        totalTime = compound.getShort("totalTime");
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.put("Items", inventory.serializeNBT());
        compound.putShort("time", time);
        compound.putShort("totalTime", totalTime);
        return compound;
    }
    
    @Override
    public void tick() {
        if(totalTime > 0) {
            if(isPowered()) {
                if(time < totalTime)
                    time += speed;
                if(time >= totalTime) {
                    time -= totalTime;
                    
                    if(!world.isRemote) {
                        Optional<FluidMixingRecipe> recipe = world.getRecipeManager().getRecipe(FluidMixingRecipe.FluidMixingRecipeType.FLUID_MIXING, new RecipeWrapper(inventory), world);
                        if(recipe.isPresent()) {
                            RecipeUtil.forceInsertSameOrEmptyStack(inventory, 5, recipe.get().getResult());
                        }
                        
                        if(!inventory.getStackInSlot(1).isEmpty())
                            RecipeUtil.forceInsertSameOrEmptyStack(inventory, 6, new ItemStack(ModItems.FLASK));
                        if(!inventory.getStackInSlot(2).isEmpty())
                            RecipeUtil.forceInsertSameOrEmptyStack(inventory, 7, new ItemStack(ModItems.FLASK));
                    }
                    inventory.extractItem(0, 1, false);
                    inventory.extractItem(1, 1, false);
                    inventory.extractItem(2, 1, false);
                    inventory.extractItem(3, 1, false);
                    inventory.extractItem(4, 1, false);
                    world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
                    reevaluate(false);
                }
            } else if(time > 0) {
                time -= 5;
                if(time < 0)
                    time = 0;
            }
            markDirty();
        }
    }
    
    protected abstract boolean isPowered();
    
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), 1, write(new CompoundNBT()));
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        readClient(pkt.getNbtCompound());
    }
    
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();
        return write(tag);
    }
    
    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        super.handleUpdateTag(tag);
        readClient(tag);
    }
    
    
    public void reevaluate(boolean resetTime) {
        if(resetTime)
            time = 0;
        Optional<FluidMixingRecipe> recipe = world.getRecipeManager().getRecipe(FluidMixingRecipe.FluidMixingRecipeType.FLUID_MIXING, new RecipeWrapper(inventory), world);
        if(recipe.isPresent()) {
            totalTime = recipe.map(FluidMixingRecipe::getTime).orElse(600).shortValue();
        } else {
            totalTime = 0;
        }
        
        if(totalTime <= 0)
            time = 0;
    }
    
    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(block.getTranslationKey());
    }
    
    
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return inventoryCapabilityExternal.cast();
        }
        return super.getCapability(cap, side);
    }
}
