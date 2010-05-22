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

package net.neilcsmith.praxis.audio.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.audio.AudioInputClient;
import net.neilcsmith.praxis.audio.AudioHub;
import net.neilcsmith.praxis.audio.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Container;
import net.neilcsmith.praxis.core.ParentVetoException;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.rapl.components.Placeholder;
import net.neilcsmith.rapl.core.Sink;

/**
 *
 * @author Neil C Smith
 */
public class AudioInput extends AbstractComponent implements AudioInputClient {
    
    private Placeholder[] placeholders;
    
    public AudioInput() {
        placeholders = new Placeholder[2];
        placeholders[0] = new Placeholder();
        placeholders[1] = new Placeholder();
        registerPort(Port.OUT + "-1", new DefaultAudioOutputPort(this, placeholders[0]));
        registerPort(Port.OUT + "-2", new DefaultAudioOutputPort(this, placeholders[1]));
    }

    public int getInputCount() {
        return 2;
    }

    public Sink getInputSink(int index) {
        return placeholders[index];
    }

        @Override // @TODO implement unregister
    public void parentNotify(Container parent) throws ParentVetoException {
        super.parentNotify(parent);
        if (parent instanceof AudioHub) {
            try {
                ((AudioHub) parent).registerAudioInputClient(this);
            } catch (Exception ex) {
                Logger.getLogger(AudioInput.class.getName()).log(Level.SEVERE, null, ex);
                throw new ParentVetoException();
            }
        }
    }
    
}
