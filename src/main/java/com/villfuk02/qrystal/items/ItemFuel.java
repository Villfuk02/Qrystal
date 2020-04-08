package com.villfuk02.qrystal.items;

import net.minecraft.item.ItemStack;

public class ItemFuel extends ItemBase {
    protected int fuel;
    
    public ItemFuel(String name, int fuel) {
        super(name);
        this.fuel = fuel;
    }
    
    @Override
    public int getBurnTime(ItemStack itemStack) {
        return fuel;
    }
}
