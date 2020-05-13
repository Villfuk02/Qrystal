package com.villfuk02.qrystal.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.QrystalConfig;
import com.villfuk02.qrystal.container.EvaporatorContainer;
import com.villfuk02.qrystal.dataserializers.FluidTierManager;
import com.villfuk02.qrystal.dataserializers.MaterialManager;
import com.villfuk02.qrystal.items.Crystal;
import com.villfuk02.qrystal.network.Networking;
import com.villfuk02.qrystal.network.PacketCycleAutoIO;
import com.villfuk02.qrystal.network.PacketTrashFluid;
import com.villfuk02.qrystal.tileentity.EvaporatorTileEntity;
import com.villfuk02.qrystal.tileentity.IAutoIO;
import com.villfuk02.qrystal.tileentity.IBurnerEvaporator;
import com.villfuk02.qrystal.tileentity.IPowerConsumer;
import com.villfuk02.qrystal.util.ColorUtils;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;

public class EvaporatorScreen extends ContainerScreen<EvaporatorContainer> {
    private static final ResourceLocation BURNER_BACKGROUND_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/burner_evaporator.png");
    private static final ResourceLocation POWERED_BACKGROUND_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/powered_evaporator.png");
    private final boolean burner;
    
    public static final int[] TRASH_BUTTONS = new int[]{0, 49, 21};
    
