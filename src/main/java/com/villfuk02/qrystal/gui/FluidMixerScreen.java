package com.villfuk02.qrystal.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.container.FluidMixerContainer;
import com.villfuk02.qrystal.network.Networking;
import com.villfuk02.qrystal.network.PacketCycleAutoIO;
import com.villfuk02.qrystal.network.PacketTrashFluid;
import com.villfuk02.qrystal.tileentity.BurnerFluidMixerTileEntity;
import com.villfuk02.qrystal.tileentity.FluidMixerTileEntity;
import com.villfuk02.qrystal.tileentity.IAutoIO;
import com.villfuk02.qrystal.tileentity.IPowerConsumer;
import com.villfuk02.qrystal.util.ColorUtils;
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

public class FluidMixerScreen extends ContainerScreen<FluidMixerContainer> {
    private static final ResourceLocation BURNER_BACKGROUND_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/burner_fluid_mixer.png");
    private static final ResourceLocation POWERED_BACKGROUND_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/powered_fluid_mixer.png");
    private final boolean burner;
    
    public static final int[] TRASH_BUTTONS = new int[]{0, 61, 24, 1, 134, 24};
    
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
        boolean energyBar1Hovered = relMouseX > 55 && relMouseX < 74 && relMouseY > 74 && relMouseY < 93;
        boolean energyBar2Hovered = relMouseX > 131 && relMouseX < 150 && relMouseY > 74 && relMouseY < 93;
        if(burner) {
            if(energyBar1Hovered || energyBar2Hovered) {
                String tooltip;
                if(((BurnerFluidMixerTileEntity)tileEntity).heatTotal > 0)
                    tooltip = new TranslationTextComponent("gui." + Main.MODID + ".fuel_time").appendSibling(new StringTextComponent(((BurnerFluidMixerTileEntity)tileEntity).heatLeft / 40 + "s"))
                            .applyTextStyle(TextFormatting.GOLD)
                            .getFormattedText();
                else
                    tooltip = new TranslationTextComponent("gui." + Main.MODID + ".no_fuel").applyTextStyle(TextFormatting.RED).getFormattedText();
                renderTooltip(tooltip, mouseX, mouseY);
            }
        } else {
            if(energyBar1Hovered || energyBar2Hovered) {
                String tooltip = new TranslationTextComponent("gui." + Main.MODID + ".energy").appendSibling(new StringTextComponent(((IPowerConsumer)tileEntity).getPowerString()))
                        .applyTextStyle(((IPowerConsumer)tileEntity).getPower() >= ((IPowerConsumer)tileEntity).getRequiredPower() ? TextFormatting.GOLD : TextFormatting.RED)
                        .getFormattedText();
                renderTooltip(tooltip, mouseX, mouseY);
            }
        }
        
        boolean fluid1Hovered = relMouseX > 55 && relMouseX < 74 && relMouseY > 37 && relMouseY < 72;
        if(fluid1Hovered) {
            if(!tileEntity.tanks.getFluidInTank(0).isEmpty()) {
                String tooltip = new TranslationTextComponent(tileEntity.tanks.getFluidInTank(0).getTranslationKey()).appendSibling(
                        new StringTextComponent(" " + tileEntity.tanks.getFluidInTank(0).getAmount() + "mB")).getFormattedText();
                renderTooltip(tooltip, mouseX, mouseY);
            }
        }
        
        boolean fluid2Hovered = relMouseX > 131 && relMouseX < 150 && relMouseY > 37 && relMouseY < 72;
        if(fluid2Hovered) {
            if(!tileEntity.tanks.getFluidInTank(1).isEmpty()) {
                String tooltip = new TranslationTextComponent(tileEntity.tanks.getFluidInTank(1).getTranslationKey()).appendSibling(
                        new StringTextComponent(" " + tileEntity.tanks.getFluidInTank(1).getAmount() + "mB")).getFormattedText();
                renderTooltip(tooltip, mouseX, mouseY);
            }
        }
        
