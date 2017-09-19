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
 *
 */
package net.neilcsmith.praxis.code.userapi;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.logging.LogLevel;
import net.neilcsmith.praxis.util.ArrayUtils;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class Property {

    private final static long TO_NANO = 1000000000;

    private final Listener listener;

    private CodeContext<?> context;
    private Animator animator;
    private BaseLink[] links;

    protected Property() {
        this.listener = new Listener();
        this.links = new BaseLink[0];
    }

    protected void attach(CodeContext<?> context, Property previous) {
        this.context = context;
        if (previous != null && previous.animator != null) {
            animator = previous.animator;
            previous.animator = null;
            animator.attach(this);
            if (animator.isAnimating()) {
                previous.stopClock();
                this.startClock();
            }
        }
    }

    protected abstract void setImpl(long time, Argument arg) throws Exception;

    protected abstract void setImpl(long time, double value) throws Exception;

    protected abstract Argument getImpl();

    protected abstract double getImpl(double def);

    public Argument get() {
        return getImpl();
    }

    public double getDouble() {
        return getDouble(0);
    }

    public double getDouble(double def) {
        return getImpl(def);
    }

    public int getInt() {
        return (int) Math.round(getDouble());
    }

    public int getInt(int def) {
        return (int) Math.round(getDouble(def));
    }

    public boolean getBoolean() {
        return getBoolean(false);
    }

    public boolean getBoolean(boolean def) {
        try {
            return PBoolean.coerce(get()).value();
        } catch (ArgumentFormatException ex) {
            return def;
        }
    }

    public Property set(Argument arg) {
        if (arg == null) {
            throw new NullPointerException();
        }
        finishAnimating();
        try {
            setImpl(context.getTime(), arg);
        } catch (Exception ex) {
            // no op?
        }
        return this;
    }

    public Property set(double value) {
        finishAnimating();
        try {
            setImpl(context.getTime(), value);
        } catch (Exception ex) {
            // no op?
        }
        return this;
    }

    public Property link(DoubleConsumer consumer) {
        DoubleLink dl = new DoubleLink();
        dl.link(consumer);
        return this;
    }

    public Property link(DoubleConsumer... consumers) {
        for (DoubleConsumer consumer : consumers) {
            link(consumer);
        }
        return this;
    }

    public Linkable.Double values() {
        return new DoubleLink();
    }

    public <T> Property linkAs(
            Function<Argument, T> converter,
            Consumer<T> consumer) {
        ArgumentLink al = new ArgumentLink();
        al.map(converter).link(consumer);
        return this;
    }

    public <T> Property linkAs(
            Function<Argument, T> converter,
            Consumer<T>... consumers) {
        for (Consumer<T> consumer : consumers) {
            linkAs(converter, consumer);
        }
        return this;
    }

    public <T> Linkable<T> valuesAs(Function<Argument, T> converter) {
        return new ArgumentLink().map(converter);
    }

    public Property clearLinks() {
        links = new BaseLink[0];
        return this;
    }

    public Animator animator() {
        if (animator == null) {
            animator = new Animator(this);
        }
        return animator;
    }

    public Property.Animator to(double... to) {
        return animator().to(to);
    }

    public boolean isAnimating() {
        return animator != null && animator.isAnimating();
    }

    protected void finishAnimating() {
        if (animator != null) {
            animator.stop();
        }
    }

    protected void updateLinks(double value) {
        for (BaseLink link : links) {
            link.update(value);
        }
    }

    protected void updateLinks(Argument value) {
        for (BaseLink link : links) {
            link.update(value);
        }
    }

    protected boolean hasLinks() {
        return links.length > 0;
    }

    protected void reset(boolean full) {
        clearLinks();
        if (animator != null) {
            animator.onDoneConsumer = null;
        }
    }

    private void startClock() {
        context.addClockListener(listener);
    }

    private void stopClock() {
        context.removeClockListener(listener);
    }

    private class Listener implements CodeContext.ClockListener {

        @Override
        public void tick() {
            if (animator == null || !animator.animating) {
                assert false;
                return;
            }
            animator.tick();
        }
    }

    private static interface BaseLink {

        void update(double value);

        void update(Argument value);

    }

    private class DoubleLink implements BaseLink, Linkable.Double {

        private DoubleConsumer consumer;

        @Override
        public void link(DoubleConsumer consumer) {
            if (this.consumer != null) {
                throw new IllegalStateException("Cannot link multiple consumers in one chain");
            }
            this.consumer = Objects.requireNonNull(consumer);
            update(getDouble());
            links = ArrayUtils.add(links, this);
        }

        @Override
        public void update(double value) {
            try {
                consumer.accept(value);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }

        @Override
        public void update(Argument value) {
            PNumber.from(value).ifPresent((pn) -> consumer.accept(pn.value()));
        }

    }

    private class ArgumentLink implements BaseLink, Linkable<Argument> {

        private Consumer<Argument> consumer;

        @Override
        public void link(Consumer<Argument> consumer) {
            if (this.consumer != null) {
                throw new IllegalStateException("Cannot link multiple consumers in one chain");
            }
            this.consumer = Objects.requireNonNull(consumer);
            update(get());
            links = ArrayUtils.add(links, this);
        }

        @Override
        public void update(double value) {
            update(PNumber.valueOf(value));
        }

        @Override
        public void update(Argument value) {
            try {
                consumer.accept(value);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }

    }

    public static class Animator {

        private final static long[] DEFAULT_IN = new long[]{0};
        private final static Easing[] DEFAULT_EASING = new Easing[]{Easing.linear};

        private Property property;
        int index;
        private double[] to;
        private long[] in;
        private Easing[] easing;

        private double fromValue;
        private long fromTime;
        private boolean animating;

        private Consumer<Property> onDoneConsumer;
        private long overrun;

        private Animator(Property p) {
            this.property = p;
            in = DEFAULT_IN;
            easing = DEFAULT_EASING;
        }

        private void attach(Property p) {
            this.property = p;
        }

        public double to() {
            return to[index];
        }

        public Animator to(double... to) {
            if (to.length < 1) {
                throw new IllegalArgumentException();
            }
            this.to = to;
            index = 0;
            in = DEFAULT_IN;
            easing = DEFAULT_EASING;
            fromValue = property.getDouble();
            fromTime = property.context.getTime();
            if (!animating) {
                property.startClock();
                animating = true;
            }
            return this;
        }

        public Animator in(double... in) {
            if (in.length < 1) {
                this.in = DEFAULT_IN;
            } else {
                this.in = new long[in.length];
                for (int i = 0; i < in.length; i++) {
                    this.in[i] = (long) (in[i] * TO_NANO);
                }
                this.in[0] = Math.max(0, this.in[0] - overrun);
            }
            return this;
        }

        public Animator easing(Easing... easing) {
            if (easing.length < 1) {
                this.easing = DEFAULT_EASING;
            } else {
                this.easing = easing;
            }
            return this;
        }

        public Animator linear() {
            return easing(Easing.linear);
        }

        public Animator ease() {
            return easing(Easing.ease);
        }

        public Animator easeIn() {
            return easing(Easing.easeIn);
        }

        public Animator easeOut() {
            return easing(Easing.easeOut);
        }

        public Animator easeInOut() {
            return easing(Easing.easeInOut);
        }

        public Animator stop() {
            index = 0;
            animating = false;
            property.stopClock();
            return this;
        }

        public boolean isAnimating() {
            return animating;
        }

        public Animator whenDone(Consumer<Property> whenDoneConsumer) {
            this.onDoneConsumer = whenDoneConsumer;
            if (!animating) {
                onDoneConsumer.accept(property);
            }
            return this;
        }

        private void tick() {
            if (!animating) {
                assert false;
                return;
            }
            try {
                long currentTime = property.context.getTime();
                double toValue = to[index];
                long duration = in[index % in.length];
                double proportion;
                if (duration < 1) {
                    proportion = 1;
                } else {
                    proportion = (currentTime - fromTime) / (double) duration;
                }
                if (proportion >= 1) {
                    index++;
                    if (index >= to.length) {
                        finish(currentTime - (fromTime + duration));
                    } else {
                        fromValue = toValue;
                        fromTime += duration;
                    }
                    property.setImpl(fromTime, toValue);
                } else if (proportion > 0) {
                    Easing ease = easing[index % easing.length];
                    double d = ease.calculate(proportion);
                    d = (d * (toValue - fromValue)) + fromValue;
                    property.setImpl(fromTime, d);
                } else {
//                    p.setImpl(fromTime, fromValue); ???
                }
            } catch (Exception exception) {
                finish(0);
            }

        }

        private void finish(long overrun) {
            index = 0;
            animating = false;
            this.overrun = overrun;
            if (onDoneConsumer != null) {
                onDoneConsumer.accept(property);
            }
            this.overrun = 0;
            if (!animating) {
                property.stopClock();
            }
        }

    }

}
