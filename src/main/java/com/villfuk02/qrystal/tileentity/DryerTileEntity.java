package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.QrystalConfig;
import com.villfuk02.qrystal.dataserializers.MaterialManager;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import com.villfuk02.qrystal.items.Crystal;
import com.villfuk02.qrystal.items.CrystalDust;
import com.villfuk02.qrystal.util.CrystalUtil;
import com.villfuk02.qrystal.util.RecipeUtil;
import javafx.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ISidedInventoryProvider;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class DryerTileEntity extends TileEntity implements ISidedInventory, ITickableTileEntity {
    
    private int water;
    private String material = "";
    private int amt;
    private int seeds;
    private int waste;
    private int crystallize;
    protected NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
    protected boolean autoDrop = false;
    
    //CLIENT ONLY
    private int color;
    private int crystalData;
    private final Map<String, Float> renderValues = new HashMap<>();
    
    private static final int PROCESS_MULTIPLIER = 8;
    private static final int MAX_VALUE = PROCESS_MULTIPLIER * 40 * RecipeUtil.BASE_VALUE * QrystalConfig.material_tier_multiplier;
    
    public DryerTileEntity() {
        super(ModTileEntityTypes.DRYER);
    }
    
    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        items = NonNullList.withSize(items.size(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, items);
        setWater(compound.getInt("water"));
        material = compound.getString("material");
        amt = compound.getInt("amt");
        seeds = compound.getInt("seeds");
        waste = compound.getInt("waste");
        crystallize = compound.getInt("crystallize");
        autoDrop = compound.getBoolean("autoDrop");
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        ItemStackHelper.saveAllItems(compound, items);
        compound.putInt("water", water);
        compound.putString("material", material);
        compound.putInt("amt", amt);
        compound.putInt("seeds", seeds);
        compound.putInt("waste", waste);
        compound.putInt("crystallize", crystallize);
        compound.putBoolean("autoDrop", autoDrop);
        return compound;
    }
    
    public CompoundNBT writeClient(CompoundNBT compound) {
        compound.putInt("water", water);
        compound.putInt("amt", amt);
        if(!material.isEmpty() && MaterialManager.materials != null && MaterialManager.materials.containsKey(material))
            compound.putInt("color", MaterialManager.materials.get(material).color.getKey());
        else
            compound.putInt("color", 0);
        int[] crystals = RecipeUtil.separateCrystals(material, 0, items.toArray(new ItemStack[0])).getKey();
        int crystalInt = Math.max(Math.min(seeds, 63), 0);
        for(int i = 0; i < 4; i++) {
            crystalInt = (crystalInt << 6) + Math.max(Math.min(crystals[i], 63), 0);
        }
        compound.putInt("crystalData", crystalInt);
        compound.putString("material", material);
        compound.putBoolean("autoDrop", autoDrop);
        return compound;
    }
    
    public void readClient(CompoundNBT compound) {
        setWater(compound.getInt("water"));
        amt = compound.getInt("amt");
        color = compound.getInt("color");
        crystalData = compound.getInt("crystalData");
        material = compound.getString("material");
        autoDrop = compound.getBoolean("autoDrop");
    }
    
    
    @Override
    public int[] getSlotsForFace(Direction side) {
        if(side == Direction.DOWN)
            return new int[0];
        return new int[]{0};
    }
    
    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction) {
        return index == 0 && direction != Direction.DOWN && acceptsItemAmt(itemStackIn) >= itemStackIn.getCount();
    }
    
    public int acceptsItemAmt(ItemStack itemStackIn) {
        if(itemStackIn.getItem() == Items.WATER_BUCKET) {
            if(isEmpty() && water == 0 && waste == 0 && crystallize == 0)
                return 1;
            else
                return 0;
        }
        if(!itemStackIn.hasTag() || !itemStackIn.getTag().contains("material"))
            return 0;
        if(itemStackIn.getItem() instanceof CrystalDust) {
            if(itemStackIn.getTag().getString("material").equals(CrystalUtil.Color.QLEAR.toString())) {
                if(material.isEmpty() || (!material.equals(CrystalUtil.Color.QERI.toString()) && !material.equals(CrystalUtil.Color.QAWA.toString()) && !material.equals(CrystalUtil.Color.QINI.toString())))
                    return 0;
            } else if(!material.isEmpty() && !itemStackIn.getTag().getString("material").equals(material)) {
                return 0;
            }
            CrystalDust dust = (CrystalDust)itemStackIn.getItem();
            return (water - amt) / PROCESS_MULTIPLIER / dust.size / QrystalConfig.material_tier_multiplier;
        }
        if(itemStackIn.getItem() instanceof Crystal) {
            if(!RecipeUtil.isQrystalMaterial(itemStackIn.getTag().getString("material"), false))
                return 0;
            if(!material.isEmpty() && !itemStackIn.getTag().getString("material").equals(material))
                return 0;
            Crystal crystal = (Crystal)itemStackIn.getItem();
            if(crystal.size == CrystalUtil.Size.SEED && crystal.tier == 1)
                return 16 - seeds;
            else
                return 0;
        }
        return 0;
    }
    
    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        return false;
    }
    
    @Override
    public int getSizeInventory() {
        return items.size();
    }
    
    @Override
    public boolean isEmpty() {
        for(ItemStack itemstack : items) {
            if(!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    
    @Override
    public ItemStack getStackInSlot(int index) {
        return items.get(index);
    }
    
    @Override
    public ItemStack decrStackSize(int index, int count) {
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
        return ItemStackHelper.getAndSplit(items, index, count);
    }
    
    @Override
    public ItemStack removeStackFromSlot(int index) {
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
        return ItemStackHelper.getAndRemove(items, index);
    }
    
    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack itemstack = items.get(index);
        boolean same = !stack.isEmpty() && stack.isItemEqual(itemstack) && ItemStack.areItemStackTagsEqual(stack, itemstack);
        items.set(index, stack);
        if(stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        
        if(!same) {
            if(index == 0)
                dissolve();
            else
                markDirty();
        }
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
    }
    
    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return false;
    }
    
    
    @Override
    public void clear() {
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
        items.clear();
    }
    
    @Override
    public void tick() {
        if(water <= 0)
            return;
        
        if(water <= QrystalConfig.material_tier_multiplier) {
            int temp = amt * 4 / 5;
            crystallize += temp;
            amt -= temp;
            waste += amt;
            amt = 0;
            setWater(0);
            crystallize();
            if(autoDrop) {
                drop(true, removeItems());
                drop(true, removeWasteAsDust());
                drop(true, removeSeeds());
                material = "";
            }
        } else {
            int processAmt = amt * QrystalConfig.material_tier_multiplier / water;
            amt -= processAmt;
            int temp = processAmt * 4 / 5;
            crystallize += temp;
            if(world.rand.nextInt(PROCESS_MULTIPLIER * RecipeUtil.BASE_VALUE * QrystalConfig.material_tier_multiplier * QrystalConfig.material_tier_multiplier / 2) < temp)
                crystallize();
            processAmt -= temp;
            temp = processAmt;
            waste += temp;
            setWater(water - QrystalConfig.material_tier_multiplier);
        }
    }
    
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), 1, writeClient(new CompoundNBT()));
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        readClient(pkt.getNbtCompound());
    }
    
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();
        return writeClient(tag);
    }
    
    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        super.handleUpdateTag(tag);
        readClient(tag);
    }
    
    public void dissolve() {
        ItemStack stack = removeStackFromSlot(0);
        if(stack.getItem() == Items.WATER_BUCKET) {
            refill();
            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
            drop(true, new ItemStack(Items.BUCKET));
        } else {
            if(stack.hasTag() && stack.getTag().contains("material")) {
                String mat = stack.getTag().getString("material");
                if(!material.equals("qlear"))
                    material = mat;
                if(stack.getItem() instanceof CrystalDust) {
                    CrystalDust dust = (CrystalDust)stack.getItem();
                    amt += PROCESS_MULTIPLIER * dust.size * stack.getCount() * QrystalConfig.material_tier_multiplier;
                } else if(stack.getItem() instanceof Crystal) {
                    seeds += stack.getCount();
                } else {
                    Main.LOGGER.error("Trying to dissolve unsupported item.");
                }
            } else {
                Main.LOGGER.error("Trying to dissolve unsupported item.");
            }
        }
        markDirty();
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
    }
    
    public void refill() {
        amt = 0;
        crystallize = 0;
        waste = 0;
        setWater(MAX_VALUE);
        markDirty();
    }
    
    public void drop(boolean canPush, ItemStack... stacks) {
        if(canPush) {
            IInventory iinventory = null;
            BlockState blockstate = world.getBlockState(pos.down());
            Block block = blockstate.getBlock();
            if(block instanceof ISidedInventoryProvider) {
                iinventory = ((ISidedInventoryProvider)block).createInventory(blockstate, world, pos.down());
            } else if(blockstate.hasTileEntity()) {
                TileEntity tileentity = world.getTileEntity(pos.down());
                if(tileentity instanceof IInventory) {
                    iinventory = (IInventory)tileentity;
                    if(iinventory instanceof ChestTileEntity && block instanceof ChestBlock) {
                        iinventory = ChestBlock.func_226916_a_((ChestBlock)block, blockstate, world, pos.down(), true);
                    }
                }
            }
            if(iinventory != null) {
                final Direction direction = Direction.UP;
                if(!isInventoryFull(iinventory, direction)) {
                    for(int i = 0; i < stacks.length; ++i) {
                        if(!stacks[i].isEmpty()) {
                            ItemStack itemstack = stacks[i].copy();
                            itemstack = HopperTileEntity.putStackInInventoryAllSlots(this, iinventory, itemstack, direction);
                            if(itemstack != stacks[i]) {
                                iinventory.markDirty();
                                stacks[i] = itemstack;
                            }
                        }
                    }
                }
            }
        }
        
        for(ItemStack stack : stacks) {
            if(!stack.isEmpty()) {
                double d0 = 0.25d + world.rand.nextFloat() * 0.5d;
                final double d1 = 0.75d;
                double d2 = 0.25d + world.rand.nextFloat() * 0.5d;
                ItemEntity itementity = new ItemEntity(world, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, stack);
                itementity.setDefaultPickupDelay();
                world.addEntity(itementity);
            }
        }
        
    }
    
    private static boolean isInventoryFull(IInventory iinventory, Direction direction) {
        return (iinventory instanceof ISidedInventory ? IntStream.of(((ISidedInventory)iinventory).getSlotsForFace(direction)) : IntStream.range(0, iinventory.getSizeInventory())).allMatch(s -> {
            ItemStack itemstack = iinventory.getStackInSlot(s);
            return itemstack.getCount() >= itemstack.getMaxStackSize();
        });
    }
    
    
    public ItemStack[] removeWasteAsDust() {
        markDirty();
        int temp = (waste + amt) / PROCESS_MULTIPLIER / QrystalConfig.material_tier_multiplier;
        waste = 0;
        amt = 0;
        return RecipeUtil.getResult(RecipeUtil.getDustRecipe(temp, material, 3, true, 7), world.rand).toArray(new ItemStack[0]);
    }
    
    public ItemStack[] removeItems() {
        markDirty();
        ArrayList<ItemStack> r = new ArrayList<>();
        for(int i = 0; i < getSizeInventory(); i++) {
            if(!items.get(i).isEmpty())
                r.add(removeStackFromSlot(i));
        }
        return r.toArray(new ItemStack[0]);
    }
    
    public ItemStack removeSeeds() {
        markDirty();
        ItemStack seedStack = RecipeUtil.getStackWithMatTag(RecipeUtil.getCrystal(1, CrystalUtil.Size.SEED), material);
        seedStack.setCount(seeds);
        seeds = 0;
        return seedStack;
    }
    
    public int getComparatorLevel() {
        if(water == 0)
            return 0;
        return (int)Math.min(1 + (water / (float)MAX_VALUE) * 14, 15);
    }
    
    public void setWater(int target) {
        markDirty();
        int prev = getComparatorLevel();
        water = target;
        if(prev != getComparatorLevel() && world != null) {
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
            world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());
        }
    }
    
    public void crystallize() {
        markDirty();
        Pair<Pair<Integer, Integer>, ArrayList<ItemStack>> c = RecipeUtil.crystallize(material, seeds, crystallize / PROCESS_MULTIPLIER, 0, removeItems());
        seeds = c.getKey().getKey();
        waste += c.getKey().getValue() * PROCESS_MULTIPLIER;
        for(int i = 0; i < c.getValue().size(); i++) {
            if(i + 1 >= getSizeInventory()) {
                Main.LOGGER.error("Evaporating Bowl has run out of inventory space. HOW???");
                break;
            }
            setInventorySlotContents(i + 1, c.getValue().get(i));
        }
        crystallize -= (crystallize / PROCESS_MULTIPLIER) * PROCESS_MULTIPLIER;
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
    }
    
    public int getMaterialColor() {
        return color;
    }
    
    public float getConcentration() {
        return water <= 0 ? 0 : amt / (float)water;
    }
    
    public float getWaterLevel() {
        return water / (float)MAX_VALUE;
    }
    
    public int[] getCrystalData() {
        int[] result = new int[5];
        int temp = crystalData;
        for(int i = 0; i < 5; i++) {
            result[i] = temp & 63;
            temp = temp >> 6;
        }
        return result;
    }
    
    public String getMaterial() {
        return material;
    }
    
    public float getRenderValueSquared(String address) {
        if(!renderValues.containsKey(address)) {
            float f = world.rand.nextFloat();
            renderValues.put(address, f * f * (world.rand.nextBoolean() ? 1 : -1));
        }
        return renderValues.get(address);
    }
    
    public void toggleAutoDrop(PlayerEntity player) {
        autoDrop = !autoDrop;
        markDirty();
        player.sendStatusMessage(new TranslationTextComponent("qrystal.dryer.autodrop_" + autoDrop), true);
    }
    
    public void conditionalDrop() {
        if(water == 0 && (waste != 0 || !isEmpty())) {
            drop(false, removeItems());
            drop(false, removeWasteAsDust());
            drop(false, removeSeeds());
            material = "";
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
        }
    }
}
