package com.villfuk02.qrystal.tileentity;

import com.mojang.datafixers.util.Pair;
import com.villfuk02.qrystal.QrystalConfig;
import com.villfuk02.qrystal.blocks.CondensingBarrelBlock;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import com.villfuk02.qrystal.items.CondensedMaterial;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Locale;

public class CondensingBarrelTileEntity extends TileEntity {
    
    public long stored = 0;
    public int mode = 0;
    public ItemStack item = ItemStack.EMPTY;
    public int overflow = 0;
    public Pair<ItemStack, Integer>[] inputs = new Pair[0];
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new IItemHandler() {
        @Override
        public int getSlots() {
            return 2;
        }
        
        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 1 ? getStack(false) : ItemStack.EMPTY;
        }
        
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stackIn, boolean simulate) {
            ItemStack stack = stackIn.copy();
            if(slot != 0)
                return stack;
            int acceptedAmt = Math.min(acceptedAmt(stack), stack.getCount());
            if(acceptedAmt == 0)
                return stack;
            ItemStack gib = stack.split(acceptedAmt);
            if(!simulate)
                modifyAmount(gib, false);
            return stack;
        }
        
        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if(slot != 1)
                return ItemStack.EMPTY;
            ItemStack s = getStack(false).copy();
            s.setCount(Math.min(amount, s.getCount()));
            if(!simulate)
                modifyAmount(s, true);
            return s;
        }
        
        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }
        
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if(slot != 0)
                return false;
            return acceptedAmt(stack) > 0;
        }
    });
    
    public CondensingBarrelTileEntity() {
        super(ModTileEntityTypes.CONDENSING_BARREL);
    }
    
    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        readClient(compound);
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.put("item", item.write(new CompoundNBT()));
        compound.putLong("stored", stored);
        compound.putInt("mode", mode);
        compound.putInt("overflow", overflow);
        compound.put("inputs", writeInputs());
        return compound;
    }
    
    
    public void readClient(CompoundNBT compound) {
        ItemStack temp = ItemStack.read(compound.getCompound("item"));
        stored = compound.getLong("stored");
        mode = compound.getInt("mode");
        overflow = compound.getInt("overflow");
        if(temp != item) {
            item = temp;
            if(world != null && !world.isRemote)
                inputs = RecipeUtil.getComponentList(item.copy(), world);
            else
                readInputs(compound.getCompound("inputs"));
        }
    }
    
    private CompoundNBT writeInputs() {
        CompoundNBT nbt = new CompoundNBT();
        int[] values = new int[inputs.length];
        ListNBT items = new ListNBT();
        for(int i = 0; i < inputs.length; i++) {
            values[i] = inputs[i].getSecond();
            items.add(i, inputs[i].getFirst().write(new CompoundNBT()));
        }
        nbt.putIntArray("values", values);
        nbt.put("items", items);
        return nbt;
    }
    
    private void readInputs(CompoundNBT in) {
        int[] values = in.getIntArray("values");
        ListNBT items = in.getList("items", Constants.NBT.TAG_COMPOUND);
        inputs = new Pair[values.length];
        for(int i = 0; i < values.length; i++) {
            inputs[i] = new Pair<>(ItemStack.read(items.getCompound(i)), values[i]);
        }
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
    
    public long getCapacity() {
        return QrystalConfig.barrel_base_size * RecipeUtil.longPositivePower(QrystalConfig.barrel_tier_multiplier, getTier());
    }
    
    public int getTier() {
        return ((CondensingBarrelBlock)getBlockState().getBlock()).tier;
    }
    
    public int getComparatorLevel() {
        if(stored == 0 && overflow == 0)
            return 0;
        return 1 + (int)(14 * (stored / 64) / (getCapacity() / 64));
    }
    
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }
    
    public ItemStack getStack(boolean force) {
        ItemStack stack = item.copy();
        if(stack.isEmpty() || (!force && stored == 0 && overflow == 0))
            return ItemStack.EMPTY;
        if(mode == 0) {
            if(force)
                return stack;
            if(stored > 64)
                stack.setCount(64);
            else
                stack.setCount((int)stored);
            if(stack.getCount() == 0)
                stack = ItemStack.EMPTY;
            return stack;
        } else if(mode > 0) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString("material", RecipeUtil.getAssociatedMaterial(item));
            tag.putInt("power", mode);
            tag.put("item", item.serializeNBT());
            stack = RecipeUtil.getStackWithTag(ModItems.CONDENSED_MATERIAL, force ? 1 : (int)Math.min(64, stored / RecipeUtil.getCondensedValue(mode)), tag);
            if(stack.getCount() == 0)
                stack = ItemStack.EMPTY;
            return stack;
        } else {
            int value = inputs[-mode].getSecond();
            ItemStack s = inputs[-mode].getFirst().copy();
            if(force)
                return s;
            int amount;
            if(stored >= 64)
                amount = 64;
            else
                amount = (int)Math.min((stored * inputs[0].getSecond() + overflow) / value, 64L);
            s.setCount(amount);
            return s;
        }
    }
    
    public void modifyAmount(ItemStack stack, boolean remove) {
        if(!stack.isEmpty()) {
            if(item.isEmpty()) {
                inputs = RecipeUtil.getComponentList(stack.copy(), world);
                item = inputs[0].getFirst().copy();
            }
            Pair<Long, Integer> value = getValue(stack);
            if(remove) {
                stored -= value.getFirst();
                overflow -= value.getSecond();
                if(overflow < 0) {
                    overflow += inputs[0].getSecond();
                    stored--;
                }
            } else {
                stored += value.getFirst();
                overflow += value.getSecond();
                if(overflow >= inputs[0].getSecond()) {
                    overflow -= inputs[0].getSecond();
                    stored++;
                }
            }
        }
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
    }
    
    private Pair<Long, Integer> getValue(ItemStack stack) {
        ItemStack realStack;
        long multiplier = 1;
        if(stack.getItem() instanceof CondensedMaterial) {
            if(stack.hasTag() && stack.getTag().contains("power", Constants.NBT.TAG_INT) && stack.getTag().contains("item", Constants.NBT.TAG_COMPOUND)) {
                realStack = ItemStack.read(stack.getTag().getCompound("item"));
                multiplier = RecipeUtil.getCondensedValue(stack.getTag().getInt("power"));
            } else {
                return new Pair<>(0L, 0);
            }
        } else {
            realStack = stack.copy();
        }
        Pair<ItemStack, Integer>[] ins = inputs;
        if(item.isEmpty())
            ins = RecipeUtil.getComponentList(stack, world);
        BigInteger v = BigInteger.valueOf(0L);
        for(int i = 0; i < ins.length; i++) {
            if(ItemHandlerHelper.canItemStacksStack(ins[i].getFirst(), realStack)) {
                v = BigInteger.valueOf(ins[i].getSecond());
                break;
            }
        }
        if(v.equals(BigInteger.ZERO))
            return new Pair<>(0L, 0);
        v = v.multiply(BigInteger.valueOf(multiplier)).multiply(BigInteger.valueOf(stack.getCount()));
        if(v.compareTo(BigInteger.valueOf(ins[0].getSecond())) != -1) {
            BigInteger[] div = v.divideAndRemainder(BigInteger.valueOf(ins[0].getSecond()));
            return new Pair<>(div[0].longValueExact(), div[1].intValueExact());
        }
        return new Pair<>(0L, v.intValueExact());
    }
    
    public int acceptedAmt(ItemStack stackIn) {
        ItemStack stack = stackIn.copy();
        if(stack.isEmpty())
            return 0;
        stack.setCount(1);
        Pair<Long, Integer> value = getValue(stack);
        Pair<ItemStack, Integer>[] ins = inputs;
        if(item.isEmpty())
            ins = RecipeUtil.getComponentList(stack, world);
        if(value.equals(new Pair<>(0L, 0)))
            return 0;
        long s = getCapacity() - stored;
        int o = 0;
        if(overflow > 0) {
            s--;
            o = ins[0].getSecond() - overflow;
        }
        if(s / (value.getFirst() + 1) >= 64L)
            return 64;
        if(value.getSecond() == 0)
            return (int)Math.min(s / value.getFirst(), 64L);
        if(value.getFirst() == 0) {
            if(s >= 64L)
                return 64;
            long w = s * ins[0].getSecond() + o;
            return Math.min((int)(w / value.getSecond()), 64);
        }
        for(int i = 0; i < 64; i++) {
            s -= value.getFirst();
            o -= value.getSecond();
            if(o < 0) {
                o += ins[0].getSecond();
                s--;
            }
            if(s < 0)
                return i;
        }
        return 64;
    }
    
    public void drop() {
        ArrayList<ItemStack> stacks = stackAll();
        for(ItemStack stack : stacks) {
            if(!stack.isEmpty()) {
                double d0 = 0.25d + world.rand.nextFloat() * 0.5d;
                final double d1 = 0.5d;
                double d2 = 0.25d + world.rand.nextFloat() * 0.5d;
                ItemEntity itementity = new ItemEntity(world, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, stack);
                itementity.setDefaultPickupDelay();
                world.addEntity(itementity);
            }
        }
    }
    
    public void cycleMode() {
        mode++;
        if(mode > 10 || RecipeUtil.getCondensedValue(mode) > getCapacity())
            mode = item.isEmpty() ? 0 : 1 - inputs.length;
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
    }
    
    public String getSecondaryRenderedText(boolean shiftKeyDown) {
        if(shiftKeyDown) {
            if(mode > 1)
                return "(64^" + mode + ")";
            return "";
        }
        return "(" + String.format("%.1f", stored / (getCapacity() * 0.01f)) + "%)";
    }
    
    public String getRenderedText(boolean shiftKeyDown) {
        if(shiftKeyDown) {
            if(mode > 0)
                return String.format(Locale.US, "%,d", RecipeUtil.getCondensedValue(mode)).replace(",", " ");
            if(item.isEmpty())
                return "1";
            if(mode == 0)
                return "1 (0::" + String.format(getFormatString(), inputs[0].getSecond()) + ")";
            
            return "0::" + String.format(getFormatString(), inputs[-mode].getSecond());
        }
        if(stored <= 9999L) {
            return stored + (overflow <= 0 ? "" : "::" + String.format(getFormatString(), overflow));
        }
        return String.format(Locale.US, "%,d", stored).replace(",", " ") + (overflow > 0 ? "+" : "");
    }
    
    public void giveAll(PlayerEntity player) {
        ArrayList<ItemStack> stacks = stackAll();
        item = ItemStack.EMPTY;
        for(ItemStack stack : stacks) {
            if(!stack.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, stack);
            }
        }
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
    }
    
    public ArrayList<ItemStack> stackAll() {
        if(item.isEmpty())
            return new ArrayList<>();
        ArrayList<ItemStack> stacks = new ArrayList<>();
        while(stored > 0) {
            Pair<ItemStack, Long> r = RecipeUtil.condenseL(item, RecipeUtil.getAssociatedMaterial(item), stored, 64);
            stacks.add(r.getFirst());
            stored -= MathHelper.lfloor(r.getSecond());
        }
        int m = 0;
        while(overflow > 0) {
            m--;
            int value = inputs[-m].getSecond();
            int amount = Math.min(overflow / value, 64);
            if(amount != 0) {
                overflow -= amount * value;
                ItemStack stack = inputs[-m].getFirst().copy();
                stack.setCount(amount);
                stacks.add(stack);
            }
        }
        return stacks;
    }
    
    private String getFormatString() {
        if(item.isEmpty())
            return "%0d";
        return "%0" + inputs[0].getSecond().toString().length() + "d";
    }
}
