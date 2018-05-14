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
package org.praxislive.osc.components;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Neil C Smith
 */
class OSCContext {
    
    private final static Logger LOG = Logger.getLogger(OSCContext.class.getName());
    
    private Map<String, List<OSCListener>> listeners;
    
    
    OSCContext() {
        listeners = new HashMap<String, List<OSCListener>>();
    }
    
    void addListener(String oscAddress, OSCListener listener) {
        List<OSCListener> ls = listeners.get(oscAddress);
        LOG.log(Level.FINE, "Adding OSC listener on {0}", oscAddress);
        if (ls == null) {
            LOG.log(Level.FINE, "Creating OSC map for {0}", oscAddress);
            ls = new ArrayList<OSCListener>();
            listeners.put(oscAddress, ls);
        }
        ls.add(listener);
    }
    
    void removeListener(String oscAddress, OSCListener listener) {
        List<OSCListener> ls = listeners.get(oscAddress);
        if (ls == null) {
            LOG.log(Level.FINE, 
                    "Attempting to remove listener for address without listeners - {0}",
                    oscAddress);
            return;
        }
        boolean changed = ls.remove(listener);
        if (!changed) {
            LOG.log(Level.FINE, 
                    "Attempting to remove non-existent listener on {0}",
                    oscAddress);
            return;
        }
        if (ls.isEmpty()) {
            LOG.log(Level.FINE, "Removing OSC map for {0}", oscAddress);
            listeners.remove(oscAddress);
        }
    }
    
    void dispatch(OSCMessage msg, long time) {
        List<OSCListener> ls = listeners.get(msg.getName());
        if (ls != null) {
            for (OSCListener l : ls) {
                l.messageReceived(msg, null, time);
            }
        }
    }
    
}
