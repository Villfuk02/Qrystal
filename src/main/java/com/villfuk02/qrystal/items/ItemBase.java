package com.villfuk02.qrystal.items;

import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.util.IHasModel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemBase extends Item implements IHasModel {
    
    public ItemBase(String name){
        setUnlocalizedName(name);
        setRegistryName(name);
        setCreativeTab(CreativeTabs.MATERIALS);
        ModItems.ITEMS.add(this);
    }
    
    @Override
    public void registerModels() {
        Main.proxy.registerItemRenderer(this,0,"inventory");
    }
}
