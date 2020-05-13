package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.QrystalConfig;
import com.villfuk02.qrystal.dataserializers.FluidTierManager;
import com.villfuk02.qrystal.dataserializers.MaterialManager;
import com.villfuk02.qrystal.items.Crystal;
import com.villfuk02.qrystal.items.CrystalDust;
import com.villfuk02.qrystal.util.CrystalUtil;
import com.villfuk02.qrystal.util.RecipeUtil;
import com.villfuk02.qrystal.util.handlers.FluidStackHandler;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

import static com.villfuk02.qrystal.util.RecipeUtil.BASE_VALUE;

public abstract class EvaporatorTileEntity extends TileEntity implements INamedContainerProvider, ITickableTileEntity, IAutoIO, ITrashableFluids {
    
    
    public final ItemStackHandler inventory;
    public final ItemStackHandler externalInventory;
    private final LazyOptional<ItemStackHandler> inventoryCapabilityExternal;
    public final FluidStackHandler tanks = new FluidStackHandler(1, FluidAttributes.BUCKET_VOLUME * 2) {
        
        @Override
        public void onContentsChanged() {
            super.onContentsChanged();
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
            markDirty();
        }
    };
    public final FluidStackHandler externalTanks = new FluidStackHandler(1, FluidAttributes.BUCKET_VOLUME * 2) {
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
            if(stack.isEmpty() || tank != 0)
                return false;
            return FluidTierManager.solvents.containsKey(stack.getFluid().getRegistryName());
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
            return FluidStack.EMPTY;
        }
        
