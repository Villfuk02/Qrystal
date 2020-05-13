package com.villfuk02.qrystal.container;

import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModContainerTypes;
import com.villfuk02.qrystal.tileentity.BasicEvaporatorTileEntity;
import com.villfuk02.qrystal.tileentity.BurnerEvaporatorTileEntity;
import com.villfuk02.qrystal.tileentity.EvaporatorTileEntity;
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

public class EvaporatorContainer extends Container {
    
    public final EvaporatorTileEntity tileEntity;
    private final IWorldPosCallable canInteractWithCallable;
    public final boolean burner;
    
    /**
     * Logical-client-side constructor, called from {@link ContainerType#create(IContainerFactory)}
     * Calls the logical-server-side constructor with the TileEntity at the pos in the PacketBuffer
     */
    public EvaporatorContainer(int windowId, PlayerInventory playerInventory, PacketBuffer data) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }
    
    
    public EvaporatorContainer(int windowId, PlayerInventory playerInventory, EvaporatorTileEntity tileEntity) {
        super(ModContainerTypes.EVAPORATOR.get(), windowId);
        this.tileEntity = tileEntity;
        canInteractWithCallable = IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos());
        burner = tileEntity instanceof BurnerEvaporatorTileEntity || tileEntity instanceof BasicEvaporatorTileEntity;
        
        // Add tracking for data (Syncs to client/updates value when it changes)
        trackInt(new FunctionalIntReferenceHolder(() -> tileEntity.time, v -> tileEntity.time = (short)v));
        trackInt(new FunctionalIntReferenceHolder(() -> tileEntity.materialAmount, v -> tileEntity.materialAmount = v));
        trackInt(new FunctionalIntReferenceHolder(() -> tileEntity.seeds, v -> tileEntity.seeds = (byte)v));
        if(tileEntity instanceof BasicEvaporatorTileEntity) {
            trackInt(new FunctionalIntReferenceHolder(() -> ((BasicEvaporatorTileEntity)tileEntity).heatLeft, v -> ((BasicEvaporatorTileEntity)tileEntity).heatLeft = v));
            trackInt(new FunctionalIntReferenceHolder(() -> ((BasicEvaporatorTileEntity)tileEntity).heatTotal, v -> ((BasicEvaporatorTileEntity)tileEntity).heatTotal = v));
        }
        if(tileEntity instanceof BurnerEvaporatorTileEntity) {
            trackInt(new FunctionalIntReferenceHolder(() -> ((BurnerEvaporatorTileEntity)tileEntity).heatLeft, v -> ((BurnerEvaporatorTileEntity)tileEntity).heatLeft = v));
            trackInt(new FunctionalIntReferenceHolder(() -> ((BurnerEvaporatorTileEntity)tileEntity).heatTotal, v -> ((BurnerEvaporatorTileEntity)tileEntity).heatTotal = v));
        }
        
        // Add all the slots for the tileEntity's inventory and the playerInventory to this container
        
        // Tile inventory slot(s)
        addSlot(new SlotItemHandler(tileEntity.inventory, 0, 74, 23));
        addSlot(new SlotItemHandler(tileEntity.inventory, 1, 74, 47));
        addSlot(new SlotItemHandler(tileEntity.inventory, 2, 74, 73));
        addSlot(new SlotItemHandler(tileEntity.inventory, 3, 102, 73));
        addSlot(new SlotItemHandler(tileEntity.inventory, 4, 120, 73));
        addSlot(new SlotItemHandler(tileEntity.inventory, 5, 148, 73));
        if(burner)
            addSlot(new SlotItemHandler(tileEntity.inventory, 6, 148, 47));
        
        
        final int playerInventoryStartX = 8;
        final int playerInventoryStartY = 110;
        final int slotSizePlus2 = 18; // slots are 16x16, plus 2 (for spacing/borders) is 18x18
        
        // Player Top Inventory slots
        for(int row = 0; row < 3; ++row) {
            for(int column = 0; column < 9; ++column) {
                addSlot(new Slot(playerInventory, 9 + (row * 9) + column, playerInventoryStartX + (column * slotSizePlus2), playerInventoryStartY + (row * slotSizePlus2)));
            }
        }
        
        final int playerHotbarY = playerInventoryStartY + slotSizePlus2 * 3 + 4;
        // Player Hotbar slots
        for(int column = 0; column < 9; ++column) {
            addSlot(new Slot(playerInventory, column, playerInventoryStartX + (column * slotSizePlus2), playerHotbarY));
        }
    }
    
    private static EvaporatorTileEntity getTileEntity(PlayerInventory playerInventory, PacketBuffer data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null!");
        Objects.requireNonNull(data, "data cannot be null!");
        TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());
        if(tileAtPos instanceof EvaporatorTileEntity)
            return (EvaporatorTileEntity)tileAtPos;
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
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if(slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            returnStack = slotStack.copy();
            int containerSlots = inventorySlots.size() - player.inventory.mainInventory.size();
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
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        return isWithinUsableDistance(canInteractWithCallable, player, ModBlocks.BURNER_EVAPORATOR) || isWithinUsableDistance(canInteractWithCallable, player, ModBlocks.BASIC_EVAPORATOR) ||
                isWithinUsableDistance(canInteractWithCallable, player, ModBlocks.POWERED_EVAPORATOR) || isWithinUsableDistance(canInteractWithCallable, player, ModBlocks.ULTIMATE_EVAPORATOR);
    }
    
}
