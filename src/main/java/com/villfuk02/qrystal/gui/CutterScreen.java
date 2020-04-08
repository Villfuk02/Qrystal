package com.villfuk02.qrystal.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.container.CutterContainer;
import com.villfuk02.qrystal.tileentity.CutterTileEntity;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class CutterScreen extends ContainerScreen<CutterContainer> {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/cutter.png");
    
    public CutterScreen(final CutterContainer container, final PlayerInventory inventory, final ITextComponent title) {
        super(container, inventory, title);
    }
    
    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        
        int relMouseX = mouseX - guiLeft;
        int relMouseY = mouseY - guiTop;
        final CutterTileEntity tileEntity = container.tileEntity;
        boolean energyBarHovered = relMouseX > 37 && relMouseX < 56 && relMouseY > 54 && relMouseY < 73;
        if(energyBarHovered) {
            String tooltip = new TranslationTextComponent("gui." + Main.MODID + (true ? ".not_enough_energy" : ".energy")).applyTextStyle(TextFormatting.GOLD)
                    .getFormattedText();
            renderTooltip(tooltip, mouseX, mouseY);
        }
        boolean toolHovered = relMouseX > 37 && relMouseX < 56 && relMouseY > 33 && relMouseY < 52;
        if(toolHovered && tileEntity.inventory.getStackInSlot(0).isEmpty()) {
            String tooltip = new TranslationTextComponent("gui." + Main.MODID + ".no_tool." + container.tileEntity.getBlockState().getBlock().getRegistryName().getPath())
                    .applyTextStyle(TextFormatting.RED)
                    .getFormattedText();
            renderTooltip(tooltip, mouseX, mouseY);
        }
    }
    
    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        // Copied from AbstractFurnaceScreen#drawGuiContainerForegroundLayer
        String s = title.getFormattedText();
        font.drawString(s, (float)(xSize / 2 - font.getStringWidth(s) / 2), 6.0F, 0x404040);
        font.drawString(playerInventory.getDisplayName().getFormattedText(), 8.0F, (float)(ySize - 96 + 2), 0x404040);
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        int startX = guiLeft;
        int startY = guiTop;
        
        // Screen#blit draws a part of the current texture (assumed to be 256x256) to the screen
        // The parameters are (x, y, u, v, width, height)
        
        blit(startX, startY, 0, 0, xSize, ySize);
        
        final CutterTileEntity tileEntity = container.tileEntity;
        if(tileEntity.time > 0) {
            int arrowWidth = getSmeltTimeScaled();
            blit(startX + 28, startY + 34, 176, 16, arrowWidth, 16);
        }
        if(tileEntity.inventory.getStackInSlot(0).isEmpty()) {
            blit(startX + 38, startY + 34, 176, 33, 18, 18);
        }
    }
    
    private int getSmeltTimeScaled() {
        final CutterTileEntity tileEntity = container.tileEntity;
        final short smeltTimeLeft = tileEntity.time;
        final short maxSmeltTime = tileEntity.totalTime;
        if(smeltTimeLeft <= 0 || maxSmeltTime <= 0)
            return 0;
        return smeltTimeLeft * 40 / maxSmeltTime; // 24 is the width of the arrow
    }
    
}
