package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.crafting.FluidMixingRecipe;
import com.villfuk02.qrystal.util.RecipeUtil;
import com.villfuk02.qrystal.util.handlers.FluidStackHandler;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class FluidMixerTileEntity extends TileEntity implements INamedContainerProvider, ITickableTileEntity, IAutoIO, ITrashableFluids {
    
    
    public final ItemStackHandler inventory;
    public final ItemStackHandler externalInventory;
    private final LazyOptional<ItemStackHandler> inventoryCapabilityExternal;
    
    public final FluidStackHandler tanks = new FluidStackHandler(3, FluidAttributes.BUCKET_VOLUME * 2) {
        
        @Override
        public void onContentsChanged() {
            super.onContentsChanged();
            reevaluate(false);
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
            markDirty();
        }
    };
    public final FluidStackHandler externalTanks = new FluidStackHandler(3, FluidAttributes.BUCKET_VOLUME * 2) {
        @Override
        public FluidStack getFluidInTank(int tank) {
            return tanks.getFluidInTank(tank);
        }
        
        @Override
        public NonNullList<FluidStack> getContents() {
            return tanks.getContents();
        }
        
        @Override
        public int getTanks() {
            return tanks.getTanks();
        }
        
        @Override
        public int getTankCapacity(int tank) {
            return tanks.getTankCapacity(tank);
        }
        
        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            if(stack.isEmpty())
                return false;
            switch(tank) {
                case 0:
                    return !tanks.getFluidInTank(1).getFluid().equals(stack.getFluid());
                case 1:
                    return !tanks.getFluidInTank(0).getFluid().equals(stack.getFluid());
                default:
                    return false;
            }
        }
        
        @Override
        public int fill(int tank, FluidStack resource, IFluidHandler.FluidAction action) {
            if(resource.isEmpty() || !isFluidValid(tank, resource)) {
                return 0;
            }
            return tanks.fill(tank, resource, action);
        }
        
        @Nonnull
        @Override
        public FluidStack drain(int tank, int maxDrain, FluidAction action) {
            if(tank != 2)
                return FluidStack.EMPTY;
            return tanks.drain(tank, maxDrain, action);
        }
        
        @Override
        public boolean isEmpty() {
            return tanks.isEmpty();
        }
    };
    private final LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> externalTanks);
    
    public short time;
    public short totalTime;
    public final int speed;
    private final Block block;
    
    private final IAutoIO.Button[] buttons;
    
    public FluidMixerTileEntity(TileEntityType<?> type, int speed, int slots, Block block) {
        super(type);
        this.speed = speed;
        this.block = block;
        
        if(slots > 2)
            buttons = new IAutoIO.Button[]{new IAutoIO.Button(true, 20, 16, 100), new IAutoIO.Button(true, 33, 16, 101), new IAutoIO.Button(true, 33, 29, 2), new IAutoIO.Button(true, 20, 29, 0, 1),
                                           new IAutoIO.Button(false, 20, 47, 102)};
        else
            buttons = new IAutoIO.Button[]{new IAutoIO.Button(true, 20, 16, 100), new IAutoIO.Button(true, 33, 16, 101), new IAutoIO.Button(true, 20, 29, 0, 1), new IAutoIO.Button(false, 20, 47, 102)};
        
        inventory = new ItemStackHandler(slots) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if(stack.isEmpty())
                    return false;
                switch(slot) {
                    case 0:
                        return (inventory.getStackInSlot(1).isEmpty() || !ItemHandlerHelper.canItemStacksStack(stack, inventory.getStackInSlot(1)));
                    case 1:
                        return (inventory.getStackInSlot(0).isEmpty() || !ItemHandlerHelper.canItemStacksStack(stack, inventory.getStackInSlot(0)));
                    case 2:
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
                if(stack.isEmpty())
                    return ItemStack.EMPTY;
                if(!isItemValid(slot, stack))
                    return stack;
                return inventory.insertItem(slot, stack, simulate);
            }
            
            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }
            
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if(stack.isEmpty())
                    return false;
                switch(slot) {
                    case 0:
                        return (inventory.getStackInSlot(1).isEmpty() || !ItemHandlerHelper.canItemStacksStack(stack, inventory.getStackInSlot(1))) &&
                                (slots <= 2 || !AbstractFurnaceTileEntity.isFuel(stack) || (!inventory.getStackInSlot(2).isEmpty() && !ItemHandlerHelper.canItemStacksStack(stack, inventory.getStackInSlot(2))));
                    case 1:
                        return (inventory.getStackInSlot(0).isEmpty() || !ItemHandlerHelper.canItemStacksStack(stack, inventory.getStackInSlot(0))) &&
                                (slots <= 2 || !AbstractFurnaceTileEntity.isFuel(stack) || (!inventory.getStackInSlot(2).isEmpty() && !ItemHandlerHelper.canItemStacksStack(stack, inventory.getStackInSlot(2))));
                    case 2:
                        return AbstractFurnaceTileEntity.isFuel(stack);
                    default:
                        return false;
                }
            }
            
            @Override
            public CompoundNBT serializeNBT() {
                return inventory.serializeNBT();
            }
            
            @Override
            public void deserializeNBT(CompoundNBT nbt) {
                inventory.deserializeNBT(nbt);
            }
        };
        inventoryCapabilityExternal = LazyOptional.of(() -> externalInventory);
    }
    
    @Override
    public void read(CompoundNBT compound) {
        readClient(compound);
    }
    
    
    public void readClient(CompoundNBT compound) {
        super.read(compound);
        inventory.deserializeNBT(compound.getCompound("Items"));
        tanks.deserializeNBT(compound.getCompound("Tanks"));
        time = compound.getShort("time");
        totalTime = compound.getShort("totalTime");
        IAutoIO.readButtonsNBT(compound, buttons);
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.put("Items", inventory.serializeNBT());
        compound.put("Tanks", tanks.serializeNBT());
        compound.putShort("time", time);
        compound.putShort("totalTime", totalTime);
        IAutoIO.writeButtonsNBT(compound, buttons);
        return compound;
    }
    
    @Override
    public void tick() {
        if(totalTime > 0) {
            if(isPowered()) {
                if(time < totalTime)
                    time += speed;
                if(time >= totalTime) {
                    if(!world.isRemote) {
                        Optional<IRecipe<?>> recipe = world.getRecipeManager()
                                .getRecipes()
                                .stream()
                                .filter(r -> r instanceof FluidMixingRecipe)
                                .filter(r -> ((FluidMixingRecipe)r).realMatch(inventory, tanks))
                                .findFirst();
                        if(recipe.isPresent()) {
                            FluidMixingRecipe r = (FluidMixingRecipe)recipe.get();
                            time -= totalTime;
                            tanks.fill(2, r.getResult(), IFluidHandler.FluidAction.EXECUTE);
                            tanks.drain(r.getFluidStack(0), IFluidHandler.FluidAction.EXECUTE);
                            tanks.drain(r.getFluidStack(1), IFluidHandler.FluidAction.EXECUTE);
                            inventory.extractItem(0, 1, false);
                            inventory.extractItem(1, 1, false);
                            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
                            reevaluate(false);
                        }
                    }
                }
            } else if(time > 0) {
                time -= 5;
                if(time < 0)
                    time = 0;
            }
            markDirty();
        }
        
        if((world.getGameTime() + RecipeUtil.hashPos(pos) & 7) == 0) {
            IAutoIO.tickAutoIO(buttons, world, pos, externalInventory, externalTanks);
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
        Optional<IRecipe<?>> recipe = world.getRecipeManager().getRecipes().stream().filter(r -> r instanceof FluidMixingRecipe).filter(r -> ((FluidMixingRecipe)r).realMatch(inventory, tanks)).findFirst();
        if(recipe.isPresent()) {
            totalTime = (short)((FluidMixingRecipe)recipe.get()).getTime();
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
        if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidCapability.cast();
        }
        return super.getCapability(cap, side);
    }
    
    @Override
    public int getButtonAmt() {
        return buttons.length;
    }
    
    @Override
    public Button getButton(int i) {
        return buttons[i];
    }
    
    @Override
    public void cycleButton(int i) {
        getButton(i).cycleDir();
        if(pos != null && world != null)
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
    }
    
    @Override
    public void trashFluid(int i) {
        tanks.drain(i, tanks.getTankCapacity(i), IFluidHandler.FluidAction.EXECUTE);
    }
}
