/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.impl;

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.info.ControlInfo;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractSingleArgProperty extends AbstractProperty {

    protected AbstractSingleArgProperty(ControlInfo info) {
        super(info);
    }

    public ControlPort.Input createPort() {
        return new InputPort();
    }

    protected void setArguments(long time, CallArguments args) throws Exception {
        set(time, args.getArg(0));
    }

    protected CallArguments getArguments() {
        return CallArguments.create(get());
    }

    protected abstract void set(long time, Argument value) throws Exception;

    protected abstract void set(long time, double value) throws Exception;

    protected abstract Argument get();

    private class InputPort extends AbstractControlInputPort {

        @Override
        public void receive(long time, double value) {
            try {
                if (isLatest(time)) {
                    set(time, value);
                    setLatest(time);
                }
            } catch (Exception ex) {
                // @TODO logging
            }
        }

        @Override
        public void receive(long time, Argument value) {
            try {
                if (isLatest(time)) {
                    set(time, value);
                    setLatest(time);
                }
            } catch (Exception ex) {
                // @TODO logging
            }
        }
    }
}
