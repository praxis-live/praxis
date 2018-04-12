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
 *
 */
package org.praxislive.code.userapi;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import org.praxislive.code.CodeContext;
import org.praxislive.code.DefaultCodeDelegate;
import org.praxislive.core.Value;
import org.praxislive.core.types.PBoolean;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PString;
import org.praxislive.core.Value;
import org.praxislive.logging.LogLevel;
import org.praxislive.util.ArrayUtils;

/**
 * A field type for properties (see {@link P @P}). The Property type also backs
 * other none-resource-loading properties - use {@link DefaultCodeDelegate#p(java.lang.String)
 * p(String id)} to access the backing Property.
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

    protected abstract void setImpl(long time, Value arg) throws Exception;

    protected abstract void setImpl(long time, double value) throws Exception;

    protected abstract Value getImpl();

    protected abstract double getImpl(double def);

    /**
     * Return the current value.
     *
     * @return value
     */
    public Value get() {
        return getImpl();
    }

    /**
     * Return the current value as a double, or zero if the value isn't numeric.
     *
     * @see DefaultCodeDelegate#d(org.praxislive.code.userapi.Property)
     * @return current value as double
     */
    public double getDouble() {
        return getDouble(0);
    }

    /**
     * Return the current value as a double, or the provided default if the
     * value isn't numeric.
     *
     * @return current value as double
     */
    public double getDouble(double def) {
        return getImpl(def);
    }

    /**
     * Return the current value as an int, or zero if the value isn't numeric.
     * Floating point values are rounded to the nearest int.
     *
     * @see DefaultCodeDelegate#i(org.praxislive.code.userapi.Property)
     * @return current value as int
     */
    public int getInt() {
        return (int) Math.round(getDouble());
    }

    /**
     * Return the current value as an int, or the provided default if the value
     * isn't numeric. Floating point values are rounded to the nearest int.
     *
     * @return current value as int
     */
    public int getInt(int def) {
        return (int) Math.round(getDouble(def));
    }

    /**
     * Return the current value as a boolean, or false if the value isn't a
     * valid boolean.
     *
     * @return current value as boolean
     */
    public boolean getBoolean() {
        return getBoolean(false);
    }

    /**
     * Return the current value as a boolean, or the provided default if the
     * value isn't a valid boolean.
     *
     * @return current value as boolean
     */
    public boolean getBoolean(boolean def) {
        return PBoolean.from(get()).orElse(def ? PBoolean.TRUE : PBoolean.FALSE).value();
    }

    /**
     * Set the current value. Also stops any active animation.
     *
     * @param value Value subclass to set
     * @return this
     */
    public Property set(Value value) {
        if (value == null) {
            throw new NullPointerException();
        }
        finishAnimating();
        try {
            setImpl(context.getTime(), value);
        } catch (Exception ex) {
            // no op?
        }
        return this;
    }
    
