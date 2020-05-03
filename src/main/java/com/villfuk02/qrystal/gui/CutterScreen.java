package com.villfuk02.qrystal.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.container.CutterContainer;
import com.villfuk02.qrystal.network.Networking;
import com.villfuk02.qrystal.network.PacketCycleAutoIO;
import com.villfuk02.qrystal.tileentity.CutterTileEntity;
import com.villfuk02.qrystal.tileentity.IAutoIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class CutterScreen extends ContainerScreen<CutterContainer> {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/cutter.png");
    
    public CutterScreen(CutterContainer container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        
        int relMouseX = mouseX - guiLeft;
        int relMouseY = mouseY - guiTop;
        CutterTileEntity tileEntity = container.tileEntity;
        boolean energyBarHovered = relMouseX > 37 && relMouseX < 56 && relMouseY > 54 && relMouseY < 73;
        if(energyBarHovered) {
            String tooltip = new TranslationTextComponent("gui." + Main.MODID + ".energy").appendSibling(new StringTextComponent(tileEntity.getPowerString()))
                    .applyTextStyle(tileEntity.getPower() >= tileEntity.getRequiredPower() ? TextFormatting.GOLD : TextFormatting.RED)
                    .getFormattedText();
            renderTooltip(tooltip, mouseX, mouseY);
        }
        boolean toolHovered = relMouseX > 37 && relMouseX < 56 && relMouseY > 33 && relMouseY < 52;
        if(toolHovered && tileEntity.inventory.getStackInSlot(0).isEmpty()) {
            String tooltip = new TranslationTextComponent("gui." + Main.MODID + ".no_tool." + container.tileEntity.getBlockState().getBlock().getRegistryName().getPath()).applyTextStyle(TextFormatting.RED)
                    .getFormattedText();
            renderTooltip(tooltip, mouseX, mouseY);
        }
        
        for(int i = 0; i < tileEntity.getButtonAmt(); i++) {
            IAutoIO.Button b = tileEntity.getButton(i);
            if(relMouseX > b.x && relMouseX < b.x + 10 && relMouseY > b.y && relMouseY < b.y + 10) {
                String tooltip = new TranslationTextComponent("gui." + Main.MODID + ".button." + b.input + "." + IAutoIO.getDirLetter(b.dir)).getFormattedText();
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
        
        if(container.tileEntity.getPower() < container.tileEntity.getRequiredPower())
            font.drawStringWithShadow(container.tileEntity.getPowerString(), 38, 61, 16733525);
        
        for(int i = 0; i < container.tileEntity.getButtonAmt(); i++) {
            IAutoIO.Button b = container.tileEntity.getButton(i);
            font.drawString(IAutoIO.getDirLetter(b.dir), b.x + 3, b.y + 2, b.dir == null ? 0xC6C6C6 : 0x404040);
        }
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        int startX = guiLeft;
        int startY = guiTop;
        
        // Screen#blit draws a part of the current texture (assumed to be 256x256) to the screen
        // The parameters are (x, y, u, v, width, height)
        
        blit(startX, startY, 0, 0, xSize, ySize);
        
        CutterTileEntity tileEntity = container.tileEntity;
        if(tileEntity.time > 0) {
            int arrowWidth = getSmeltTimeScaled();
            blit(startX + 28, startY + 34, 176, 16, arrowWidth, 16);
        }
        if(tileEntity.inventory.getStackInSlot(0).isEmpty()) {
            blit(startX + 38, startY + 34, 176, 33, 18, 18);
        }
        
        if(tileEntity.getPower() >= tileEntity.getRequiredPower()) {
            blit(startX + 39, startY + 56, 176, 0, 16, 16);
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
        
    }
    
    private int getSmeltTimeScaled() {
        CutterTileEntity tileEntity = container.tileEntity;
        short smeltTimeLeft = tileEntity.time;
        short maxSmeltTime = tileEntity.totalTime;
        if(smeltTimeLeft <= 0 || maxSmeltTime <= 0)
            return 0;
        return smeltTimeLeft * 40 / maxSmeltTime; // 24 is the width of the arrow
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
        
        return super.mouseClicked(x, y, p_mouseClicked_5_);
    }
    
}
