package com.villfuk02.qrystal.items;

import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.init.ModItems;
import net.minecraft.item.Item;

import static com.villfuk02.qrystal.Main.MOD_ITEM_GROUP;

public class Blade extends Item {
    
    public Blade(String id, int durability) {
        super(new Item.Properties().group(MOD_ITEM_GROUP).maxDamage(durability));
        setRegistryName(Main.MODID, id);
        ModItems.ITEMS.add(this);
    }
}
