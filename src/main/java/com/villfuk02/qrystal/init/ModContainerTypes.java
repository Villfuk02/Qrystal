package com.villfuk02.qrystal.init;

import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.container.CutterContainer;
import com.villfuk02.qrystal.container.EvaporatorContainer;
import com.villfuk02.qrystal.container.FluidMixerContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModContainerTypes {
    
    public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = new DeferredRegister<>(ForgeRegistries.CONTAINERS, Main.MODID);
    
    public static final RegistryObject<ContainerType<CutterContainer>> CUTTER = CONTAINER_TYPES.register("cutter",
                                                                                                         () -> IForgeContainerType.create(CutterContainer::new));
    
    public static final RegistryObject<ContainerType<FluidMixerContainer>> FLUID_MIXER = CONTAINER_TYPES.register("fluid_mixer", () -> IForgeContainerType.create(
            FluidMixerContainer::new));
    public static final RegistryObject<ContainerType<EvaporatorContainer>> EVAPORATOR = CONTAINER_TYPES.register("evaporator", () -> IForgeContainerType.create(
            EvaporatorContainer::new));
    
}
