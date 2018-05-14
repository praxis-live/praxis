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
package org.praxislive.video.pgl.syphon;

import codeanticode.syphon.SyphonClient;
import java.util.Optional;
import org.praxislive.video.pgl.PGLContext;
import org.praxislive.video.pgl.PGLTextureSharer;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 *
 * @author neilcsmith
 */
class SyphonReceiver implements PGLTextureSharer.Receiver {

    private final SyphonClient client;
    
    private PGraphics cache;
    
    SyphonReceiver(PGLContext context, String serverID) {
        if (serverID.isEmpty()) {
            client = new SyphonClient(context.parent());
        } else {
            client = new SyphonClient(context.parent(), "", serverID);
        }
    }
    
    @Override
    public boolean hasNewFrame() {
        return client.newFrame();
    }

    @Override
    public Optional<PImage> acquireFrame() {
        if (client.newFrame()) {
            cache = client.getGraphics(cache);
        }
        return Optional.ofNullable(cache);
    }

    @Override
    public void dispose() {
        client.stop();
        if (cache != null) {
            cache.dispose();
            cache = null;
        }
    }
    
    
    
}
