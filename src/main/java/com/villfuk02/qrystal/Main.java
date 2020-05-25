package com.villfuk02.qrystal;

import com.villfuk02.qrystal.crafting.*;
import com.villfuk02.qrystal.dataserializers.FluidTierManager;
import com.villfuk02.qrystal.dataserializers.MaterialManager;
import com.villfuk02.qrystal.gui.CutterScreen;
import com.villfuk02.qrystal.gui.EvaporatorScreen;
import com.villfuk02.qrystal.gui.FluidMixerScreen;
import com.villfuk02.qrystal.init.*;
import com.villfuk02.qrystal.network.Networking;
import com.villfuk02.qrystal.renderers.*;
import com.villfuk02.qrystal.tileentity.*;
import com.villfuk02.qrystal.util.handlers.CondensedMaterialColorHandler;
import com.villfuk02.qrystal.util.handlers.CrystalColorHandler;
import com.villfuk02.qrystal.util.handlers.SurfaceColorHandler;
import com.villfuk02.qrystal.world.OreGeneration;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "qrystal";
    
    public static final ItemGroup MOD_ITEM_GROUP = new QrystalItemGroup();
    
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static Main instance;
    
    public static boolean addedServerListeners = false;
    
    public Main() {
        instance = this;
        
        QrystalConfig.loadDefaults(FMLPaths.CONFIGDIR.get().resolve("qrystal.toml").toString(), QrystalConfig.SPEC);
        QrystalConfig.bake();
        QrystalConfig.rebuild();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, QrystalConfig.SPEC, "qrystal.toml");
        
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModContainerTypes.CONTAINER_TYPES.register(modEventBus);
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Main::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Main::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Main::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Main::clientRegistries);
        
        MinecraftForge.EVENT_BUS.addListener(Main::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(Main::onServerAboutToStart);
        
        MinecraftForge.EVENT_BUS.register(this);
        Networking.registerMessages();
    }
    
    private static void setup(FMLCommonSetupEvent event) {
        LOGGER.info("QRYSTAL setup");
        OreGeneration.setupOreGeneration();
    }
    
    
    private static void clientRegistries(FMLClientSetupEvent event) {
        for(Item i : ModItems.CRYSTALS.values()) {
            Minecraft.getInstance().getItemColors().register(new CrystalColorHandler(), i);
        }
        for(Item i : ModItems.DUSTS.values()) {
            Minecraft.getInstance().getItemColors().register(new CrystalColorHandler(), i);
        }
        Minecraft.getInstance().getItemColors().register(new SurfaceColorHandler(), ModItems.SURFACE_RENDERER);
        Minecraft.getInstance().getItemColors().register(new CondensedMaterialColorHandler(), ModItems.CONDENSED_MATERIAL_CAGE_RENDERER);
        
        ClientRegistry.bindTileEntityRenderer(ModTileEntityTypes.DRYER, DryerTileEntityRenderer::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntityTypes.STEEL_CUTTER, CutterTileEntityRenderer::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntityTypes.DIAMOND_CUTTER, CutterTileEntityRenderer::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntityTypes.LASER_CUTTER, LaserCutterTileEntityRenderer::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntityTypes.CONDENSING_BARREL, CondensingBarrelTileEntityRenderer::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntityTypes.EMITTER, EmitterTileEntityRenderer::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntityTypes.RESERVOIR, ReservoirTileEntityRenderer::new);
        
        RenderTypeLookup.setRenderLayer(ModBlocks.RESERVOIR, RenderType.getTranslucent());
        
        
        DeferredWorkQueue.runLater(() -> {
            ScreenManager.registerFactory(ModContainerTypes.CUTTER.get(), CutterScreen::new);
            ScreenManager.registerFactory(ModContainerTypes.FLUID_MIXER.get(), FluidMixerScreen::new);
            ScreenManager.registerFactory(ModContainerTypes.EVAPORATOR.get(), EvaporatorScreen::new);
            LOGGER.debug("Registered Container Screens");
        });
    }
    
    private static void enqueueIMC(InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }
    
    private static void processIMC(InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    
    public static void onServerStarting(FMLServerStartingEvent event) {
        //Hammer.getEffectiveOn(event.getServer().getWorld(DimensionType.OVERWORLD).getWorld());
        LOGGER.info("Initialized qrystal hammers");
    }
    
    public static void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        if(!addedServerListeners) {
            event.getServer().getResourceManager().addReloadListener(new MaterialManager());
            event.getServer().getResourceManager().addReloadListener(new FluidTierManager());
            addedServerListeners = true;
        }
    }
    
    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            ModBlocks.init();
            event.getRegistry().registerAll(ModBlocks.BLOCKS.toArray(new Block[0]));
        }
        
        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            ModItems.init();
            event.getRegistry().registerAll(ModItems.ITEMS.toArray(new Item[0]));
        }
        
        @SubscribeEvent
        public static void registerFluids(RegistryEvent.Register<Fluid> event) {
            event.getRegistry().registerAll(ModFluids.FLUIDS.toArray(new Fluid[0]));
        }
        
        @SubscribeEvent
        public static void registerRecipes(RegistryEvent.Register<IRecipeSerializer<?>> event) {
            event.getRegistry().register(new UncondensingRecipe.Serializer().setRegistryName(MODID, "uncondensing"));
            event.getRegistry().register(new DustCombiningRecipe.Serializer().setRegistryName(MODID, "dust_combining"));
            event.getRegistry().register(new DustSplittingRecipe.Serializer().setRegistryName(MODID, "dust_splitting"));
            event.getRegistry().register(new CustomCuttingRecipe.Serializer().setRegistryName(MODID, "cutting"));
            event.getRegistry().register(new FluidMixingRecipe.Serializer().setRegistryName(MODID, "fluid_mixing"));
            event.getRegistry().register(new ActivationRecipe.Serializer().setRegistryName(MODID, "activation"));
        }
        
        @SubscribeEvent
        public static void onRegisterTileEntityTypes(RegistryEvent.Register<TileEntityType<?>> event) {
            event.getRegistry().register(TileEntityType.Builder.create(DryerTileEntity::new, ModBlocks.DRYER).build(null).setRegistryName(MODID, "dryer"));
            event.getRegistry().register(TileEntityType.Builder.create(SteelCutterTileEntity::new, ModBlocks.STEEL_CUTTER).build(null).setRegistryName(MODID, "steel_cutter"));
            event.getRegistry().register(TileEntityType.Builder.create(DiamondCutterTileEntity::new, ModBlocks.DIAMOND_CUTTER).build(null).setRegistryName(MODID, "diamond_cutter"));
            event.getRegistry().register(TileEntityType.Builder.create(LaserCutterTileEntity::new, ModBlocks.LASER_CUTTER).build(null).setRegistryName(MODID, "laser_cutter"));
            event.getRegistry().register(TileEntityType.Builder.create(BurnerFluidMixerTileEntity::new, ModBlocks.BURNER_FLUID_MIXER).build(null).setRegistryName(MODID, "burner_fluid_mixer"));
            event.getRegistry().register(TileEntityType.Builder.create(PoweredFluidMixerTileEntity::new, ModBlocks.POWERED_FLUID_MIXER).build(null).setRegistryName(MODID, "powered_fluid_mixer"));
            event.getRegistry().register(TileEntityType.Builder.create(UltimateFluidMixerTileEntity::new, ModBlocks.ULTIMATE_FLUID_MIXER).build(null).setRegistryName(MODID, "ultimate_fluid_mixer"));
            event.getRegistry().register(TileEntityType.Builder.create(BasicEvaporatorTileEntity::new, ModBlocks.BASIC_EVAPORATOR).build(null).setRegistryName(MODID, "basic_evaporator"));
            event.getRegistry().register(TileEntityType.Builder.create(BurnerEvaporatorTileEntity::new, ModBlocks.BURNER_EVAPORATOR).build(null).setRegistryName(MODID, "burner_evaporator"));
            event.getRegistry().register(TileEntityType.Builder.create(PoweredEvaporatorTileEntity::new, ModBlocks.POWERED_EVAPORATOR).build(null).setRegistryName(MODID, "powered_evaporator"));
            event.getRegistry().register(TileEntityType.Builder.create(UltimateEvaporatorTileEntity::new, ModBlocks.ULTIMATE_EVAPORATOR).build(null).setRegistryName(MODID, "ultimate_evaporator"));
            event.getRegistry()
                    .register(TileEntityType.Builder.create(CondensingBarrelTileEntity::new, ModBlocks.WOODEN_CONDENSING_BARREL, ModBlocks.STONE_CONDENSING_BARREL, ModBlocks.IRON_CONDENSING_BARREL,
                                                            ModBlocks.GOLD_CONDENSING_BARREL, ModBlocks.IMBUED_CONDENSING_BARREL, ModBlocks.STEEL_CONDENSING_BARREL, ModBlocks.DIAMOND_CONDENSING_BARREL,
                                                            ModBlocks.EMERALD_CONDENSING_BARREL, ModBlocks.ENDSTEEL_CONDENSING_BARREL).build(null).setRegistryName(MODID, "condensing_barrel"));
            event.getRegistry().register(TileEntityType.Builder.create(EmitterTileEntity::new, ModBlocks.EMITTER_0, ModBlocks.EMITTER_1, ModBlocks.EMITTER_2).build(null).setRegistryName(MODID, "emitter"));
            event.getRegistry().register(TileEntityType.Builder.create(ReservoirTileEntity::new, ModBlocks.RESERVOIR).build(null).setRegistryName(MODID, "reservoir"));
        }
        
    }
    
    
}
