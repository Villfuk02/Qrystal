package com.villfuk02.qrystal.items;

import com.villfuk02.qrystal.dataserializers.FluidTierManager;
import com.villfuk02.qrystal.init.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

public class FilledFlask extends ItemWithContainer {
    
    public int amt;
    
    public FilledFlask(int amt) {
        super("filled_flask_" + amt, new ItemStack(ModItems.FLASK));
        this.amt = amt;
        ModItems.FILLED_FLASKS.put(amt, this);
    }
    
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if(stack.hasTag() && stack.getTag().contains("fluid", Constants.NBT.TAG_STRING)) {
            ResourceLocation fluid = new ResourceLocation(stack.getTag().getString("fluid"));
            if(ForgeRegistries.FLUIDS.containsKey(fluid)) {
                tooltip.add(new TranslationTextComponent(ForgeRegistries.FLUIDS.getValue(fluid).getAttributes().getTranslationKey()).appendSibling(new StringTextComponent(" " + amt + "mB"))
                                    .applyTextStyle(TextFormatting.AQUA));
                if(FluidTierManager.solvents.containsKey(fluid))
                    tooltip.add(new TranslationTextComponent("qrystal.tier." + FluidTierManager.solvents.get(fluid).getFirst()).appendSibling(new TranslationTextComponent("qrystal.solvent_tier"))
                                        .applyTextStyle(TextFormatting.GOLD));
            } else {
                tooltip.add(new TranslationTextComponent("qrystal.fluid.unknown").applyTextStyle(TextFormatting.RED));
            }
        } else {
            tooltip.add(new TranslationTextComponent("qrystal.fluid.none").applyTextStyle(TextFormatting.RED));
        }
    }
}
