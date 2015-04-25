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
package org.spongepowered.common.world.biome;

import com.google.common.collect.Lists;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeGenerationSettingsBuilder;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.GroundCoverLayer;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;

import java.util.Collection;
import java.util.List;

public class SpongeBiomeGenerationSettingsBuilder implements BiomeGenerationSettingsBuilder {

	private float max;
	private float min;

	private List<GroundCoverLayer> groundcover;
	private List<Populator> populator;
	private List<GenerationPopulator> genpop;

	@Override
	public BiomeGenerationSettingsBuilder minHeight(float height) {
		this.min = height;
		return this;
	}

	@Override
	public BiomeGenerationSettingsBuilder maxHeight(float height) {
		this.max = height;
		return this;
	}

	@Override
	public BiomeGenerationSettingsBuilder groundCoverLayers(GroundCoverLayer... layers) {
		this.groundcover = Lists.newArrayList(layers);
		return this;
	}

	@Override
	public BiomeGenerationSettingsBuilder groundCoverLayers(Collection<GroundCoverLayer> layers) {
		this.groundcover = Lists.newArrayList(layers);
		return this;
	}

	@Override
	public BiomeGenerationSettingsBuilder generationPopulators(GenerationPopulator... genpops) {
		this.genpop = Lists.newArrayList(genpops);
		return this;
	}

	@Override
	public BiomeGenerationSettingsBuilder generationPopulators(Collection<GenerationPopulator> genpops) {
		this.genpop = Lists.newArrayList(genpops);
		return this;
	}

	@Override
	public BiomeGenerationSettingsBuilder populators(Populator... pops) {
		this.populator = Lists.newArrayList(pops);
		return this;
	}

	@Override
	public BiomeGenerationSettingsBuilder populators(Collection<Populator> pops) {
		this.populator = Lists.newArrayList(pops);
		return this;
	}

	@Override
	public BiomeGenerationSettingsBuilder reset() {
		this.min = 0.1f;
		this.max = 0.2f;
		return this;
	}

	@Override
	public BiomeGenerationSettingsBuilder reset(BiomeGenerationSettings type) {
		this.min = type.getMinHeight();
		this.max = type.getMaxHeight();
		this.groundcover = Lists.newArrayList(type.getGroundCoverLayers());
		this.populator = Lists.newArrayList(type.getPopulators());
		this.genpop = Lists.newArrayList(type.getGenerationPopulators());
		return this;
	}

	@Override
	public BiomeGenerationSettings build() throws IllegalArgumentException {
		if (this.max < this.min) {
			throw new IllegalStateException("max height cannot be less than min");
		}
		if (this.groundcover == null) {
			throw new IllegalStateException("Groundcoverlayers not defined");
		}
		if (this.populator == null) {
			throw new IllegalStateException("Populators not defined");
		}
		if (this.genpop == null) {
			throw new IllegalStateException("Generationpopulators not defined");
		}
		SpongeBiomeGenerationSettings gen = new SpongeBiomeGenerationSettings();
		gen.setMinHeight(this.min);
		gen.setMaxHeight(this.max);
		gen.getGroundCoverLayers().addAll(this.groundcover);
		gen.getPopulators().addAll(this.populator);
		gen.getGenerationPopulators().addAll(this.genpop);
		return gen;
	}

}
