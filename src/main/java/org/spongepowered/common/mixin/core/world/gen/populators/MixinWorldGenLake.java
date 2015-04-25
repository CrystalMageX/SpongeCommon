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
package org.spongepowered.common.mixin.core.world.gen.populators;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGenLakes;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.data.property.block.MatterProperty.Matter;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.populator.Lake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Random;

@Mixin(WorldGenLakes.class)
public class MixinWorldGenLake implements Lake {

    private BlockState liquid;
    private double prob;
    private VariableAmount height;

    @Shadow private Block block;

    @Inject(method = "<init>(Lnet/minecraft/block/Block;)V", at = @At("RETURN") )
    public void onConstructed(Block block, CallbackInfo ci) {
        this.liquid = (BlockState) block.getDefaultState();
        this.prob = 0.25D;
        this.height = VariableAmount.baseWithRandomAddition(0, 256);
    }

    @Override
    public void populate(Chunk chunk, Random random) {
        World world = (World) chunk.getWorld();
        if (random.nextDouble() < this.prob) {
            int x = random.nextInt(16) + 8;
            int y = this.height.getFlooredAmount(random);
            int z = random.nextInt(16) + 8;
            generate(world, random, new BlockPos(x, y, z));
        }
    }

    // TODO once we can replace lines with injections this can be changed to an
    // @Inject to the replace the changed line
    // for now though our only option would be a work around or calling
    // setBlockState twice, so an overwrite is easier
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        for (position = position.add(-8, 0, -8); position.getY() > 5 && worldIn.isAirBlock(position); position = position.down()) {
            ;
        }