    public EvaporatorScreen(EvaporatorContainer container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
        burner = container.burner;
        ySize = 192;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        
        int relMouseX = mouseX - guiLeft;
        int relMouseY = mouseY - guiTop;
        EvaporatorTileEntity tileEntity = container.tileEntity;
        boolean energyBarHovered = relMouseX > 111 && relMouseX < 130 && relMouseY > 44 && relMouseY < 63;
        if(burner) {
            if(energyBarHovered) {
                String tooltip;
                if(((IBurnerEvaporator)tileEntity).getHeatTotal() > 0)
                    tooltip = new TranslationTextComponent("gui." + Main.MODID + ".fuel_time").appendSibling(
                            new StringTextComponent(((IBurnerEvaporator)tileEntity).getHeatLeft() / 20 / ((IBurnerEvaporator)tileEntity).getSpeed() + "s"))
                            .applyTextStyle(TextFormatting.GOLD)
                            .getFormattedText();
                else
                    tooltip = new TranslationTextComponent("gui." + Main.MODID + ".no_fuel").applyTextStyle(TextFormatting.RED).getFormattedText();
                renderTooltip(tooltip, mouseX, mouseY);
            }
        } else {
            if(energyBarHovered) {
                String tooltip = new TranslationTextComponent("gui." + Main.MODID + ".energy").appendSibling(new StringTextComponent(((IPowerConsumer)tileEntity).getPowerString()))
                        .applyTextStyle(((IPowerConsumer)tileEntity).getPower() >= ((IPowerConsumer)tileEntity).getRequiredPower() ? TextFormatting.GOLD : TextFormatting.RED)
                        .getFormattedText();
                renderTooltip(tooltip, mouseX, mouseY);
            }
        }
        
        boolean fluidHovered = relMouseX > 43 && relMouseX < 62 && relMouseY > 34 && relMouseY < 69;
        if(fluidHovered) {
            if(!tileEntity.tanks.getFluidInTank(0).isEmpty()) {
                String tooltip = new TranslationTextComponent(tileEntity.tanks.getFluidInTank(0).getTranslationKey()).appendSibling(
                        new StringTextComponent(" " + tileEntity.tanks.getFluidInTank(0).getAmount() + "mB")).getFormattedText();
                if(FluidTierManager.solvents.containsKey(tileEntity.tanks.getFluidInTank(0).getFluid().getRegistryName())) {
                    String tooltip2 = new TranslationTextComponent("qrystal.tier." + FluidTierManager.solvents.get(tileEntity.tanks.getFluidInTank(0).getFluid().getRegistryName()).getFirst()).appendSibling(
                            new TranslationTextComponent("qrystal.solvent_tier")).applyTextStyle(TextFormatting.GOLD).getFormattedText();
                    renderTooltip(Arrays.asList(tooltip, tooltip2), mouseX, mouseY);
                } else {
                    renderTooltip(tooltip, mouseX, mouseY);
                }
            }
        }
        
        if(relMouseX > 114 && relMouseX < 123 && relMouseY > 31 && relMouseY < 40) {
            ArrayList<String> tooltips = new ArrayList<>();
            if(tileEntity.fluid.isEmpty()) {
                tooltips.add(new TranslationTextComponent("gui." + Main.MODID + ".empty").getFormattedText());
            } else {
                tooltips.add(new TranslationTextComponent("qrystal.tier." + tileEntity.tier).appendSibling(new TranslationTextComponent("qrystal.solvent_tier"))
                                     .applyTextStyle(TextFormatting.GOLD)
                                     .getFormattedText());
                tooltips.add(new TranslationTextComponent("gui." + Main.MODID + ".amount").appendSibling(new StringTextComponent(tileEntity.fluid.getAmount() + "mB"))
                                     .applyTextStyle(TextFormatting.GOLD)
                                     .getFormattedText());
                if(tileEntity.materialAmount > 0) {
                    tooltips.add(new TranslationTextComponent("gui." + Main.MODID + ".material").appendSibling(new TranslationTextComponent(MaterialManager.materials.get(tileEntity.material).lang))
                                         .applyTextStyle(TextFormatting.YELLOW)
                                         .getFormattedText());
                    tooltips.add(new TranslationTextComponent("gui." + Main.MODID + ".amount").appendSibling(
                            new StringTextComponent(tileEntity.tier == 0 ? String.format("%.2f", tileEntity.materialAmount / (float)tileEntity.tierMultiplier()) : Integer.toString(tileEntity.materialAmount)))
                                         .applyTextStyle(TextFormatting.YELLOW)
                                         .getFormattedText());
                    tooltips.add(new TranslationTextComponent("gui." + Main.MODID + ".seeds").appendSibling(new StringTextComponent(Integer.toString(tileEntity.seeds)))
                                         .applyTextStyle(TextFormatting.GREEN)
                                         .getFormattedText());
                    float[] values = RecipeUtil.getCrystalChances(tileEntity.materialAmount / tileEntity.tierMultiplier(), tileEntity.seeds);
                    if(values[0] > 0) {
                        tooltips.add(new TranslationTextComponent("gui." + Main.MODID + ".expected").getFormattedText());
                        tooltips.add(new TranslationTextComponent("qrystal.size.small").appendText(" ")
                                             .appendSibling(new TranslationTextComponent("qrystal.tier." + tileEntity.tier))
                                             .appendSibling(new StringTextComponent(
                                                     (tileEntity.materialAmount / tileEntity.tierMultiplier() < QrystalConfig.material_tier_multiplier ? " " : " ~") + String.format("%.2f", values[0])))
                                             .applyTextStyle(TextFormatting.GRAY)
                                             .getFormattedText());
                    }
                    if(values[1] > 0)
                        tooltips.add(new TranslationTextComponent("qrystal.size.medium").appendText(" ")
                                             .appendSibling(new TranslationTextComponent("qrystal.tier." + tileEntity.tier))
                                             .appendSibling(new StringTextComponent(" ~" + String.format("%.2f", values[1])))
                                             .applyTextStyle(TextFormatting.GRAY)
                                             .getFormattedText());
                    if(values[2] > 0)
                        tooltips.add(new TranslationTextComponent("qrystal.size.large").appendText(" ")
                                             .appendSibling(new TranslationTextComponent("qrystal.tier." + tileEntity.tier))
                                             .appendSibling(new StringTextComponent(" ~" + String.format("%.2f", values[2])))
                                             .applyTextStyle(TextFormatting.GRAY)
                                             .getFormattedText());
                    if(values[3] > 0)
                        tooltips.add(new TranslationTextComponent("qrystal.size.small").appendText(" ")
                                             .appendSibling(new TranslationTextComponent("qrystal.tier." + (tileEntity.tier + 1)))
                                             .appendSibling(new StringTextComponent(" ~" + String.format("%.2f", values[3])))
                                             .applyTextStyle(TextFormatting.GRAY)
                                             .getFormattedText());
                    
                }
            }
            renderTooltip(tooltips, mouseX, mouseY);
        }
        
        if(!tileEntity.fluid.isEmpty()) {
            if(relMouseX > 64 && relMouseX < 73 && relMouseY > 22 && relMouseY < 39 && !tileEntity.inventory.getStackInSlot(0).isEmpty() &&
                    ((Crystal)tileEntity.inventory.getStackInSlot(0).getItem()).tier != tileEntity.tier) {
                String tooltip = new TranslationTextComponent("gui." + Main.MODID + ".tier_error").appendSibling(new TranslationTextComponent("qrystal.tier." + tileEntity.tier))
                        .appendText(" ")
                        .appendSibling(new TranslationTextComponent("qrystal.crystals"))
                        .applyTextStyle(TextFormatting.RED)
                        .getFormattedText();
                renderTooltip(tooltip, mouseX, mouseY);
            }
            
            if(relMouseX > 64 && relMouseX < 73 && relMouseY > 46 && relMouseY < 63 && !tileEntity.inventory.getStackInSlot(1).isEmpty() &&
                    ((Crystal)tileEntity.inventory.getStackInSlot(1).getItem()).tier != tileEntity.tier + 1) {
                String tooltip = new TranslationTextComponent("gui." + Main.MODID + ".tier_error").appendSibling(new TranslationTextComponent("qrystal.tier." + (tileEntity.tier + 1)))
                        .appendText(" ")
                        .appendSibling(new TranslationTextComponent("qrystal.seeds"))
                        .applyTextStyle(TextFormatting.RED)
                        .getFormattedText();
                renderTooltip(tooltip, mouseX, mouseY);
            }
            
            if(relMouseX > 64 && relMouseX < 73 && relMouseY > 46 && relMouseY < 63 && !tileEntity.inventory.getStackInSlot(1).isEmpty() && tileEntity.materialAmount > 0 &&
                    !tileEntity.inventory.getStackInSlot(1).getTag().getString("material").equals(MaterialManager.materials.get(tileEntity.material).seed.toString())) {
                String tooltip = new TranslationTextComponent("gui." + Main.MODID + ".seed_error").appendSibling(
                        new TranslationTextComponent("qrystal.mat." + MaterialManager.materials.get(tileEntity.material).seed.toString()))
                        .appendText(" ")
                        .appendSibling(new TranslationTextComponent("qrystal.seeds"))
                        .applyTextStyle(TextFormatting.RED)
                        .getFormattedText();
                renderTooltip(tooltip, mouseX, mouseY);
            }
            
        }
        
        for(int i = 0; i < tileEntity.getButtonAmt(); i++) {
            IAutoIO.Button b = tileEntity.getButton(i);
            if(relMouseX > b.x && relMouseX < b.x + 10 && relMouseY > b.y && relMouseY < b.y + 10) {
                String tooltip = new TranslationTextComponent("gui." + Main.MODID + ".button." + b.input + "." + IAutoIO.getDirLetter(b.dir)).getFormattedText();
                renderTooltip(tooltip, mouseX, mouseY);
            }
        }
        
        for(int i = 0; i < TRASH_BUTTONS.length / 3; i++) {
            if(relMouseX > TRASH_BUTTONS[i * 3 + 1] && relMouseX < TRASH_BUTTONS[i * 3 + 1] + 10 && relMouseY > TRASH_BUTTONS[i * 3 + 2] && relMouseY < TRASH_BUTTONS[i * 3 + 2] + 10) {
                String tooltip = new TranslationTextComponent("gui." + Main.MODID + ".trash_button").getFormattedText();
                renderTooltip(tooltip, mouseX, mouseY);
            }
        }
        
    }
    
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        String s = title.getFormattedText();
        font.drawString(s, (float)(xSize / 2 - font.getStringWidth(s) / 2), 6.0F, 0x404040);
        font.drawString(playerInventory.getDisplayName().getFormattedText(), 8.0F, (float)(ySize - 96 + 2), 0x404040);
        
