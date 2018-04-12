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
package org.praxislive.audio.impl.components;

import org.praxislive.audio.impl.AudioInputPortEx;
import org.praxislive.audio.impl.AudioOutputPortEx;
import org.praxislive.core.ExecutionContext;
import org.praxislive.impl.AbstractClockComponent;
import org.praxislive.impl.DefaultControlOutputPort;
import org.jaudiolibs.pipes.Buffer;
import org.jaudiolibs.pipes.impl.SingleInOut;


/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class Level extends AbstractClockComponent {
    
    private AudioMeasure cmp;
    private DefaultControlOutputPort out;
    private float[] cache;
    
    public Level() {
        cmp = new AudioMeasure();
        out = new DefaultControlOutputPort();
        registerPort(PortEx.IN, new AudioInputPortEx(cmp));
        registerPort(PortEx.OUT, new AudioOutputPortEx(cmp));
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
