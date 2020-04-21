package com.villfuk02.qrystal;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class QrystalConfig {
    
    public static Config CONFIG;
    public static ForgeConfigSpec SPEC;
    
    static {
        Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
        SPEC = specPair.getRight();
        CONFIG = specPair.getLeft();
    }
    
    public static boolean baked = false;
    
    public static int ore_vein_size;
    public static int ore_vein_size_rich;
    
    public static int pebbles_spawn_tries;
    public static int ore_spawn_tries_qlear;
    public static int ore_spawn_tries;
    public static int ore_spawn_tries_rich;
    
    public static int material_tier_multiplier;
    public static double base_yield_multiplier;
    public static double yield_tier_multiplier;
    public static double qrystal_yield_multiplier;
    public static double base_seed_chance;
    
    public static void rebuild() {
        Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
        SPEC = specPair.getRight();
        CONFIG = specPair.getLeft();
    }
    
    
    public static void bake() {
        baked = true;
        ore_vein_size = CONFIG.ore_vein_size.get();
        ore_vein_size_rich = CONFIG.ore_vein_size_rich.get();
        
        pebbles_spawn_tries = CONFIG.pebbles_spawn_tries.get();
        ore_spawn_tries_qlear = CONFIG.ore_spawn_tries_qlear.get();
        ore_spawn_tries = CONFIG.ore_spawn_tries.get();
        ore_spawn_tries_rich = CONFIG.ore_spawn_tries_rich.get();
        
        material_tier_multiplier = CONFIG.material_tier_multiplier.get();
        base_yield_multiplier = CONFIG.base_yield_multiplier.get();
        yield_tier_multiplier = CONFIG.yield_tier_multiplier.get();
        qrystal_yield_multiplier = CONFIG.qrystal_yield_multiplier.get();
        base_seed_chance = CONFIG.base_seed_chance.get();
    }
    
    @SubscribeEvent
    public static void onModConfigEvent(ModConfig.ModConfigEvent configEvent) {
        if(configEvent.getConfig().getSpec() == SPEC) {
            bake();
        }
    }
    
    public static void loadDefaults(String path, ForgeConfigSpec spec) {
        CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
        file.load();
        spec.setConfig(file);
    }
    
    public static class Config {
        
        public ForgeConfigSpec.IntValue ore_vein_size;
        public ForgeConfigSpec.IntValue ore_vein_size_rich;
        
        public ForgeConfigSpec.IntValue pebbles_spawn_tries;
        public ForgeConfigSpec.IntValue ore_spawn_tries_qlear;
        public ForgeConfigSpec.IntValue ore_spawn_tries;
        public ForgeConfigSpec.IntValue ore_spawn_tries_rich;
        
        public ForgeConfigSpec.IntValue material_tier_multiplier;
        public ForgeConfigSpec.DoubleValue base_yield_multiplier;
        public ForgeConfigSpec.DoubleValue yield_tier_multiplier;
        public ForgeConfigSpec.DoubleValue qrystal_yield_multiplier;
        public ForgeConfigSpec.DoubleValue base_seed_chance;
        
        public Config(ForgeConfigSpec.Builder builder) {
            builder.comment(baked ? "" : "WARNING: Global configs are used when loading a world for the first time. For adjusting the config for an existing world, go to the world's serverconfig folder.")
                    .push("Qrystal");
            
            builder.push("WorldGen");
            ore_vein_size = builder.comment("The size of qrystal ore veins").defineInRange("ore_vein_size", baked ? QrystalConfig.ore_vein_size : 9, 0, 64);
            ore_vein_size_rich = builder.comment("The size of rich qrystal ore veins").defineInRange("ore_vein_size_rich", baked ? QrystalConfig.ore_vein_size_rich : 5, 0, 64);
            
            pebbles_spawn_tries = builder.comment("Number of potential pebble block veins per chunk").defineInRange("pebbles_spawn_tries", baked ? QrystalConfig.pebbles_spawn_tries : 0, 0, 64);
            ore_spawn_tries_qlear = builder.comment("Number of potential qlear qrystal ore veins per chunk").defineInRange("ore_spawn_tries_qlear", baked ? QrystalConfig.ore_spawn_tries_qlear : 2, 0, 64);
            ore_spawn_tries = builder.comment("Number of potential qrystal ore veins per chunk").defineInRange("ore_spawn_tries", baked ? QrystalConfig.ore_spawn_tries : 1, 0, 64);
            ore_spawn_tries_rich = builder.comment("Number of potential rich qrystal ore veins per chunk").defineInRange("ore_spawn_tries_rich", baked ? QrystalConfig.ore_vein_size_rich : 2, 0, 64);
            builder.pop();
            
            builder.push("Multipliers");
            material_tier_multiplier = builder.comment("How many times more material is needed for next tier (multiplicative)")
                    .defineInRange("material_tier_multiplier", baked ? QrystalConfig.material_tier_multiplier : 4, 2, 12);
            base_yield_multiplier = builder.comment("How many ingots do you get from one Dust or Medium Triangular Crystal (assuming 100% efficiency)")
                    .defineInRange("base_yield_multiplier", baked ? QrystalConfig.base_yield_multiplier : 1.667, 1, 16);
            yield_tier_multiplier = builder.comment("How many times more material you get for every tier (multiplicative)")
                    .defineInRange("yield_tier_multiplier", baked ? QrystalConfig.yield_tier_multiplier : 1.415, 1, 4);
            qrystal_yield_multiplier = builder.comment("How many times more material you get for every tier (multiplicative)")
                    .defineInRange("qrystal_yield_multiplier", baked ? QrystalConfig.qrystal_yield_multiplier : 1.2, 1, 2);
            base_seed_chance = builder.comment("Chance for getting seeds of higher tier from a large crystal").defineInRange("base_seed_chance", baked ? QrystalConfig.base_seed_chance : 0.2, 0, 100);
            builder.pop();
            builder.pop();
        }
        
    }
    
    
}
