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
package org.praxislive.video;

import org.praxislive.core.Lookup;
import org.praxislive.video.pipes.VideoPipe;

/**
 *
 * @author Neil C Smith
 */
public abstract class VideoContext {

   
    public abstract int registerVideoInputClient(InputClient client)
            throws ClientRegistrationException;
    
    public abstract void unregisterVideoInputClient(InputClient client);

    public abstract int registerVideoOutputClient(OutputClient client)
            throws ClientRegistrationException;

    public abstract void unregisterVideoOutputClient(OutputClient client);

    public static abstract class Client {
        
        // @TODO add client listeners to allow some changes at runtime?

//        public Object getClientHint(String key) {
//            return null;
//        }
        public abstract Lookup getLookup();

    }

    public static abstract class InputClient extends Client {

        public abstract int getInputCount();

        public abstract VideoPipe getInputSink(int index);

    }

    public static abstract class OutputClient extends Client {

        public abstract int getOutputCount();

        public abstract VideoPipe getOutputSource(int index);

    }
}
