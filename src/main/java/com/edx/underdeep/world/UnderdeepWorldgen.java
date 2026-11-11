package com.edx.underdeep.world;

import com.edx.underdeep.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transitional cave/deepslate pass for the original 0..255 world.
 *
 * The cave field is evaluated in absolute world coordinates, so tunnels and
 * chambers continue across chunk borders instead of being clipped to a chunk.
 * This class deliberately does NOT claim to provide negative Y support.
 */
public final class UnderdeepWorldgen {
    private static final String PROCESSED_KEY = "underdeep_terrain_v2";
    private static final int MIN_CAVE_Y = 5;
    private static final int MAX_CAVE_Y = 62;
    private static final int DEEPSLATE_TRANSITION_Y = 44;
    private static final int ROOM_GRID_SIZE = 64;

    private static final Map<World, Set<Long>> PROCESSED_CHUNKS =
            Collections.synchronizedMap(new WeakHashMap<World, Set<Long>>());

    private static World getOverworld(IWorld candidate) {
        if (!(candidate instanceof World)) {
            return null;
        }
        World world = (World) candidate;
        return !world.isRemote && world.getDimensionKey().equals(World.OVERWORLD) ? world : null;
    }

    private static Set<Long> processedChunks(World world) {
        synchronized (PROCESSED_CHUNKS) {
            Set<Long> chunks = PROCESSED_CHUNKS.get(world);
            if (chunks == null) {
                chunks = ConcurrentHashMap.newKeySet();
                PROCESSED_CHUNKS.put(world, chunks);
            }
            return chunks;
        }
    }

    private static long chunkKey(ChunkPos pos) {
        return ((long) pos.x << 32) ^ (pos.z & 0xffffffffL);
    }

    @SubscribeEvent
    public void onChunkDataLoad(ChunkDataEvent.Load event) {
        World world = getOverworld(event.getWorld());
        if (world == null) {
            return;
        }
        if (event.getData().getBoolean(PROCESSED_KEY)) {
            processedChunks(world).add(chunkKey(event.getChunk().getPos()));
        }
    }

