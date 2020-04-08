package com.villfuk02.qrystal.items;

import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.init.ModItems;
import net.minecraft.item.Item;

import static com.villfuk02.qrystal.Main.MOD_ITEM_GROUP;

public class ItemBase extends Item {
    
    public ItemBase(String name) {
        super(new Item.Properties().group(MOD_ITEM_GROUP));
        setRegistryName(Main.MODID, name);
        ModItems.ITEMS.add(this);
    }
}
