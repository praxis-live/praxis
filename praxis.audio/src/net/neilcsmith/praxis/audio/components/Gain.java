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

package net.neilcsmith.praxis.audio.components;

import net.neilcsmith.praxis.audio.impl.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.LinkPort;
import org.jaudiolibs.audioops.AudioOp;
import org.jaudiolibs.audioops.impl.GainOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith
 */
public class Gain extends AbstractComponent {

    private OpHolder ug;
    private GainOp op;
    private LinkPort<GainOp> link;
    
    public Gain() {
        op = new GainOp();
        ug = new OpHolder(op);
        FloatProperty level =  FloatProperty.create( new GainBinding(), 0, 2, op.getGain(), PMap.create("scale-hint", "Exponential"));
        registerPort(Port.IN, new DefaultAudioInputPort(this, ug));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, ug));
        registerControl("level", level);
        registerPort("level", level.createPort()); 
        link = new LinkPort<GainOp>(GainOp.class, new LinkHandler(), op);
        registerPort("link", link);
    }
    
    private class GainBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            op.setGain((float) value);
            link.fireUpdate();
        }

        public double getBoundValue() {
            return op.getGain();
        }
        
    }
    
    private class LinkHandler implements LinkPort.Handler<GainOp> {

        public void update(GainOp source) {
            op.setGain(source.getGain());
        }
        
    }
    
}
