package com.villfuk02.qrystal.items;

import com.villfuk02.qrystal.dataserializers.MaterialManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class CrystalDust extends ItemBase {
    public final int size;
    
    public CrystalDust(int size) {
        super("dust_" + size);
        this.size = size;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if(stack.hasTag() && stack.getTag().contains("material", Constants.NBT.TAG_STRING)) {
            if(!MaterialManager.material_names.contains(stack.getTag().getString("material"))) {
                tooltip.add(new TranslationTextComponent("qrystal.mat.unknown").applyTextStyle(TextFormatting.RED));
            } else {
                if(Screen.hasShiftDown()) {
                    if(stack.getTag().getString("material").equals("qlear"))
                        tooltip.add(new TranslationTextComponent("qrystal.tooltip.qlear_dust").applyTextStyle(TextFormatting.AQUA));
                    else
                        tooltip.add(new TranslationTextComponent("qrystal.tooltip.dust").applyTextStyle(TextFormatting.BLUE));
                    tooltip.add(new TranslationTextComponent("qrystal.tooltip." + stack.getItem().getRegistryName().getPath()).applyTextStyle(TextFormatting.BLUE));
                } else {
                    tooltip.add(new TranslationTextComponent("qrystal.tooltip.shift").applyTextStyle(TextFormatting.BLUE));
                }
            }
        } else {
            tooltip.add(new TranslationTextComponent("qrystal.mat.none").applyTextStyle(TextFormatting.RED));
        }
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public ITextComponent getDisplayName(ItemStack stack) {
        ITextComponent material;
        if(stack.hasTag() && stack.getTag().contains("material", Constants.NBT.TAG_STRING)) {
            String mat = stack.getTag().getString("material");
            if(MaterialManager.material_names.contains(mat))
                material = new TranslationTextComponent(MaterialManager.materials.get(mat).lang).appendText(" ");
            else
                material = new StringTextComponent(mat).appendText(" ");
        } else {
            material = new StringTextComponent("");
        }
        return new TranslationTextComponent("qrystal.dust." + size + ".prefix").appendSibling(material)
                .appendSibling(new TranslationTextComponent("qrystal.crystal"))
                .appendText(" ")
                .appendSibling(new TranslationTextComponent("qrystal.dust." + size + ".suffix"));
    }
}
