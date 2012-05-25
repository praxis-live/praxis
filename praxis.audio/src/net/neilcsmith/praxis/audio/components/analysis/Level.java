/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
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
package net.neilcsmith.praxis.audio.components.analysis;

import net.neilcsmith.praxis.audio.impl.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractClockComponent;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.TriggerControl;
import org.jaudiolibs.pipes.Buffer;
import org.jaudiolibs.pipes.impl.SingleInOut;


/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class Level extends AbstractClockComponent {
    
    private AudioMeasure cmp;
    private ControlPort.Output out;
    private float[] cache;
    
    public Level() {
        cmp = new AudioMeasure();
        out = new DefaultControlOutputPort();
        registerPort(Port.IN, new DefaultAudioInputPort(this, cmp));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, cmp));
        registerPort("level", out);
    }
    
    private static double calculateRMS(float[] buffer) {
        double ret = 0;
        for (float sample : buffer) {
            ret += (sample * sample);
        }
        ret /= buffer.length;
        return Math.sqrt(ret);
    }

    public void tick(ExecutionContext source) {
        if (cache == null) {
            out.send(source.getTime(), 0);
        } else {
            out.send(source.getTime(), calculateRMS(cache));
        }
    }

    @Override
    public void stateChanged(ExecutionContext source) {
        cache = null;
    }
        
    private class AudioMeasure extends SingleInOut {
        
        

        @Override
        protected void process(Buffer buffer, boolean rendering) {
            float[] audio = buffer.getData();
            int size = buffer.getSize();
            if (cache == null || cache.length != size) {
                cache = new float[size];
            }
            System.arraycopy(audio, 0, cache, 0, size);         
        }
        
    }
    
}
