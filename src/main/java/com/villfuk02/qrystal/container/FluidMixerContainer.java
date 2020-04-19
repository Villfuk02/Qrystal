package com.villfuk02.qrystal.container;

import com.villfuk02.qrystal.init.ModBlocks;
import com.villfuk02.qrystal.init.ModContainerTypes;
import com.villfuk02.qrystal.tileentity.BurnerFluidMixerTileEntity;
import com.villfuk02.qrystal.tileentity.FluidMixerTileEntity;
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

public class FluidMixerContainer extends Container {
    
    public final FluidMixerTileEntity tileEntity;
    private final IWorldPosCallable canInteractWithCallable;
    public final boolean burner;
    
    /**
     * Logical-client-side constructor, called from {@link ContainerType#create(IContainerFactory)}
     * Calls the logical-server-side constructor with the TileEntity at the pos in the PacketBuffer
     */
    public FluidMixerContainer(int windowId, PlayerInventory playerInventory, PacketBuffer data) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }
    
    
    public FluidMixerContainer(int windowId, PlayerInventory playerInventory, FluidMixerTileEntity tileEntity) {
        super(ModContainerTypes.FLUID_MIXER.get(), windowId);
        this.tileEntity = tileEntity;
        canInteractWithCallable = IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos());
        burner = tileEntity instanceof BurnerFluidMixerTileEntity;
        
        // Add tracking for data (Syncs to client/updates value when it changes)
        trackInt(new FunctionalIntReferenceHolder(() -> tileEntity.time, v -> tileEntity.time = (short)v));
        trackInt(new FunctionalIntReferenceHolder(() -> tileEntity.totalTime, v -> tileEntity.totalTime = (short)v));
        
        // Add all the slots for the tileEntity's inventory and the playerInventory to this container
        
        // Tile inventory slot(s)
        addSlot(new SlotItemHandler(tileEntity.inventory, 0, 95, 51));
        addSlot(new SlotItemHandler(tileEntity.inventory, 1, 56, 51));
        addSlot(new SlotItemHandler(tileEntity.inventory, 2, 134, 51));
        addSlot(new SlotItemHandler(tileEntity.inventory, 3, 86, 18));
        addSlot(new SlotItemHandler(tileEntity.inventory, 4, 104, 18));
        addSlot(new SlotItemHandler(tileEntity.inventory, 5, 95, 73));
        addSlot(new SlotItemHandler(tileEntity.inventory, 6, 56, 77));
        addSlot(new SlotItemHandler(tileEntity.inventory, 7, 134, 77));
        if(burner)
            addSlot(new SlotItemHandler(tileEntity.inventory, 8, 11, 67));
        
        
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
    
    private static FluidMixerTileEntity getTileEntity(PlayerInventory playerInventory, PacketBuffer data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null!");
        Objects.requireNonNull(data, "data cannot be null!");
        TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());
        if(tileAtPos instanceof FluidMixerTileEntity)
            return (FluidMixerTileEntity)tileAtPos;
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
        return isWithinUsableDistance(canInteractWithCallable, player, ModBlocks.BURNER_FLUID_MIXER) || isWithinUsableDistance(canInteractWithCallable, player, ModBlocks.POWERED_FLUID_MIXER) ||
                isWithinUsableDistance(canInteractWithCallable, player, ModBlocks.ULTIMATE_FLUID_MIXER);
    }
    
}