        @Override
        public boolean isEmpty() {
            return tanks.isEmpty();
        }
    };
    private final LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> externalTanks);
    
    public short time = 0;
    public final short cycle;
    private final Block block;
    public int materialAmount = 0;
    public int tier = -1;
    public String material = "";
    public final byte requiredPower;
    public FluidStack fluid = FluidStack.EMPTY;
    public byte seeds = 0;
    
    private final IAutoIO.Button[] buttons;
    
    public EvaporatorTileEntity(TileEntityType<?> type, short cycle, int slots, byte requiredPower, Block block) {
        super(type);
        this.cycle = cycle;
        this.block = block;
        this.requiredPower = requiredPower;
        
        if(slots > 6)
            buttons = new IAutoIO.Button[]{new IAutoIO.Button(true, 9, 54, 100), new IAutoIO.Button(true, 9, 67, 0, 1), new IAutoIO.Button(false, 27, 54, 2), new IAutoIO.Button(false, 27, 67, 3, 4),
                                           new IAutoIO.Button(false, 27, 80, 5), new IAutoIO.Button(true, 9, 80, 6)};
        else
            buttons = new IAutoIO.Button[]{new IAutoIO.Button(true, 9, 54, 100), new IAutoIO.Button(true, 9, 67, 0, 1), new IAutoIO.Button(false, 27, 54, 2), new IAutoIO.Button(false, 27, 67, 3, 4),
                                           new IAutoIO.Button(false, 27, 80, 5)};
        
        inventory = new ItemStackHandler(slots) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if(stack.isEmpty())
                    return false;
                switch(slot) {
                    case 0:
                        return (stack.getItem() instanceof CrystalDust || (stack.getItem() instanceof Crystal && ((Crystal)stack.getItem()).size == CrystalUtil.Size.SMALL)) && stack.hasTag() &&
                                stack.getTag().contains("material", Constants.NBT.TAG_STRING);
                    case 1:
                        return stack.getItem() instanceof Crystal && ((Crystal)stack.getItem()).size == CrystalUtil.Size.SEED && stack.hasTag() && stack.getTag().contains("material", Constants.NBT.TAG_STRING);
                    case 6:
                        return AbstractFurnaceTileEntity.isFuel(stack);
                    default:
                        return true;
                }
            }
            
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
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
                if(slot != 3 && slot <= 8)
                    return ItemStack.EMPTY;
                return inventory.extractItem(slot, amount, simulate);
            }
            
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if(slot <= 1 || slot == 6)
                    return inventory.isItemValid(slot, stack);
                return false;
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
        readClient(compound);
    }
    
    
    public void readClient(CompoundNBT compound) {
        super.read(compound);
        inventory.deserializeNBT(compound.getCompound("Items"));
        tanks.deserializeNBT(compound.getCompound("Tanks"));
        time = compound.getShort("time");
        materialAmount = compound.getInt("materialAmount");
        tier = compound.getInt("tier");
        material = compound.getString("material");
        seeds = compound.getByte("seeds");
        fluid = new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(compound.getString("fluid"))), compound.getInt("fluidAmount"));
        IAutoIO.readButtonsNBT(compound, buttons);
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound = super.write(compound);
        compound.put("Items", inventory.serializeNBT());
        compound.put("Tanks", tanks.serializeNBT());
        compound.putShort("time", time);
        compound.putInt("materialAmount", materialAmount);
        compound.putInt("tier", tier);
        compound.putString("material", material);
        compound.putString("fluid", fluid.getFluid().getRegistryName().toString());
        compound.putInt("fluidAmount", fluid.getAmount());
        compound.putByte("seeds", seeds);
        IAutoIO.writeButtonsNBT(compound, buttons);
        return compound;
    }
    
    @Override
    public void tick() {
        if(isPowered()) {
            if(!world.isRemote()) {
                if(!fluid.isEmpty() && (materialAmount >= tierMultiplier() || fluid.getAmount() != 100)) {
                    time++;
                    if(time >= cycle) {
                        if(tier == 0 && materialAmount % BASE_VALUE > 0)
                            materialAmount -= materialAmount % BASE_VALUE;
                        int targetMaterialAmt = (fluid.getAmount() - 10) * getMaxAmt() / 100;
                        
                        if(RecipeUtil.checkCrystalStack(inventory.getStackInSlot(2), tier, CrystalUtil.Size.SMALL, material,
                                                        64 - (materialAmount - targetMaterialAmt + tierMultiplier() - 1) / tierMultiplier()) &&
                                RecipeUtil.checkCrystalStack(inventory.getStackInSlot(3), tier, CrystalUtil.Size.MEDIUM, material, 64 -
                                        (materialAmount - targetMaterialAmt + tierMultiplier() * QrystalConfig.material_tier_multiplier - 1) / QrystalConfig.material_tier_multiplier / tierMultiplier()) &&
                                RecipeUtil.checkCrystalStack(inventory.getStackInSlot(4), tier, CrystalUtil.Size.LARGE, material, 64 -
                                        (materialAmount - targetMaterialAmt + tierMultiplier() * QrystalConfig.material_tier_multiplier * QrystalConfig.material_tier_multiplier - 1) /
                                                QrystalConfig.material_tier_multiplier / QrystalConfig.material_tier_multiplier / tierMultiplier()) &&
                                RecipeUtil.checkCrystalStack(inventory.getStackInSlot(5), tier + 1, CrystalUtil.Size.SMALL, material, 64 -
                                        Math.min((materialAmount - targetMaterialAmt + tierMultiplier() * QrystalConfig.material_tier_multiplier - 1) / QrystalConfig.material_tier_multiplier / tierMultiplier(),
                                                 seeds))) {
                            time -= cycle;
                            fluid.shrink(10);
                            while(materialAmount > targetMaterialAmt) {
                                if(seeds > 0 && materialAmount >= QrystalConfig.material_tier_multiplier * tierMultiplier() && world.rand.nextFloat() < 0.8f) {
                                    seeds--;
                                    materialAmount -= QrystalConfig.material_tier_multiplier * tierMultiplier();
                                    inventory.insertItem(5, RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(tier + 1, CrystalUtil.Size.SMALL), material), false);
                                } else if(materialAmount >= QrystalConfig.material_tier_multiplier * QrystalConfig.material_tier_multiplier * tierMultiplier() &&
                                        world.rand.nextFloat() < RecipeUtil.getLargeChance()) {
                                    materialAmount -= QrystalConfig.material_tier_multiplier * QrystalConfig.material_tier_multiplier * tierMultiplier();
                                    inventory.insertItem(4, RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(tier, CrystalUtil.Size.LARGE), material), false);
                                } else if(materialAmount >= QrystalConfig.material_tier_multiplier * tierMultiplier() && world.rand.nextFloat() < 0.8f) {
                                    materialAmount -= QrystalConfig.material_tier_multiplier * tierMultiplier();
                                    inventory.insertItem(3, RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(tier, CrystalUtil.Size.MEDIUM), material), false);
                                } else {
                                    materialAmount -= tierMultiplier();
                                    inventory.insertItem(2, RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(tier, CrystalUtil.Size.SMALL), material), false);
                                }
                            }
                            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
                        } else {
                            time--;
                            if(this instanceof IBurnerEvaporator)
                                ((IBurnerEvaporator)this).restoreHeat();
                        }
                    }
                } else {
                    time = 0;
                    if(this instanceof IBurnerEvaporator)
                        ((IBurnerEvaporator)this).restoreHeat();
                    world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
                }
            }
            if(fluid.isEmpty() || materialAmount == 0) {
                reevaluate();
            }
        }
        
        if((world.getGameTime() + RecipeUtil.hashPos(pos) & 7) == 0) {
            IAutoIO.tickAutoIO(buttons, world, pos, inventory, tanks);
        }
    }
    
    void reevaluate() {
        if(fluid.isEmpty() && tanks.getFluidInTank(0).getAmount() >= 100) {
            fluid = tanks.drain(0, 100, IFluidHandler.FluidAction.EXECUTE);
            tier = FluidTierManager.solvents.get(fluid.getFluid().getRegistryName()).getFirst();
        }
        if(!fluid.isEmpty() && fluid.getAmount() == 100 && !inventory.getStackInSlot(0).isEmpty()) {
            if(tier == 0) {
                if(inventory.getStackInSlot(0).getItem() instanceof Crystal && ((Crystal)inventory.getStackInSlot(0).getItem()).tier == tier &&
                        !inventory.getStackInSlot(0).getTag().getString("material").equals("qlear")) {
                    int amt = Math.min(inventory.getStackInSlot(0).getCount(), getMaxAmt() / BASE_VALUE);
                    materialAmount += amt * BASE_VALUE;
                    material = inventory.getStackInSlot(0).getTag().getString("material");
                    inventory.extractItem(0, amt, false);
                } else if(inventory.getStackInSlot(0).getItem() instanceof CrystalDust && !inventory.getStackInSlot(0).getTag().getString("material").equals("qlear")) {
                    CrystalDust dust = (CrystalDust)inventory.getStackInSlot(0).getItem();
                    int amt = Math.min(inventory.getStackInSlot(0).getCount(), getMaxAmt() / QrystalConfig.material_tier_multiplier / dust.size);
                    materialAmount += amt * QrystalConfig.material_tier_multiplier * dust.size;
                    material = inventory.getStackInSlot(0).getTag().getString("material");
                    inventory.extractItem(0, amt, false);
                }
            } else {
                if(inventory.getStackInSlot(0).getItem() instanceof Crystal && ((Crystal)inventory.getStackInSlot(0).getItem()).tier == tier &&
                        !inventory.getStackInSlot(0).getTag().getString("material").equals("qlear")) {
                    int amt = Math.min(inventory.getStackInSlot(0).getCount(), getMaxAmt());
                    materialAmount += amt;
                    material = inventory.getStackInSlot(0).getTag().getString("material");
                    inventory.extractItem(0, amt, false);
                }
            }
        }
        if(!fluid.isEmpty() && fluid.getAmount() == 100 && materialAmount >= getMaxAmt() / 4 && !inventory.getStackInSlot(1).isEmpty() && ((Crystal)inventory.getStackInSlot(1).getItem()).tier == tier + 1 &&
                !inventory.getStackInSlot(1).getTag().getString("material").equals("qlear") &&
                inventory.getStackInSlot(1).getTag().getString("material").equals(MaterialManager.materials.get(material).seed.toString())) {
            int amt = Math.min(inventory.getStackInSlot(1).getCount(), 4 * materialAmount / getMaxAmt());
            seeds = (byte)amt;
            inventory.extractItem(1, amt, false);
        }
    }
    
    public int getMaxAmt() {
        return QrystalConfig.material_tier_multiplier * 8 * tierMultiplier();
    }
    
    public int tierMultiplier() {
        if(!fluid.isEmpty() && tier == 0)
            return BASE_VALUE;
        return 1;
    }
    
    abstract boolean isPowered();
    
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
    
    public byte getRequiredPower() {
        return requiredPower;
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
