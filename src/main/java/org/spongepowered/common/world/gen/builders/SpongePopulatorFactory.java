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
package org.spongepowered.common.world.gen.builders;

import net.minecraft.world.gen.feature.WorldGenDesertWells;
import org.spongepowered.api.world.gen.PopulatorFactory;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.populator.BigMushroom.Builder;
import org.spongepowered.api.world.gen.populator.BlockBlob;
import org.spongepowered.api.world.gen.populator.Cactus;
import org.spongepowered.api.world.gen.populator.DesertWell;
import org.spongepowered.api.world.gen.populator.DoublePlant;
import org.spongepowered.api.world.gen.populator.Dungeon;
import org.spongepowered.api.world.gen.populator.EnderCrystalPlatform;
import org.spongepowered.api.world.gen.populator.Flower;
import org.spongepowered.api.world.gen.populator.Forest;
import org.spongepowered.api.world.gen.populator.Glowstone;
import org.spongepowered.api.world.gen.populator.IcePath;
import org.spongepowered.api.world.gen.populator.IceSpike;
import org.spongepowered.api.world.gen.populator.Lake;
import org.spongepowered.api.world.gen.populator.Melon;
import org.spongepowered.api.world.gen.populator.Mushroom;
import org.spongepowered.api.world.gen.populator.Ore;
import org.spongepowered.api.world.gen.populator.Pumpkin;
import org.spongepowered.api.world.gen.populator.RandomBlock;
import org.spongepowered.api.world.gen.populator.RandomObject;
import org.spongepowered.api.world.gen.populator.Reed;
import org.spongepowered.api.world.gen.populator.SeaFloor;
import org.spongepowered.api.world.gen.populator.Shrub;
import org.spongepowered.api.world.gen.populator.Vine;
import org.spongepowered.api.world.gen.populator.WaterLily;

public class SpongePopulatorFactory implements PopulatorFactory {

    public static SpongePopulatorFactory getInstance() {
        return Holder.INSTANCE;
    }
    
    private final PopulatorObject well = (PopulatorObject) new WorldGenDesertWells();

    @Override
    public Builder createBigMushroomPopulator() {
        return new BigMushroomBuilder();
    }

    @Override
    public Mushroom.Builder createMushroomPopulator() {
        return new MushroomBuilder();
    }

    @Override
    public BlockBlob.Builder createBlockBlockPopulator() {
        return new BlockBlobBuilder();
    }

    @Override
    public Cactus.Builder createCactusPopulator() {
        return new CactusBuilder();
    }

    @Override
    public org.spongepowered.api.world.gen.populator.DeadBush.Builder createDeadBushPopulator() {
        return new DeadBushBuilder();
    }

    @Override
    public DesertWell.Builder createDesertWellPopulator() {
        return new DesertWellBuilder();
    }

    @Override
    public DoublePlant.Builder createDoublePlantPopulator() {
        return new DoublePlantBuilder();
    }

    @Override
    public Dungeon.Builder createDungeonPopulator() {
        return new DungeonBuilder();
    }

    @Override
    public EnderCrystalPlatform.Builder createEnderCrystalPlatformPopulator() {
        return new EnderCrystalPlatformBuilder();
    }

    @Override
    public Flower.Builder createFlowerPopulator() {
        return new FlowersBuilder();
    }

    @Override
    public Forest.Builder createForestPopulator() {
        return new ForestBuilder();
    }

    @Override
    public Glowstone.Builder createGlowstonePopulator() {
        return new GlowstoneBuilder();
    }

    @Override
    public IcePath.Builder createIcePathPopulator() {
        return new IcePathBuilder();
    }

    @Override
    public IceSpike.Builder createIceSpikePopulator() {
        return new IceSpikeBuilder();
    }

    @Override
    public Lake.Builder createLakePopulator() {
        return new LakeBuilder();
    }

    @Override
    public Melon.Builder createMelonPopulator() {
        return new MelonBuilder();
    }

    @Override
    public Ore.Builder createOrePopulator() {
        return new OreBuilder();
    }

    @Override
    public Pumpkin.Builder createPumpkinPopulator() {
        return new PumpkinBuilder();
    }

    @Override
    public RandomBlock.Builder createRandomBlockPopulator() {
        return new RandomBlockBuilder();
    }

    @Override
    public RandomObject.Builder createRandomObjectPopulator() {
        return new RandomObjectBuilder();
    }

    @Override
    public Reed.Builder createReedPopulator() {
        return new ReedsBuilder();
    }

    @Override
    public SeaFloor.Builder createSeaFloorPopulator() {
        return new SeaFloorBuilder();
    }

    @Override
    public Shrub.Builder createShrubPopulator() {
        return new ShrubBuilder();
    }

    @Override
    public Vine.Builder createVinePopulator() {
        return new VinesBuilder();
    }

    @Override
    public WaterLily.Builder createWaterLilyPopulator() {
        return new WaterLilyBuilder();
    }

    private SpongePopulatorFactory() { }

    private static final class Holder {
        private static final SpongePopulatorFactory INSTANCE = new SpongePopulatorFactory();
    }

    @Override
    public PopulatorObject getDesertWellObject() {
        return this.well;
    }

}
