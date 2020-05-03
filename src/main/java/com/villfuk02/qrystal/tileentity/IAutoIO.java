package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.util.handlers.FluidStackHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Arrays;

public interface IAutoIO {
    Button getButton(int i);
    
    int getButtonAmt();
    
    void cycleButton(int i);
    
    default ArrayList<Integer> getDirSlots(Direction d, boolean input) {
        ArrayList<Integer> ints = new ArrayList<>();
        for(int i = 0; i < getButtonAmt(); i++) {
            Button b = getButton(i);
            if(b.dir == d && b.input == input)
                ints.addAll(Arrays.asList(b.slots));
        }
        return ints;
    }
    
    static String getDirLetter(Direction dir) {
        if(dir == null) {
            return "X";
        }
        switch(dir) {
            case DOWN:
                return "D";
            case UP:
                return "U";
            case NORTH:
                return "N";
            case SOUTH:
                return "S";
            case WEST:
                return "W";
            case EAST:
                return "E";
        }
        return "error";
    }
    
    class Button {
        public Direction dir;
        public final boolean input;
        public final int x;
        public final int y;
        public final Integer[] slots;
        
        public Button(boolean input, int x, int y, Integer... slots) {
            this.input = input;
            this.x = x;
            this.y = y;
            this.slots = slots;
        }
        
        protected void cycleDir() {
            if(dir == null) {
                dir = Direction.DOWN;
                return;
            }
            switch(dir) {
                case DOWN:
                    dir = Direction.UP;
                    return;
                case UP:
                    dir = Direction.NORTH;
                    return;
                case NORTH:
                    dir = Direction.SOUTH;
                    return;
                case SOUTH:
                    dir = Direction.WEST;
                    return;
                case WEST:
                    dir = Direction.EAST;
                    return;
                case EAST:
                    dir = null;
            }
        }
    }
    
    static void writeButtonsNBT(CompoundNBT nbt, Button[] btns) {
        byte[] converted = new byte[btns.length];
        for(int i = 0; i < btns.length; i++) {
            if(btns[i].dir == null)
                converted[i] = (byte)0;
            else
                converted[i] = (byte)(btns[i].dir.getIndex() + 1);
        }
        nbt.putByteArray("directions", converted);
    }
    
    static void readButtonsNBT(CompoundNBT nbt, Button[] btns) {
        byte[] converted = nbt.getByteArray("directions");
        for(int i = 0; i < btns.length; i++) {
            if(converted[i] == 0)
                btns[i].dir = null;
            else
                btns[i].dir = Direction.byIndex(converted[i] - 1);
        }
    }
    
    static void tickAutoIO(Button[] buttons, World world, BlockPos pos, ItemStackHandler inventory, FluidStackHandler tanks) {
        if(inventory != null) {
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
                                        if(k < 100) {
                                            if(inventory.insertItem(k, stack, true) != stack) {
                                                int amt = stack.getCount() - inventory.insertItem(k, stack, true).getCount();
                                                inventory.insertItem(k, inv.extractItem(j, amt, false), false);
                                                found = true;
                                                break;
                                            }
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
                                if(j < 100) {
                                    ItemStack stack = inventory.extractItem(j, 64, true);
                                    if(!stack.isEmpty()) {
                                        boolean found = false;
                                        for(int k = 0; k < inv.getSlots(); k++) {
                                            if(inv.insertItem(k, stack, true) != stack) {
                                                int amt = stack.getCount() - inv.insertItem(k, stack, true).getCount();
                                                inv.insertItem(k, inventory.extractItem(j, amt, false), false);
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
        if(tanks != null) {
            for(int i = 0; i < buttons.length; i++) {
                if(buttons[i].dir != null) {
                    if(buttons[i].input) {
                        if(world.getTileEntity(pos.offset(buttons[i].dir)) != null &&
                                world.getTileEntity(pos.offset(buttons[i].dir)).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, buttons[i].dir.getOpposite()).isPresent()) {
                            IFluidHandler inv = world.getTileEntity(pos.offset(buttons[i].dir)).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, buttons[i].dir.getOpposite()).orElse(null);
                            for(int j = 0; j < inv.getTanks(); j++) {
                                FluidStack stack = inv.getFluidInTank(j).copy();
                                if(!stack.isEmpty()) {
                                    boolean found = false;
                                    for(int k : buttons[i].slots) {
                                        if(k >= 100) {
                                            if(tanks.fill(k - 100, stack, IFluidHandler.FluidAction.SIMULATE) != 0) {
                                                int amt = tanks.fill(k - 100, stack, IFluidHandler.FluidAction.SIMULATE);
                                                tanks.fill(k - 100, inv.drain(new FluidStack(stack, amt), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                    if(found)
                                        break;
                                }
                                
                            }
                        }
                    } else {
                        if(world.getTileEntity(pos.offset(buttons[i].dir)) != null &&
                                world.getTileEntity(pos.offset(buttons[i].dir)).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, buttons[i].dir.getOpposite()).isPresent()) {
                            IFluidHandler inv = world.getTileEntity(pos.offset(buttons[i].dir)).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, buttons[i].dir.getOpposite()).orElse(null);
                            for(int j : buttons[i].slots) {
                                if(j >= 100) {
                                    FluidStack stack = tanks.drain(j - 100, 2000, IFluidHandler.FluidAction.SIMULATE);
                                    if(!stack.isEmpty()) {
                                        if(inv.fill(stack, IFluidHandler.FluidAction.SIMULATE) != 0) {
                                            int amt = inv.fill(stack, IFluidHandler.FluidAction.SIMULATE);
                                            inv.fill(tanks.drain(j - 100, amt, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
