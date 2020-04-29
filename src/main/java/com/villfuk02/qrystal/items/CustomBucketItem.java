package com.villfuk02.qrystal.items;

import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.init.ModItems;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;

import static com.villfuk02.qrystal.Main.MOD_ITEM_GROUP;

public class CustomBucketItem extends BucketItem {
    public CustomBucketItem(Fluid containedFluidIn) {
        super(containedFluidIn, new Item.Properties().group(MOD_ITEM_GROUP));
        setRegistryName(Main.MODID, containedFluidIn.getRegistryName().getPath() + "_bucket");
        ModItems.ITEMS.add(this);
    }
}