    @SubscribeEvent
    public void onChunkDataSave(ChunkDataEvent.Save event) {
        World world = getOverworld(event.getWorld());
        if (world == null) {
            return;
        }
        if (processedChunks(world).contains(chunkKey(event.getChunk().getPos()))) {
            event.getData().putBoolean(PROCESSED_KEY, true);
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        World world = getOverworld(event.getWorld());
        if (world == null) {
            return;
        }

        IChunk chunk = event.getChunk();
        if (!processedChunks(world).add(chunkKey(chunk.getPos()))) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        int startX = chunkPos.x << 4;
        int startZ = chunkPos.z << 4;
        long worldSeed = world.getWorldInfo().getSeed();
        long chunkSeed = mix64(worldSeed
                ^ ((long) chunkPos.x * 341873128712L)
                ^ ((long) chunkPos.z * 132897987541L));

        convertDeepStoneAndPlaceOre(chunk, startX, startZ, chunkSeed);
        carveCaves(chunk, startX, startZ, worldSeed);
    }

    private static void convertDeepStoneAndPlaceOre(IChunk chunk, int startX, int startZ, long seed) {
        Random random = new Random(seed);

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = startX + localX;
                int worldZ = startZ + localZ;

                for (int y = 1; y <= MAX_CAVE_Y; y++) {
                    BlockPos pos = new BlockPos(worldX, y, worldZ);
                    BlockState state = chunk.getBlockState(pos);
                    if (state.getBlock() != Blocks.STONE) {
                        continue;
                    }

                    // A gradual transition keeps some ordinary stone above the deep layer.
                    if (y <= DEEPSLATE_TRANSITION_Y || random.nextFloat() < 0.25F) {
                        chunk.setBlockState(pos, ModBlocks.DEEPSLATE.get().getDefaultState(), false);
                    }

                    // Sparse ore placement; cave carving happens afterwards.
                    if (y >= 4 && y <= 38 && random.nextInt(1200) == 0) {
                        chunk.setBlockState(pos, ModBlocks.DEEPSLATE_IRON_ORE.get().getDefaultState(), false);
                    }
                }
            }
        }
    }

    private static void carveCaves(IChunk chunk, int startX, int startZ, long worldSeed) {
        List<CaveRoom> rooms = createRooms(startX, startZ, worldSeed);
        long tunnelSeedA = mix64(worldSeed ^ 0x6A09E667F3BCC909L);
        long tunnelSeedB = mix64(worldSeed ^ 0xBB67AE8584CAA73BL);

        for (int localX = 0; localX < 16; localX++) {
            int worldX = startX + localX;

            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = startZ + localZ;

                for (int y = MIN_CAVE_Y; y <= MAX_CAVE_Y; y++) {
                    BlockPos pos = new BlockPos(worldX, y, worldZ);
                    BlockState state = chunk.getBlockState(pos);
                    if (!isUnderdeepStone(state)) {
                        continue;
                    }

                    boolean chamber = insideAnyRoom(worldX, y, worldZ, rooms);
                    boolean tunnel = isTunnel(worldX, y, worldZ, tunnelSeedA, tunnelSeedB);
                    if (chamber || tunnel) {
                        chunk.setBlockState(pos, Blocks.AIR.getDefaultState(), false);
                    }
                }
            }
        }
    }

    private static boolean isUnderdeepStone(BlockState state) {
        return state.getBlock() == Blocks.STONE
                || state.getBlock() == ModBlocks.DEEPSLATE.get()
                || state.getBlock() == ModBlocks.DEEPSLATE_IRON_ORE.get();
    }

    /**
     * Build rooms on a world-aligned 64-block grid. Each chunk calculates the
     * neighboring rooms too, which prevents visible cuts at chunk boundaries.
     */
    private static List<CaveRoom> createRooms(int startX, int startZ, long worldSeed) {
        List<CaveRoom> rooms = new ArrayList<CaveRoom>();

        int minCellX = Math.floorDiv(startX - 22, ROOM_GRID_SIZE);
        int maxCellX = Math.floorDiv(startX + 15 + 22, ROOM_GRID_SIZE);
        int minCellZ = Math.floorDiv(startZ - 22, ROOM_GRID_SIZE);
        int maxCellZ = Math.floorDiv(startZ + 15 + 22, ROOM_GRID_SIZE);

        for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
            for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
                long cellSeed = mix64(worldSeed
                        ^ ((long) cellX * 0x9E3779B97F4A7C15L)
                        ^ ((long) cellZ * 0xC2B2AE3D27D4EB4FL));
                Random random = new Random(cellSeed);
                if (random.nextInt(3) != 0) {
                    continue;
                }

                int centerX = cellX * ROOM_GRID_SIZE + 32 + random.nextInt(17) - 8;
                int centerZ = cellZ * ROOM_GRID_SIZE + 32 + random.nextInt(17) - 8;
                int centerY = 10 + random.nextInt(42);
                int radiusX = 11 + random.nextInt(8);
                int radiusY = 5 + random.nextInt(6);
                int radiusZ = 11 + random.nextInt(8);

                rooms.add(new CaveRoom(centerX, centerY, centerZ, radiusX, radiusY, radiusZ));
            }
        }

        return rooms;
    }

    private static boolean insideAnyRoom(int x, int y, int z, List<CaveRoom> rooms) {
        for (CaveRoom room : rooms) {
            if (room.contains(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Two smooth 3D noise fields create winding tunnels. Because the noise is
     * sampled from absolute coordinates, neighboring chunks agree on the cut.
     */
    private static boolean isTunnel(int x, int y, int z, long seedA, long seedB) {
        double nx = x * 0.026D;
        double ny = y * 0.034D;
        double nz = z * 0.026D;

        double first = fractalNoise(nx, ny, nz, seedA);
        double second = fractalNoise(nx + 71.3D, ny - 29.7D, nz + 113.9D, seedB);
        return Math.abs(first) < 0.105D && Math.abs(second) < 0.105D;
    }

    private static double fractalNoise(double x, double y, double z, long seed) {
        double total = 0.0D;
        double amplitude = 1.0D;
        double normalizer = 0.0D;
        double frequency = 1.0D;

        for (int octave = 0; octave < 3; octave++) {
            total += valueNoise(x * frequency, y * frequency, z * frequency,
                    seed + octave * 0x9E3779B97F4A7C15L) * amplitude;
            normalizer += amplitude;
            amplitude *= 0.5D;
            frequency *= 2.0D;
        }

        return total / normalizer;
    }

    private static double valueNoise(double x, double y, double z, long seed) {
        int x0 = floor(x);
        int y0 = floor(y);
        int z0 = floor(z);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;

        double tx = smooth(x - x0);
        double ty = smooth(y - y0);
        double tz = smooth(z - z0);

        double c00 = lerp(hashNoise(x0, y0, z0, seed), hashNoise(x1, y0, z0, seed), tx);
        double c10 = lerp(hashNoise(x0, y1, z0, seed), hashNoise(x1, y1, z0, seed), tx);
        double c01 = lerp(hashNoise(x0, y0, z1, seed), hashNoise(x1, y0, z1, seed), tx);
        double c11 = lerp(hashNoise(x0, y1, z1, seed), hashNoise(x1, y1, z1, seed), tx);

        double c0 = lerp(c00, c10, ty);
        double c1 = lerp(c01, c11, ty);
        return lerp(c0, c1, tz);
    }

    private static double hashNoise(int x, int y, int z, long seed) {
        long value = seed;
        value ^= (long) x * 0x632BE59BD9B4E019L;
        value ^= (long) y * 0x9E3779B185EBCA87L;
        value ^= (long) z * 0xC2B2AE3D27D4EB4FL;
        value = mix64(value);
        return ((value >>> 11) * 0x1.0p-53D) * 2.0D - 1.0D;
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }

    private static double smooth(double value) {
        return value * value * (3.0D - 2.0D * value);
    }

    private static double lerp(double start, double end, double delta) {
        return start + (end - start) * delta;
    }

    private static final class CaveRoom {
        private final int centerX;
        private final int centerY;
        private final int centerZ;
        private final int radiusX;
        private final int radiusY;
        private final int radiusZ;

        private CaveRoom(int centerX, int centerY, int centerZ,
                         int radiusX, int radiusY, int radiusZ) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
            this.radiusX = radiusX;
            this.radiusY = radiusY;
            this.radiusZ = radiusZ;
        }

        private boolean contains(int x, int y, int z) {
            double dx = (x - centerX) / (double) radiusX;
            double dy = (y - centerY) / (double) radiusY;
            double dz = (z - centerZ) / (double) radiusZ;
            return dx * dx + dy * dy + dz * dz <= 1.0D;
        }
    }
}