//    @Deprecated
//    public Property set(Value value) {
//        Value v = value instanceof Value ? (Value) value : PString.valueOf(value);
//        return set(v);
//    }

    /**
     * Set the current value. Also stops any active animation.
     *
     * @param value double value to set
     * @return this
     */
    public Property set(double value) {
        finishAnimating();
        try {
            setImpl(context.getTime(), value);
        } catch (Exception ex) {
            // no op?
        }
        return this;
    }

    /**
     * Call the provided consumer with the double value whenever the value
     * changes. This is a shorthand for {@code values().link(consumer);}. The
     * double value will be as if calling {@link #getDouble()}.
     *
     * @param consumer double consumer
     * @return this
     */
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

    /**
     * Return a new {@link Linkable.Double} for observing changing values. The
     * double value will be as if calling {@link #getDouble()}.
     *
     * @return Linkable.Double of values.
     */
    public Linkable.Double values() {
        return new DoubleLink();
    }

    /**
     * Call the provided consumer when the value changes, transformed using the
     * converter into the required type T. This is shorthand for
     * {@code valuesAs(converter).link(consumer);}.
     *
     * @param <T> type
     * @param converter convert Value into required type
     * @param consumer
     * @return this
     */
    public <T> Property linkAs(
            Function<Value, T> converter,
            Consumer<T> consumer) {
        ValueLink al = new ValueLink();
        al.map(converter).link(consumer);
        return this;
    }

    public <T> Property linkAs(
            Function<Value, T> converter,
            Consumer<T>... consumers) {
        for (Consumer<T> consumer : consumers) {
            linkAs(converter, consumer);
        }
        return this;
    }

    /**
     * Return a new {@link Linkable} for observing changing values. The value
     * will be mapped to the required type using the passed in converter.
     *
     * @param <T> required type
     * @param converter convert Value into required type
     * @return Linkable of values
     */
    public <T> Linkable<T> valuesAs(Function<Value, T> converter) {
        return new ValueLink().map(converter);
    }

    /**
     * Return a new {@link Linkable} for observing changing values. The value
     * will be mapped to the provided Value sub-type. If the value cannot be
     * coerced into the required type no value will be received by the created
     * Linkable.
     *
     * @param <T> required type
     * @param type Sub-type of Value
     * @return Linkable of values
     */
    public <T extends Value> Linkable<T> valuesAs(Class<T> type) {
        Function<Value, Optional<T>> converter = Value.Type.of(type).converter();
        return new ValueLink()
                .map(converter::apply)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    /**
     * Clear all Linkables from the Property.
     *
     * @return this
     */
    public Property clearLinks() {
        links = new BaseLink[0];
        return this;
    }

    /**
     * Return the Animator for the Property, creating it if necessary.
     *
     * @return property animator
     */
    public Animator animator() {
        if (animator == null) {
            animator = new Animator(this);
        }
        return animator;
    }

    /**
     * Animate the property value to the provided values. This is a shorthand
     * for calling {@code animator().to(...)}.
     * <p>
     * This method returns the animator so that you can chain calls, eg.
     * {@code prop.to(1, 0).in(1, 0.25).easeInOut();}
     *
     * @return property animator
     */
    public Property.Animator to(double... to) {
        return animator().to(to);
    }

    /**
     * Return whether the property is currently animating.
     *
     * @return property animator active
     */
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
            try {
                link.update(value);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }
    }

    protected void updateLinks(Value value) {
        for (BaseLink link : links) {
            try {
                link.update(value);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
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

        void update(Value value);

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
        public void update(Value value) {
            PNumber.from(value).ifPresent((pn) -> consumer.accept(pn.value()));
        }

    }

    private class ValueLink implements BaseLink, Linkable<Value> {

        private Consumer<Value> consumer;

        @Override
        public void link(Consumer<Value> consumer) {
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
        public void update(Value value) {
            try {
                consumer.accept(value);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }

    }

    /**
     * Provides keyframe animation support for Property. Methods return this so
     * that they can be chained - eg. {@code to(1, 0).in(1, 0.25).easeInOut()}
     */
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

        /**
         * Set the target values for animation and start animation. The number
         * of values provided to this method controls the number of keyframes.
         *
         * @param to target values
         * @return this
         */
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

        /**
         * Set the time in seconds for each keyframe. The number of provided
         * values may be different than the number of keyframes passed to to().
         * Values will be cycled through as needed.
         * <p>
         * eg. {@code to(100, 50, 250).in(1, 0.5)} is the same as
         * {@code to(100, 50, 250).in(1, 0.5, 1)}
         *
         * @param in times in seconds
         * @return this
         */
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

        /**
         * Set the easing mode for each keyframe. The number of provided values
         * may be different than the number of keyframes passed to to(). Values
         * will be cycled through as needed.
         *
         * @param easing easing mode to use
         * @return this
         */
        public Animator easing(Easing... easing) {
            if (easing.length < 1) {
                this.easing = DEFAULT_EASING;
            } else {
                this.easing = easing;
            }
            return this;
        }

        /**
         * Convenience method to use {@link Easing#linear} easing for all
         * keyframes.
         *
         * @return this
         */
        public Animator linear() {
            return easing(Easing.linear);
        }

        /**
         * Convenience method to use {@link Easing#ease} easing for all
         * keyframes.
         *
         * @return this
         */
        public Animator ease() {
            return easing(Easing.ease);
        }

        /**
         * Convenience method to use {@link Easing#easeIn} easing for all
         * keyframes.
         *
         * @return this
         */
        public Animator easeIn() {
            return easing(Easing.easeIn);
        }

        /**
         * Convenience method to use {@link Easing#easeOut} easing for all
         * keyframes.
         *
         * @return this
         */
        public Animator easeOut() {
            return easing(Easing.easeOut);
        }

        /**
         * Convenience method to use {@link Easing#easeInOut} easing for all
         * keyframes.
         *
         * @return this
         */
        public Animator easeInOut() {
            return easing(Easing.easeInOut);
        }

        /**
         * Stop animating. The current property value will be retained.
         *
         * @return this
         */
        public Animator stop() {
            index = 0;
            animating = false;
            property.stopClock();
            return this;
        }

        /**
         * Whether an animation is currently active.
         *
         * @return animation active
         */
        public boolean isAnimating() {
            return animating;
        }

        /**
         * Set a consumer to be called each time the Animator finishes animation.
         * Also calls the consumer immediately if no animation is currently active.
         * <p>
         * Unlike restarting an animation by polling isAnimating(), an animation
         * started inside this consumer will take into account any time overrun
         * between the target and actual finish time of the completing animation.
         * 
         * @param whenDoneConsumer function to call
         * @return this
         */
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
                try {
                    onDoneConsumer.accept(property);
                } catch (Exception ex) {
                    property.context.getLog().log(LogLevel.ERROR, ex);
                }
            }
            this.overrun = 0;
            if (!animating) {
                property.stopClock();
            }
        }

    }

}
