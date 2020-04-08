package com.villfuk02.qrystal;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.List;

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
    public static double material_dust_multiplier;
    public static double yield_tier_multiplier;
    public static double large_tier_multiplier;
    public static double large_base_multiplier;
    
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
        material_dust_multiplier = parseFraction(CONFIG.material_dust_multiplier.get(), 0.25d, 4);
        yield_tier_multiplier = parseFraction(CONFIG.yield_tier_multiplier.get(), 1, 4);
        large_base_multiplier = parseFraction(CONFIG.large_base_multiplier.get(), 1, 4);
        large_tier_multiplier = parseFraction(CONFIG.large_tier_multiplier.get(), 1, 1.25d);
    }
    
    public static double parseFraction(String s, double min, double max) {
        String[] v = s.split("/");
        double d;
        if(v.length < 2)
            d = Double.parseDouble(s);
        else
            d = Double.parseDouble(v[0]) / Double.parseDouble(v[1]);
        if(d > max) {
            Main.LOGGER.error("Config value " + d + " is out of range!");
            return max;
        }
        if(d < min) {
            Main.LOGGER.error("Config value " + d + " is out of range!");
            return min;
        }
        return d;
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
        ForgeConfigSpec.ConfigValue<String> material_dust_multiplier;
        ForgeConfigSpec.ConfigValue<String> yield_tier_multiplier;
        ForgeConfigSpec.ConfigValue<String> large_tier_multiplier;
        ForgeConfigSpec.ConfigValue<String> large_base_multiplier;
        
        public ForgeConfigSpec.ConfigValue<List<? extends String>> material_strings;
        
        public Config(ForgeConfigSpec.Builder builder) {
            builder.comment(baked ? "" : "WARNING: Global configs are used when loading a world for the first time. For adjusting the config for an existing world, go to the world's serverconfig folder.")
                    .push("QRYSTAL");
            
            builder.push("WorldGen");
            ore_vein_size = builder.comment("The size of qrystal ore veins").defineInRange("ore_vein_size", baked ? QrystalConfig.ore_vein_size : 9, 0, 64);
            ore_vein_size_rich = builder.comment("The size of rich qrystal ore veins").defineInRange("ore_vein_size_rich", baked ? QrystalConfig.ore_vein_size_rich : 5, 0, 64);
            
            pebbles_spawn_tries = builder.comment("Number of potential pebble block veins per chunk").defineInRange("pebbles_spawn_tries", baked ? QrystalConfig.pebbles_spawn_tries : 0, 0, 64);
            ore_spawn_tries_qlear = builder.comment("Number of potential qlear qrystal ore veins per chunk").defineInRange("ore_spawn_tries_qlear", baked ? QrystalConfig.ore_spawn_tries_qlear : 2, 0, 64);
            ore_spawn_tries = builder.comment("Number of potential qrystal ore veins per chunk").defineInRange("ore_spawn_tries", baked ? QrystalConfig.ore_spawn_tries : 1, 0, 64);
            ore_spawn_tries_rich = builder.comment("Number of potential rich qrystal ore veins per chunk").defineInRange("ore_spawn_tries_rich", baked ? QrystalConfig.ore_vein_size_rich : 2, 0, 64);
            builder.pop();
            
            builder.comment("(string fraction) accepts a string of a value in decimal form or a fraction with /").push("Multipliers");
            material_tier_multiplier = builder.comment("How many times more material is needed for next tier (multiplicative)")
                    .defineInRange("material_tier_multiplier", baked ? QrystalConfig.material_tier_multiplier : 4, 2, 12);
            material_dust_multiplier = builder.comment("How many times more dust you get from crushing ores (string fraction, range 0.25 ~ 4)")
                    .define("material_dust_multiplier", baked ? Double.toString(QrystalConfig.material_dust_multiplier) : "4/3");
            yield_tier_multiplier = builder.comment("How many times more material you get for every tier (multiplicative, string fraction, range 1 ~ 4)")
                    .define("yield_tier_multiplier", baked ? Double.toString(QrystalConfig.yield_tier_multiplier) : "1.45");
            large_base_multiplier = builder.comment("How many times more medium crystals you get from a large qrystal crystal (string fraction, range 1 ~ 4)")
                    .define("large_base_multiplier", baked ? Double.toString(QrystalConfig.large_base_multiplier) : "2");
            large_tier_multiplier = builder.comment("How many times more medium crystals you get from a large qrystal crystal for every tier (multiplicative, string fraction, range 1 ~ 1.25)")
                    .define("large_tier_multiplier", baked ? Double.toString(QrystalConfig.large_tier_multiplier) : "1.1");
            builder.pop();
            builder.pop();
        }
        
    }
    
    
}
