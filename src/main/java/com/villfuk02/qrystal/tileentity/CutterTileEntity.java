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
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public abstract class CutterTileEntity extends TileEntity implements INamedContainerProvider, ITickableTileEntity, IPowerConsumer, IAutoIO {
    
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
    private final ItemStackHandler externalInventory = new ItemStackHandler(7) {
        
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
            if(slot <= 1)
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
    private final LazyOptional<ItemStackHandler> inventoryCapabilityExternal = LazyOptional.of(() -> externalInventory);
    
    public short time;
    public short totalTime;
    public final int speed;
    private final Predicate<ItemStack> acceptedTools;
    private final ToIntFunction<ItemStack> toolTier;
    private final RecipeUtil.CuttingType cutterType;
    private final Block block;
    private byte powered = 0;
    public final byte requiredPower;
    
    private final IAutoIO.Button[] buttons = new IAutoIO.Button[]{new IAutoIO.Button(true, 121, 60, 0), new IAutoIO.Button(true, 108, 60, 1), new IAutoIO.Button(false, 159, 60, 2, 3, 4, 5, 6)};
    
    public CutterTileEntity(TileEntityType<?> type, int speed, Predicate<ItemStack> acceptedTools, ToIntFunction<ItemStack> toolTier, byte requiredPower, RecipeUtil.CuttingType cutterType, Block block) {
        super(type);
        this.speed = speed;
        this.acceptedTools = acceptedTools;
        this.toolTier = toolTier;
        this.cutterType = cutterType;
        this.block = block;
        this.requiredPower = requiredPower;
    }
    
    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        readClient(compound);
    }
    
    
    public void readClient(CompoundNBT compound) {
        super.read(compound);
        inventory.deserializeNBT(compound.getCompound("Items"));
        time = compound.getShort("time");
        totalTime = compound.getShort("totalTime");
        setPower(compound.getByte("powered"));
        IAutoIO.readButtonsNBT(compound, buttons);
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.put("Items", inventory.serializeNBT());
        compound.putShort("time", time);
        compound.putShort("totalTime", totalTime);
        compound.putByte("powered", getPower());
        IAutoIO.writeButtonsNBT(compound, buttons);
        return compound;
    }
    
    @Override
    public void tick() {
        if(totalTime > 0 && getPower() >= requiredPower) {
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
        
        if((world.getGameTime() + RecipeUtil.hashPos(pos) & 7) == 0) {
            for(int i = 0; i < buttons.length; i++) {
                if(buttons[i].dir != null) {
                    if(buttons[i].input) {
                        if(world.getTileEntity(pos.offset(buttons[i].dir)) != null &&
                                world.getTileEntity(pos.offset(buttons[i].dir)).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, buttons[i].dir.getOpposite()).isPresent()) {
                            IItemHandler inv = world.getTileEntity(pos.offset(buttons[i].dir)).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, buttons[i].dir.getOpposite()).orElse(null);
                            for(int j = 0; j < inv.getSlots(); j++) {
                                ItemStack stack = inv.extractItem(j, 64, true);
                                if(!stack.isEmpty()) {
                                    boolean found = false;
                                    for(int k : buttons[i].slots) {
                                        if(externalInventory.insertItem(k, stack, true) != stack) {
                                            int amt = stack.getCount() - externalInventory.insertItem(k, stack, true).getCount();
                                            externalInventory.insertItem(k, inv.extractItem(j, amt, false), false);
                                            found = true;
                                            break;
                                        }
                                    }
                                    if(found)
                                        break;
                                }
                                
                            }
                        }
                    } else {
                        if(world.getTileEntity(pos.offset(buttons[i].dir)) != null &&
                                world.getTileEntity(pos.offset(buttons[i].dir)).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, buttons[i].dir.getOpposite()).isPresent()) {
                            IItemHandler inv = world.getTileEntity(pos.offset(buttons[i].dir)).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, buttons[i].dir.getOpposite()).orElse(null);
                            for(int j : buttons[i].slots) {
                                ItemStack stack = externalInventory.extractItem(j, 64, true);
                                if(!stack.isEmpty()) {
                                    boolean found = false;
                                    for(int k = 0; k < inv.getSlots(); k++) {
                                        if(inv.insertItem(k, stack, true) != stack) {
                                            int amt = stack.getCount() - inv.insertItem(k, stack, true).getCount();
                                            inv.insertItem(k, externalInventory.extractItem(j, amt, false), false);
                                            found = true;
                                            break;
                                        }
                                    }
                                    if(found)
                                        break;
                                }
                            }
                        }
                    }
                }
            }
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
            return inventoryCapabilityExternal.cast();
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
    
    @Override
    public byte getPower() {
        return powered;
    }
    
    @Override
    public void setPower(byte power) {
        powered = power;
        if(pos != null && world != null)
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
    }
    
    public String getPowerString() {
        return (getPower() >= requiredPower ? requiredPower : getPower()) + "/" + requiredPower;
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
}