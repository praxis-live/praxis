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
package org.praxislive.audio;

import org.jaudiolibs.pipes.Pipe;

/**
 *
 * @author Neil C Smith
 */
public abstract class AudioContext {

    // return is max potential channels, not guaranteed to be available!
    public abstract int registerAudioInputClient(InputClient client)
            throws ClientRegistrationException;

    public abstract void unregisterAudioInputClient(InputClient client);

    public abstract int registerAudioOutputClient(OutputClient client)
            throws ClientRegistrationException;

    public abstract void unregisterAudioOutputClient(OutputClient client);
    
    public abstract double getSampleRate();
    
    public abstract int getBlockSize();

    public static abstract class Client {
        
        //@TODO add change listeners to client for changes to counts
    }

    public static abstract class InputClient extends Client {

        public abstract int getInputCount();

        public abstract Pipe getInputSink(int index);
    }

    public static abstract class OutputClient extends Client {

        public abstract int getOutputCount();

        public abstract Pipe getOutputSource(int index);
    }
}
