/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.world.gen;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.GroundCoverLayer;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.common.interfaces.gen.IBiomeGenBase;
import org.spongepowered.common.interfaces.gen.IFlaggedPopulator;
import org.spongepowered.common.util.gen.ByteArrayMutableBiomeBuffer;
import org.spongepowered.common.util.gen.ChunkPrimerBuffer;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Similar class to {@link ChunkProviderGenerate}, but instead gets its blocks
 * from a custom chunk generator.
 */
public class SpongeChunkProvider implements WorldGenerator, IChunkProvider {

    private static final Vector2i CHUNK_AREA = new Vector2i(16, 16);

    protected BiomeGenerator biomeGenerator;
    protected GenerationPopulator baseGenerator;
    protected List<GenerationPopulator> genpop;
    protected List<Populator> pop;
    protected Map<BiomeType, BiomeGenerationSettings> biomeSettings;
    protected final World world;
    private final ByteArrayMutableBiomeBuffer cachedBiomes;

    protected Random rand;
    private NoiseGeneratorPerlin noise4;
    private double[] stoneNoise;

    public SpongeChunkProvider(World world, GenerationPopulator base, BiomeGenerator biomegen) {
        this.world = checkNotNull(world, "world");
        this.baseGenerator = checkNotNull(base, "baseGenerator");
        this.biomeGenerator = checkNotNull(biomegen, "biomeGenerator");

        // Make initially empty biome cache
        this.cachedBiomes = new ByteArrayMutableBiomeBuffer(Vector2i.ZERO, CHUNK_AREA);
        this.cachedBiomes.detach();

        this.genpop = Lists.newArrayList();
        this.pop = Lists.newArrayList();
        this.biomeSettings = Maps.newHashMap();
        this.rand = new Random(world.getSeed());
        this.noise4 = new NoiseGeneratorPerlin(this.rand, 4);
        this.stoneNoise = new double[256];
    }

    @Override
    public GenerationPopulator getBaseGenerationPopulator() {
        return this.baseGenerator;
    }

    @Override
    public void setBaseGenerationPopulator(GenerationPopulator baseGenerationPopulator) {
        this.baseGenerator = baseGenerationPopulator;
    }

    @Override
    public List<GenerationPopulator> getGenerationPopulators() {
        return this.genpop;
    }

    public void setGenerationPopulators(List<GenerationPopulator> generationPopulators) {
        this.genpop = Lists.newArrayList(generationPopulators);
    }

    @Override
    public List<Populator> getPopulators() {
        return this.pop;
    }

    public void setPopulators(List<Populator> populators) {
        this.pop = Lists.newArrayList(populators);
    }

    public Map<BiomeType, BiomeGenerationSettings> getBiomeOverrides() {
        return this.biomeSettings;
    }

    public void setBiomeOverrides(Map<BiomeType, BiomeGenerationSettings> biomeOverrides) {
        this.biomeSettings = Maps.newHashMap(biomeOverrides);
    }

    @Override
    public BiomeGenerator getBiomeGenerator() {
        return this.biomeGenerator;
    }

    @Override
    public void setBiomeGenerator(BiomeGenerator biomeGenerator) {
        this.biomeGenerator = biomeGenerator;
    }

    @Override
    public BiomeGenerationSettings getBiomeSettings(BiomeType type) {
        if (!this.biomeSettings.containsKey(type)) {
            this.biomeSettings.put(type, ((IBiomeGenBase) type).initPopulators(this.world));
        }
        return this.biomeSettings.get(type);
    }

    @Override
    public List<GenerationPopulator> getGenerationPopulators(Class<? extends GenerationPopulator> type) {
        return this.genpop.stream().filter((p) -> {
            return type.isAssignableFrom(p.getClass());
        }).collect(Collectors.toList());
    }

    @Override
    public List<Populator> getPopulators(Class<? extends Populator> type) {
        return this.pop.stream().filter((p) -> {
            return type.isAssignableFrom(p.getClass());
        }).collect(Collectors.toList());
    }