        if(!burner) {
            if(((IPowerConsumer)container.tileEntity).getPower() < ((IPowerConsumer)container.tileEntity).getRequiredPower()) {
                font.drawStringWithShadow(((IPowerConsumer)container.tileEntity).getPowerString(), 111, 50, 16733525);
            }
        }
        
        for(int i = 0; i < container.tileEntity.getButtonAmt(); i++) {
            IAutoIO.Button b = container.tileEntity.getButton(i);
            font.drawString(IAutoIO.getDirLetter(b.dir), b.x + 3, b.y + 2, b.dir == null ? 0xC6C6C6 : 0x404040);
        }
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        getMinecraft().getTextureManager().bindTexture(burner ? BURNER_BACKGROUND_TEXTURE : POWERED_BACKGROUND_TEXTURE);
        int startX = guiLeft;
        int startY = guiTop;
        
        // Screen#blit draws a part of the current texture (assumed to be 256x256) to the screen
        // The parameters are (x, y, u, v, width, height)
        
        blit(startX, startY, 0, 0, xSize, ySize);
        
        EvaporatorTileEntity tileEntity = container.tileEntity;
        if(burner) {
            if(getBurnTime() >= 0)
                blit(startX + 112, startY + 56, 176, 12, 14, (int)(-13 * getBurnTime()) - 1);
        } else if(((IPowerConsumer)tileEntity).getPower() >= ((IPowerConsumer)tileEntity).getRequiredPower()) {
            blit(startX + 112, startY + 45, 176, 60, 16, 16);
        }
        
