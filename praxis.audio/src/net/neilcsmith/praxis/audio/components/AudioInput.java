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

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.audio.AudioContext;
import net.neilcsmith.praxis.audio.ClientRegistrationException;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.rapl.components.Placeholder;
import net.neilcsmith.rapl.core.Sink;

/**
 *
 * @author Neil C Smith
 */
public class AudioInput extends AbstractComponent {
    
    private Placeholder[] placeholders;
    private AudioContext.InputClient client;
    private AudioContext context;
    
    public AudioInput() {
        placeholders = new Placeholder[2];
        placeholders[0] = new Placeholder();
        placeholders[1] = new Placeholder();
        registerPort(Port.OUT + "-1", new DefaultAudioOutputPort(this, placeholders[0]));
        registerPort(Port.OUT + "-2", new DefaultAudioOutputPort(this, placeholders[1]));
        client = new AudioContext.InputClient() {

            @Override
            public int getInputCount() {
                return 2;
            }

            @Override
            public Sink getInputSink(int index) {
                return placeholders[index];
            }
        };
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



    
}