    @Override
    public Chunk provideChunk(int chunkX, int chunkZ) {
//        System.out.println("Generating " + chunkX + " " + chunkZ);
        this.rand.setSeed((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L);
        this.cachedBiomes.reuse(new Vector2i(chunkX * 16, chunkZ * 16));
        this.biomeGenerator.generateBiomes(this.cachedBiomes);

//        BiomeType idc = this.cachedBiomes.getBiome(chunkX * 16, chunkZ * 16);
//        BiomeType biomec;
//        outer: for (int x = 0; x < 16; x++) {
//            for (int z = 0; z < 16; z++) {
//                biomec = this.cachedBiomes.getBiome(chunkX * 16 + x, chunkZ * 16 + z);
//                if (biomec != idc) {
//                    System.err.println("Found chunk with non-uniform biomes post-gen " + chunkX + " " + chunkZ);
//                    break outer;
//                }
//            }
//        }
        // Generate base terrain
        ChunkPrimer chunkprimer = new ChunkPrimer();
        MutableBlockVolume blockBuffer = new ChunkPrimerBuffer(chunkprimer, chunkX, chunkZ);
        ImmutableBiomeArea biomeBuffer = this.cachedBiomes.getImmutableBiomeCopy();
        this.baseGenerator.populate((org.spongepowered.api.world.World) this.world, blockBuffer, biomeBuffer);

        replaceBiomeBlocks(this.world, this.rand, chunkX, chunkZ, chunkprimer, biomeBuffer);

        // Apply the generator populators to complete the blockBuffer
        for (GenerationPopulator populator : this.genpop) {
            populator.populate((org.spongepowered.api.world.World) this.world, blockBuffer, biomeBuffer);
        }

        // Get unique biomes to determine what generator populators to run
        List<BiomeType> uniqueBiomes = Lists.newArrayList();
        BiomeType biome;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                biome = this.cachedBiomes.getBiome(chunkX * 16 + x, chunkZ * 16 + z);
                if (!uniqueBiomes.contains(biome)) {
                    uniqueBiomes.add(biome);
                }
            }
        }
//        if (uniqueBiomes.size() != 1) {
//            System.err.println("Found chunk with non-uniform biomes unique gather " + chunkX + " " + chunkZ);
//        }

        // run our generator populators, checking for overrides from the
        // generator
        for (BiomeType type : uniqueBiomes) {
            if (!this.biomeSettings.containsKey(type)) {
                this.biomeSettings.put(type, ((IBiomeGenBase) type).initPopulators(this.world));
            }
            for (GenerationPopulator populator : this.biomeSettings.get(type).getGenerationPopulators()) {
                populator.populate((org.spongepowered.api.world.World) this.world, blockBuffer, biomeBuffer);
            }
        }