        if(tileEntity.time > 0)
            blit(startX + 128, startY + 50, 176, 43, 11, (int)(-29 * getProgress()));
        
        if(!tileEntity.fluid.isEmpty()) {
            if(!tileEntity.inventory.getStackInSlot(0).isEmpty() && ((Crystal)tileEntity.inventory.getStackInSlot(0).getItem()).tier != tileEntity.tier)
                blit(startX + 65, startY + 23, 190, 0, 6, 14);
            if(!tileEntity.inventory.getStackInSlot(1).isEmpty() && ((Crystal)tileEntity.inventory.getStackInSlot(1).getItem()).tier != tileEntity.tier + 1)
                blit(startX + 65, startY + 47, 190, 0, 6, 14);
            if(!tileEntity.inventory.getStackInSlot(1).isEmpty() && tileEntity.materialAmount > 0 &&
                    !tileEntity.inventory.getStackInSlot(1).getTag().getString("material").equals(MaterialManager.materials.get(tileEntity.material).seed.toString()))
                blit(startX + 65, startY + 47, 190, 0, 6, 14);
        }
        
        
        int relMouseX = mouseX - guiLeft;
        int relMouseY = mouseY - guiTop;
        for(int i = 0; i < tileEntity.getButtonAmt(); i++) {
            IAutoIO.Button b = tileEntity.getButton(i);
            int d = b.dir == null ? 0x54 : 0x8B;
            if(relMouseX > b.x && relMouseX < b.x + 10 && relMouseY > b.y && relMouseY < b.y + 10) {
                d += 80;
            }
            RenderSystem.color3f(d / 255f, d / 255f, d / 255f);
            blit(startX + b.x + 1, startY + b.y + 1, b.x + 1, b.y + 1, 9, 9);
        }
        for(int i = 0; i < TRASH_BUTTONS.length / 3; i++) {
            int d = 0x8B;
            if(relMouseX > TRASH_BUTTONS[i * 3 + 1] && relMouseX < TRASH_BUTTONS[i * 3 + 1] + 10 && relMouseY > TRASH_BUTTONS[i * 3 + 2] && relMouseY < TRASH_BUTTONS[i * 3 + 2] + 10) {
                d += 80;
            }
            RenderSystem.color3f(d / 255f, d / 255f, d / 255f);
            blit(startX + TRASH_BUTTONS[i * 3 + 1] + 1, startY + TRASH_BUTTONS[i * 3 + 2] + 1, TRASH_BUTTONS[i * 3 + 1] + 1, TRASH_BUTTONS[i * 3 + 2] + 1, 9, 9);
        }
        
        if(!tileEntity.tanks.getFluidInTank(0).isEmpty()) {
            renderFluid(tileEntity.tanks.getFluidInTank(0), startX + 45, startY + 67);
        }
        
