package com.villfuk02.qrystal.tileentity;

import com.villfuk02.qrystal.blocks.EmitterBlock;
import com.villfuk02.qrystal.blocks.QrystalBlock;
import com.villfuk02.qrystal.blocks.ReceiverBlock;
import com.villfuk02.qrystal.init.ModTileEntityTypes;
import com.villfuk02.qrystal.util.CrystalUtil;
import com.villfuk02.qrystal.util.RecipeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;

public class EmitterTileEntity extends TileEntity implements ITickableTileEntity {
    
    public byte range = -1;
    public HashSet<BlockPos> uPos = new HashSet<>();
    
    public EmitterTileEntity() {
        super(ModTileEntityTypes.EMITTER);
    }
    
    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        readClient(compound);
    }
    
    
    public void readClient(CompoundNBT compound) {
        super.read(compound);
        range = compound.getByte("range");
        uPos = packPositions(compound.getIntArray("uPos"));
    }
    
    private static HashSet<BlockPos> packPositions(int[] p) {
        HashSet<BlockPos> t = new HashSet<>();
        for(int i = 0; i < p.length / 3; i++) {
            t.add(new BlockPos(p[i * 3], p[i * 3 + 1], p[i * 3 + 2]));
        }
        return t;
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putByte("range", range);
        compound.putIntArray("uPos", unpackPositions(uPos));
        return compound;
    }
    
    private static int[] unpackPositions(HashSet<BlockPos> p) {
        int[] t = new int[p.size() * 3];
        int i = 0;
        for(BlockPos bp : p) {
            t[i * 3] = bp.getX();
            t[i * 3 + 1] = bp.getY();
            t[i * 3 + 2] = bp.getZ();
            i++;
        }
        return t;
    }
    
    @Override
    public void tick() {
        if((world.getGameTime() + RecipeUtil.hashPos(pos) & 127) == 0) {
            if(isActivated()) {
                boolean gud = true;
                byte newRange = 15;
                for(byte i = 1; i <= 15; i++) {
                    BlockPos p = pos.offset(getBlockState().get(DirectionalBlock.FACING), i);
                    if(world.getBlockState(p).isOpaqueCube(world, p) && !powerable(p)) {
                        gud = false;
                        if(newRange >= i)
                            newRange = (byte)(i - 1);
                    }
                    if(gud) {
                        activate(p);
                    } else if(i <= range && !gud) {
                        deactivate(p, getBlockState());
                    }
                }
                range = newRange;
            } else if(range >= 0) {
                for(byte i = 1; i <= range; i++) {
                    deactivate(pos.offset(getBlockState().get(DirectionalBlock.FACING), i), getBlockState());
                }
                range = -1;
                uPos.clear();
            }
        }
    }
    
    public static int getTier(BlockState state) {
        return ((EmitterBlock)state.getBlock()).tier;
    }
    
    private boolean isActivated() {
        Block b = world.getBlockState(pos.offset(getBlockState().get(DirectionalBlock.FACING).getOpposite())).getBlock();
        if(b instanceof QrystalBlock) {
            QrystalBlock q = (QrystalBlock)b;
            return q.tier == getTier(getBlockState()) && q.color == CrystalUtil.Color.QONDO && q.activated;
        }
        if(b instanceof ReceiverBlock) {
            ReceiverBlock r = (ReceiverBlock)b;
            return r.tier == getTier(getBlockState()) && r.activated;
        }
        return false;
    }
    
    private boolean powerable(BlockPos p) {
        Block b = world.getBlockState(p).getBlock();
        if(b instanceof ReceiverBlock)
            return ((ReceiverBlock)b).tier == getTier(getBlockState());
        else if(world.getTileEntity(p) instanceof IPowerConsumer)
            return true;
        return false;
    }
    
    private void activate(BlockPos p) {
        if(world.getBlockState(p).getBlock() instanceof ReceiverBlock) {
            ReceiverBlock b = (ReceiverBlock)world.getBlockState(p).getBlock();
            if(b.tier == getTier(getBlockState()))
                b.activate(world, p, uPos, pos);
        } else if(world.getTileEntity(p) instanceof IPowerConsumer && ((IPowerConsumer)world.getTileEntity(p)).getPower() < getTier(getBlockState()) + 1) {
            ((IPowerConsumer)world.getTileEntity(p)).setPower((byte)(getTier(getBlockState()) + 1));
        }
    }
    
    private void deactivate(BlockPos p, BlockState state) {
        if(world.getBlockState(p).getBlock() instanceof ReceiverBlock) {
            ReceiverBlock b = (ReceiverBlock)world.getBlockState(p).getBlock();
            if(b.tier == getTier(state))
                b.deactivate(world, p);
        } else if(world.getTileEntity(p) instanceof IPowerConsumer) {
            ((IPowerConsumer)world.getTileEntity(p)).setPower((byte)0);
        }
    }
    
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), 1, write(new CompoundNBT()));
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        readClient(pkt.getNbtCompound());
    }
    
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();
        return write(tag);
    }
    
    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        super.handleUpdateTag(tag);
        readClient(tag);
    }
    
    public void deactivateBlocks(BlockState state) {
        for(int i = 1; i <= range; i++) {
            deactivate(pos.offset(state.get(DirectionalBlock.FACING), i), state);
        }
    }
    
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos, pos.offset(getBlockState().get(DirectionalBlock.FACING), range + 1).add(1, 1, 1));
    }
}
