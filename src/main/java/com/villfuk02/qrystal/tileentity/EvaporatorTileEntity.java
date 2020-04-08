package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.QrystalConfig;
import com.villfuk02.qrystal.crafting.FluidMixingRecipe;
import com.villfuk02.qrystal.dataserializers.FluidTierManager;
import com.villfuk02.qrystal.dataserializers.HeatRegulatorManager;
import com.villfuk02.qrystal.dataserializers.MaterialManager;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.items.Crystal;
import com.villfuk02.qrystal.items.CrystalDust;
import com.villfuk02.qrystal.items.FilledFlask;
import com.villfuk02.qrystal.util.CrystalUtil;
import com.villfuk02.qrystal.util.RecipeUtil;
import com.villfuk02.qrystal.util.handlers.CrystalColorHandler;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;

import javax.annotation.Nonnull;

public abstract class EvaporatorTileEntity extends TileEntity implements INamedContainerProvider, ITickableTileEntity {
    
    
    public final ItemStackHandler inventory;
    private final LazyOptional<ItemStackHandler> inventoryCapabilityExternal;
    private final LazyOptional<IItemHandlerModifiable> inventoryCapabilityExternalOutput;
    
    public short time = 0;
    public final short cycle;
    private final Block block;
    public int materialAmount = 0;
    public int tier = -1;
    public int fluidAmount = 0;
    public String material = "";
    public int materialColor = 0;
    public int materialTemp = 0;
    public ResourceLocation fluid = new ResourceLocation("");
    public int temperature = 0;
    public int tempTarget = 0;
    public int currentBatch = 0;
    public int impurities = 0;
    public boolean reevaluate = true;
    
    
    public EvaporatorTileEntity(TileEntityType<?> type, short cycle, int slots, Block block) {
        super(type);
        this.cycle = cycle;
        this.block = block;
        
        inventory = new ItemStackHandler(slots) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if(stack.isEmpty())
                    return false;
                switch(slot) {
                    case 0:
                        return stack.getItem() instanceof CrystalDust || (stack.getItem() instanceof Crystal && ((Crystal)stack.getItem()).size != CrystalUtil.Size.SEED);
                    case 1:
                        return stack.getItem() instanceof FilledFlask && stack.hasTag() && stack.getTag().contains("fluid") &&
                                FluidTierManager.solvents.containsKey(new ResourceLocation(stack.getTag().getString("fluid")));
                    case 2:
                        return stack.getItem() instanceof Crystal && ((Crystal)stack.getItem()).size == CrystalUtil.Size.SEED && ((Crystal)stack.getItem()).tier == tier + 1 && materialAmount > 0 &&
                                stack.hasTag() && stack.getTag().contains("material") && RecipeUtil.isQrystalMaterial(stack.getTag().getString("material"), false) &&
                                stack.getTag().getString("material").equals(MaterialManager.materials.get(material).seed.toString());
                    case 14:
                        return HeatRegulatorManager.heat_regulators.containsKey(stack.getItem().getRegistryName());
                    default:
                        return false;
                }
            }
            
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                if(slot <= 3)
                    reevaluate();
                if(slot >= 4 && slot <= 9 && getStackInSlot(slot).isEmpty())
                    reevaluate();
                world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
                markDirty();
            }
        };
        inventoryCapabilityExternal = LazyOptional.of(() -> inventory);
        inventoryCapabilityExternalOutput = LazyOptional.of(() -> new CombinedInvWrapper(new RangedWrapper(inventory, 3, 4), new RangedWrapper(inventory, 9, 14)));
    }
    
    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        readClient(compound);
        
        if(pos != null && world != null) {
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
        }
    }
    
    
    public void readClient(CompoundNBT compound) {
        super.read(compound);
        inventory.deserializeNBT(compound.getCompound("Items"));
        time = compound.getShort("time");
        materialAmount = compound.getInt("materialAmount");
        tier = compound.getInt("tier");
        fluidAmount = compound.getInt("fluidAmount");
        material = compound.getString("material");
        fluid = new ResourceLocation(compound.getString("fluid"));
        temperature = compound.getInt("temperature");
        currentBatch = compound.getInt("currentBatch");
        impurities = compound.getInt("impurities");
        materialColor = compound.getInt("materialColor");
        materialTemp = compound.getInt("materialTemp");
        tempTarget = compound.getInt("tempTarget");
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound = super.write(compound);
        compound.put("Items", inventory.serializeNBT());
        compound.putShort("time", time);
        compound.putInt("materialAmount", materialAmount);
        compound.putInt("tier", tier);
        compound.putInt("fluidAmount", fluidAmount);
        compound.putString("material", material);
        compound.putString("fluid", fluid.toString());
        compound.putInt("temperature", temperature);
        compound.putInt("currentBatch", currentBatch);
        compound.putInt("impurities", impurities);
        compound.putInt("materialColor", materialColor);
        compound.putInt("materialTemp", materialTemp);
        compound.putInt("tempTarget", tempTarget);
        return compound;
    }
    
    @Override
    public void tick() {
        time++;
        temperature += tickTemperature(temperature * 499 / 500 - temperature);
        if(fluidAmount > 0 && materialAmount > 0) {
            if(time >= cycle) {
                time = 0;
                int convert = (int)(((long)materialAmount * 5) / fluidAmount);
                fluidAmount -= 5;
                materialAmount -= convert;
                currentBatch += convert;
                impurities += (((50 * getTemperatureMultiplier() * convert) / RecipeUtil.SMALL_VALUE) * convert) / RecipeUtil.SMALL_VALUE;
                if(!world.isRemote) {
                    if(world.rand.nextInt(300000 * QrystalConfig.material_tier_multiplier * QrystalConfig.material_tier_multiplier) < impurities || fluidAmount <= 0) {
                        reevaluate = false;
                        crystallizeCurrentBatch();
                        reevaluate = true;
                        reevaluate();
                    }
                }
            }
        } else {
            for(int i = 2; i < 9; i++) {
                if(i == 3)
                    continue;
                if(!inventory.getStackInSlot(i).isEmpty()) {
                    for(int j = 9; j < 14; j++) {
                        if(inventory.getStackInSlot(j).isEmpty()) {
                            inventory.setStackInSlot(j, inventory.extractItem(i, 64, false));
                        }
                    }
                }
            }
        }
        markDirty();
    }
    
    public abstract int tickTemperature(int move);
    
    private void crystallizeCurrentBatch() {
        int x = inventory.getStackInSlot(2).isEmpty() ? 0 : inventory.getStackInSlot(2).getCount();
        int s = inventory.getStackInSlot(4).isEmpty() ? 0 : inventory.getStackInSlot(4).getCount();
        int m = inventory.getStackInSlot(5).isEmpty() ? 0 : inventory.getStackInSlot(5).getCount();
        int l = inventory.getStackInSlot(6).isEmpty() ? 0 : inventory.getStackInSlot(6).getCount();
        int n = inventory.getStackInSlot(7).isEmpty() ? 0 : inventory.getStackInSlot(7).getCount();
        int w = inventory.getStackInSlot(8).isEmpty() ? 0 : inventory.getStackInSlot(8).getCount();
        int walue = RecipeUtil.SMALL_VALUE / (tier == 0 ? 6 : QrystalConfig.material_tier_multiplier);
        boolean change = true;
        while(currentBatch >= RecipeUtil.SMALL_VALUE || !change) {
            change = false;
            if(x > 0 && n < 64 && currentBatch >= RecipeUtil.SMALL_VALUE * QrystalConfig.material_tier_multiplier) {
                x--;
                n++;
                currentBatch -= RecipeUtil.SMALL_VALUE * QrystalConfig.material_tier_multiplier;
                change = true;
            }
            if((m > 2 * l || m > 64) && l < 127 && currentBatch >= RecipeUtil.SMALL_VALUE * QrystalConfig.material_tier_multiplier * (QrystalConfig.material_tier_multiplier - 1)) {
                m--;
                l++;
                currentBatch -= RecipeUtil.SMALL_VALUE * QrystalConfig.material_tier_multiplier * (QrystalConfig.material_tier_multiplier - 1);
                change = true;
            }
            if((s > 2 * m || s > 64) && m < 127 && currentBatch >= RecipeUtil.SMALL_VALUE * (QrystalConfig.material_tier_multiplier - 1)) {
                s--;
                m++;
                currentBatch -= RecipeUtil.SMALL_VALUE * (QrystalConfig.material_tier_multiplier - 1);
                change = true;
            }
            if(s < 127 && currentBatch >= RecipeUtil.SMALL_VALUE) {
                s++;
                currentBatch -= RecipeUtil.SMALL_VALUE;
                change = true;
            }
        }
        w += currentBatch / walue;
        if(w > 127)
            w = 127;
        currentBatch = 0;
        impurities = 0;
        
        inventory.setStackInSlot(2, RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(tier + 1, CrystalUtil.Size.SEED), x, MaterialManager.materials.get(material).seed.toString()));
        inventory.setStackInSlot(4, RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(tier, CrystalUtil.Size.SMALL), s, material));
        inventory.setStackInSlot(5, RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(tier, CrystalUtil.Size.MEDIUM), m, material));
        inventory.setStackInSlot(6, RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(tier, CrystalUtil.Size.LARGE), l, material));
        inventory.setStackInSlot(7, RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(tier + 1, CrystalUtil.Size.SMALL), n, material));
        if(tier == 0)
            inventory.setStackInSlot(8, RecipeUtil.getStackWithMatTag(ModItems.DUSTS.get("dust_" + RecipeUtil.SMALL_VALUE / 6), w, material));
        else
            inventory.setStackInSlot(8, RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(tier - 1, CrystalUtil.Size.SMALL), w, material));
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
    
    
    public void reevaluate() {
        if(world.isRemote || !reevaluate)
            return;
        if(materialAmount == 0) {
            if(inventory.getStackInSlot(2).isEmpty())
                material = "";
            else
                material = inventory.getStackInSlot(2).getTag().getString("material");
        }
        if(!inventory.getStackInSlot(0).isEmpty() && fluidAmount > 0) {
            int value = validateAndGetValue(inventory.getStackInSlot(0));
            if(value > 0) {
                if(materialAmount + value <= RecipeUtil.SMALL_VALUE * fluidAmount) {
                    if(material.isEmpty())
                        material = inventory.getStackInSlot(0).getTag().getString("material");
                    materialAmount += value;
                    inventory.extractItem(0, 1, false);
                }
            }
        }
        if(fluidAmount == 0) {
            if(inventory.getStackInSlot(1).isEmpty()) {
                tier = -1;
            } else if(!inventory.getStackInSlot(4).isEmpty() || !inventory.getStackInSlot(5).isEmpty() || !inventory.getStackInSlot(6).isEmpty() || !inventory.getStackInSlot(7).isEmpty() ||
                    !inventory.getStackInSlot(8).isEmpty() || !inventory.getStackInSlot(2).isEmpty()) {
                tier = -1;
            } else {
                if(FluidMixingRecipe.testEmptyFlaskOutput(inventory.getStackInSlot(3))) {
                    fluidAmount = ((FilledFlask)inventory.getStackInSlot(1).getItem()).amt;
                    fluid = new ResourceLocation(inventory.getStackInSlot(1).getTag().getString("fluid"));
                    tier = FluidTierManager.solvents.get(fluid).getKey();
                    inventory.extractItem(1, 1, false);
                    RecipeUtil.forceInsertSameOrEmptyStack(inventory, 3, new ItemStack(ModItems.FLASK));
                }
            }
        }
        materialColor = CrystalColorHandler.getColor(material, 3);
        if(material.isEmpty())
            materialTemp = 0;
        else
            materialTemp = MaterialManager.materials.get(material).temp;
        markDirty();
    }
    
    public int validateAndGetValue(ItemStack stack) {
        if(tier == -1)
            return 0;
        if(!stack.hasTag() || !stack.getTag().contains("material"))
            return 0;
        if(!material.isEmpty() && !stack.getTag().getString("material").equals(material) &&
                !((material.equals("qeri") || material.equals("qawa") || material.equals("qini")) && stack.getTag().getString("material").equals("qlear")))
            return 0;
        if(material.isEmpty() && stack.getTag().getString("material").equals("qlear"))
            return 0;
        if(stack.getItem() instanceof CrystalDust) {
            if(tier != 0)
                return 0;
            CrystalDust d = (CrystalDust)stack.getItem();
            if(d.size > 500 * RecipeUtil.SMALL_VALUE)
                return 0;
            return (int)d.size;
        }
        if(stack.getItem() instanceof Crystal) {
            Crystal c = (Crystal)stack.getItem();
            if(c.tier != tier)
                return 0;
            switch(c.size) {
                case SEED:
                    return 0;
                case SMALL:
                    return RecipeUtil.SMALL_VALUE;
                case MEDIUM:
                    return RecipeUtil.SMALL_VALUE * QrystalConfig.material_tier_multiplier;
                case LARGE:
                    return RecipeUtil.SMALL_VALUE * QrystalConfig.material_tier_multiplier * QrystalConfig.material_tier_multiplier;
            }
        }
        return 0;
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
    
    public long getTemperatureMultiplier() {
        long tempMulti = MathHelper.abs(temperature - materialTemp);
        if(tempMulti <= 25)
            return 100;
        tempMulti *= 63 - Long.numberOfLeadingZeros(tempMulti);
        return 2 * tempMulti;
    }
}
