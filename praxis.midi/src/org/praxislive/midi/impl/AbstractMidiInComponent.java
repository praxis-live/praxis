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
 *
 */

package org.praxislive.midi.impl;

import org.praxislive.impl.AbstractComponent;
import org.praxislive.midi.MidiInputContext;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class AbstractMidiInComponent extends AbstractComponent
        implements MidiInputContext.Listener {
    
    private MidiInputContext context;

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        MidiInputContext ctxt = getLookup().get(MidiInputContext.class);
        if (context != ctxt) {
            if (context != null) {
                context.removeListener(this);
            }
            if (ctxt != null) {
                ctxt.addListener(this);
            }
            context = ctxt;
        }
    }
    



}
