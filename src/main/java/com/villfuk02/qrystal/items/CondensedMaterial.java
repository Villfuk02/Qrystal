package com.villfuk02.qrystal.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CondensedMaterial extends ItemBase {
    
    public CondensedMaterial() {
        super("condensed_material");
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public ITextComponent getDisplayName(ItemStack stack) {
        ITextComponent itemString;
        ITextComponent power;
        if(stack.hasTag() && stack.getTag().contains("item")) {
            CompoundNBT item = stack.getTag().getCompound("item");
            ItemStack s = ItemStack.read(item);
            itemString = s.getDisplayName();
        } else {
            itemString = new StringTextComponent("Material");
        }
        if(stack.hasTag() && stack.getTag().contains("power")) {
            int pow = stack.getTag().getInt("power");
            power = new TranslationTextComponent("qrystal.power." + pow);
        } else {
            power = new StringTextComponent("");
        }
        
        return power.appendSibling(new TranslationTextComponent("qrystal.condensed")).appendText(" ").appendSibling(itemString);
    }
}
