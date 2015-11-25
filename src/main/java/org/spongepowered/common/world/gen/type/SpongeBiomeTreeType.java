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
package org.spongepowered.common.world.gen.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Optional;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.type.BiomeTreeType;

import javax.annotation.Nullable;

public class SpongeBiomeTreeType implements BiomeTreeType {

    private String name;
    private PopulatorObject smallObject;
    private @Nullable PopulatorObject largeObject;

    public SpongeBiomeTreeType(String name, PopulatorObject small) {
        this.name = name;
        this.smallObject = small;
    }

    public SpongeBiomeTreeType(String name, PopulatorObject small, @Nullable PopulatorObject large) {
        this(name, small);
        this.largeObject = large;
    }

    @Override
    public String getId() {
        return this.name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public PopulatorObject getPopulatorObject() {
        return this.smallObject;
    }

    @Override
    public void setPopulatorObject(PopulatorObject object) {
        this.smallObject = checkNotNull(object);
    }

    @Override
    public boolean hasLargeEquivalent() {
        return this.largeObject != null;
    }

    @Override
    public Optional<PopulatorObject> getLargePopulatorObject() {
        return Optional.fromNullable(this.largeObject);
    }

    @Override
    public void setLargePopulatorObject(@Nullable PopulatorObject object) {
        this.largeObject = object;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SpongeBiomeTreeType)) {
            return false;
        }
        SpongeBiomeTreeType b = (SpongeBiomeTreeType) o;
        return getId().equals(b.getId());
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        ToStringHelper tsh = Objects.toStringHelper(this)
                .add("id", this.getId())
                .add("smallObj", this.smallObject.getClass().getName());
        if (this.largeObject != null) {
            tsh.add("largeObj", this.largeObject.getClass().getName());
        }
        return tsh.toString();
    }

}
