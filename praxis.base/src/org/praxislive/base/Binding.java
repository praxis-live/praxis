/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2019 Neil C Smith.
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
package org.praxislive.base;

import java.util.List;
import java.util.Optional;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.Value;

/**
 * A BindingContext will normally have one Binding for each bound address. The
 * Binding may have more than one BindingAdaptor attached to it.
 */
public abstract class Binding {

    /**
     * Possible rates for syncing.
     */
    public static enum SyncRate {
        None, Low, Medium, High;
    }

    /**
     * Get the ControlInfo of the bound Control, if available.
     *
     * @return Optional of ControlInfo
     */
    public abstract Optional<ControlInfo> getControlInfo();

    /**
     * Get the most recent synced values, if the Control is a property.
     *
     * @return synced values
     */
    public abstract List<Value> getValues();

    protected abstract void send(Adaptor adaptor, List<Value> args);

    protected abstract void updateAdaptorConfiguration(Adaptor adaptor);

    protected void bind(Adaptor adaptor) {
        adaptor.setBinding(this);
    }

    protected void unbind(Adaptor adaptor) {
        adaptor.setBinding(null);
    }

    /**
     * Abstract type for binding to a Control.
     */
    public static abstract class Adaptor {

        private Binding binding;
        private SyncRate syncRate = SyncRate.None;
        private boolean active;

        private void setBinding(Binding binding) {
            this.binding = binding;
            updateBindingConfiguration();
        }

        /**
         * Get the Binding this adaptor is attached to. The binding provides
         * access to latest values and the ControlInfo.
         *
         * @return binding
         */
        public final Binding getBinding() {
            return binding;
        }

        /**
         * The current SyncRate.
         *
         * @return syncrate
         */
        public final SyncRate getSyncRate() {
            return syncRate;
        }

        /**
         * Whether this Adaptor is currently active.
         *
         * @return active
         */
        public final boolean isActive() {
            return active;
        }

        /**
         * Set whether this Adaptor is currently active. By default an Adaptor
         * is inactive. The Binding will not sync unless at least one attached
         * Adaptor is active and has a sync rate above None.
         *
         * @param active
         */
        public final void setActive(boolean active) {
            if (active != this.active) {
                this.active = active;
                if (binding != null) {
                    binding.updateAdaptorConfiguration(this);
                }
            }
        }

        /**
         * Set the SyncRate of the Adaptor. By default an Adaptor has a sync
         * rate of None. The Binding will not sync unless at least one attached
         * Adaptor is active and has a sync rate above None. The highest active
         * sync rate will be used by the binding.
         *
         * @param syncRate
         */
        public final void setSyncRate(SyncRate syncRate) {
            if (syncRate != this.syncRate) {
                this.syncRate = syncRate;
                if (binding != null) {
                    binding.updateAdaptorConfiguration(this);
                }
            }
        }

        /**
         * Send the provided values to the Control. Other attached Adaptors will
         * be immediately updated.
         *
         * @param args
         */
        protected final void send(List<Value> args) {
            if (this.binding != null) {
                binding.send(this, args);
            }
        }

        /**
         * Whether the Adaptor is currently actively updating and sending
         * values, eg. as a response to user input. The Binding implementation
         * will normally send quiet calls in such cases as the values are
         * expected to be superseded before a reply is received.
         *
         * @return value currently being adjuested
         */
        protected boolean getValueIsAdjusting() {
            return false;
        }

        /**
         * An optional hook for adaptors to access the returned response from a
         * call to send. This will only be called on the adaptor that initiated
         * the call.
         *
         * @param args returned values
         */
        protected void onResponse(List<Value> args) {
        }

        /**
         * An optional hook for adaptors to access any error response from a
         * call to send. This will only be called on the adaptor that initiated
         * the call.
         *
         * @param args error values
         */
        protected void onError(List<Value> args) {
        }

        /**
         * Optional hook called when the Binding configuration has changed. Eg.
         * new ControlInfo available.
         */
        protected void updateBindingConfiguration() {
        }

        /**
         * Optional hook called whenever values have been updated, by a sync
         * call or another Adaptor.
         */
        protected void update() {
        }

    }

}