        // Assemble chunk
        Chunk chunk = new Chunk(this.world, chunkprimer, chunkX, chunkZ);
        byte[] biomeArray = chunk.getBiomeArray();
        System.arraycopy(this.cachedBiomes.detach(), 0, biomeArray, 0, biomeArray.length);
//        byte id = biomeArray[0];
//        for (int i = 0; i < 256; i++) {
//            if (biomeArray[i] != id) {
//                System.err.println("Found chunk with non-uniform biomes end " + chunkX + " " + chunkZ);
//                break;
//            }
//        }
        chunk.generateSkylightMap();

//        System.out.println("Done Generating " + chunkX + " " + chunkZ);
        return chunk;
    }

    @Override
    public void populate(IChunkProvider chunkProvider, int chunkX, int chunkZ) {
//        System.out.println("Populating " + chunkX + " " + chunkZ);
        this.rand.setSeed(this.world.getSeed());
        long i1 = this.rand.nextLong() / 2L * 2L + 1L;
        long j1 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed((long)chunkX * i1 + (long)chunkZ * j1 ^ this.world.getSeed());
        BlockFalling.fallInstantly = true;

        BlockPos blockpos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        BiomeType biome = (BiomeType) this.world.getBiomeGenForCoords(blockpos.add(16, 0, 16));

        org.spongepowered.api.world.Chunk chunk = (org.spongepowered.api.world.Chunk) this.world.getChunkFromChunkCoords(chunkX, chunkZ);

//        System.out.println("Got Chunk " + chunkX + " " + chunkZ);
        if (!this.biomeSettings.containsKey(biome)) {
            this.biomeSettings.put(biome, ((IBiomeGenBase) biome).initPopulators(this.world));
        }
        List<Populator> populators = Lists.newArrayList(this.biomeSettings.get(biome).getPopulators());
        populators.addAll(this.pop);//Non-biome populators come after the biome populators
        Sponge.getGame().getEventManager().post(SpongeEventFactory.createPopulateChunkEventPre(Sponge.getGame(), Cause.empty(), populators, chunk));

//        System.out.println("Posted event " + chunkX + " " + chunkZ);
        List<String> flags = Lists.newArrayList();
        for (Populator populator : populators) {
//            System.out.println("Running populator " + chunkX + " " + chunkZ + " " + populator.getClass());
            if (populator instanceof IFlaggedPopulator) {
                ((IFlaggedPopulator) populator).populate(chunkProvider, chunk, this.rand, flags);
            } else {
                populator.populate(chunk, this.rand);
            }
        }
//        System.out.println("Done Populators " + chunkX + " " + chunkZ);

        Sponge.getGame().getEventManager().post(SpongeEventFactory.createPopulateChunkEventPost(Sponge.getGame(), Cause.empty(), ImmutableMap.of(), chunk));

        BlockFalling.fallInstantly = false;
//        System.out.println("Done Populating " + chunkX + " " + chunkZ);
    }

    @Override
    public boolean func_177460_a(IChunkProvider chunkProvider, Chunk chunk, int chunkX, int chunkZ) {
        // This method normally places ocean monuments in existing chunks
        return false;
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        BiomeGenBase biome = this.world.getBiomeGenForCoords(pos);
        @SuppressWarnings("unchecked")
        List<SpawnListEntry> creatures = biome.getSpawnableList(creatureType);
        return creatures;
    }

    @Override
    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
        // TODO Maybe allow to register stronghold locations?
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunk, int chunkX, int chunkZ) {
        // TODO No structure support
    }

    // Methods below are simply mirrors of the methods in ChunkProviderGenerate

    @Override
    public Chunk provideChunk(BlockPos blockPosIn) {
        return provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public String makeString() {
        return "RandomLevelSource";
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public boolean saveChunks(boolean bool, IProgressUpdate progressUpdate) {
        return true;
    }

    @Override
    public void saveExtraData() {

    }

    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    @Override
    public boolean chunkExists(int x, int z) {
        return true;
    }

    public void replaceBiomeBlocks(World world, Random rand, int x, int z, ChunkPrimer chunk, ImmutableBiomeArea biomes) {
        double d0 = 0.03125D;
        this.stoneNoise = this.noise4.func_151599_a(this.stoneNoise, (double) (x * 16), (double) (z * 16), 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);
        Vector2i min = biomes.getBiomeMin();
        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                BiomeType biomegenbase = biomes.getBiome(min.getX() + k, min.getY() + l);
                generateBiomeTerrain(world, rand, chunk, x * 16 + k, z * 16 + l, this.stoneNoise[l + k * 16], getBiomeSettings(biomegenbase).getGroundCoverLayers());
            }
        }
    }

    public void generateBiomeTerrain(World worldIn, Random rand, ChunkPrimer chunk, int x, int z, double stoneNoise, List<GroundCoverLayer> groundcover) {
        IBlockState currentPlacement = null;
        int k = -1;
        int relativeX = x & 15;
        int relativeZ = z & 15;
        int i = 0;
        for (int currentY = 255; currentY >= 0; --currentY) {
            if (currentY <= rand.nextInt(5)) {
                chunk.setBlockState(relativeZ, currentY, relativeX, Blocks.bedrock.getDefaultState());
            } else {
                IBlockState nextBlock = chunk.getBlockState(relativeZ, currentY, relativeX);

                if (nextBlock.getBlock().getMaterial() == Material.air) {
                    k = -1;
                } else if (nextBlock.getBlock() == Blocks.stone) {
                    if (k == -1) {
                        if (groundcover.isEmpty()) {
                            k = 0;
                            continue;
                        }
                        i = 0;
                        GroundCoverLayer layer = groundcover.get(i);
                        currentPlacement = (IBlockState) layer.getBlockState().apply(stoneNoise);
                        k = layer.getDepth().getFlooredAmount(rand, stoneNoise);
                        if (k <= 0) {
                            continue;
                        }

                        if (currentY >= 62) {
                            chunk.setBlockState(relativeZ, currentY, relativeX, currentPlacement);
                            ++i;
                            if (i < groundcover.size()) {
                                layer = groundcover.get(i);
                                k = layer.getDepth().getFlooredAmount(rand, stoneNoise);
                                currentPlacement = (IBlockState) layer.getBlockState().apply(stoneNoise);
                            }
                        } else if (currentY < 56 - k) {
                            k = 0;
                            chunk.setBlockState(relativeZ, currentY, relativeX, Blocks.gravel.getDefaultState());
                        } else {
                            ++i;
                            if (i < groundcover.size()) {
                                layer = groundcover.get(i);
                                k = layer.getDepth().getFlooredAmount(rand, stoneNoise);
                                currentPlacement = (IBlockState) layer.getBlockState().apply(stoneNoise);
                                chunk.setBlockState(relativeZ, currentY, relativeX, currentPlacement);
                            }
                        }
                    } else if (k > 0) {
                        --k;
                        chunk.setBlockState(relativeZ, currentY, relativeX, currentPlacement);

                        if (k == 0) {
                            ++i;
                            if (i < groundcover.size()) {
                                GroundCoverLayer layer = groundcover.get(i);
                                k = layer.getDepth().getFlooredAmount(rand, stoneNoise);
                                currentPlacement = (IBlockState) layer.getBlockState().apply(stoneNoise);
                            }
                        }
                    }
                }
            }
        }
    }

}
