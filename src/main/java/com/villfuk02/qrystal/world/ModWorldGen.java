package com.villfuk02.qrystal.world;

import com.villfuk02.qrystal.Main;
import com.villfuk02.qrystal.blocks.QrystalOre;
import com.villfuk02.qrystal.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class ModWorldGen implements IWorldGenerator {
    
    static final int MIN_HEIGHT = 7;
    static final int PEAK_HEIGHT = 17;
    static final int MAX_HEIGHT = 48;
    static final int TRIES = 1;
    static final int MIN_SIZE = 8;
    static final int MAX_SIZE = 16;
    
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if(world.provider.getDimension() == 0)
            generateOverworld(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
    }
    
    private void generateOverworld(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        generateQrystalOre(ModBlocks.PEBBLE_BLOCK.getDefaultState(), random, chunkX * 16, chunkZ * 16, world, 3, 30, 60, 120, 15, 20);
        
        generateQrystalOre(ModBlocks.QRYSTAL_ORE.getDefaultState().withProperty(Main.qrystalColor, Main.EnumColor.QERI).withProperty(QrystalOre.rich, false), random,
                           chunkX * 16, chunkZ * 16, world, TRIES, MIN_HEIGHT, PEAK_HEIGHT, MAX_HEIGHT, MIN_SIZE, MAX_SIZE);
        generateQrystalOre(ModBlocks.QRYSTAL_ORE.getDefaultState().withProperty(Main.qrystalColor, Main.EnumColor.QAWA).withProperty(QrystalOre.rich, false), random,
                           chunkX * 16, chunkZ * 16, world, TRIES, MIN_HEIGHT, PEAK_HEIGHT, MAX_HEIGHT, MIN_SIZE, MAX_SIZE);
        generateQrystalOre(ModBlocks.QRYSTAL_ORE.getDefaultState().withProperty(Main.qrystalColor, Main.EnumColor.QINI).withProperty(QrystalOre.rich, false), random,
                           chunkX * 16, chunkZ * 16, world, TRIES, MIN_HEIGHT, PEAK_HEIGHT, MAX_HEIGHT, MIN_SIZE, MAX_SIZE);
        /*
        generateQrystalOre(ModBlocks.QRYSTAL_ORE.getDefaultState().withProperty(Main.qrystalColor, Main.EnumColor.QITAE).withProperty(QrystalOre.rich, true), random,
                           chunkX * 16, chunkZ * 16, world, TRIES, MIN_HEIGHT, PEAK_HEIGHT, MAX_HEIGHT, MIN_SIZE, MAX_SIZE);
        generateQrystalOre(ModBlocks.QRYSTAL_ORE.getDefaultState().withProperty(Main.qrystalColor, Main.EnumColor.QALB).withProperty(QrystalOre.rich, true), random,
                           chunkX * 16, chunkZ * 16, world, TRIES, MIN_HEIGHT, PEAK_HEIGHT, MAX_HEIGHT, MIN_SIZE, MAX_SIZE);                           */
    }
    
    private void generateQrystalOre(IBlockState ore, Random random, int x, int z, World world, int tries, int minY, int peak, int maxY, int minS, int maxS) {
        WorldGenMinable generator = new WorldGenMinable(ore, minS + random.nextInt(maxS - minS + 1));
        for(int i = 0; i < tries; i++) {
            int m = (maxY - peak) / (peak - minY - 3) + 2;
            int y = peak + (random.nextInt(m) - 1) * random.nextInt(peak - minY - 2) + random.nextInt(5) + random.nextInt(3) - 2;
            BlockPos pos = new BlockPos(x + random.nextInt(16), y, z + random.nextInt(16));
            generator.generate(world, random, pos);
        }
    }
}
