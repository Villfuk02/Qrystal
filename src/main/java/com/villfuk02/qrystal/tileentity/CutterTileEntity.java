package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.container.CutterContainer;
import com.villfuk02.qrystal.crafting.CustomCuttingRecipe;
import com.villfuk02.qrystal.items.Crystal;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public abstract class CutterTileEntity extends TileEntity implements INamedContainerProvider, ITickableTileEntity {
    
    public final ItemStackHandler inventory = new ItemStackHandler(7) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if(stack.isEmpty())
                return false;
            switch(slot) {
                case 0:
                    return acceptedTools.test(stack);
                case 1:
                    return RecipeUtil.doesCut(stack, world, false);
                default:
                    return false;
            }
        }
        
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if(slot <= 1)
                reevaluate(false);
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
            markDirty();
        }
    };
    private final LazyOptional<ItemStackHandler> inventoryCapabilityExternal = LazyOptional.of(() -> inventory);
    private final LazyOptional<IItemHandlerModifiable> inventoryCapabilityExternalOutput = LazyOptional.of(() -> new RangedWrapper(inventory, 2, 7));
    
    public short time;
    public short totalTime;
    public final int speed;
    private final Predicate<ItemStack> acceptedTools;
    private final ToIntFunction<ItemStack> toolTier;
    private final RecipeUtil.CuttingType cutterType;
    private final Block block;
    
    public CutterTileEntity(TileEntityType<?> type, int speed, Predicate<ItemStack> acceptedTools, ToIntFunction<ItemStack> toolTier, RecipeUtil.CuttingType cutterType, Block block) {
        super(type);
        this.speed = speed;
        this.acceptedTools = acceptedTools;
        this.toolTier = toolTier;
        this.cutterType = cutterType;
        this.block = block;
    }
    
    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        inventory.deserializeNBT(compound.getCompound("Items"));
        time = compound.getShort("time");
        totalTime = compound.getShort("totalTime");
        
        if(pos != null && world != null) {
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
        }
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
            if(time < totalTime)
                time += speed;
            if(time >= totalTime) {
                ItemStack input = inventory.getStackInSlot(1).copy();
                input.setCount(1);
                ArrayList<ItemStack> max = RecipeUtil.getResult(RecipeUtil.roundUp(RecipeUtil.getCuttingRecipe(cutterType, toolTier.applyAsInt(inventory.getStackInSlot(0)), input, world, false)), world.rand);
                ArrayList<ItemStack> result = RecipeUtil.stackTogether(getOutputSlotContents(), max.toArray(new ItemStack[0]));
                if(result.size() <= inventory.getSlots() - 2) {
                    if(!world.isRemote) {
                        ArrayList<ItemStack> real = RecipeUtil.getResult(RecipeUtil.getCuttingRecipe(cutterType, toolTier.applyAsInt(inventory.getStackInSlot(0)), input, world, false), world.rand);
                        result = RecipeUtil.stackTogether(getOutputSlotContents(), real.toArray(new ItemStack[0]));
                        for(int i = 0; i < inventory.getSlots() - 2; i++) {
                            if(i >= result.size())
                                inventory.setStackInSlot(i + 2, ItemStack.EMPTY);
                            else
                                inventory.setStackInSlot(i + 2, result.get(i));
                        }
                    }
                    time -= totalTime;
                    if(inventory.getStackInSlot(0).isDamageable() && inventory.getStackInSlot(0).attemptDamageItem(1, world.rand, null))
                        inventory.extractItem(0, 1, false);
                    inventory.extractItem(1, 1, false);
                    world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
                }
            }
            markDirty();
        }
    }
    
    private ItemStack[] getOutputSlotContents() {
        ItemStack[] result = new ItemStack[inventory.getSlots() - 2];
        for(int i = 0; i < result.length; i++) {
            result[i] = inventory.getStackInSlot(i + 2).copy();
        }
        return result;
    }
    
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
        if(acceptedTools.test(inventory.getStackInSlot(0))) {
            if(RecipeUtil.doesCut(inventory.getStackInSlot(1), world, false)) {
                Optional<CustomCuttingRecipe> recipe = world.getRecipeManager().getRecipe(CustomCuttingRecipe.CustomCuttingRecipeType.CUTTING, RecipeUtil.FakeInventory(inventory.getStackInSlot(1)), world);
                totalTime = recipe.map(CustomCuttingRecipe::getTime).orElse(getDefaultCutTime(inventory.getStackInSlot(1))).shortValue();
            } else {
                totalTime = 0;
            }
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
    public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
        return new CutterContainer(windowId, inventory, this);
    }
    
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if(side == null)
                return inventoryCapabilityExternal.cast();
            switch(side) {
                case DOWN:
                    return inventoryCapabilityExternalOutput.cast();
                case UP:
                case NORTH:
                case SOUTH:
                case WEST:
                case EAST:
                    return inventoryCapabilityExternal.cast();
            }
        }
        return super.getCapability(cap, side);
    }
    
    public static int getDefaultCutTime(ItemStack s) {
        if(s.getItem() instanceof Crystal) {
            switch(((Crystal)s.getItem()).size) {
                case SEED:
                case SMALL:
                    return CustomCuttingRecipe.DEFAULT_TIME / 3;
                case MEDIUM:
                    return CustomCuttingRecipe.DEFAULT_TIME / 2;
                case LARGE:
                    return CustomCuttingRecipe.DEFAULT_TIME;
            }
        }
        return CustomCuttingRecipe.DEFAULT_TIME;
    }
}
