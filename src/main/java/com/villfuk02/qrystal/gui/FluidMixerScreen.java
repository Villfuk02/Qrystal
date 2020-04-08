package com.villfuk02.qrystal.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.container.FluidMixerContainer;
import com.villfuk02.qrystal.tileentity.FluidMixerTileEntity;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class FluidMixerScreen extends ContainerScreen<FluidMixerContainer> {
    private static final ResourceLocation BURNER_BACKGROUND_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/burner_fluid_mixer.png");
    private static final ResourceLocation POWERED_BACKGROUND_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/powered_fluid_mixer.png");
    private final boolean burner;
    
    public FluidMixerScreen(FluidMixerContainer container, PlayerInventory inventory, ITextComponent title) {
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
        FluidMixerTileEntity tileEntity = container.tileEntity;
        if(burner) {
            boolean energyBarHovered = relMouseX > 37 && relMouseX < 56 && relMouseY > 54 && relMouseY < 73;
            if(energyBarHovered) {
                String tooltip = new TranslationTextComponent(true ? "gui." + Main.MODID + ".no_fuel" : "30s").applyTextStyle(TextFormatting.GOLD).getFormattedText();
                renderTooltip(tooltip, mouseX, mouseY);
            }
        } else {
            boolean energyBarHovered = relMouseX > 9 && relMouseX < 28 && relMouseY > 50 && relMouseY < 69;
            if(energyBarHovered) {
                String tooltip = new TranslationTextComponent("gui." + Main.MODID + (true ? ".not_enough_energy" : ".energy")).applyTextStyle(TextFormatting.GOLD)
                        .getFormattedText();
                renderTooltip(tooltip, mouseX, mouseY);
            }
        }
    }
    
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        // Copied from AbstractFurnaceScreen#drawGuiContainerForegroundLayer
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
        
        FluidMixerTileEntity tileEntity = container.tileEntity;
        if(tileEntity.time > 0) {
            if(!tileEntity.inventory.getStackInSlot(1).isEmpty())
                blit(startX + 43, startY + 69, 176, 43, 10, (int)(-29 * getProgress()));
            if(!tileEntity.inventory.getStackInSlot(2).isEmpty())
                blit(startX + 153, startY + 69, 176, 43, 10, (int)(-29 * getProgress()));
            if(!tileEntity.inventory.getStackInSlot(3).isEmpty() || !tileEntity.inventory.getStackInSlot(4).isEmpty())
                blit(startX + 97, startY + 36, 176, 47, 11, (int)(13 * getProgress()));
        }
    }
    
    private float getProgress() {
        FluidMixerTileEntity tileEntity = container.tileEntity;
        short smeltTimeLeft = tileEntity.time;
        short maxSmeltTime = tileEntity.totalTime;
        if(smeltTimeLeft <= 0 || maxSmeltTime <= 0)
            return 0;
        return smeltTimeLeft / (float)maxSmeltTime;
    }
    
}
