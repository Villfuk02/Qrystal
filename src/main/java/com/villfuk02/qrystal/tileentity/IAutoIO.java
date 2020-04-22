package com.villfuk02.qrystal.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

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
}
