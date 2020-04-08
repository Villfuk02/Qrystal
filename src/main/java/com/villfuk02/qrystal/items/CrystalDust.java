package com.villfuk02.qrystal.items;

import com.villfuk02.qrystal.dataserializers.MaterialManager;
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

public class CrystalDust extends ItemBase {
    public final long size;
    
    public CrystalDust(long size) {
        super("dust_" + size);
        this.size = size;
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
    @OnlyIn(Dist.CLIENT)
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
        return new TranslationTextComponent("qrystal.dust." + size + ".prefix").appendSibling(material)
                .appendSibling(new TranslationTextComponent("qrystal.crystal"))
                .appendText(" ")
                .appendSibling(new TranslationTextComponent("qrystal.dust." + size + ".suffix"));
    }
}
