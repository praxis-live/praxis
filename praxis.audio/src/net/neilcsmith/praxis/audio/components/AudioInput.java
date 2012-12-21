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

package net.neilcsmith.praxis.audio.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.audio.AudioContext;
import net.neilcsmith.praxis.audio.AudioPort;
import net.neilcsmith.praxis.audio.ClientRegistrationException;
import net.neilcsmith.praxis.audio.impl.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.IntProperty;
import org.jaudiolibs.pipes.Pipe;
import org.jaudiolibs.pipes.impl.Placeholder;

/**
 *
 * @author Neil C Smith
 */
public class AudioInput extends AbstractComponent {
    
    private final static int MAX_CHANNELS = 16;
    
    private final Placeholder[] placeholders;
    private final AudioPort.Output[] ports;
    private AudioContext.InputClient client;
    
    private int channelCount;
    private AudioContext context;
    
    public AudioInput() {
        placeholders = new Placeholder[MAX_CHANNELS];
        for (int i=0; i<MAX_CHANNELS; i++) {
            placeholders[i] = new Placeholder();
        }
        ports = new AudioPort.Output[MAX_CHANNELS];
        
        client = new Client();
        
        channelCount = 2;
        
        registerControl("channels",
                IntProperty.create(new CountBinding(), 1, MAX_CHANNELS, channelCount));
        
        syncPorts();
        markDynamic();
    }
    

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        AudioContext ctxt = getLookup().get(AudioContext.class);
        if (ctxt != context) {
            if (context != null) {
                context.unregisterAudioInputClient(client);
                context = null;
            }
            if (ctxt == null) {
                return;
            }
            try {
                ctxt.registerAudioInputClient(client);
                context = ctxt;
            } catch (ClientRegistrationException ex) {
                Logger.getLogger(AudioInput.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void syncPorts() {
        for (int i=0; i<MAX_CHANNELS; i++) {
            if (i < channelCount) {
                if (ports[i] == null) {
                    AudioPort.Output port = new DefaultAudioOutputPort(this, placeholders[i]);
                    registerPort(Port.OUT + "-" + (i+1), port);
                    ports[i] = port;
                }
            } else {
                if (ports[i] != null) {
                    // unregister will disconnect all
                    unregisterPort(Port.OUT + "-" + (i+1));
                    ports[i] = null;
                }
            }
        }
    }
    
    
    private class Client extends AudioContext.InputClient {

        @Override
        public int getInputCount() {
            return channelCount;
        }

        @Override
        public Pipe getInputSink(int index) {
            return placeholders[index];
        }
        
    }
    
    private class CountBinding implements IntProperty.Binding {

        public void setBoundValue(long time, int value) {
            channelCount = value;
            syncPorts();
        }

        public int getBoundValue() {
            return channelCount;
        }
        
    }



    
}
