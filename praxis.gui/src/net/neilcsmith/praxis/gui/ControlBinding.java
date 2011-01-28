/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Neil C Smith.
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

package net.neilcsmith.praxis.gui;

import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.info.ControlInfo;

/**
 *
 * @author Neil C Smith
 */
public abstract class ControlBinding {

    public abstract ControlAddress getAddress();
    
    public abstract ControlInfo getBindingInfo();
    
    public abstract CallArguments getArguments();
    
    protected abstract void send(Adaptor adaptor, CallArguments args);
    
    protected abstract void updateAdaptorConfiguration(Adaptor adaptor);
    

    
    protected void bind(Adaptor adaptor) {
        adaptor.setBinding(this);
    }
    
    protected void unbind(Adaptor adaptor) {
        adaptor.setBinding(null);
    }
    
    public static abstract class Adaptor {
        
        private ControlBinding binding;
        private SyncRate syncRate = SyncRate.None;
        private boolean active;
        
        private void setBinding(ControlBinding binding) {
            this.binding = binding;
            updateBindingConfiguration();
        }
        
        public final ControlBinding getBinding() {
            return binding;
        }
        
        public final SyncRate getSyncRate() {
            return syncRate;
        }
        
        public final boolean isActive() {
            return active;
        }
        
        public final void setActive(boolean active) {
            if (active != this.active) {
                this.active = active;
                if (binding != null) {
                    binding.updateAdaptorConfiguration(this);
                }
            }
        }
        
        public final void setSyncRate(SyncRate syncRate) {
            if (syncRate != this.syncRate) {
                this.syncRate = syncRate;
                if (binding != null) {
                    binding.updateAdaptorConfiguration(this);
                }
            }
        }
        
        protected final void send(CallArguments args) {
            if (this.binding != null) {
                binding.send(this, args);
            }
        }

        public boolean getValueIsAdjusting() {
            return false;
        }

        public void onResponse(CallArguments args) {}

        public void onError(CallArguments args) {}

        public abstract void update();
        
        public abstract void updateBindingConfiguration();
        
        
        
    }
    
    public static enum SyncRate {
        None, Low, Medium, High;
    }

}
