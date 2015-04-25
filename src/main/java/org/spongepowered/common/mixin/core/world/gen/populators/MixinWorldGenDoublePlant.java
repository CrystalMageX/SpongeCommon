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

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDoublePlant;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.populator.DoublePlant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

@Mixin(WorldGenDoublePlant.class)
public class MixinWorldGenDoublePlant implements DoublePlant {

    private WeightedTable<DoublePlantType> types;
    private VariableAmount count;

    @Shadow private BlockDoublePlant.EnumPlantType field_150549_a;

    @Inject(method = "<init>()V", at = @At("RETURN") )
    public void onConstructed(CallbackInfo ci) {
        this.types = new WeightedTable<>();
        this.count = VariableAmount.fixed(1);
    }

    @Override
    public void populate(Chunk chunk, Random random) {
        World world = (World) chunk.getWorld();
        Vector3i min = chunk.getBlockMin();
        BlockPos chunkPos = new BlockPos(min.getX(), min.getY(), min.getZ());
        int x, z;
        int n = this.count.getFlooredAmount(random);

        for (int i = 0; i < n; ++i) {
            x = random.nextInt(16) + 8;
            z = random.nextInt(16) + 8;
            generate(world, random, world.getHeight(chunkPos.add(x, 0, z)));
        }
    }

    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        boolean flag = false;

        if (this.types.isEmpty()) {
            this.types.add(new WeightedObject<DoublePlantType>((DoublePlantType) (Object) this.field_150549_a, 1));
        }
        DoublePlantType type = DoublePlantTypes.GRASS;
        List<DoublePlantType> result;
        for (int i = 0; i < 64; ++i) {
            BlockPos blockpos1 = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4),
                    rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(blockpos1) && (!worldIn.provider.getHasNoSky() || blockpos1.getY() < 254)
                    && Blocks.double_plant.canPlaceBlockAt(worldIn, blockpos1)) {
                // BEGIN sponge
                // Blocks.double_plant.placeAt(worldIn, blockpos1,
                // this.field_150549_a, 2);
                result = this.types.get(rand);
                if (!result.isEmpty()) {
                    type = result.get(0);
                    Blocks.double_plant.placeAt(worldIn, position,
                            (BlockDoublePlant.EnumPlantType) (Object) type, 2);
                }
                // END sponge
                flag = true;
            }
        }

        return flag;
    }

    @Override
    public WeightedTable<DoublePlantType> getPossibleTypes() {
        return this.types;
    }

    @Override
    public VariableAmount getPlantsPerChunk() {
        return this.count;
    }

    @Override
    public void setPlantsPerChunk(VariableAmount count) {
        this.count = count;
    }

}
