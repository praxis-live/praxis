/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.code.userapi;

import java.util.Objects;
import java.util.function.IntConsumer;
import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.logging.LogLevel;
import net.neilcsmith.praxis.util.ArrayUtils;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
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

    public Trigger clearLinks() {
        links = new Link[0];
        return this;
    }

    public Trigger link(Runnable runnable) {
        Link l = new Link();
        l.link(i -> runnable.run());
        return this;
    }

    public Linkable.Int on() {
        return new Link();
    }

    public Trigger index(int idx) {
        if (idx < 0) {
            throw new IllegalArgumentException("Index cannot be less than zero");
        }
        index = (idx % maxIndex);
        return this;
    }
    
    public Trigger maxIndex(int max) {
        if (max < 1) {
            throw new IllegalArgumentException("Max index must be greater than 0");
        }
        maxIndex = max;
        index %= maxIndex;
        return this;
    }

    public int index() {
        return index;
    }
    
    public int maxIndex() {
        return maxIndex;
    }
    
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
