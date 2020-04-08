package com.villfuk02.qrystal;

import com.villfuk02.qrystal.init.ModItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class QrystalItemGroup extends ItemGroup {
    public QrystalItemGroup() {
        super("qrystal");
    }
    
    @Override
    public ItemStack createIcon() {
        return new ItemStack(ModItems.STONE_PEBBLES);
    }
}
