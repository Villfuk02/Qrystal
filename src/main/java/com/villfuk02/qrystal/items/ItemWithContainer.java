package com.villfuk02.qrystal.items;

import net.minecraft.item.ItemStack;

public class ItemWithContainer extends ItemBase {
    
    final boolean itself;
    final ItemStack stack;
    
    public ItemWithContainer(String name, ItemStack stack) {
        super(name);
        itself = stack == null;
        this.stack = stack;
    }
    
    public ItemWithContainer(String name) {
        this(name, null);
    }
    
    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }
    
    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        if(itself) {
            ItemStack result = itemStack.copy();
            result.setCount(1);
            return result;
        }
        return stack.copy();
    }
}
