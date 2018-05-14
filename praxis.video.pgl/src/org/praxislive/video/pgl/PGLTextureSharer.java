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
package org.praxislive.video.pgl;

import java.util.Optional;
import org.praxislive.core.Lookup;
import processing.core.PImage;

/**
 *
 * @author neilcsmith
 */
public interface PGLTextureSharer {
    
    public Sender createSender(PGLContext context, String id) throws Exception;
    
    public Receiver createReceiver(PGLContext context, String server) throws Exception;
    
    public boolean isSupported();
    
    
    public static Optional<PGLTextureSharer> find() {
        return Lookup.SYSTEM.findAll(PGLTextureSharer.class)
                .filter(PGLTextureSharer::isSupported)
                .findFirst();
    }
    
    public static interface Sender {
        
        public void sendFrame(PImage frame);
        
        public void dispose();
        
    }
    
    public static interface Receiver {
        
        public boolean hasNewFrame();
        
        public Optional<PImage> acquireFrame();
        
        public void dispose();
        
    }
    
}
