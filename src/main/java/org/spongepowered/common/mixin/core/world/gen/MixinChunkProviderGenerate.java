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
package org.spongepowered.common.mixin.core.world.gen;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorFactory;
import org.spongepowered.api.world.gen.populator.Dungeon;
import org.spongepowered.api.world.gen.populator.Lake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.gen.IPopulatorProvider;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.util.gen.ChunkBufferPrimer;
import org.spongepowered.common.world.gen.SpongeWorldGenerator;
import org.spongepowered.common.world.gen.populators.AnimalPopulator;
import org.spongepowered.common.world.gen.populators.FilteredPopulator;
import org.spongepowered.common.world.gen.populators.SnowPopulator;

@Mixin(ChunkProviderGenerate.class)
public abstract class MixinChunkProviderGenerate implements IChunkProvider, GenerationPopulator, IPopulatorProvider {

    @Shadow private net.minecraft.world.World worldObj;
    @Shadow private double[] field_147434_q;
    @Shadow private Block field_177476_s;
    @Shadow private ChunkProviderSettings settings;
    @Shadow private boolean mapFeaturesEnabled;

    @Shadow private MapGenBase caveGenerator;
    @Shadow private MapGenStronghold strongholdGenerator;
    @Shadow private MapGenVillage villageGenerator;
    @Shadow private MapGenMineshaft mineshaftGenerator;
    @Shadow private MapGenScatteredFeature scatteredFeatureGenerator;
    @Shadow private MapGenBase ravineGenerator;
    @Shadow private StructureOceanMonument oceanMonumentGenerator;

    @Shadow
    public abstract void func_147423_a(int p_147423_1_, int p_147423_2_, int p_147423_3_);

    @Shadow
    public abstract void replaceBlocksForBiome(int p_180517_1_, int p_180517_2_, ChunkPrimer p_180517_3_, BiomeGenBase[] p_180517_4_);

    @Shadow private BiomeGenBase[] biomesForGeneration;

