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
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDeadBush;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.populator.DeadBush;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldGenDeadBush.class)
public abstract class MixinWorldGenDeadBush extends WorldGenerator implements DeadBush {

    private VariableAmount count;

    @Inject(method = "<init>", at = @At("RETURN") )
    public void onConstructed(CallbackInfo ci) {
        this.count = VariableAmount.fixed(128);
    }

    @Override
    public void populate(Chunk chunk, Random random) {
        Vector3i min = chunk.getBlockMin();
        World world = (World) chunk.getWorld();
        BlockPos position = new BlockPos(min.getX(), min.getY(), min.getZ());
        int n = (int) Math.ceil(this.count.getFlooredAmount(random) / 16f);
        for (int i = 0; i < n; i++) {
            BlockPos pos = position.add(random.nextInt(16) + 8, 0, random.nextInt(16) + 8);
            pos = world.getTopSolidOrLiquidBlock(pos).add(0, 1, 0);
            generate(world, random, pos);
        }
    }

    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
//        Block block;
//
//        while (((block = worldIn.getBlockState(position).getBlock()).getMaterial() == Material.air || block.getMaterial() == Material.leaves) && position.getY() > 0)
//        {
//            position = position.down();
//        }

        for (int i = 0; i < 4; ++i)
        {
            BlockPos blockpos1 = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(blockpos1) && Blocks.deadbush.canBlockStay(worldIn, blockpos1, Blocks.deadbush.getDefaultState()))
            {
                worldIn.setBlockState(blockpos1, Blocks.deadbush.getDefaultState(), 2);
            }
        }

        return true;
    }
    
    @Override
    public VariableAmount getShrubsPerChunk() {
        return this.count;
    }

    @Override
    public void setShrubsPerChunk(VariableAmount count) {
        this.count = count;
    }

}
