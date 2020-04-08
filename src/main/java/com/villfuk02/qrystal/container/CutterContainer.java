package com.villfuk02.qrystal.container;

import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModContainerTypes;
import com.villfuk02.qrystal.tileentity.CutterTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class CutterContainer extends Container {
    
    public final CutterTileEntity tileEntity;
    private final IWorldPosCallable canInteractWithCallable;
    
    /**
     * Logical-client-side constructor, called from {@link ContainerType#create(IContainerFactory)}
     * Calls the logical-server-side constructor with the TileEntity at the pos in the PacketBuffer
     */
    public CutterContainer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }
    
    
    public CutterContainer(final int windowId, final PlayerInventory playerInventory, final CutterTileEntity tileEntity) {
        super(ModContainerTypes.CUTTER.get(), windowId);
        this.tileEntity = tileEntity;
        canInteractWithCallable = IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos());
        
        // Add tracking for data (Syncs to client/updates value when it changes)
        trackInt(new FunctionalIntReferenceHolder(() -> tileEntity.time, v -> tileEntity.time = (short)v));
        trackInt(new FunctionalIntReferenceHolder(() -> tileEntity.totalTime, v -> tileEntity.totalTime = (short)v));
        
        // Add all the slots for the tileEntity's inventory and the playerInventory to this container
        
        // Tile inventory slot(s)
        addSlot(new SlotItemHandler(tileEntity.inventory, 0, 39, 35));
        addSlot(new SlotItemHandler(tileEntity.inventory, 1, 8, 35));
        for(int i = 0; i < tileEntity.inventory.getSlots() - 2; i++) {
            addSlot(new SlotItemHandler(tileEntity.inventory, i + 2, 74 + i * 18, 35));
        }
        
        
        final int playerInventoryStartX = 8;
        final int playerInventoryStartY = 84;
        final int slotSizePlus2 = 18; // slots are 16x16, plus 2 (for spacing/borders) is 18x18
        
        // Player Top Inventory slots
        for(int row = 0; row < 3; ++row) {
            for(int column = 0; column < 9; ++column) {
                addSlot(new Slot(playerInventory, 9 + (row * 9) + column, playerInventoryStartX + (column * slotSizePlus2),
                                 playerInventoryStartY + (row * slotSizePlus2)));
            }
        }
        
        final int playerHotbarY = playerInventoryStartY + slotSizePlus2 * 3 + 4;
        // Player Hotbar slots
        for(int column = 0; column < 9; ++column) {
            addSlot(new Slot(playerInventory, column, playerInventoryStartX + (column * slotSizePlus2), playerHotbarY));
        }
    }
    
    private static CutterTileEntity getTileEntity(final PlayerInventory playerInventory, final PacketBuffer data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null!");
        Objects.requireNonNull(data, "data cannot be null!");
        final TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());
        if(tileAtPos instanceof CutterTileEntity)
            return (CutterTileEntity)tileAtPos;
        throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
    }
    
    /**
     * Generic & dynamic version of {@link Container#transferStackInSlot(PlayerEntity, int)}.
     * Handle when the stack in slot {@code index} is shift-clicked.
     * Normally this moves the stack between the player inventory and the other inventory(s).
     *
     * @param player the player passed in
     * @param index  the index passed in
     * @return the {@link ItemStack}
     */
    @Nonnull
    @Override
    public ItemStack transferStackInSlot(final PlayerEntity player, final int index) {
        ItemStack returnStack = ItemStack.EMPTY;
        final Slot slot = inventorySlots.get(index);
        if(slot != null && slot.getHasStack()) {
            final ItemStack slotStack = slot.getStack();
            returnStack = slotStack.copy();
            final int containerSlots = inventorySlots.size() - player.inventory.mainInventory.size();
            if(index < containerSlots) {
                if(!mergeItemStack(slotStack, containerSlots, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                for(int i = 0; i < containerSlots; i++) {
                    if(tileEntity.inventory.isItemValid(i, slotStack) && !mergeItemStack(slotStack, i, i + 1, false))
                        return ItemStack.EMPTY;
                }
            }
            if(slotStack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
            if(slotStack.getCount() == returnStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, slotStack);
        }
        return returnStack;
    }
    
    @Override
    public boolean canInteractWith(@Nonnull final PlayerEntity player) {
        return isWithinUsableDistance(canInteractWithCallable, player, ModBlocks.STEEL_CUTTER) ||
                isWithinUsableDistance(canInteractWithCallable, player, ModBlocks.DIAMOND_CUTTER) ||
                isWithinUsableDistance(canInteractWithCallable, player, ModBlocks.LASER_CUTTER);
    }
    
}
