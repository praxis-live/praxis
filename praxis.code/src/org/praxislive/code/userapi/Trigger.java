/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.code.userapi;

import java.util.Objects;
import java.util.function.IntConsumer;
import org.praxislive.code.CodeContext;
import org.praxislive.logging.LogLevel;
import org.praxislive.util.ArrayUtils;

/**
 * A field type for triggers (actions) - see {@link T @T}. The Trigger type
 * provides a Linkable.Int for listening for triggers, and maintains a count of
 * each time the trigger has been called (useful for sequencing). It is also
 * possible to connect Runnable functions to be called on each trigger.
 */
public abstract class Trigger {

    private Link[] links;
    private int index;
    private int maxIndex;
    private CodeContext<?> context;

    protected Trigger() {
        this.links = new Link[0];
        maxIndex = Integer.MAX_VALUE;
    }

    protected void attach(CodeContext<?> context, Trigger previous) {
        this.context = context;
        if (previous != null) {
            index = previous.index;
        }
    }

    @Deprecated
    public abstract boolean poll();

    /**
     * Clear all Linkables from this Trigger.
     *
     * @return this
     */
    public Trigger clearLinks() {
        links = new Link[0];
        return this;
    }

    /**
     * Run the provided Runnable each time this Trigger is triggered. This
     * method is shorthand for {@code on().link(i -> runnable.run());}.
     *
     * @param runnable function to run on trigger
     * @return this
     */
    public Trigger link(Runnable runnable) {
        Link l = new Link();
        l.link(i -> runnable.run());
        return this;
    }

    /**
     * Returns a new {@link Linkable.Int} for listening to each trigger. The int
     * passed to the created linkable will be the same as index, incrementing
     * each time, wrapping at maxIndex.
     *
     * @return new Linkable.Int for reacting to triggers
     */
    public Linkable.Int on() {
        return new Link();
    }

    /**
     * Set the current index. Must not be negative.
     *
     * @param idx new index
     * @return this
     */
    public Trigger index(int idx) {
        if (idx < 0) {
            throw new IllegalArgumentException("Index cannot be less than zero");
        }
        index = (idx % maxIndex);
        return this;
    }

    /**
     * Set the maximum index, at which the index will wrap back to zero.
     *
     * @param max maximum index
     * @return this
     */
    public Trigger maxIndex(int max) {
        if (max < 1) {
            throw new IllegalArgumentException("Max index must be greater than 0");
        }
        maxIndex = max;
        index %= maxIndex;
        return this;
    }

    /**
     * Get the current index.
     *
     * @return current index
     */
    public int index() {
        return index;
    }

    /**
     * Get the current maximum index.
     *
     * @return maximum index
     */
    public int maxIndex() {
        return maxIndex;
    }

    /**
     * Manually trigger this Trigger. Useful for chaining this trigger to other
     * sources of input. Otherwise behaves as if externally called, incrementing
     * index and calling linkables.
     * 
     * @return this
     */
    public Trigger trigger() {
        trigger(context.getTime());
        return this;
    }

    protected void trigger(long time) {
        if (hasLinks()) {
            triggerLinks();
        }
        incrementIndex();
    }

    protected boolean hasLinks() {
        return links.length > 0;
    }

    protected void triggerLinks() {
        for (Link l : links) {
            try {
                l.fire(index);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }
    }

    protected void incrementIndex() {
        index = (index + 1) % maxIndex;
    }

    private class Link implements Linkable.Int {

        private IntConsumer consumer;

        @Override
        public void link(IntConsumer consumer) {
            if (this.consumer != null) {
                throw new IllegalStateException("Cannot link multiple consumers in one chain");
            }
            this.consumer = Objects.requireNonNull(consumer);
            links = ArrayUtils.add(links, this);
        }

        private void fire(int value) {
            try {
                consumer.accept(value);
            } catch (Exception ex) {

            }
        }

    }

}