        if(!tileEntity.fluid.isEmpty()) {
            ResourceLocation rl = tileEntity.fluid.getFluid().getAttributes().getStillTexture();
            getMinecraft().getTextureManager().bindTexture(new ResourceLocation(rl.getNamespace(), "textures/" + rl.getPath() + ".png"));
            float[] c = ColorUtils.int2Float(ColorUtils.unwrapRGB(tileEntity.fluid.getFluid().getAttributes().getColor()));
            RenderSystem.color3f(c[0], c[1], c[2]);
            TextureAtlasSprite tex = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(rl);
            int textureHeight = tex.getFrameCount();
            int offset;
            if(textureHeight <= 1) {
                offset = 0;
            } else {
                offset = (int)(Minecraft.getInstance().world.getGameTime() / 2) % (textureHeight * 2 - 2);
                if(offset >= textureHeight)
                    offset = textureHeight * 2 - 2 - offset;
            }
            blit(startX + 115, startY + 40, 4, offset * 16 + 21, 8, -1 - (7 * tileEntity.fluid.getAmount() / 100), tex.getWidth(), tex.getHeight() * textureHeight);
        }
    }
    
    private float getBurnTime() {
        IBurnerEvaporator tileEntity = (IBurnerEvaporator)container.tileEntity;
        int smeltTimeLeft = tileEntity.getHeatLeft();
        int maxSmeltTime = tileEntity.getHeatTotal();
        if(smeltTimeLeft <= 0 || maxSmeltTime <= 0)
            return -1;
        return smeltTimeLeft / (float)maxSmeltTime;
    }
    
    private int getFluidHeight(int amt) {
        if(amt == 0)
            return 0;
        if(amt == 2000)
            return 32;
        return 31 * amt / 2000 + 1;
    }
    
    private float getProgress() {
        EvaporatorTileEntity tileEntity = container.tileEntity;
        short smeltTimeLeft = tileEntity.time;
        short maxSmeltTime = tileEntity.cycle;
        if(smeltTimeLeft <= 0 || maxSmeltTime <= 0)
            return 0;
        return smeltTimeLeft / (float)maxSmeltTime + 0.03f;
    }
    
    private void renderFluid(FluidStack stack, int x, int y) {
        ResourceLocation rl = stack.getFluid().getAttributes().getStillTexture();
        getMinecraft().getTextureManager().bindTexture(new ResourceLocation(rl.getNamespace(), "textures/" + rl.getPath() + ".png"));
        float[] c = ColorUtils.int2Float(ColorUtils.unwrapRGB(stack.getFluid().getAttributes().getColor()));
        RenderSystem.color3f(c[0], c[1], c[2]);
        TextureAtlasSprite tex = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(rl);
        int textureHeight = tex.getFrameCount();
        int offset;
        if(textureHeight <= 1) {
            offset = 0;
        } else {
            offset = (int)(Minecraft.getInstance().world.getGameTime() / 2) % (textureHeight * 2 - 2);
            if(offset >= textureHeight)
                offset = textureHeight * 2 - 2 - offset;
        }
        blit(x, y + 1, 0, offset * 16 + 17, 16, -Math.min(getFluidHeight(stack.getAmount()), 16), tex.getWidth(), tex.getHeight() * textureHeight);
        blit(x, y - 15, 0, offset * 16 + 17, 16, -Math.max(getFluidHeight(stack.getAmount()) - 16, 0), tex.getWidth(), tex.getHeight() * textureHeight);
    }
    
    @Override
    public boolean mouseClicked(double x, double y, int p_mouseClicked_5_) {
        double relMouseX = x - guiLeft;
        double relMouseY = y - guiTop;
        
        for(int i = 0; i < container.tileEntity.getButtonAmt(); i++) {
            IAutoIO.Button b = container.tileEntity.getButton(i);
            if(relMouseX >= b.x && relMouseX < b.x + 10 && relMouseY >= b.y && relMouseY < b.y + 10) {
                Networking.INSTANCE.sendToServer(new PacketCycleAutoIO((byte)i, container.tileEntity.getWorld().getWorldType().getId(), container.tileEntity.getPos()));
                Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK, 1, 1);
            }
        }
        
        for(int i = 0; i < TRASH_BUTTONS.length / 3; i++) {
            if(relMouseX >= TRASH_BUTTONS[i * 3 + 1] && relMouseX < TRASH_BUTTONS[i * 3 + 1] + 10 && relMouseY >= TRASH_BUTTONS[i * 3 + 2] && relMouseY < TRASH_BUTTONS[i * 3 + 2] + 10) {
                Networking.INSTANCE.sendToServer(new PacketTrashFluid((byte)TRASH_BUTTONS[i * 3], container.tileEntity.getWorld().getWorldType().getId(), container.tileEntity.getPos()));
                Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK, 1, 1);
            }
        }
        
        return super.mouseClicked(x, y, p_mouseClicked_5_);
    }
    
    
}
