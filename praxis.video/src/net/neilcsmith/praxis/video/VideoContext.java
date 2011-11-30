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
package net.neilcsmith.praxis.video;

import net.neilcsmith.ripl.Sink;
import net.neilcsmith.ripl.Source;

/**
 *
 * @author Neil C Smith
 */
public abstract class VideoContext {

//    public final static String CLIENT_KEY_WIDTH = "client.width";
//    public final static String CLIENT_KEY_HEIGHT = "client.height";
//    public final static String CLIENT_KEY_ROTATION = "client.rotation";
//    public final static String CLIENT_KEY_DEVICE = "client.device";
    
    public abstract int registerVideoInputClient(InputClient client)
            throws ClientRegistrationException;
    
    public abstract void unregisterVideoInputClient(InputClient client);

    public abstract int registerVideoOutputClient(OutputClient client)
            throws ClientRegistrationException;

    public abstract void unregisterVideoOutputClient(OutputClient client);

    public static abstract class Client {

        public Object getClientHint(String key) {
            return null;
        }

    }

    public static abstract class InputClient extends Client {

        public abstract int getInputCount();

        public abstract Sink getInputSink(int index);

    }

    public static abstract class OutputClient extends Client {

        public abstract int getOutputCount();

        public abstract Source getOutputSource(int index);

    }
}
