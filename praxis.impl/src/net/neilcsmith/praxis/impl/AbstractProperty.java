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

import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.info.ControlInfo;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractProperty extends BasicControl {

    private ControlInfo info;
    
    protected AbstractProperty(Component host, ControlInfo info) {
        super(host);
        this.info = info;
    }
    

    @Override
    protected Call processInvoke(Call call, boolean quiet) throws Exception {
        CallArguments args = call.getArgs();
        int argCount = args.getCount();
        long time = call.getTimecode();
        if (argCount > 0) {
            if (isLatest(time)) {
                setArguments(time, args);
                setLatest(time);
            }
            if (!quiet) {
                return Call.createReturnCall(call, args);
            } else {
                return null;
            }
        } else {
            // ignore quiet hint?
            return Call.createReturnCall(call, getArguments());
        }

    }


    public ControlInfo getInfo() {
        return info;
    }
    
    
    protected abstract void setArguments(long time, CallArguments args) throws Exception;
    
    protected abstract CallArguments getArguments();
    
  
  
}
