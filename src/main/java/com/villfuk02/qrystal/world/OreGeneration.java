package com.villfuk02.qrystal.world;

import com.villfuk02.qrystal.QrystalConfig;
import com.villfuk02.qrystal.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.registries.ForgeRegistries;

public class OreGeneration {
    
    
    public static void setupOreGeneration() {
        QrystalPlacement.PlacementConfig qlearOreCfg = new QrystalPlacement.PlacementConfig(QrystalConfig.ore_spawn_tries_qlear, 7, 62, "peaked", 26);
        QrystalPlacement.PlacementConfig oreCfg = new QrystalPlacement.PlacementConfig(QrystalConfig.ore_spawn_tries, 7, 48, "peaked", 17);
        QrystalPlacement.PlacementConfig pebbleCfg = new QrystalPlacement.PlacementConfig(QrystalConfig.pebbles_spawn_tries, 30, 120, "peaked", 60);
        QrystalPlacement.PlacementConfig richQeriCfg = new QrystalPlacement.PlacementConfig(QrystalConfig.ore_spawn_tries_rich * 3, 100, 200, "surface", 2);
        QrystalPlacement.PlacementConfig richQawaCfg = new QrystalPlacement.PlacementConfig(QrystalConfig.ore_spawn_tries_rich, 30, 40, "ocean_floor", 2);
        QrystalPlacement.PlacementConfig richQiniCfg = new QrystalPlacement.PlacementConfig(QrystalConfig.ore_spawn_tries_rich * 5, 4, 20, "lava", 0);
        QrystalPlacement.PlacementConfig richSecondTierCfg = new QrystalPlacement.PlacementConfig(QrystalConfig.ore_spawn_tries_rich, 20, 40, "", 0);
        
        ForgeRegistries.BIOMES.getValues()
                .stream()
                .filter(biome -> biome.getCategory() != Biome.Category.THEEND)
                .filter(biome -> biome.getCategory() != Biome.Category.NETHER)
                .forEach(biome -> {
                    addOre(biome, ModBlocks.QLEAR_ORE, QrystalConfig.ore_vein_size, qlearOreCfg);
                    
                    addOre(biome, ModBlocks.QERI_ORE, QrystalConfig.ore_vein_size, oreCfg);
                    addOre(biome, ModBlocks.QAWA_ORE, QrystalConfig.ore_vein_size, oreCfg);
                    addOre(biome, ModBlocks.QINI_ORE, QrystalConfig.ore_vein_size, oreCfg);
                    
                    addOre(biome, ModBlocks.PEBBLE_BLOCK, 16, pebbleCfg);
                    
                    addOre(biome, ModBlocks.QERI_ORE_RICH, QrystalConfig.ore_vein_size_rich, richQeriCfg);
                    addOre(biome, ModBlocks.QAWA_ORE_RICH, QrystalConfig.ore_vein_size_rich, richQawaCfg);
                    addOre(biome, ModBlocks.QINI_ORE_RICH, QrystalConfig.ore_vein_size_rich, richQiniCfg);
                    
                    if(biome.getCategory() == Biome.Category.JUNGLE)
                        addOre(biome, ModBlocks.QITAE_ORE_RICH, QrystalConfig.ore_vein_size_rich, richSecondTierCfg);
                    if(biome.getCategory() == Biome.Category.SWAMP || biome.getCategory() == Biome.Category.MUSHROOM)
                        addOre(biome, ModBlocks.QOID_ORE_RICH, QrystalConfig.ore_vein_size_rich, richSecondTierCfg);
                    if(biome.getCategory() == Biome.Category.MESA)
                        addOre(biome, ModBlocks.QONDO_ORE_RICH, QrystalConfig.ore_vein_size_rich, richSecondTierCfg);
                });
        
        
    }
    
    public static void addOre(Biome biome, Block block, int size, QrystalPlacement.PlacementConfig cfg) {
        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
                         Feature.ORE.withConfiguration(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, block.getDefaultState(), size))
                                 .withPlacement(new QrystalPlacement(QrystalPlacement.PlacementConfig::deserialize).configure(cfg)));
        
    }
    
    
}
