package com.edx.underdeep.world;

import com.edx.underdeep.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.ChunkPos;
import net.minecraftforge.common.extensions.IForgeChunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

/**
 * Playable-height terrain prototype for Forge 32.0.108.
 *
 * This deliberately stays inside the 1.16.1 build-height range. It does not
 * create negative Y coordinates or change the engine's chunk section layout.
 * Run it in a new test world first; unprocessed existing chunks are also
 * eligible for the one-time terrain pass.
 */
public final class UnderdeepWorldgen {
    private static final String PROCESSED_KEY = "underdeep_terrain_v1";

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        IWorld eventWorld = event.getWorld();
        if (eventWorld.isClientSide() || !(eventWorld instanceof World)) {
            return;
        }

        World world = (World) eventWorld;
        if (!world.dimension().equals(World.OVERWORLD)) {
            return;
        }

        IChunk chunk = event.getChunk();
        CompoundNBT persistent = ((IForgeChunk) chunk).getPersistentData();
        if (persistent.getBoolean(PROCESSED_KEY)) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        long seed = ((long) chunkPos.x * 341873128712L)
                ^ ((long) chunkPos.z * 132897987541L)
                ^ 0x5DEECE66DL;
        Random random = new Random(seed);
        int startX = chunkPos.x << 4;
        int startZ = chunkPos.z << 4;

        // Add a dark stone band in the lower 38 blocks of the old height range.
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int y = 1; y <= 38; y++) {
                    BlockPos pos = new BlockPos(startX + localX, y, startZ + localZ);
                    BlockState oldState = chunk.getBlockState(pos);
                    if (oldState.getBlock() != Blocks.STONE) {
                        continue;
                    }

                    if (y <= 24 || random.nextFloat() < 0.35F) {
                        chunk.setBlockState(pos, ModBlocks.DEEPSLATE.get().getDefaultState(), false);
                    }

                    if (y >= 5 && y <= 30 && random.nextInt(180) == 0) {
                        chunk.setBlockState(pos, ModBlocks.DEEPSLATE_IRON_ORE.get().getDefaultState(), false);
                    }
                }
            }
        }

        // Carve broad chambers through the new lower stone band.
        int chambers = 1 + random.nextInt(2);
        for (int chamber = 0; chamber < chambers; chamber++) {
            int centerX = startX + 3 + random.nextInt(10);
            int centerY = 8 + random.nextInt(23);
            int centerZ = startZ + 3 + random.nextInt(10);
            int radiusX = 5 + random.nextInt(5);
            int radiusY = 3 + random.nextInt(3);
            int radiusZ = 5 + random.nextInt(5);

            for (int x = Math.max(startX, centerX - radiusX); x <= Math.min(startX + 15, centerX + radiusX); x++) {
                for (int y = Math.max(2, centerY - radiusY); y <= Math.min(36, centerY + radiusY); y++) {
                    for (int z = Math.max(startZ, centerZ - radiusZ); z <= Math.min(startZ + 15, centerZ + radiusZ); z++) {
                        double dx = (x - centerX) / (double) radiusX;
                        double dy = (y - centerY) / (double) radiusY;
                        double dz = (z - centerZ) / (double) radiusZ;
                        if (dx * dx + dy * dy + dz * dz > 1.0D) {
                            continue;
                        }

                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = chunk.getBlockState(pos);
                        if (state.getBlock() == ModBlocks.DEEPSLATE.get()
                                || state.getBlock() == Blocks.STONE) {
                            chunk.setBlockState(pos, Blocks.AIR.getDefaultState(), false);
                        }
                    }
                }
            }
        }

        persistent.putBoolean(PROCESSED_KEY, true);
    }
}
