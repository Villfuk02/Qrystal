package com.villfuk02.qrystal.items;

import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.util.handlers.ISTERHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import static com.villfuk02.qrystal.Main.MOD_ITEM_GROUP;

public class CondensedMaterial extends Item {
    
    public CondensedMaterial() {
        super(new Item.Properties().group(MOD_ITEM_GROUP).setISTER(ISTERHandler::condensedMaterial));
        setRegistryName(Main.MODID, "condensed_material");
        ModItems.ITEMS.add(this);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public ITextComponent getDisplayName(ItemStack stack) {
        ITextComponent itemString;
        ITextComponent power;
        if(stack.hasTag() && stack.getTag().contains("item", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT item = stack.getTag().getCompound("item");
            ItemStack s = ItemStack.read(item);
            itemString = s.getDisplayName();
        } else {
            itemString = new StringTextComponent("Material");
        }
        if(stack.hasTag() && stack.getTag().contains("power", Constants.NBT.TAG_INT)) {
            int pow = stack.getTag().getInt("power");
            power = new TranslationTextComponent("qrystal.power." + pow);
        } else {
            power = new StringTextComponent("");
        }
        
        return power.appendSibling(new TranslationTextComponent("qrystal.condensed")).appendText(" ").appendSibling(itemString);
    }
}
