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
import net.neilcsmith.praxis.audio.AudioOutputClient;
import net.neilcsmith.praxis.audio.AudioHub;
import net.neilcsmith.praxis.audio.DefaultAudioInputPort;
import net.neilcsmith.praxis.core.Container;
import net.neilcsmith.praxis.core.VetoException;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.rapl.components.Placeholder;
import net.neilcsmith.rapl.core.Source;

/**
 *
 * @author Neil C Smith
 */
public class AudioOutput extends AbstractComponent implements AudioOutputClient {

    private Placeholder[] placeholders;

    public AudioOutput() {
        placeholders = new Placeholder[2];
        placeholders[0] = new Placeholder();
        placeholders[1] = new Placeholder();
        registerPort(Port.IN + "-1", new DefaultAudioInputPort(this, placeholders[0]));
        registerPort(Port.IN + "-2", new DefaultAudioInputPort(this, placeholders[1]));
    }

    public int getOutputCount() {
        return 2;
    }

    public Source getOutputSource(int index) {
        return placeholders[index];
    }

    @Override // @TODO implement unregister
    public void parentNotify(Container parent) throws VetoException {
        super.parentNotify(parent);
        if (parent instanceof AudioHub) {
            try {
                ((AudioHub) parent).registerAudioOutputClient(this);
            } catch (Exception ex) {
                Logger.getLogger(AudioOutput.class.getName()).log(Level.SEVERE, null, ex);
                throw new VetoException();
            }
        }
    }
}
