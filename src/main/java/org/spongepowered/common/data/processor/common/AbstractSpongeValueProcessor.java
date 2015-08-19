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
package org.spongepowered.common.data.processor.common;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.ValueProcessor;

public abstract class AbstractSpongeValueProcessor<E, V extends BaseValue<E>> implements ValueProcessor<E, V> {

    private final Key<V> key;

    protected AbstractSpongeValueProcessor(Key<V> key) {
        this.key = checkNotNull(key, "The key is null!");
    }

    @Override
    public final Key<? extends BaseValue<E>> getKey() {
        return this.key;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, BaseValue<E> value) {
        return offerToStore(container, value.get());
    }

    @Override
    public DataTransactionResult transform(ValueContainer<?> container, Function<E, E> function) {
        Optional<E> optionalValue = getValueFromContainer(container);
        if (optionalValue.isPresent()) {
            return offerToStore(container, checkNotNull(function.apply(optionalValue.get()), "function returned null"));
        } else {
            return DataTransactionBuilder.failNoData();
        }
    }

    @Override
    public Optional<V> getApiValueFromContainer(ValueContainer<?> container) {
        Optional<E> optionalValue = getValueFromContainer(container);
        if(optionalValue.isPresent()) {
            return Optional.of(constructValue(optionalValue.get()));
        } else {
            return Optional.absent();
        }
    }

}