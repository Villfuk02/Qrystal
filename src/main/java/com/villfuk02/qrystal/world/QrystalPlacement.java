package com.villfuk02.qrystal.world;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class QrystalPlacement extends Placement<QrystalPlacement.PlacementConfig> {
    
    
    public QrystalPlacement(Function<Dynamic<?>, ? extends PlacementConfig> configFactoryIn) {
        super(configFactoryIn);
    }
    
    @Override
    public Stream<BlockPos> getPositions(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generatorIn, Random random, PlacementConfig configIn,
                                         BlockPos blockPos) {
        List<BlockPos> positions = IntStream.range(0, configIn.count)
                .mapToObj((pos) -> new BlockPos(blockPos.getX() + random.nextInt(16), 0, blockPos.getZ() + random.nextInt(16)))
                .collect(Collectors.toList());
        int randY = configIn.minY + random.nextInt(configIn.maxY - configIn.minY + 1);
        switch(configIn.mode) {
            default:
                return positions.stream().map(b -> b.up(randY));
            case "peaked":
                return positions.stream().map(b -> mapPeaked(b, random, configIn.minY, configIn.maxY, configIn.arg));
            case "lava":
                return positions.stream().map(b -> mapLava(b, worldIn, configIn.minY, randY)).filter(Objects::nonNull);
            case "surface":
                return positions.stream().map(b -> mapSurface(b, worldIn, configIn.minY, configIn.maxY, configIn.arg)).filter(Objects::nonNull);
            case "ocean_floor":
                return positions.stream().map(b -> mapOceanFloor(b, worldIn, configIn.minY, configIn.maxY, configIn.arg)).filter(Objects::nonNull);
        }
    }
    
    public static class PlacementConfig implements IPlacementConfig {
        public final int count;
        public final int minY;
        public final int maxY;
        public final String mode;
        public final int arg;
        
        public PlacementConfig(int count, int minY, int maxY, String mode, int arg) {
            this.count = count;
            this.minY = minY;
            this.maxY = maxY;
            this.mode = mode;
            this.arg = arg;
        }
        
        @Override
        public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
            return new Dynamic<>(ops, ops.createMap(
                    ImmutableMap.of(ops.createString("count"), ops.createInt(count), ops.createString("minY"), ops.createInt(minY), ops.createString("maxY"),
                                    ops.createInt(maxY), ops.createString("mode"), ops.createString(mode), ops.createString("arg"), ops.createInt(arg))));
        }
        
        public static PlacementConfig deserialize(Dynamic<?> p_214732_0_) {
            int f = p_214732_0_.get("count").asInt(0);
            int i = p_214732_0_.get("minY").asInt(0);
            int j = p_214732_0_.get("maxY").asInt(0);
            String s = p_214732_0_.get("mode").asString(null);
            int a = p_214732_0_.get("arg").asInt(0);
            return new PlacementConfig(f, i, j, s, a);
        }
    }
    
    public static BlockPos mapLava(BlockPos b, IWorld worldIn, int minY, int randY) {
        boolean lava = false;
        for(int y = randY + 1; y >= minY; y--) {
            if(worldIn.getBlockState(b.up(y)).getMaterial() == Material.LAVA) {
                lava = true;
            } else if(lava) {
                return b.up(y);
            }
        }
        return null;
    }
    
    public static BlockPos mapPeaked(BlockPos b, Random random, int minY, int maxY, int peak) {
        int m = (maxY - peak) / (peak - minY - 3) + 2;
        int y = peak + (random.nextInt(m) - 1) * random.nextInt(peak - minY - 2) + random.nextInt(5) + random.nextInt(3) - 2;
        return b.up(y);
    }
    
    public static BlockPos mapSurface(BlockPos b, IWorld worldIn, int minY, int maxY, int depth) {
        int y = worldIn.getHeight(Heightmap.Type.WORLD_SURFACE_WG, b.getX(), b.getZ());
        if(y >= minY && y <= maxY) {
            return b.up(y - depth);
        }
        return null;
    }
    
    public static BlockPos mapOceanFloor(BlockPos b, IWorld worldIn, int minY, int maxY, int depth) {
        int y = worldIn.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, b.getX(), b.getZ());
        if(y >= minY && y <= maxY) {
            return b.up(y - depth);
        }
        return null;
    }
}