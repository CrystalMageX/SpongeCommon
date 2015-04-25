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
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.World;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.common.interfaces.gen.IBiomeGenBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link WorldGenerator}.
 */
public final class SpongeWorldGenerator implements WorldGenerator {

    private final World world;
    /**
     * Holds the populators. May be mutable or immutable, but must be changed to
     * be mutable before the first call to {@link #getPopulators()}.
     */
    private List<Populator> populators;
    /**
     * Holds the generator populators. May be mutable or immutable, but must be
     * changed to be mutable before the first call to
     * {@link #getGenerationPopulators()}.
     */
    private List<GenerationPopulator> generationPopulators;
    private Map<BiomeType, BiomeGenerationSettings> biomeSettings;
    private BiomeGenerator biomeGenerator;
    private GenerationPopulator baseGenerator;

    private boolean biomeGeneratorChanged;
    private boolean baseGeneratorChanged;
    

    public SpongeWorldGenerator(World world, BiomeGenerator biomeGenerator, GenerationPopulator baseGenerator,
            List<GenerationPopulator> generationPopulators, List<Populator> populators, Map<BiomeType, BiomeGenerationSettings> biomeOverrides) {
        this.world = world;
        this.biomeGenerator = checkNotNull(biomeGenerator, "biomeGenerator");
        this.baseGenerator = checkNotNull(baseGenerator, "baseGenerator");

        // Note that ImmutableList.copyOf returns actually the list itself if it
        // is already immutable
        this.populators = ImmutableList.copyOf(populators);
        this.generationPopulators = ImmutableList.copyOf(generationPopulators);
        this.biomeSettings = ImmutableMap.copyOf(biomeOverrides);
    }

    @Override
    public List<GenerationPopulator> getGenerationPopulators() {
        if (!(this.generationPopulators instanceof ArrayList)) {
            // Need to make a copy to make the populators mutable
            this.generationPopulators = new ArrayList<>(this.generationPopulators);
        }
        return this.generationPopulators;
    }

    @Override
    public List<Populator> getPopulators() {
        if (!(this.populators instanceof ArrayList)) {
            // Need to make a copy to make the populators mutable
            this.populators = new ArrayList<>(this.populators);
        }
        return this.populators;
    }

    @Override
    public BiomeGenerator getBiomeGenerator() {
        return this.biomeGenerator;
    }

    @Override
    public void setBiomeGenerator(BiomeGenerator biomeGenerator) {
        checkState(!this.biomeGeneratorChanged,
                "Another plugin already set the biome generator to " + this.biomeGenerator.getClass().getName());

        this.biomeGenerator = checkNotNull(biomeGenerator, "biomeGenerator");
        this.biomeGeneratorChanged = true;
    }

    @Override
    public GenerationPopulator getBaseGenerationPopulator() {
        return this.baseGenerator;
    }

    @Override
    public void setBaseGenerationPopulator(GenerationPopulator generator) {
        checkState(!this.baseGeneratorChanged,
                "Another plugin already set the base generator to " + this.biomeGenerator.getClass().getName());

        this.baseGenerator = checkNotNull(generator, "generator");
        this.baseGeneratorChanged = true;
    }

    @Override
    public BiomeGenerationSettings getBiomeSettings(BiomeType type) {
        checkNotNull(type);
        if (!this.biomeSettings.containsKey(type)) {
            if (!(this.biomeSettings instanceof HashMap)) {
                this.biomeSettings = new HashMap<BiomeType, BiomeGenerationSettings>(this.biomeSettings);
            }
            this.biomeSettings.put(type, ((IBiomeGenBase) type).initPopulators(this.world));
        }
        return this.biomeSettings.get(type);
    }

    public Map<BiomeType, BiomeGenerationSettings> getBiomeSettings() {
        return ImmutableMap.copyOf(this.biomeSettings);
    }

    @Override
    public List<GenerationPopulator> getGenerationPopulators(Class<? extends GenerationPopulator> type) {
        return this.generationPopulators.stream().filter((p) -> {
            return type.isAssignableFrom(p.getClass());
        }).collect(Collectors.toList());
    }

    @Override
    public List<Populator> getPopulators(Class<? extends Populator> type) {
        return this.populators.stream().filter((p) -> {
            return type.isAssignableFrom(p.getClass());
        }).collect(Collectors.toList());
    }
}