    @Override
    public void addPopulators(SpongeWorldGenerator generator) {
        if (this.settings.useCaves) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.caveGenerator);
        }

        if (this.settings.useRavines) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.ravineGenerator);
        }

        // Structures are both generation populators and populators as they are
        // placed in a two phase system

        if (this.settings.useMineShafts && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.mineshaftGenerator);
            generator.getPopulators().add((Populator) this.mineshaftGenerator);
        }

        if (this.settings.useVillages && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.villageGenerator);
            generator.getPopulators().add((Populator) this.villageGenerator);
        }

        if (this.settings.useStrongholds && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.strongholdGenerator);
            generator.getPopulators().add((Populator) this.strongholdGenerator);
        }

        if (this.settings.useTemples && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.scatteredFeatureGenerator);
            generator.getPopulators().add((Populator) this.scatteredFeatureGenerator);
        }

        if (this.settings.useMonuments && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.oceanMonumentGenerator);
            generator.getPopulators().add((Populator) this.oceanMonumentGenerator);
        }
        PopulatorFactory factory = Sponge.getRegistry().getPopulatorFactory();

        if (this.settings.useWaterLakes) {
            Lake lake = factory.createLakePopulator()
                    .chance(1d / this.settings.waterLakeChance)
                    .liquidType((BlockState) Blocks.water.getDefaultState())
                    .height(VariableAmount.baseWithRandomAddition(0, 256))
                    .build();
            FilteredPopulator filtered = new FilteredPopulator(lake, (c) -> {
                BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(VecHelper.toBlockPos(c.getBlockMin()).add(16, 0, 16));
                return biomegenbase != BiomeGenBase.desert && biomegenbase != BiomeGenBase.desertHills;
            });
            filtered.setRequiredFlags("VILLAGE");
            generator.getPopulators().add(filtered);
        }
        
        if(this.settings.useLavaLakes) {
            Lake lake = factory.createLakePopulator()
                    .chance(1d / this.settings.waterLakeChance)
                    .liquidType((BlockState) Blocks.water.getDefaultState())
                    .height(VariableAmount.baseWithVariance(0, VariableAmount.baseWithRandomAddition(8, VariableAmount.baseWithOptionalAddition(55, 193, 0.1))))
                    .build();
            FilteredPopulator filtered = new FilteredPopulator(lake);
            filtered.setRequiredFlags("VILLAGE");
            generator.getPopulators().add(filtered);
        }
        
        if(this.settings.useDungeons) {
            Dungeon dungeon = factory.createDungeonPopulator()
                    .attempts(this.settings.dungeonChance)//this is actually a count, terrible naming
                    .build();
            generator.getPopulators().add(dungeon);
        }
        
        generator.getPopulators().add(new AnimalPopulator());
        generator.getPopulators().add(new SnowPopulator());
        
        
    }

    @Override
    public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeArea biomes) {
        int minx = biomes.getBiomeMin().getX();
        int maxx = biomes.getBiomeMax().getX();
        int minz = biomes.getBiomeMin().getY();
        int maxz = biomes.getBiomeMax().getY();
        // Need to add one here as the maximum value is inclusive not exclusive
        int width = maxx - minx + 1;
        int height = maxz - minz + 1;
        this.biomesForGeneration = new BiomeGenBase[width * height];
        for (int bx = 0; bx < width; bx++) {
            int absX = bx + minx;
            for (int bz = 0; bz < height; bz++) {
                int absZ = bz + minz;
                this.biomesForGeneration[bx + bz * width] = (BiomeGenBase) biomes.getBiome(absX, absZ);
            }
        }
        ChunkPrimer chunkprimer = new ChunkBufferPrimer(buffer);
        int x = buffer.getBlockMin().getX() / 16;
        int z = buffer.getBlockMin().getZ() / 16;
        this.setBlocksInChunk(x, z, chunkprimer);
    }

    @Overwrite
    public void setBlocksInChunk(int p_180518_1_, int p_180518_2_, ChunkPrimer p_180518_3_) {
//        this.biomesForGeneration = this.worldObj.getWorldChunkManager().getBiomesForGeneration(this.biomesForGeneration, p_180518_1_ * 4 - 2, p_180518_2_ * 4 - 2, 10, 10);
        this.func_147423_a(p_180518_1_ * 4, 0, p_180518_2_ * 4);

        for (int k = 0; k < 4; ++k) {
            int l = k * 5;
            int i1 = (k + 1) * 5;

            for (int j1 = 0; j1 < 4; ++j1) {
                int k1 = (l + j1) * 33;
                int l1 = (l + j1 + 1) * 33;
                int i2 = (i1 + j1) * 33;
                int j2 = (i1 + j1 + 1) * 33;

                for (int k2 = 0; k2 < 32; ++k2) {
                    double d0 = 0.125D;
                    double d1 = this.field_147434_q[k1 + k2];
                    double d2 = this.field_147434_q[l1 + k2];
                    double d3 = this.field_147434_q[i2 + k2];
                    double d4 = this.field_147434_q[j2 + k2];
                    double d5 = (this.field_147434_q[k1 + k2 + 1] - d1) * d0;
                    double d6 = (this.field_147434_q[l1 + k2 + 1] - d2) * d0;
                    double d7 = (this.field_147434_q[i2 + k2 + 1] - d3) * d0;
                    double d8 = (this.field_147434_q[j2 + k2 + 1] - d4) * d0;

                    for (int l2 = 0; l2 < 8; ++l2) {
                        double d9 = 0.25D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        for (int i3 = 0; i3 < 4; ++i3) {
                            double d14 = 0.25D;
                            double d16 = (d11 - d10) * d14;
                            double d15 = d10 - d16;

                            for (int j3 = 0; j3 < 4; ++j3) {
                                if ((d15 += d16) > 0.0D) {
                                    p_180518_3_.setBlockState(k * 4 + i3, k2 * 8 + l2, j1 * 4 + j3, Blocks.stone.getDefaultState());
                                } else if (k2 * 8 + l2 < this.settings.seaLevel) {
                                    p_180518_3_.setBlockState(k * 4 + i3, k2 * 8 + l2, j1 * 4 + j3, this.field_177476_s.getDefaultState());
                                }
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }
}
