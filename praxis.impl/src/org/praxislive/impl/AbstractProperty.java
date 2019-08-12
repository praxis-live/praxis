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
package org.praxislive.impl;

import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.ControlInfo;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractProperty extends AbstractControl {

    private ControlInfo info;
    private long latest;
    private boolean latestSet;

    protected AbstractProperty(ControlInfo info) {
        this.info = info;
    }

    public void call(Call call, PacketRouter router) throws Exception {
        Call.Type type = call.getType();
        if (type == Call.Type.INVOKE || type == Call.Type.INVOKE_QUIET) {
            CallArguments args = call.getArgs();
            int argCount = args.getSize();
            long time = call.time();
            if (argCount > 0) {
                if (isLatest(time)) {
                    setArguments(time, args);
                    setLatest(time);
                }
                if (type == Call.Type.INVOKE) {
                    router.route(Call.createReturnCall(call, args));
                }
            } else {
                // ignore quiet hint?
                router.route(Call.createReturnCall(call, getArguments()));
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    protected void setLatest(long time) {
        latestSet = true;
        latest = time;
    }

    protected boolean isLatest(long time) {
        if (latestSet) {
            return (time - latest) >= 0;
        } else {
            return true;
        }

    }

    public ControlInfo getInfo() {
        return info;
    }

    protected abstract void setArguments(long time, CallArguments args) throws Exception;

    protected abstract CallArguments getArguments();
    
    public static abstract class Builder<B extends Builder<B>> extends AbstractControl.Builder<B> {
        
        private boolean readOnly;
        
        @SuppressWarnings("unchecked")
        public B readOnly() {
            readOnly = true;
            return (B) this;
        }

        @Override
        protected ControlInfo buildInfo() {
            controlType(readOnly ? ControlInfo.Type.ReadOnlyProperty : ControlInfo.Type.Property);
            return super.buildInfo();
        }
        
        
    }
}
