package com.villfuk02.qrystal.items;

import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.init.ModItems;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import static com.villfuk02.qrystal.Main.MOD_ITEM_GROUP;

public class Blade extends Item {
    
    public Blade(String id, int durability) {
        super(new Item.Properties().group(MOD_ITEM_GROUP).maxDamage(durability));
        setRegistryName(Main.MODID, id);
        ModItems.ITEMS.add(this);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if(Screen.hasShiftDown())
            tooltip.add(new TranslationTextComponent("qrystal.tooltip." + stack.getItem().getRegistryName().getPath()).applyTextStyle(TextFormatting.BLUE));
        else
            tooltip.add(new TranslationTextComponent("qrystal.tooltip.shift").applyTextStyle(TextFormatting.BLUE));
    }
}
