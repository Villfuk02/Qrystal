package com.villfuk02.qrystal.util.handlers;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

//by Tfarcenim
public interface IMultiTank extends IFluidHandler {
    
    /**
     * Fills fluid into specified tank.
     *
     * @param tank     Tank to insert into.
     * @param resource FluidStack representing the Fluid and maximum amount of fluid to be filled.
     * @param action   If SIMULATE, fill will only be simulated.
     * @return Amount of resource that was (or would have been, if simulated) filled.
     */
    int fill(int tank, FluidStack resource, FluidAction action);
    
    /**
     * Drains fluid out of specified tank.
     *
     * @param tank     Tank to extract from.
     * @param resource FluidStack representing the Fluid and maximum amount of fluid to be drained.
     * @param action   If SIMULATE, drain will only be simulated.
     * @return FluidStack representing the Fluid and amount that was (or would have been, if
     * simulated) drained.
     */
    @Nonnull
    FluidStack drain(int tank, FluidStack resource, FluidAction action);
    
    /**
     * Drains fluid out of specified tank.
     * <p/>
     * This method is not Fluid-sensitive.
     *
     * @param tank     Tank to extract from.
     * @param maxDrain Maximum amount of fluid to drain.
     * @param action   If SIMULATE, drain will only be simulated.
     * @return FluidStack representing the Fluid and amount that was (or would have been, if
     * simulated) drained.
     */
    @Nonnull
    FluidStack drain(int tank, int maxDrain, FluidAction action);
    
}
