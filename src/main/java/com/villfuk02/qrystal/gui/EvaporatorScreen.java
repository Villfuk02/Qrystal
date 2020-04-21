package com.villfuk02.qrystal.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.container.EvaporatorContainer;
import com.villfuk02.qrystal.tileentity.EvaporatorTileEntity;
import com.villfuk02.qrystal.util.ColorUtils;
import com.villfuk02.qrystal.util.RecipeUtil;
import com.villfuk02.qrystal.util.handlers.FilledFlaskColorHandler;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class EvaporatorScreen extends ContainerScreen<EvaporatorContainer> {
    private static final ResourceLocation BURNER_BACKGROUND_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/evaporator.png");
    private static final ResourceLocation POWERED_BACKGROUND_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/powered_evaporator.png");
    private final boolean burner;
    
    public EvaporatorScreen(EvaporatorContainer container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
        burner = container.burner;
        ySize = 221;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        
        int relMouseX = mouseX - guiLeft;
        int relMouseY = mouseY - guiTop;
        EvaporatorTileEntity tileEntity = container.tileEntity;
        if(burner) {
            boolean energyBarHovered = relMouseX > 37 && relMouseX < 56 && relMouseY > 54 && relMouseY < 73;
            if(energyBarHovered) {
                String tooltip = new TranslationTextComponent(true ? "gui." + Main.MODID + ".no_fuel" : "30s").applyTextStyle(TextFormatting.GOLD).getFormattedText();
                renderTooltip(tooltip, mouseX, mouseY);
            }
        } else {
            boolean energyBarHovered = relMouseX > 9 && relMouseX < 28 && relMouseY > 50 && relMouseY < 69;
            if(energyBarHovered) {
                String tooltip = new TranslationTextComponent("gui." + Main.MODID + (true ? ".not_enough_energy" : ".energy")).applyTextStyle(TextFormatting.GOLD).getFormattedText();
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
        int[] fc = FilledFlaskColorHandler.getColor(tileEntity.fluid);
        RenderSystem.color3f(fc[0] / 255f, fc[1] / 255f, fc[2] / 255f);
        if(tileEntity.fluidAmount > 0) {
            blit(startX + 53, startY + 92, 176, 152, 11, (-48 * tileEntity.fluidAmount + 1) / 500 - 1);
        }
        int[] mc = ColorUtils.unwrapRGB(tileEntity.materialColor);
        RenderSystem.color3f(mc[0] / 255f, mc[1] / 255f, mc[2] / 255f);
        if(tileEntity.materialAmount > 0) {
            blit(startX + 44, startY + 92, 176, 152, 7, (int)((-48 * (long)tileEntity.materialAmount + 1) / (500 * RecipeUtil.BASE_VALUE)) - 1);
        }
        
        RenderSystem.color3f(1, 1, 1);
        if(tileEntity.time > 0 && tileEntity.materialAmount > 0 && tileEntity.fluidAmount > 0) {
            blit(startX + 105, startY + 64, 176, 57, 11, (-29 * tileEntity.time - tileEntity.cycle + 1) / tileEntity.cycle);
        }
        
    }
    
    
}
