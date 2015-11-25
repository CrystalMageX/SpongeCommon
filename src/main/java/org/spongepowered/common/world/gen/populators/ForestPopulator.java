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
package org.spongepowered.common.world.gen.populators;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Objects;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.populator.Forest;

import java.util.List;
import java.util.Random;

public class ForestPopulator implements Forest {

    private VariableAmount count;
    private WeightedTable<PopulatorObject> types;

    public ForestPopulator() {
        this.count = VariableAmount.fixed(10);
        this.types = new WeightedTable<>();
    }

    @Override
    public void populate(Chunk chunk, Random random) {
        Vector3i min = chunk.getBlockMin();
        int n = this.count.getFlooredAmount(random);
        int x, z;
        BlockPos pos;
        for (int i = 0; i < n; i++) {
            List<PopulatorObject> treeTypes = this.types.get(random);
            if (treeTypes.isEmpty()) {
                continue;
            }
            PopulatorObject tree = treeTypes.get(0);
            x = random.nextInt(16) + 8;
            z = random.nextInt(16) + 8;
            pos = ((net.minecraft.world.World) chunk.getWorld()).getTopSolidOrLiquidBlock(new BlockPos(min.getX() + x, min.getY(), min.getZ() + z));
            if (tree.canPlaceAt(chunk.getWorld(), pos.getX(), pos.getY(), pos.getZ())) {
                tree.placeObject(chunk.getWorld(), random, pos.getX(), pos.getY(), pos.getZ());
            }
        }
    }

    @Override
    public VariableAmount getTreesPerChunk() {
        return this.count;
    }

    @Override
    public void setTreesPerChunk(VariableAmount count) {
        this.count = count;
    }

    @Override
    public WeightedTable<PopulatorObject> getTypes() {
        return this.types;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("count", this.count)
                .add("types", this.types)
                .toString();
    }

}
