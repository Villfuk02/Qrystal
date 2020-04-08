package com.villfuk02.qrystal.fluids;

import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.init.ModFluids;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.fluids.FluidAttributes;

public class FluidBase extends Fluid {
    
    private final String name;
    private final int color;
    private final int density;
    private final int viscosity;
    private final int temperature;
    
    
    public FluidBase(String name, int color, int density, int viscosity, int temperature) {
        this.name = name;
        this.color = color;
        this.density = density;
        this.viscosity = viscosity;
        this.temperature = temperature;
        setRegistryName(Main.MODID, name);
        ModFluids.FLUIDS.add(this);
    }
    
    @Override
    protected FluidAttributes createAttributes() {
        return net.minecraftforge.fluids.FluidAttributes.Water.builder(new ResourceLocation("block/water_still"), new ResourceLocation("block/water_flow"))
                .overlay(new ResourceLocation("block/water_overlay"))
                .translationKey("fluid.qrystal." + name)
                .color(color)
                .density(density)
                .viscosity(viscosity)
                .temperature(temperature)
                .build(this);
    }
    
    @Override
    public Item getFilledBucket() {
        return null;
    }
    
    @Override
    protected boolean canDisplace(IFluidState p_215665_1_, IBlockReader p_215665_2_, BlockPos p_215665_3_, Fluid p_215665_4_, Direction p_215665_5_) {
        return false;
    }
    
    @Override
    protected Vec3d getFlow(IBlockReader p_215663_1_, BlockPos p_215663_2_, IFluidState p_215663_3_) {
        return null;
    }
    
    @Override
    public int getTickRate(IWorldReader p_205569_1_) {
        return 0;
    }
    
    @Override
    protected float getExplosionResistance() {
        return 0;
    }
    
    @Override
    public float getActualHeight(IFluidState p_215662_1_, IBlockReader p_215662_2_, BlockPos p_215662_3_) {
        return 0;
    }
    
    @Override
    public float getHeight(IFluidState p_223407_1_) {
        return 0;
    }
    
    @Override
    protected BlockState getBlockState(IFluidState state) {
        return null;
    }
    
    @Override
    public boolean isSource(IFluidState state) {
        return false;
    }
    
    @Override
    public int getLevel(IFluidState p_207192_1_) {
        return 0;
    }
    
    @Override
    public VoxelShape func_215664_b(IFluidState p_215664_1_, IBlockReader p_215664_2_, BlockPos p_215664_3_) {
        return null;
    }
}
