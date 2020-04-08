package com.villfuk02.qrystal.items;

import com.villfuk02.qrystal.dataserializers.MaterialManager;
import com.villfuk02.qrystal.util.CrystalUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class Crystal extends ItemBase {
    public final CrystalUtil.Size size;
    public final int tier;
    
    public Crystal(CrystalUtil.Size size, int tier) {
        super("crystal_" + size.toString() + "_" + tier);
        this.size = size;
        this.tier = tier;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if(stack.hasTag() && stack.getTag().contains("material")) {
            if(!MaterialManager.material_names.contains(stack.getTag().getString("material")))
                tooltip.add(new TranslationTextComponent("qrystal.mat.unknown").applyTextStyle(TextFormatting.RED));
        } else {
            tooltip.add(new TranslationTextComponent("qrystal.mat.none").applyTextStyle(TextFormatting.RED));
        }
    }
    
    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        ITextComponent material;
        if(stack.hasTag() && stack.getTag().contains("material")) {
            String mat = stack.getTag().getString("material");
            if(MaterialManager.material_names.contains(mat))
                material = new TranslationTextComponent(MaterialManager.materials.get(mat).lang).appendText(" ");
            else
                material = new StringTextComponent(mat).appendText(" ");
        } else {
            material = new StringTextComponent("");
        }
        if(size == CrystalUtil.Size.SEED) {
            if(tier == 0)
                return material.appendSibling(new TranslationTextComponent("item.qrystal.waste"));
            return new TranslationTextComponent("qrystal.tier." + tier).appendText(" ")
                    .appendSibling(material)
                    .appendSibling(new TranslationTextComponent("qrystal.crystal"))
                    .appendText(" ")
                    .appendSibling(new TranslationTextComponent("qrystal.size.seed"));
        }
        return new TranslationTextComponent("qrystal.size." + size.toString()).appendText(" ")
                .appendSibling(new TranslationTextComponent("qrystal.tier." + tier))
                .appendText(" ")
                .appendSibling(material)
                .appendSibling(new TranslationTextComponent("qrystal.crystal"));
    }
    
    
}