        if (position.getY() <= 4) {
            return false;
        } else {
            position = position.down(4);
            boolean[] aboolean = new boolean[2048];
            int i = rand.nextInt(4) + 4;
            int j;

            for (j = 0; j < i; ++j) {
                double d0 = rand.nextDouble() * 6.0D + 3.0D;
                double d1 = rand.nextDouble() * 4.0D + 2.0D;
                double d2 = rand.nextDouble() * 6.0D + 3.0D;
                double d3 = rand.nextDouble() * (16.0D - d0 - 2.0D) + 1.0D + d0 / 2.0D;
                double d4 = rand.nextDouble() * (8.0D - d1 - 4.0D) + 2.0D + d1 / 2.0D;
                double d5 = rand.nextDouble() * (16.0D - d2 - 2.0D) + 1.0D + d2 / 2.0D;

                for (int l = 1; l < 15; ++l) {
                    for (int i1 = 1; i1 < 15; ++i1) {
                        for (int j1 = 1; j1 < 7; ++j1) {
                            double d6 = ((double) l - d3) / (d0 / 2.0D);
                            double d7 = ((double) j1 - d4) / (d1 / 2.0D);
                            double d8 = ((double) i1 - d5) / (d2 / 2.0D);
                            double d9 = d6 * d6 + d7 * d7 + d8 * d8;

                            if (d9 < 1.0D) {
                                aboolean[(l * 16 + i1) * 8 + j1] = true;
                            }
                        }
                    }
                }
            }

            int k;
            int k1;
            boolean flag;

            for (j = 0; j < 16; ++j) {
                for (k1 = 0; k1 < 16; ++k1) {
                    for (k = 0; k < 8; ++k) {
                        flag =
                                !aboolean[(j * 16 + k1) * 8 + k]
                                        && (j < 15 && aboolean[((j + 1) * 16 + k1) * 8 + k] || j > 0 && aboolean[((j - 1) * 16 + k1) * 8 + k]
                                                || k1 < 15 && aboolean[(j * 16 + k1 + 1) * 8 + k] || k1 > 0 && aboolean[(j * 16 + (k1 - 1)) * 8 + k]
                                                || k < 7 && aboolean[(j * 16 + k1) * 8 + k + 1] || k > 0 && aboolean[(j * 16 + k1) * 8 + (k - 1)]);

                        if (flag) {
                            Material material = worldIn.getBlockState(position.add(j, k, k1)).getBlock().getMaterial();

                            if (k >= 4 && material.isLiquid()) {
                                return false;
                            }

                            if (k < 4 && !material.isSolid() && worldIn.getBlockState(position.add(j, k, k1)).getBlock() != this.block) {
                                return false;
                            }
                        }
                    }
                }
            }

            for (j = 0; j < 16; ++j) {
                for (k1 = 0; k1 < 16; ++k1) {
                    for (k = 0; k < 8; ++k) {
                        if (aboolean[(j * 16 + k1) * 8 + k]) {
                            // worldIn.setBlockState(position.add(j, k, k1),
                            // (IBlockState) (k >= 4 ?
                            // Blocks.air.getDefaultState() :
                            // this.field_150556_a.getDefaultState()), 2);
                            worldIn.setBlockState(position.add(j, k, k1), (IBlockState) (k >= 4 ? Blocks.air.getDefaultState() : this.liquid), 2);
                        }
                    }
                }
            }

            for (j = 0; j < 16; ++j) {
                for (k1 = 0; k1 < 16; ++k1) {
                    for (k = 4; k < 8; ++k) {
                        if (aboolean[(j * 16 + k1) * 8 + k]) {
                            BlockPos blockpos1 = position.add(j, k - 1, k1);

                            if (worldIn.getBlockState(blockpos1).getBlock() == Blocks.dirt
                                    && worldIn.getLightFor(EnumSkyBlock.SKY, position.add(j, k, k1)) > 0) {
                                BiomeGenBase biomegenbase = worldIn.getBiomeGenForCoords(blockpos1);

                                if (biomegenbase.topBlock.getBlock() == Blocks.mycelium) {
                                    worldIn.setBlockState(blockpos1, Blocks.mycelium.getDefaultState(), 2);
                                } else {
                                    worldIn.setBlockState(blockpos1, Blocks.grass.getDefaultState(), 2);
                                }
                            }
                        }
                    }
                }
            }

            if (this.block.getMaterial() == Material.lava) {
                for (j = 0; j < 16; ++j) {
                    for (k1 = 0; k1 < 16; ++k1) {
                        for (k = 0; k < 8; ++k) {
                            flag =
                                    !aboolean[(j * 16 + k1) * 8 + k]
                                            && (j < 15 && aboolean[((j + 1) * 16 + k1) * 8 + k] || j > 0 && aboolean[((j - 1) * 16 + k1) * 8 + k]
                                                    || k1 < 15 && aboolean[(j * 16 + k1 + 1) * 8 + k] || k1 > 0
                                                            && aboolean[(j * 16 + (k1 - 1)) * 8 + k]
                                                    || k < 7 && aboolean[(j * 16 + k1) * 8 + k + 1] || k > 0
                                                            && aboolean[(j * 16 + k1) * 8 + (k - 1)]);

                            if (flag && (k < 4 || rand.nextInt(2) != 0)
                                    && worldIn.getBlockState(position.add(j, k, k1)).getBlock().getMaterial().isSolid()) {
                                worldIn.setBlockState(position.add(j, k, k1), Blocks.stone.getDefaultState(), 2);
                            }
                        }
                    }
                }
            }
            if (this.block.getMaterial() == Material.water) {
                for (j = 0; j < 16; ++j) {
                    for (k1 = 0; k1 < 16; ++k1) {
                        byte b0 = 4;

                        if (worldIn.canBlockFreezeWater(position.add(j, b0, k1))) {
                            worldIn.setBlockState(position.add(j, b0, k1), Blocks.ice.getDefaultState(), 2);
                        }
                    }
                }
            }

            return true;
        }
    }

    @Override
    public BlockState getLiquidType() {
        return this.liquid;
    }

    @Override
    public void setLiquidType(BlockState liquid) {
        Optional<MatterProperty> matter = liquid.getType().getProperty(MatterProperty.class);
        checkArgument(matter.isPresent() && matter.get().getValue() == Matter.LIQUID);
        this.liquid = checkNotNull(liquid);
        this.block = (Block) this.liquid.getType();
    }

    @Override
    public double getLakeProbability() {
        return this.prob;
    }

    @Override
    public void setLakeProbability(double p) {
        this.prob = p;
    }

    @Override
    public VariableAmount getHeight() {
        return this.height;
    }

    @Override
    public void setHeight(VariableAmount height) {
        this.height = height;
    }

}
