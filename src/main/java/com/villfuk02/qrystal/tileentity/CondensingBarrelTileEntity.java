package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.blocks.CondensingBarrelBlock;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import com.villfuk02.qrystal.items.CondensedMaterial;
import com.villfuk02.qrystal.items.CrystalDust;
import com.villfuk02.qrystal.util.RecipeUtil;
import javafx.util.Pair;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class CondensingBarrelTileEntity extends TileEntity {
    
    public long stored = 0;
    public int mode = 0;
    public ItemStack item = ItemStack.EMPTY;
    public long dustOverflow = 0;
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
        if(pos != null && world != null) {
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
        }
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.put("item", item.write(new CompoundNBT()));
        compound.putLong("stored", stored);
        compound.putInt("mode", mode);
        compound.putLong("dustOverflow", dustOverflow);
        return compound;
    }
    
    public void readClient(CompoundNBT compound) {
        item = ItemStack.read(compound.getCompound("item"));
        stored = compound.getLong("stored");
        mode = compound.getInt("mode");
        dustOverflow = compound.getLong("dustOverflow");
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
        return getValue(getRawCapacity());
    }
    
    public int getRawCapacity() {
        return ((CondensingBarrelBlock)getBlockState().getBlock()).capacity;
    }
    
    public int getComparatorLevel() {
        if(stored == 0 && dustOverflow == 0)
            return 0;
        return 1 + (int)(14 * stored / getCapacity());
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
        if(stack.isEmpty() || (!force && stored == 0 && dustOverflow == 0))
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
            stack = RecipeUtil.getStackWithTag(ModItems.CONDENSED_MATERIAL, force ? 1 : (int)Math.min(64, stored / getValue(mode)), tag);
            if(stack.getCount() == 0)
                stack = ItemStack.EMPTY;
            return stack;
        } else if(item.getItem() instanceof CrystalDust) {
            long value = ModItems.DUST_SIZES[-mode];
            if(force)
                return RecipeUtil.getStackWithTag(ModItems.DUSTS.get("dust_" + value), 1, item.hasTag() ? item.getTag() : new CompoundNBT());
            int amount;
            if(stored >= 64)
                amount = 64;
            else
                amount = (int)Math.min((stored * ModItems.DUST_SIZES[0] + dustOverflow) / value, 64L);
            stack = RecipeUtil.getStackWithTag(ModItems.DUSTS.get("dust_" + value), amount, item.hasTag() ? item.getTag() : new CompoundNBT());
            if(stack.getCount() == 0)
                stack = ItemStack.EMPTY;
            return stack;
        }
        return ItemStack.EMPTY;
    }
    
    public void modifyAmount(ItemStack stack, boolean remove) {
        if(!stack.isEmpty()) {
            Pair<Long, Long> value = getValue(stack);
            if(remove) {
                stored -= value.getKey();
                dustOverflow -= value.getValue();
                if(dustOverflow < 0) {
                    dustOverflow += ModItems.DUST_SIZES[0];
                    stored--;
                }
            } else {
                stored += value.getKey();
                dustOverflow += value.getValue();
                if(dustOverflow >= ModItems.DUST_SIZES[0]) {
                    dustOverflow -= ModItems.DUST_SIZES[0];
                    stored++;
                }
            }
            item = getFake(stack);
        }
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
    }
    
    public static ItemStack getFake(ItemStack stack) {
        if(stack.getItem() instanceof CrystalDust) {
            return RecipeUtil.getStackWithTag(ModItems.DUSTS.get("dust_" + ModItems.DUST_SIZES[0]), stack.getTag());
        } else if(stack.getItem() instanceof CondensedMaterial) {
            if(stack.hasTag() && stack.getTag().contains("power") && stack.getTag().contains("item"))
                return ItemStack.read(stack.getTag().getCompound("item"));
            else
                return ItemStack.EMPTY;
        } else {
            ItemStack fake = stack.copy();
            fake.setCount(1);
            return fake;
        }
    }
    
    private static Pair<Long, Long> getValue(ItemStack stack) {
        if(stack.getItem() instanceof CondensedMaterial) {
            if(stack.hasTag() && stack.getTag().contains("power"))
                return new Pair<>((long)stack.getCount() * getValue(stack.getTag().getInt("power")), 0L);
            else
                return new Pair<>(0L, 0L);
        }
        if(stack.getItem() instanceof CrystalDust) {
            long v = ((CrystalDust)stack.getItem()).size * stack.getCount();
            long w = v / ModItems.DUST_SIZES[0];
            v -= w * ModItems.DUST_SIZES[0];
            return new Pair<>(w, v);
        }
        return new Pair<>((long)stack.getCount(), 0L);
    }
    
    public int acceptedAmt(ItemStack stackIn) {
        ItemStack stack = stackIn.copy();
        if(stack.isEmpty())
            return 0;
        stack.setCount(1);
        ItemStack fake = getFake(stack);
        if(fake.isEmpty())
            return 0;
        if(!item.isEmpty() && !ItemHandlerHelper.canItemStacksStack(item, fake))
            return 0;
        Pair<Long, Long> value = getValue(stack);
        long s = getCapacity() - stored;
        long o = -dustOverflow;
        if(value.getKey() == 0L && value.getValue() != 0L) {
            int c = (int)Math.min(65L, s);
            o += c * ModItems.DUST_SIZES[0];
            return (int)Math.min(64L, o / value.getValue());
        } else if(value.getValue() == 0L && value.getKey() != 0L) {
            if(dustOverflow > 0)
                s--;
            return (int)Math.min(64L, s / value.getKey());
        }
        return 0;
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
        if(mode > getRawCapacity())
            mode = item.getItem() instanceof CrystalDust ? (1 - ModItems.DUST_SIZES.length) : 0;
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
    }
    
    public static long getValue(int power) {
        if(power > 10 || power < 0)
            return 0;
        return ((long)1) << (6 * power);
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
            if(mode >= 0)
                return Long.toString(getValue(mode));
            return "0::" + ModItems.DUST_SIZES[-mode];
        }
        if(stored <= 9999L) {
            return stored + (dustOverflow <= 0 ? "" : "::" + dustOverflow);
        }
        int pow = (63 - Long.numberOfLeadingZeros(stored - 1)) / 6;
        return String.format("%.2f", stored / (float)getValue(pow)) + "x64^" + pow;
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
        ArrayList<ItemStack> stacks = new ArrayList<>();
        while(stored > 0) {
            Pair<ItemStack, Long> r = RecipeUtil.condenseL(item, RecipeUtil.getAssociatedMaterial(item), stored, 64);
            stacks.add(r.getKey());
            stored -= MathHelper.lfloor(r.getValue());
        }
        int m = 0;
        while(dustOverflow > 0) {
            m--;
            long value = ModItems.DUST_SIZES[-m];
            int amount = (int)Math.min(dustOverflow / value, 64L);
            dustOverflow -= amount * value;
            ItemStack stack = RecipeUtil.getStackWithTag(ModItems.DUSTS.get("dust_" + value), amount, item.hasTag() ? item.getTag() : new CompoundNBT());
            if(stack.getCount() != 0)
                stacks.add(stack);
        }
        return stacks;
    }
}