        boolean fluid3Hovered = relMouseX > 93 && relMouseX < 112 && relMouseY > 51 && relMouseY < 86;
        if(fluid3Hovered) {
            if(!tileEntity.tanks.getFluidInTank(2).isEmpty()) {
                String tooltip = new TranslationTextComponent(tileEntity.tanks.getFluidInTank(2).getTranslationKey()).appendSibling(
                        new StringTextComponent(" " + tileEntity.tanks.getFluidInTank(2).getAmount() + "mB")).getFormattedText();
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
        // Copied from AbstractFurnaceScreen#drawGuiContainerForegroundLayer
        String s = title.getFormattedText();
        font.drawString(s, (float)(xSize / 2 - font.getStringWidth(s) / 2), 6.0F, 0x404040);
        font.drawString(playerInventory.getDisplayName().getFormattedText(), 8.0F, (float)(ySize - 96 + 2), 0x404040);
        
        if(!burner) {
            if(((IPowerConsumer)container.tileEntity).getPower() < ((IPowerConsumer)container.tileEntity).getRequiredPower()) {
                font.drawStringWithShadow(((IPowerConsumer)container.tileEntity).getPowerString(), 56, 80, 16733525);
                font.drawStringWithShadow(((IPowerConsumer)container.tileEntity).getPowerString(), 132, 80, 16733525);
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
        
        FluidMixerTileEntity tileEntity = container.tileEntity;
        if(tileEntity.time > 0) {
            if(!tileEntity.tanks.getFluidInTank(0).isEmpty())
                blit(startX + 44, startY + 72, 176, 43, 11, (int)(-29 * getProgress()));
            if(!tileEntity.tanks.getFluidInTank(1).isEmpty())
                blit(startX + 152, startY + 72, 176, 43, 11, (int)(-29 * getProgress()));
            if(!tileEntity.inventory.getStackInSlot(0).isEmpty() || !tileEntity.inventory.getStackInSlot(1).isEmpty())
                blit(startX + 97, startY + 37, 176, 47, 11, (int)(13 * getProgress()));
        }
        
        if(burner) {
            blit(startX + 57, startY + 86, 176, 12, 14, (int)(-13 * getBurnTime()) - 1);
            blit(startX + 133, startY + 86, 176, 12, 14, (int)(-13 * getBurnTime()) - 1);
        } else if(((IPowerConsumer)tileEntity).getPower() >= ((IPowerConsumer)tileEntity).getRequiredPower()) {
            blit(startX + 57, startY + 75, 176, 60, 16, 16);
            blit(startX + 133, startY + 75, 176, 60, 16, 16);
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
            renderFluid(tileEntity.tanks.getFluidInTank(0), startX + 57, startY + 70);
        }
        if(!tileEntity.tanks.getFluidInTank(1).isEmpty()) {
            renderFluid(tileEntity.tanks.getFluidInTank(1), startX + 133, startY + 70);
        }
        if(!tileEntity.tanks.getFluidInTank(2).isEmpty()) {
            renderFluid(tileEntity.tanks.getFluidInTank(2), startX + 95, startY + 83);
        }
    }
    
    private float getProgress() {
        FluidMixerTileEntity tileEntity = container.tileEntity;
        short smeltTimeLeft = tileEntity.time;
        short maxSmeltTime = tileEntity.totalTime;
        if(smeltTimeLeft <= 0 || maxSmeltTime <= 0)
            return 0;
        return smeltTimeLeft / (float)maxSmeltTime + 0.03f;
    }
    
    private float getBurnTime() {
        BurnerFluidMixerTileEntity tileEntity = (BurnerFluidMixerTileEntity)container.tileEntity;
        int smeltTimeLeft = tileEntity.heatLeft;
        int maxSmeltTime = tileEntity.heatTotal;
        if(smeltTimeLeft <= 0 || maxSmeltTime <= 0)
            return 0;
        return smeltTimeLeft / (float)maxSmeltTime;
    }
    
    private int getFluidHeight(int amt) {
        if(amt == 0)
            return 0;
        if(amt == 2000)
            return 32;
        return 31 * amt / 2000 + 1;
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
